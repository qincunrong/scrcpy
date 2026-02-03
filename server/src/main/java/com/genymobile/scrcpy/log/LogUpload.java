package com.genymobile.scrcpy.log;
import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.upload.FileUpload;
import com.genymobile.scrcpy.upload.FileUploadResult;
import com.genymobile.scrcpy.util.FileUtils;
import com.genymobile.scrcpy.util.Logger;
import com.genymobile.scrcpy.util.TimeUtils;


import java.io.File;

public class LogUpload {
    public void startUploadLog(LogReqBean reqBean) {
        try {
            startUploadLogReq(reqBean);
        } catch (Exception e) {
            Logger.i("uploadLog-"+reqBean.getFileName(), "upload failed:" + e.getMessage());
            e.printStackTrace();
            doUploadLogError(reqBean,"日志上传失败");
        }
    }
    private void startUploadLogReq(LogReqBean reqBean) {
        String path = ScrcpyConfig.getLogDir() + File.separator + reqBean.getDate();
        String fileName = reqBean.getFileName();
        String tag = "uploadLog-"+fileName;
        File sourceFile = new File(path, fileName);
        Logger.i(tag, "srcFile:" + sourceFile.getAbsolutePath());
        File tempFile = new File(path, "temp_" + fileName+"_"+ TimeUtils.getTimeStamp());
        if (!sourceFile.exists()) {
            Logger.i(tag, "file not exist");
            doUploadLogError(reqBean,"日志文件不存在");
            return;
        }
        try {
            Logger.i(tag,"file exist");
            FileUtils.copy(sourceFile, tempFile);
            long fileSize = tempFile.length();
            reqBean.setFileSize(fileSize);
            Logger.i(tag,"file temp fileSize:"+ FileUtils.formatFileSize(fileSize));
        } catch (Exception e) {
            Logger.i(tag,"file copy to temp failed");
            e.printStackTrace();
            doUploadLogError(reqBean,"拷贝临时日志文件失败");
            return;
        }
        new FileUpload().uploadFile(tempFile.getAbsolutePath(), fileName, new FileUpload.OnEventListener() {
            @Override
            public void onUploadSuccess(String file, FileUploadResult fileUploadResult) {
                Logger.i(tag,"onFile uploadSuccess");
                try {
                    tempFile.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                doUploadLogSuccess(reqBean,fileUploadResult);
            }

            @Override
            public void onUploadFailed(int code, String msg) {
                Logger.i(tag,"onFile uploadFailed");
                try {
                    tempFile.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                doUploadLogError(reqBean,"日志文件上传失败:"+msg);
            }
        });
    }
    private void doUploadLogError(LogReqBean reqBean,String errorMsg) {
        doUploadLogInfo(reqBean, null, errorMsg);
    }

    private void doUploadLogSuccess(LogReqBean reqBean, FileUploadResult fileUploadResult) {
        doUploadLogInfo(reqBean, fileUploadResult, null);
    }
    private void doUploadLogInfo(LogReqBean reqBean, FileUploadResult fileResult, String errorMsg) {
        if (reqBean == null) {
            return;
        }
//        LogFileBean bean = new LogFileBean();
//        if (fileResult != null) {
//            bean.setFileId(fileResult.getFileId());
//        }
//        String tag = "uploadLog-"+reqBean.getFileName();
//        bean.setFileName(reqBean.getFileName());
//        bean.setPrintDate(reqBean.getDate());
//        bean.setPrintTime(reqBean.getHour());
//        bean.setRemark(errorMsg);
//        bean.setDeviceCode(AppConfig.getDeviceCode());
//        bean.setSize(reqBean.getFileSize());
//        ApiRetrofit.getInstance().getApiService().uploadLogFile(bean)
//                .subscribe(new BaseObserver<String>() {
//                    @Override
//                    protected void onReqError(ReqError errorCode) {
//                        YLogger.i(tag,"uploadLogInfo final failed:"+ errorCode.toString());
//                    }
//                    @Override
//                    protected void onReqSuccess(BaseBean<String> result) {
//                        YLogger.i(tag,"uploadLogInfo final success:"+ result);
//                    }
//                });;
    }
}

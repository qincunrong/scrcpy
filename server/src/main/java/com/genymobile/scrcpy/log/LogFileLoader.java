package com.genymobile.scrcpy.log;
import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.upload.FileUpload;
import com.genymobile.scrcpy.upload.FileUploadResult;
import com.genymobile.scrcpy.util.FileUtils;
import com.genymobile.scrcpy.util.Logger;
import com.genymobile.scrcpy.util.TimeUtils;


import java.io.File;

public class LogFileLoader {

    public static final int ERROR_NO_LOG_FILE = 1;
    public static final int ERROR_COPY_FAILED = 2;
    public static final int ERROR_OTHER = 3;
    private OnEventListener mListener;

    public OnEventListener getListener() {
        return mListener;
    }

    public void setListener(OnEventListener mListener) {
        this.mListener = mListener;
    }

    public void startLoader(LogReqBean reqBean,OnEventListener listener) {
        try {
            setListener(listener);
            String path = ScrcpyConfig.getLogDir() + File.separator + reqBean.getDate();
            String fileName = reqBean.getFileName();
            String tag = "uploadLog-"+fileName;
            File sourceFile = new File(path, fileName);
            Logger.i(tag, "srcFile:" + sourceFile.getAbsolutePath());
            File tempFile = new File(path, "temp_" + fileName+"_"+ TimeUtils.getTimeStamp());
            if (!sourceFile.exists()) {
                Logger.i(tag, "file not exist");
                onFileLoaderError(reqBean,ERROR_NO_LOG_FILE,"日志文件不存在");
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
                onFileLoaderError(reqBean,ERROR_COPY_FAILED,"拷贝日志文件失败");
                return;
            }
            onFileLoaderSuccess(reqBean,tempFile.getAbsolutePath(), fileName);
        } catch (Exception e) {
            Logger.i("uploadLog-"+reqBean.getFileName(), "upload failed:" + e.getMessage());
            e.printStackTrace();
            onFileLoaderError(reqBean,ERROR_OTHER,"日志文件准备失败");
        }
    }

    private void onFileLoaderError(LogReqBean reqBean, int code,String errorMsg) {
        if (mListener != null) {
            mListener.onFileLoadFailed(reqBean,code,errorMsg);
        }
    }

    private void onFileLoaderSuccess(LogReqBean reqBean,String filePath,String fileName) {
        if (mListener != null) {
            mListener.onFileLoadSuccess(reqBean,filePath,fileName);
        }
    }


    public interface OnEventListener{
        void onFileLoadSuccess(LogReqBean reqBean, String filePath,String fileName);
        void onFileLoadFailed(LogReqBean reqBean, int code, String msg);
    }

}

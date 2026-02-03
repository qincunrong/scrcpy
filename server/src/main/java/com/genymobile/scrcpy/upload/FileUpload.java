package com.genymobile.scrcpy.upload;
import com.genymobile.scrcpy.util.Logger;

import java.io.File;


public class FileUpload {
    private String TAG = "FileUpload@" + Integer.toHexString(hashCode());

    public void uploadFile(String uploadFile,String fileName,OnEventListener listener) {
        File file = new File(uploadFile);
        if (!file.exists()) {
            if (listener != null) {
                listener.onUploadFailed(-1,"文件为空");
            }
            return;
        }
        try {
            String mimeType = "text/plain";
//            RequestBody fileRq = RequestBody.create(MediaType.parse("text/plain"), file);
//            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, fileRq);
//
//            RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), fileName);
//            RequestBody mimeTypeContent = RequestBody.create(MediaType.parse("text/plain"), mimeType);
//            ApiRetrofit.getInstance().getApiService().uploadFile(body, filename,mimeTypeContent)
//                    .subscribeOn(CustomThreads.getSchedulers())
//                    .subscribe(new BaseObserver<FileUploadResult>() {
//                        @Override
//                        protected void onReqError(ReqError errorCode) {
//                            if (listener != null) {
//                                listener.onUploadFailed(-1,errorCode.getMsg());
//                            }
//                        }
//                        @Override
//                        protected void onReqSuccess(BaseBean<FileUploadResult> result) {
//                            if (listener != null) {
//                                listener.onUploadSuccess(uploadFile, result.getResult());
//                            }
//                        }
//                    });
        } catch (Exception e) {
            Logger.printThrowable("uploadLogFile exception==", e);
            e.printStackTrace();
            if (listener != null) {
                listener.onUploadFailed(-1,e.getMessage());
            }
        }
    }
    public interface OnEventListener{
        void onUploadSuccess(String srcFile,FileUploadResult result);
        void onUploadFailed(int code, String msg);
    }
}

package com.genymobile.scrcpy.upload;

public class FileUploadResult {
    private String fileId;
    private String fileName;
    private String fileSize;
    private String chunkNum;
    private String uploadId;
    private String chunkSize;
    private String fileMd5;
    private String fileType;
    private String chunkUploadedList;
    private Integer uploadStatus;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getChunkNum() {
        return chunkNum;
    }

    public void setChunkNum(String chunkNum) {
        this.chunkNum = chunkNum;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(String chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getChunkUploadedList() {
        return chunkUploadedList;
    }

    public void setChunkUploadedList(String chunkUploadedList) {
        this.chunkUploadedList = chunkUploadedList;
    }

    public Integer getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(Integer uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
}

package com.genymobile.scrcpy.control;

import java.util.Arrays;

public final class DeviceMessage {

    public static final int TYPE_CLIPBOARD = 0;
    public static final int TYPE_ACK_CLIPBOARD = 1;
    public static final int TYPE_UHID_OUTPUT = 2;
    public static final int TYPE_UPLOAD_LOG_FILE = 3;
    public static final int TYPE_UPLOAD_LOG_ERROR = 4;
    public static final int TYPE_UPLOAD_SCREENSHOT = 5;

    private int type;
    private String text;
    private long sequence;
    private int id;
    private byte[] data;
    private String filePath;
    private String fileName;
    private int errorCode;
    private String errorMsg;

    private DeviceMessage() {
    }

    public static DeviceMessage createClipboard(String text) {
        DeviceMessage event = new DeviceMessage();
        event.type = TYPE_CLIPBOARD;
        event.text = text;
        return event;
    }

    public static DeviceMessage createAckClipboard(long sequence) {
        DeviceMessage event = new DeviceMessage();
        event.type = TYPE_ACK_CLIPBOARD;
        event.sequence = sequence;
        return event;
    }

    public static DeviceMessage createUhidOutput(int id, byte[] data) {
        DeviceMessage event = new DeviceMessage();
        event.type = TYPE_UHID_OUTPUT;
        event.id = id;
        event.data = data;
        return event;
    }

    public static DeviceMessage createUploadLogFile(int id, String filePath, String fileName) {
        DeviceMessage event = new DeviceMessage();
        event.type = TYPE_UPLOAD_LOG_FILE;
        event.id = id;
        event.filePath = filePath;
        event.fileName = fileName;
        return event;
    }
    public static DeviceMessage createUploadLogError(int id, String fileName,int errorCode, String errorMsg) {
        DeviceMessage event = new DeviceMessage();
        event.type = TYPE_UPLOAD_LOG_ERROR;
        event.id = id;
        event.errorCode = errorCode;
        event.errorMsg = errorMsg;
        event.fileName = fileName;
        return event;
    }

    public static DeviceMessage createUploadScreenShot(int id, String filePath,String fileName) {
        DeviceMessage event = new DeviceMessage();
        event.type = TYPE_UPLOAD_SCREENSHOT;
        event.id = id;
        event.filePath = filePath;
        event.fileName = fileName;
        return event;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public long getSequence() {
        return sequence;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "{" +
                "type=" + type +
                ", text='" + text + '\'' +
                ", sequence=" + sequence +
                ", id=" + id +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}

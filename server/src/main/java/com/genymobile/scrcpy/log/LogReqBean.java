package com.genymobile.scrcpy.log;

public class LogReqBean {
    private String date;
    private String hour;
    private String fileName;
    private long fileSize;

    public LogReqBean(String date, String hour) {
        this.date = date;
        if (hour!=null
                &&hour.trim().length() == 1) {
            hour = "0" + hour.trim();
        }
        this.hour = hour;
        fileName = date + "-" + this.hour + ".log";
    }

    public String getFileName() {
        return fileName;
    }

    public String getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}

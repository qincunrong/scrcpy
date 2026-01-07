package com.genymobile.scrcpy.custom.report;

import com.genymobile.scrcpy.control.ControlMessage;

/**
* @author qincunrong
* @date 2026/1/7 14:22
* @email qincunrong@ysten.com
* @description
 * 对上述指令的调用，都应该记录操作日志。日志格式为：
 * 事件时间戳 指令名称(参数)
 * 1767668116 click(100,100)
 * 1767668132 sleep(100)
 * 1767668230 dragdrop(100,100,200,200,1000)
 *
 * 如：
 * 1767668116 click(100,100)
 * 1767668132 sleep(100)
 * 1767668230 dragdrop(100,100,200,200,1000)
 * 日志可根据需要在在接收到上传日志通知时，上传到服务器。
 *
*/
public class ControlReporter {

    private static volatile ControlReporter singleton = null;
    
    private ControlReporter() {}
    
    public static ControlReporter getInstance() {
        if (singleton == null) {
            synchronized (ControlReporter.class) {
                if (singleton == null) {
                    singleton = new ControlReporter();
                }
            }
        }
        return singleton;
    }
    public void reportStart(ControlMessage message) {
        
    }

    public void reportEnd(ControlMessage message) {

    }
}

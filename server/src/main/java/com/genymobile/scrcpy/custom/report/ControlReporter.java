package com.genymobile.scrcpy.custom.report;

import android.text.TextUtils;

import com.genymobile.scrcpy.control.ControlMessage;
import com.genymobile.scrcpy.device.Position;
import com.genymobile.scrcpy.log.LogManager;
import com.genymobile.scrcpy.util.Logger;

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
        try {
            String msg = "";
            switch (message.getType()) {
                case ControlMessage.TYPE_MOCK_CLICK:
                    Position position = message.getPosition();
                    msg = String.format(" click(%d,%d)", position.getPoint().getX(), position.getPoint().getY());
                    break;
                case ControlMessage.TYPE_MOCK_DOUBLE_CLICK:
                    Position position1 = message.getPosition();
                    msg = String.format(" doubleclick(%d,%d)", position1.getPoint().getX(), position1.getPoint().getY());
                    break;
                case ControlMessage.TYPE_MOCK_DRAG:
                    Position startPosi = message.getDragStartPosition();
                    Position endPosi = message.getDragEndPosition();
                    msg = String.format(" dragdrop(%d,%d,%d,%d,%d)", startPosi.getPoint().getX(), startPosi.getPoint().getY(),endPosi.getPoint().getX(), endPosi.getPoint().getY(),message.getDuration());
                    break;
                case ControlMessage.TYPE_SCREEN_SHOT:
                    msg = String.format(" screenshot(%d)", message.getId());
                    break;
                case ControlMessage.TYPE_SCREEN_SHOT_UPLOAD_RESULT:
                    //TODO:查看上传结果
    //                msg = String.format(" screenshotResult(%d,%s)", message.getId(),message.get);
                    break;
                case ControlMessage.TYPE_SLEEP:
                    msg = String.format(" sleep(%d)", message.getDuration());
                    break;
            }
            if (!TextUtils.isEmpty(msg)) {
                Logger.i("reporter","msg:"+msg);
                LogManager.getLogManager().cacheLog("control", msg);
            }else {

            }
        } catch (Exception e) {
            Logger.i("reporter","exception:"+e.getMessage());
        }


    }

    public void reportEnd(ControlMessage message) {
        switch (message.getType()) {
            case ControlMessage.TYPE_MOCK_CLICK:

                break;
            case ControlMessage.TYPE_MOCK_DOUBLE_CLICK:

                break;
            case ControlMessage.TYPE_MOCK_DRAG:

                break;
            case ControlMessage.TYPE_SCREEN_SHOT:

                break;
            case ControlMessage.TYPE_SCREEN_SHOT_UPLOAD_RESULT:

                break;
            case ControlMessage.TYPE_SLEEP:

                break;
        }
    }
}

package com.genymobile.scrcpy.control;

import android.view.MotionEvent;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.device.Position;
import com.genymobile.scrcpy.util.Binary;
import com.genymobile.scrcpy.util.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ControlMessageReader {

    public static final String TAG = ScrcpyConfig.getLogGroup() + "ControlMessageReader";
    private static final int MESSAGE_MAX_SIZE = 1 << 18; // 256k

    public static final int CLIPBOARD_TEXT_MAX_LENGTH = MESSAGE_MAX_SIZE - 14; // type: 1 byte; sequence: 8 bytes; paste flag: 1 byte; length: 4 bytes
    public static final int INJECT_TEXT_MAX_LENGTH = 300;

    private final DataInputStream dis;

    public ControlMessageReader(InputStream rawInputStream) {
        dis = new DataInputStream(new BufferedInputStream(rawInputStream));
    }

    public ControlMessage read() throws IOException {
        int type = dis.readUnsignedByte();
        switch (type) {
//            case ControlMessage.TYPE_INJECT_KEYCODE:
//                return parseInjectKeycode();
//            case ControlMessage.TYPE_INJECT_TEXT:
//                return parseInjectText();
            case ControlMessage.TYPE_INJECT_TOUCH_EVENT:
                return parseInjectTouchEventTest();
//                return parseInjectScreenShot();
//            case ControlMessage.TYPE_INJECT_SCROLL_EVENT:
//                return parseInjectScrollEvent();
//            case ControlMessage.TYPE_BACK_OR_SCREEN_ON:
//                return parseBackOrScreenOnEvent();
//            case ControlMessage.TYPE_GET_CLIPBOARD:
//                return parseGetClipboard();
//            case ControlMessage.TYPE_SET_CLIPBOARD:
//                return parseSetClipboard();
//            case ControlMessage.TYPE_SET_DISPLAY_POWER:
//                return parseSetDisplayPower();
//            case ControlMessage.TYPE_EXPAND_NOTIFICATION_PANEL:
//            case ControlMessage.TYPE_EXPAND_SETTINGS_PANEL:
//            case ControlMessage.TYPE_COLLAPSE_PANELS:
//            case ControlMessage.TYPE_ROTATE_DEVICE:
//            case ControlMessage.TYPE_OPEN_HARD_KEYBOARD_SETTINGS:
//            case ControlMessage.TYPE_RESET_VIDEO:
//                return ControlMessage.createEmpty(type);
//            case ControlMessage.TYPE_UHID_CREATE:
//                return parseUhidCreate();
//            case ControlMessage.TYPE_UHID_INPUT:
//                return parseUhidInput();
//            case ControlMessage.TYPE_UHID_DESTROY:
//                return parseUhidDestroy();
//            case ControlMessage.TYPE_START_APP:
//                return parseStartApp();
//            //add by qcr
            case ControlMessage.TYPE_MOCK_CLICK:
                return parseInjectMockClickEvent();

            case ControlMessage.TYPE_MOCK_DOUBLE_CLICK:
                return parseInjectMockDoubleClickEvent();

            case ControlMessage.TYPE_MOCK_DRAG:
                return parseInjectMockDragEvent();

            case ControlMessage.TYPE_SCREEN_SHOT:
                return parseInjectScreenShot();

            case ControlMessage.TYPE_SCREEN_SHOT_UPLOAD_RESULT:
                return parseInjectScreenShotResult();

            case ControlMessage.TYPE_SLEEP:
                return parseInjectSleep();

            case ControlMessage.TYPE_UPLOAD_LOG:
                return parseInjectUploadLog();

            case ControlMessage.TYPE_SET_NETWORK_VPN:
                return parseSetNetworkVpn();
            default:
                throw new ControlProtocolException("Unknown event type: " + type);
        }
    }



    private ControlMessage parseInjectKeycode() throws IOException {
        int action = dis.readUnsignedByte();
        int keycode = dis.readInt();
        int repeat = dis.readInt();
        int metaState = dis.readInt();
        return ControlMessage.createInjectKeycode(action, keycode, repeat, metaState);
    }

    private int parseBufferLength(int sizeBytes) throws IOException {
        assert sizeBytes > 0 && sizeBytes <= 4;
        int value = 0;
        for (int i = 0; i < sizeBytes; ++i) {
            value = (value << 8) | dis.readUnsignedByte();
        }
        return value;
    }

    private String parseString(int sizeBytes) throws IOException {
        assert sizeBytes > 0 && sizeBytes <= 4;
        byte[] data = parseByteArray(sizeBytes);
        return new String(data, StandardCharsets.UTF_8);
    }

    private String parseString() throws IOException {
        return parseString(4);
    }

    private byte[] parseByteArray(int sizeBytes) throws IOException {
        int len = parseBufferLength(sizeBytes);
        byte[] data = new byte[len];
        dis.readFully(data);
        return data;
    }

    private ControlMessage parseInjectText() throws IOException {
        String text = parseString();
        return ControlMessage.createInjectText(text);
    }

    private Position mStartPosition;
    private ControlMessage parseInjectTouchEvent() throws IOException {
        int action = dis.readUnsignedByte();
        long pointerId = dis.readLong();
        Position position = parsePosition();
        float pressure = Binary.u16FixedPointToFloat(dis.readShort());
        int actionButton = dis.readInt();
        int buttons = dis.readInt();
        ControlMessage msg = ControlMessage.createInjectTouchEvent(action, pointerId, position, pressure, actionButton, buttons);
        Logger.i(TAG,"收到触摸事件: action=%d, position:%s" , action ,position);
        return msg;
    }

    private ControlMessage parseInjectTouchEventTest() throws IOException {
        int action = dis.readUnsignedByte();
        long pointerId = dis.readLong();
        Position position = parsePosition();
        float pressure = Binary.u16FixedPointToFloat(dis.readShort());
        int actionButton = dis.readInt();
        int buttons = dis.readInt();
//        return ControlMessage.createInjectTouchEvent(action, pointerId, position, pressure, actionButton, buttons);
        //TODO:删除测试代码
        if (action != MotionEvent.ACTION_DOWN) {
            return null;
        }
        Logger.i(TAG,"收到触摸事件: action=" + action +
                ", x=" + position.getPoint().getX() + ", y=" + position.getPoint().getY() +
                ", pressure=" + pressure);
        //将点击时间模拟成拖动事件

//        return ControlMessage.createScreenShotEvent();
//        return ControlMessage.createMockClickEvent(position);

//        Position endPosition=new Position(startPosition.getPoint().getX()+100, startPosition.getPoint().getY()+100, startPosition.getScreenSize().getWidth(), startPosition.getScreenSize().getHeight());
        if (mStartPosition == null) {
            mStartPosition = position;
            Logger.i(TAG,"设置拖动开始位置: action=" + action +
                    ", x=" + position.getPoint().getX() + ", y=" + position.getPoint().getY() +
                    ", pressure=" + pressure);
        }else {
            Position endPosition = position;
            Logger.i(TAG,"设置拖动结束位置: action=" + action +
                    ", x=" + position.getPoint().getX() + ", y=" + position.getPoint().getY() +
                    ", pressure=" + pressure);
//            int duration = calculateDragTime(mStartPosition.getPoint().getX(), mStartPosition.getPoint().getY(), position.getPoint().getX(), position.getPoint().getY());
            int duration = 4000;
//            ControlMessage msg = ControlMessage.createMockDragEvent(mStartPosition, endPosition, duration);
//            mStartPosition = null;
//            return msg;
            return parseInjectUploadLog();
        }
        return null;
    }

    public static int calculateDragTime(int startX, int startY, int endX, int endY) {
        // 计算欧几里得距离
        double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
        // 基础时间 + 距离相关时间
        // 假设：每100像素增加50毫秒，最小200毫秒
        return (int) Math.max(200, 200 + (distance / 100) * 50);
    }

    private ControlMessage parseInjectScrollEvent() throws IOException {
        Position position = parsePosition();
        // Binary.i16FixedPointToFloat() decodes values assuming the full range is [-1, 1], but the actual range is [-16, 16].
        float hScroll = Binary.i16FixedPointToFloat(dis.readShort()) * 16;
        float vScroll = Binary.i16FixedPointToFloat(dis.readShort()) * 16;
        int buttons = dis.readInt();
        return ControlMessage.createInjectScrollEvent(position, hScroll, vScroll, buttons);
    }

    private ControlMessage parseBackOrScreenOnEvent() throws IOException {
        int action = dis.readUnsignedByte();
        return ControlMessage.createBackOrScreenOn(action);
    }

    private ControlMessage parseGetClipboard() throws IOException {
        int copyKey = dis.readUnsignedByte();
        return ControlMessage.createGetClipboard(copyKey);
    }

    private ControlMessage parseSetClipboard() throws IOException {
        long sequence = dis.readLong();
        boolean paste = dis.readByte() != 0;
        String text = parseString();
        return ControlMessage.createSetClipboard(sequence, text, paste);
    }

    private ControlMessage parseSetDisplayPower() throws IOException {
        boolean on = dis.readBoolean();
        return ControlMessage.createSetDisplayPower(on);
    }

    private ControlMessage parseUhidCreate() throws IOException {
        int id = dis.readUnsignedShort();
        int vendorId = dis.readUnsignedShort();
        int productId = dis.readUnsignedShort();
        String name = parseString(1);
        byte[] data = parseByteArray(2);
        return ControlMessage.createUhidCreate(id, vendorId, productId, name, data);
    }

    private ControlMessage parseUhidInput() throws IOException {
        int id = dis.readUnsignedShort();
        byte[] data = parseByteArray(2);
        return ControlMessage.createUhidInput(id, data);
    }

    private ControlMessage parseUhidDestroy() throws IOException {
        int id = dis.readUnsignedShort();
        return ControlMessage.createUhidDestroy(id);
    }

    private ControlMessage parseStartApp() throws IOException {
        String name = parseString(1);
        return ControlMessage.createStartApp(name);
    }

    private Position parsePosition() throws IOException {
        int x = dis.readInt();
        int y = dis.readInt();
        int screenWidth = dis.readUnsignedShort();
        int screenHeight = dis.readUnsignedShort();
        return new Position(x, y, screenWidth, screenHeight);
    }


    //add by qcr
    private ControlMessage parseInjectMockClickEvent() throws IOException {
        Position position = parsePosition();
        return ControlMessage.createMockClickEvent(position);
    }

    private ControlMessage parseInjectMockDoubleClickEvent() throws IOException {
        Position position = parsePosition();
        return ControlMessage.createMockDoubleClickEvent(position);
    }

    private ControlMessage parseInjectMockDragEvent() throws IOException {
        Position startPosition = parsePosition();
        Position endPosition = parsePosition();
        int duration = dis.readUnsignedShort();
        return ControlMessage.createMockDragEvent(startPosition,endPosition,duration);
    }

    private ControlMessage parseInjectSleep() throws IOException {
        int duration = dis.readInt();
        return ControlMessage.createSleepEvent(duration);
    }

    private ControlMessage parseInjectScreenShot() throws IOException {
        Logger.i(TAG, "parseInjectScreenShot");
        int duration = dis.readInt();
        return ControlMessage.createScreenShotEvent(duration);
    }
    private ControlMessage parseInjectScreenShotResult() throws IOException {
        int id = dis.readInt();
        int result=dis.readInt();
        return ControlMessage.createScreenShotEventResult(id);
    }

    private ControlMessage parseInjectUploadLog()throws IOException {
//        String logTime = parseString();//格式20260130-13
        //TODO:delete test log
        String logTime = "20260209-10,11";
        return ControlMessage.createUploadLog(logTime);
    }

    private ControlMessage parseSetNetworkVpn()  throws IOException {
        String host = parseString();
        String port = parseString();
        String excludeHost = parseString();
        return ControlMessage.createSetNetworkVpn(host, port,excludeHost);
    }


}

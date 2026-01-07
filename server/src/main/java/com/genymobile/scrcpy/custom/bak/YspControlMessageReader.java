//package com.genymobile.scrcpy.custom.controll;
//
//import com.genymobile.scrcpy.control.ControlProtocolException;
//import com.genymobile.scrcpy.device.Position;
//import com.genymobile.scrcpy.util.Binary;
//
//import java.io.BufferedInputStream;
//import java.io.DataInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//
//public class YspControlMessageReader {
//
//    private static final int MESSAGE_MAX_SIZE = 1 << 18; // 256k
//
//    public static final int CLIPBOARD_TEXT_MAX_LENGTH = MESSAGE_MAX_SIZE - 14; // type: 1 byte; sequence: 8 bytes; paste flag: 1 byte; length: 4 bytes
//    public static final int INJECT_TEXT_MAX_LENGTH = 300;
//
//    private final DataInputStream dis;
//
//    public YspControlMessageReader(InputStream rawInputStream) {
//        dis = new DataInputStream(new BufferedInputStream(rawInputStream));
//    }
//
//    public YspControlMessage read() throws IOException {
//        int type = dis.readUnsignedByte();
//        switch (type) {
//            case YspControlMessage.TYPE_INJECT_TOUCH_EVENT:
//                return parseInjectTouchEvent();
//
//            case YspControlMessage.TYPE_MOCK_CLICK:
//                return parseInjectMockClickEvent();
//
//            case YspControlMessage.TYPE_MOCK_DOUBLE_CLICK:
//                return parseInjectMockDoubleClickEvent();
//
//            case YspControlMessage.TYPE_MOCK_DRAG:
//                return parseInjectMockDragEvent();
//
//            case YspControlMessage.TYPE_SCREEN_SHOT:
//                return parseInjectMockScreenShot();
//
//            case YspControlMessage.TYPE_SLEEP:
//                return parseInjectMockSleep();
//
//            default:
//                throw new ControlProtocolException("Unknown event type: " + type);
//        }
//    }
//
//
//    private int parseBufferLength(int sizeBytes) throws IOException {
//        assert sizeBytes > 0 && sizeBytes <= 4;
//        int value = 0;
//        for (int i = 0; i < sizeBytes; ++i) {
//            value = (value << 8) | dis.readUnsignedByte();
//        }
//        return value;
//    }
//
//    private String parseString(int sizeBytes) throws IOException {
//        assert sizeBytes > 0 && sizeBytes <= 4;
//        byte[] data = parseByteArray(sizeBytes);
//        return new String(data, StandardCharsets.UTF_8);
//    }
//
//    private String parseString() throws IOException {
//        return parseString(4);
//    }
//
//    private byte[] parseByteArray(int sizeBytes) throws IOException {
//        int len = parseBufferLength(sizeBytes);
//        byte[] data = new byte[len];
//        dis.readFully(data);
//        return data;
//    }
//
//
//    private YspControlMessage parseInjectTouchEvent() throws IOException {
//        int action = dis.readUnsignedByte();
//        long pointerId = dis.readLong();
//        Position position = parsePosition();
//        float pressure = Binary.u16FixedPointToFloat(dis.readShort());
//        int actionButton = dis.readInt();
//        int buttons = dis.readInt();
//        return YspControlMessage.createInjectTouchEvent(action, pointerId, position, pressure, actionButton, buttons);
//    }
//
//    private YspControlMessage parseInjectMockClickEvent() throws IOException {
//        Position position = parsePosition();
//        return YspControlMessage.createMockClickEvent(position);
//    }
//
//    private YspControlMessage parseInjectMockDoubleClickEvent() throws IOException {
//        Position position = parsePosition();
//        return YspControlMessage.createMockDoubleClickEvent(position);
//    }
//
//    private YspControlMessage parseInjectMockDragEvent() throws IOException {
//        Position startPosition = parsePosition();
//        Position endPosition = parsePosition();
//        int duration = dis.readUnsignedShort();
//        return YspControlMessage.createMockDragEvent(startPosition,endPosition,duration);
//    }
//
//    private YspControlMessage parseInjectMockSleep() throws IOException {
//        int duration = dis.readUnsignedShort();
//        return YspControlMessage.createSleepEvent(duration);
//    }
//
//    private YspControlMessage parseInjectMockScreenShot() throws IOException {
//        return YspControlMessage.createScreenShotEvent();
//    }
//
//
//    private Position parsePosition() throws IOException {
//        int x = dis.readInt();
//        int y = dis.readInt();
//        int screenWidth = dis.readUnsignedShort();
//        int screenHeight = dis.readUnsignedShort();
//        return new Position(x, y, screenWidth, screenHeight);
//    }
//
//
//}

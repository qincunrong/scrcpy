//package com.genymobile.scrcpy.custom.controll;
//
//import com.genymobile.scrcpy.device.Position;
//
///**
// * Union of all supported event types, identified by their {@code type}.
// */
//public final class YspControlMessage {
//
//
//    public static final int TYPE_INJECT_TOUCH_EVENT = 2;
//    public static final int TYPE_MOCK_CLICK = 18; //模拟点击
//    public static final int TYPE_MOCK_DOUBLE_CLICK = 20;//模拟双击
//    public static final int TYPE_MOCK_DRAG = 19;//模拟拖动
//    public static final int TYPE_SCREEN_SHOT=21;//截屏
//    public static final int TYPE_SLEEP=22;//睡眠时间
//
//
//
//    public static final long SEQUENCE_INVALID = 0;
//
//    public static final int COPY_KEY_NONE = 0;
//    public static final int COPY_KEY_COPY = 1;
//    public static final int COPY_KEY_CUT = 2;
//
//    private int type;
//    private String text;
//    private int metaState; // KeyEvent.META_*
//    private int action; // KeyEvent.ACTION_* or MotionEvent.ACTION_*
//    private int keycode; // KeyEvent.KEYCODE_*
//    private int actionButton; // MotionEvent.BUTTON_*
//    private int buttons; // MotionEvent.BUTTON_*
//    private long pointerId;
//    private float pressure;
//    private Position position;
//    private float hScroll;
//    private float vScroll;
//    private int copyKey;
//    private boolean paste;
//    private int repeat;
//    private long sequence;
//    private int id;
//    private byte[] data;
//    private boolean on;
//    private int vendorId;
//    private int productId;
//
//    private int duration;
//    private Position dragStartPosition;
//    private Position dragEndPosition;
//
//    private YspControlMessage() {
//    }
//
//
//    public static YspControlMessage createInjectTouchEvent(int action, long pointerId, Position position, float pressure, int actionButton,
//                                                           int buttons) {
//        YspControlMessage msg = new YspControlMessage();
//        msg.type = TYPE_INJECT_TOUCH_EVENT;
//        msg.action = action;
//        msg.pointerId = pointerId;
//        msg.pressure = pressure;
//        msg.position = position;
//        msg.actionButton = actionButton;
//        msg.buttons = buttons;
//        return msg;
//    }
//
//    public static YspControlMessage createSleepEvent(int sleepDuration) {
//        YspControlMessage msg = new YspControlMessage();
//        msg.type = TYPE_SLEEP;
//        msg.duration = sleepDuration;
//        return msg;
//    }
//
//    public static YspControlMessage createMockClickEvent(Position position) {
//        YspControlMessage msg = new YspControlMessage();
//        msg.type = TYPE_MOCK_CLICK;
//        msg.position = position;
//        return msg;
//    }
//
//    public static YspControlMessage createMockDoubleClickEvent(Position position) {
//        YspControlMessage msg = new YspControlMessage();
//        msg.type = TYPE_MOCK_DOUBLE_CLICK;
//        msg.position = position;
//        return msg;
//    }
//    public static YspControlMessage createMockDragEvent(Position startPosition,Position endPosition, int duration) {
//        YspControlMessage msg = new YspControlMessage();
//        msg.type = TYPE_MOCK_DRAG;
//        msg.dragStartPosition = startPosition;
//        msg.dragEndPosition = endPosition;
//        msg.duration = duration;
//        return msg;
//    }
//
//    public static YspControlMessage createScreenShotEvent() {
//        YspControlMessage msg = new YspControlMessage();
//        msg.type = TYPE_SCREEN_SHOT;
//        return msg;
//    }
//
//    public static YspControlMessage createEmpty(int type) {
//        YspControlMessage msg = new YspControlMessage();
//        msg.type = type;
//        return msg;
//    }
//
//
//    public int getType() {
//        return type;
//    }
//
//    public String getText() {
//        return text;
//    }
//
//    public int getMetaState() {
//        return metaState;
//    }
//
//    public int getAction() {
//        return action;
//    }
//
//    public int getKeycode() {
//        return keycode;
//    }
//
//    public int getActionButton() {
//        return actionButton;
//    }
//
//    public int getButtons() {
//        return buttons;
//    }
//
//    public long getPointerId() {
//        return pointerId;
//    }
//
//    public float getPressure() {
//        return pressure;
//    }
//
//    public Position getPosition() {
//        return position;
//    }
//
//    public float getHScroll() {
//        return hScroll;
//    }
//
//    public float getVScroll() {
//        return vScroll;
//    }
//
//    public int getCopyKey() {
//        return copyKey;
//    }
//
//    public boolean getPaste() {
//        return paste;
//    }
//
//    public int getRepeat() {
//        return repeat;
//    }
//
//    public long getSequence() {
//        return sequence;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public byte[] getData() {
//        return data;
//    }
//
//    public boolean getOn() {
//        return on;
//    }
//
//    public int getVendorId() {
//        return vendorId;
//    }
//
//    public int getProductId() {
//        return productId;
//    }
//
//    public int getDuration() {
//        return duration;
//    }
//
//    public void setDuration(int duration) {
//        this.duration = duration;
//    }
//
//    public Position getDragStartPosition() {
//        return dragStartPosition;
//    }
//
//    public void setDragStartPosition(Position dragStartPosition) {
//        this.dragStartPosition = dragStartPosition;
//    }
//
//    public Position getDragEndPosition() {
//        return dragEndPosition;
//    }
//
//    public void setDragEndPosition(Position dragEndPosition) {
//        this.dragEndPosition = dragEndPosition;
//    }
//}

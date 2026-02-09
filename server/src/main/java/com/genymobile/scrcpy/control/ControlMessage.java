package com.genymobile.scrcpy.control;

import com.genymobile.scrcpy.device.Position;

import java.io.IOException;

/**
 * Union of all supported event types, identified by their {@code type}.
 */
public final class ControlMessage {

    public static final int TYPE_INJECT_KEYCODE = 0;
    public static final int TYPE_INJECT_TEXT = 1;
    public static final int TYPE_INJECT_TOUCH_EVENT = 2;
    public static final int TYPE_INJECT_SCROLL_EVENT = 3;
    public static final int TYPE_BACK_OR_SCREEN_ON = 4;
    public static final int TYPE_EXPAND_NOTIFICATION_PANEL = 5;
    public static final int TYPE_EXPAND_SETTINGS_PANEL = 6;
    public static final int TYPE_COLLAPSE_PANELS = 7;
    public static final int TYPE_GET_CLIPBOARD = 8;
    public static final int TYPE_SET_CLIPBOARD = 9;
    public static final int TYPE_SET_DISPLAY_POWER = 10;
    public static final int TYPE_ROTATE_DEVICE = 11;
    public static final int TYPE_UHID_CREATE = 12;
    public static final int TYPE_UHID_INPUT = 13;
    public static final int TYPE_UHID_DESTROY = 14;
    public static final int TYPE_OPEN_HARD_KEYBOARD_SETTINGS = 15;
    public static final int TYPE_START_APP = 16;
    public static final int TYPE_RESET_VIDEO = 17;

    //add by qcr
    public static final int TYPE_MOCK_CLICK = 18; //模拟点击
    public static final int TYPE_MOCK_DOUBLE_CLICK = 19;//模拟双击
    public static final int TYPE_MOCK_DRAG = 20;//模拟拖动
    public static final int TYPE_SCREEN_SHOT=21;//截屏
    public static final int TYPE_SCREEN_SHOT_UPLOAD_RESULT=22;//截屏上传结果反馈
    public static final int TYPE_SLEEP=23;//睡眠时间
    public static final int TYPE_UPLOAD_LOG=24;//上传日志文件
    public static final int TYPE_SET_NETWORK_VPN=26;//设置网络vpn
    //end add

    public static final long SEQUENCE_INVALID = 0;

    public static final int COPY_KEY_NONE = 0;
    public static final int COPY_KEY_COPY = 1;
    public static final int COPY_KEY_CUT = 2;

    private int type;
    private String text;
    private int metaState; // KeyEvent.META_*
    private int action; // KeyEvent.ACTION_* or MotionEvent.ACTION_*
    private int keycode; // KeyEvent.KEYCODE_*
    private int actionButton; // MotionEvent.BUTTON_*
    private int buttons; // MotionEvent.BUTTON_*
    private long pointerId;
    private float pressure;
    private Position position;
    private float hScroll;
    private float vScroll;
    private int copyKey;
    private boolean paste;
    private int repeat;
    private long sequence;
    private int id;
    private byte[] data;
    private boolean on;
    private int vendorId;
    private int productId;

    //add by qcr
    private int duration;
    private Position dragStartPosition;
    private Position dragEndPosition;
    private String uploadLogTime;//上传日志命令中 上传日志的时间配置

    private String vpnHost;
    private String vpnPort;
    private String vpnExcludeHost;

    private ControlMessage() {
    }

    public static ControlMessage createInjectKeycode(int action, int keycode, int repeat, int metaState) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_INJECT_KEYCODE;
        msg.action = action;
        msg.keycode = keycode;
        msg.repeat = repeat;
        msg.metaState = metaState;
        return msg;
    }

    public static ControlMessage createInjectText(String text) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_INJECT_TEXT;
        msg.text = text;
        return msg;
    }

    public static ControlMessage createInjectTouchEvent(int action, long pointerId, Position position, float pressure, int actionButton,
            int buttons) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_INJECT_TOUCH_EVENT;
        msg.action = action;
        msg.pointerId = pointerId;
        msg.pressure = pressure;
        msg.position = position;
        msg.actionButton = actionButton;
        msg.buttons = buttons;
        return msg;
    }

    public static ControlMessage createInjectScrollEvent(Position position, float hScroll, float vScroll, int buttons) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_INJECT_SCROLL_EVENT;
        msg.position = position;
        msg.hScroll = hScroll;
        msg.vScroll = vScroll;
        msg.buttons = buttons;
        return msg;
    }

    public static ControlMessage createBackOrScreenOn(int action) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_BACK_OR_SCREEN_ON;
        msg.action = action;
        return msg;
    }

    public static ControlMessage createGetClipboard(int copyKey) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_GET_CLIPBOARD;
        msg.copyKey = copyKey;
        return msg;
    }

    public static ControlMessage createSetClipboard(long sequence, String text, boolean paste) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_SET_CLIPBOARD;
        msg.sequence = sequence;
        msg.text = text;
        msg.paste = paste;
        return msg;
    }

    public static ControlMessage createSetDisplayPower(boolean on) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_SET_DISPLAY_POWER;
        msg.on = on;
        return msg;
    }

    public static ControlMessage createEmpty(int type) {
        ControlMessage msg = new ControlMessage();
        msg.type = type;
        return msg;
    }

    public static ControlMessage createUhidCreate(int id, int vendorId, int productId, String name, byte[] reportDesc) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_UHID_CREATE;
        msg.id = id;
        msg.vendorId = vendorId;
        msg.productId = productId;
        msg.text = name;
        msg.data = reportDesc;
        return msg;
    }

    public static ControlMessage createUhidInput(int id, byte[] data) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_UHID_INPUT;
        msg.id = id;
        msg.data = data;
        return msg;
    }

    public static ControlMessage createUhidDestroy(int id) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_UHID_DESTROY;
        msg.id = id;
        return msg;
    }

    public static ControlMessage createStartApp(String name) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_START_APP;
        msg.text = name;
        return msg;
    }


    public static ControlMessage createSleepEvent(int sleepDuration) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_SLEEP;
        msg.duration = sleepDuration;
        return msg;
    }

    public static ControlMessage createMockClickEvent(Position position) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_MOCK_CLICK;
        msg.position = position;
        return msg;
    }

    public static ControlMessage createMockDoubleClickEvent(Position position) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_MOCK_DOUBLE_CLICK;
        msg.position = position;
        return msg;
    }
    public static ControlMessage createMockDragEvent(Position startPosition,Position endPosition, int duration) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_MOCK_DRAG;
        msg.dragStartPosition = startPosition;
        msg.dragEndPosition = endPosition;
        msg.duration = duration;
        return msg;
    }

    public static ControlMessage createScreenShotEvent(int id) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_SCREEN_SHOT;
        msg.id = id;
        return msg;
    }
    public static ControlMessage createScreenShotEventResult(int id) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_SCREEN_SHOT_UPLOAD_RESULT;
        msg.id = id;
        return msg;
    }
    public static ControlMessage createUploadLog(String timeConfig) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_UPLOAD_LOG;
        msg.uploadLogTime = timeConfig;
        return msg;
    }

    public static ControlMessage createSetNetworkVpn(String host, String port, String excludeHost) {
        ControlMessage msg = new ControlMessage();
        msg.type = TYPE_SET_NETWORK_VPN;
        msg.vpnHost = host;
        msg.vpnPort=port;
        msg.vpnExcludeHost = excludeHost;
        return msg;
    }


    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getMetaState() {
        return metaState;
    }

    public int getAction() {
        return action;
    }

    public int getKeycode() {
        return keycode;
    }

    public int getActionButton() {
        return actionButton;
    }

    public int getButtons() {
        return buttons;
    }

    public long getPointerId() {
        return pointerId;
    }

    public float getPressure() {
        return pressure;
    }

    public Position getPosition() {
        return position;
    }

    public float getHScroll() {
        return hScroll;
    }

    public float getVScroll() {
        return vScroll;
    }

    public int getCopyKey() {
        return copyKey;
    }

    public boolean getPaste() {
        return paste;
    }

    public int getRepeat() {
        return repeat;
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

    public boolean getOn() {
        return on;
    }

    public int getVendorId() {
        return vendorId;
    }

    public int getProductId() {
        return productId;
    }

    public Position getDragEndPosition() {
        return dragEndPosition;
    }

    public void setDragEndPosition(Position dragEndPosition) {
        this.dragEndPosition = dragEndPosition;
    }

    public Position getDragStartPosition() {
        return dragStartPosition;
    }

    public void setDragStartPosition(Position dragStartPosition) {
        this.dragStartPosition = dragStartPosition;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUploadLogTime() {
        return uploadLogTime;
    }

    public void setUploadLogTime(String uploadLogTime) {
        this.uploadLogTime = uploadLogTime;
    }

    public String getVpnHost() {
        return vpnHost;
    }

    public String getVpnPort() {
        return vpnPort;
    }

    public String getVpnExcludeHost() {
        return vpnExcludeHost;
    }
}

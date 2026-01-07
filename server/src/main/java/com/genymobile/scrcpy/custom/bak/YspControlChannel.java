//package com.genymobile.scrcpy.custom.controll;
//
//import android.net.LocalSocket;
//
//import com.genymobile.scrcpy.control.ControlMessage;
//import com.genymobile.scrcpy.control.ControlMessageReader;
//import com.genymobile.scrcpy.control.DeviceMessage;
//import com.genymobile.scrcpy.control.DeviceMessageWriter;
//
//import java.io.IOException;
//
//public final class YspControlChannel {
//
//    private final YspControlMessageReader reader;
//    private final DeviceMessageWriter writer;
//
//    public YspControlChannel(LocalSocket controlSocket) throws IOException {
//        reader = new YspControlMessageReader(controlSocket.getInputStream());
//        writer = new DeviceMessageWriter(controlSocket.getOutputStream());
//    }
//
//    public YspControlMessage recv() throws IOException {
//        return reader.read();
//    }
//
//    public void send(DeviceMessage msg) throws IOException {
//        writer.write(msg);
//    }
//}

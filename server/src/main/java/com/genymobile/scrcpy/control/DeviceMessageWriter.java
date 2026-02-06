package com.genymobile.scrcpy.control;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.util.FileIOUtils;
import com.genymobile.scrcpy.util.FileUtils;
import com.genymobile.scrcpy.util.Logger;
import com.genymobile.scrcpy.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DeviceMessageWriter {

    public static final String TAG = ScrcpyConfig.getLogGroup() + DeviceMessageWriter.class.getSimpleName();
    private static final int MESSAGE_MAX_SIZE = 1 << 18; // 256k
    public static final int CLIPBOARD_TEXT_MAX_LENGTH = MESSAGE_MAX_SIZE - 5; // type: 1 byte; length: 4 bytes
    private final DataOutputStream dos;

    public DeviceMessageWriter(OutputStream rawOutputStream) {
        dos = new DataOutputStream(new BufferedOutputStream(rawOutputStream));
    }

    public void write(DeviceMessage msg) throws IOException {
        int type = msg.getType();
        dos.writeByte(type);
        switch (type) {
            case DeviceMessage.TYPE_CLIPBOARD:
                String text = msg.getText();
                byte[] raw = text.getBytes(StandardCharsets.UTF_8);
                int len = StringUtils.getUtf8TruncationIndex(raw, CLIPBOARD_TEXT_MAX_LENGTH);
                dos.writeInt(len);
                dos.write(raw, 0, len);
                break;
            case DeviceMessage.TYPE_ACK_CLIPBOARD:
                dos.writeLong(msg.getSequence());
                break;
            case DeviceMessage.TYPE_UHID_OUTPUT:
                dos.writeShort(msg.getId());
                byte[] data = msg.getData();
                dos.writeShort(data.length);
                dos.write(data);
                break;
            case DeviceMessage.TYPE_UPLOAD_LOG_ERROR:
                Logger.i(TAG,"uploadLogError start");
                dos.writeLong(msg.getId());
                byte[] fileNameByte2 = msg.getFileName().getBytes(StandardCharsets.UTF_8);
                dos.writeLong(fileNameByte2.length);
                dos.write(fileNameByte2);
                byte[] errorMsgBytes = msg.getErrorMsg().getBytes(StandardCharsets.UTF_8);
                dos.writeLong(errorMsgBytes.length);
                dos.write(errorMsgBytes);
                Logger.i(TAG,"uploadLogError finish");
                break;
            case DeviceMessage.TYPE_UPLOAD_LOG_FILE:
                Logger.i(TAG,"uploadLogFile start");
                dos.writeLong(msg.getId());
                byte[] fileNameBytes = msg.getFileName().getBytes(StandardCharsets.UTF_8);
                dos.writeLong(fileNameBytes.length);
                dos.write(fileNameBytes);

                File file = new File(msg.getFilePath());
                long length = file.length();
                dos.writeLong(length);
                boolean isSuccess=writeFile(file, dos, null);
                Logger.i(TAG, "uploadLogFile, fileSize:%s, result:%s" , FileUtils.formatFileSize(length),isSuccess);
                break;

            case DeviceMessage.TYPE_UPLOAD_SCREENSHOT:
                Logger.i(TAG,"uploadScreenShot start");
                dos.writeLong(msg.getId());
                byte[] fileNameBytes2 = msg.getFileName().getBytes(StandardCharsets.UTF_8);
                dos.writeLong(fileNameBytes2.length);
                dos.write(fileNameBytes2);

                File file2 = new File(msg.getFilePath());
                long length2 = file2.length();
                dos.writeLong(length2);
                boolean isSuccess2=writeFile(file2, dos, null);
                Logger.i(TAG, "uploadScreenShot, fileSize:%s, result:%s" , FileUtils.formatFileSize(length2),isSuccess2);
                break;
            default:
                throw new ControlProtocolException("Unknown event type: " + type);
        }
        dos.flush();
    }

    private static int sBufferSize = 16*1024;
    public static boolean writeFile(final File file,
                                 DataOutputStream dos,
                                 final FileIOUtils.OnProgressUpdateListener listener) {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file), sBufferSize);
            try {
                byte[] b = new byte[sBufferSize];
                int len;
                if (listener == null) {
                    while ((len = is.read(b, 0, sBufferSize)) != -1) {
                        dos.write(b, 0, len);
                    }
                } else {
                    double totalSize = is.available();
                    int curSize = 0;
                    listener.onProgressUpdate(0);
                    while ((len = is.read(b, 0, sBufferSize)) != -1) {
                        dos.write(b, 0, len);
                        curSize += len;
                        listener.onProgressUpdate(curSize / totalSize);
                    }
                }
                return true;
            } catch (Exception e) {
                Logger.i(TAG,"writeFile exception:"+e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Logger.i(TAG,"writeFile exception:"+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}

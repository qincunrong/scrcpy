// InstructionEngine.java
package com.genymobile.scrcpy.custom.controller;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import com.genymobile.scrcpy.control.Controller;
import com.genymobile.scrcpy.device.Device;
import com.genymobile.scrcpy.util.Ln;
import com.googlecode.tesseract.android.TessBaseAPI; // Tesseract OCR

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InstructionEngine {
    private static final String TAG = "InstructionEngine";
    
    private final Device device;
    private final Controller controller;
    private final ExecutorService executor;
    private Future<?> currentTask;
    
    // OCR引擎
    private TessBaseAPI tessBaseAPI;
    private boolean ocrInitialized = false;
    private final Object ocrLock = new Object();
    
    public InstructionEngine(Device device, Controller controller) {
        this.device = device;
        this.controller = controller;
        this.executor = Executors.newSingleThreadExecutor();
        
        // 初始化OCR引擎（异步）
        new Thread(this::initOCR).start();
    }
    
    private void initOCR() {
        synchronized (ocrLock) {
            try {
                // 检查是否安装了Tesseract训练数据
                File tessDir = new File("/sdcard/tessdata/");
                if (!tessDir.exists()) {
                    tessDir.mkdirs();
                }
                
                // 检查训练数据文件
                String[] languages = {"eng", "chi_sim"};
                for (String lang : languages) {
                    File trainedData = new File(tessDir, lang + ".traineddata");
                    if (!trainedData.exists()) {
                        Ln.w("OCR训练数据未找到: " + lang + ".traineddata");
                        // 这里可以从assets复制训练数据，但需要修改构建流程
                        // 为简化，我们可以依赖用户预装训练数据
                    }
                }
                
                tessBaseAPI = new TessBaseAPI();
                if (tessBaseAPI.init("/sdcard/tessdata/", "eng")) {
                    ocrInitialized = true;
                    Ln.i("OCR引擎初始化成功");
                } else {
                    Ln.e("OCR引擎初始化失败");
                }
            } catch (Exception e) {
                Ln.e("初始化OCR引擎失败", e);
            }
        }
    }
    
    public void executeSequence(InstructionProto.InstructionSequence sequence) {
        // 取消之前的任务
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        
        currentTask = executor.submit(() -> {
            try {
                InstructionProto.InstructionResult result = executeSequenceInternal(sequence);
                sendResult(result);
            } catch (Exception e) {
                Ln.e("执行指令序列失败", e);
                InstructionProto.InstructionResult errorResult = new InstructionProto.InstructionResult();
                errorResult.setSequenceId(sequence.getSequenceId());
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                sendResult(errorResult);
            }
        });
    }
    
    private InstructionProto.InstructionResult executeSequenceInternal(
            InstructionProto.InstructionSequence sequence) {
        
        InstructionProto.InstructionResult result = new InstructionProto.InstructionResult();
        result.setSequenceId(sequence.getSequenceId());
        result.setResults(new ArrayList<>());
        
        boolean allSuccess = true;
        
        for (InstructionProto.Instruction instruction : sequence.getInstructions()) {
            // 检查是否被取消
            if (Thread.currentThread().isInterrupted()) {
                result.setSuccess(false);
                result.setErrorMessage("执行被取消");
                return result;
            }
            
            // 执行延迟
            if (instruction.getDelayMs() > 0) {
                try {
                    Thread.sleep(instruction.getDelayMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    result.setSuccess(false);
                    result.setErrorMessage("执行被中断");
                    return result;
                }
            }
            
            // 执行单个指令
            InstructionProto.InstructionExecutionResult executionResult = 
                executeInstruction(instruction);
            
            result.getResults().add(executionResult);
            
            if (!executionResult.isSuccess()) {
                allSuccess = false;
                // 可以设置是否继续执行后续指令
                // break;
            }
        }
        
        result.setSuccess(allSuccess);
        return result;
    }
    
    private InstructionProto.InstructionExecutionResult executeInstruction(
            InstructionProto.Instruction instruction) {
        
        InstructionProto.InstructionExecutionResult result = 
            new InstructionProto.InstructionExecutionResult();
        result.setInstructionId(instruction.getId());
        
        long startTime = SystemClock.uptimeMillis();
        boolean success = false;
        String resultData = null;
        
        try {
            InstructionProto.Operation operation = instruction.getOperation();
            
            switch (operation.getType()) {
                case CLICK:
                    success = executeClick((InstructionProto.Click) operation);
                    break;
                case DOUBLE_CLICK:
                    success = executeDoubleClick((InstructionProto.DoubleClick) operation);
                    break;
                case SWIPE:
                    success = executeSwipe((InstructionProto.Swipe) operation);
                    break;
                case TEXT_COMPARE:
                    TextCompareResult compareResult = 
                        executeTextCompare((InstructionProto.TextCompare) operation);
                    success = compareResult.isMatch();
                    resultData = compareResult.getRecognizedText();
                    break;
                default:
                    throw new IllegalArgumentException("未知操作类型: " + operation.getType());
            }
        } catch (Exception e) {
            Ln.e("执行指令失败: " + instruction.getId(), e);
            resultData = e.getMessage();
        }
        
        result.setSuccess(success);
        result.setResultData(resultData);
        result.setExecutionTime(SystemClock.uptimeMillis() - startTime);
        
        return result;
    }
    
    private boolean executeClick(InstructionProto.Click click) {
        try {
            // 使用Controller的injectTouchEvent方法
            int x = click.getX();
            int y = click.getY();
            
            // 生成触摸按下事件
            MotionEvent.PointerProperties props = new MotionEvent.PointerProperties();
            props.id = 0;
            props.toolType = MotionEvent.TOOL_TYPE_FINGER;
            
            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            coords.x = x;
            coords.y = y;
            
            long now = SystemClock.uptimeMillis();
            
            // 按下
            MotionEvent event = MotionEvent.obtain(
                now, now, MotionEvent.ACTION_DOWN, 1,
                new MotionEvent.PointerProperties[]{props},
                new MotionEvent.PointerCoords[]{coords},
                0, 0, 1.0f, 1.0f, 0, 0,
                InputDevice.SOURCE_TOUCHSCREEN, 0
            );

            controller.injectEvent(event);
            
            // 短暂停留后抬起
            Thread.sleep(50);
            
            event = MotionEvent.obtain(
                now + 50, now + 50, MotionEvent.ACTION_UP, 1,
                new MotionEvent.PointerProperties[]{props},
                new MotionEvent.PointerCoords[]{coords},
                0, 0, 1.0f, 1.0f, 0, 0,
                InputDevice.SOURCE_TOUCHSCREEN, 0
            );
            
            controller.injectEvent(event);
            
            return true;
        } catch (Exception e) {
            Ln.e("执行点击失败", e);
            return false;
        }
    }
    
    private boolean executeDoubleClick(InstructionProto.DoubleClick doubleClick) {
        try {
            // 执行两次点击，中间有短暂间隔
            InstructionProto.Click click = new InstructionProto.Click();
            click.setX(doubleClick.getX());
            click.setY(doubleClick.getY());
            
            executeClick(click);
            Thread.sleep(100); // 双击间隔
            executeClick(click);
            
            return true;
        } catch (Exception e) {
            Ln.e("执行双击失败", e);
            return false;
        }
    }
    
    private boolean executeSwipe(InstructionProto.Swipe swipe) {
        try {
            int startX = swipe.getStartX();
            int startY = swipe.getStartY();
            int endX = swipe.getEndX();
            int endY = swipe.getEndY();
            int duration = swipe.getDurationMs();
            
            // 生成抛物线轨迹
            int steps = duration / 16; // 每步约16ms，约60fps
            if (steps < 2) steps = 2;
            
            MotionEvent.PointerProperties props = new MotionEvent.PointerProperties();
            props.id = 0;
            props.toolType = MotionEvent.TOOL_TYPE_FINGER;
            
            long startTime = SystemClock.uptimeMillis();
            
            // 按下
            MotionEvent.PointerCoords startCoords = new MotionEvent.PointerCoords();
            startCoords.x = startX;
            startCoords.y = startY;
            
            MotionEvent event = MotionEvent.obtain(
                startTime, startTime, MotionEvent.ACTION_DOWN, 1,
                new MotionEvent.PointerProperties[]{props},
                new MotionEvent.PointerCoords[]{startCoords},
                0, 0, 1.0f, 1.0f, 0, 0,
                InputDevice.SOURCE_TOUCHSCREEN, 0
            );
            
            controller.injectEvent(event);
            
            // 移动（抛物线）
            for (int i = 1; i <= steps; i++) {
                float t = (float) i / steps;
                
                // 使用贝塞尔曲线创建抛物线效果
                float controlX = (startX + endX) / 2;
                float controlY = Math.min(startY, endY) - 100; // 向上偏移100像素形成抛物线
                
                // 二次贝塞尔曲线
                float x = (1 - t) * (1 - t) * startX + 
                          2 * (1 - t) * t * controlX + 
                          t * t * endX;
                float y = (1 - t) * (1 - t) * startY + 
                          2 * (1 - t) * t * controlY + 
                          t * t * endY;
                
                MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
                coords.x = x;
                coords.y = y;
                
                event = MotionEvent.obtain(
                    startTime + (i * duration / steps), 
                    startTime + (i * duration / steps), 
                    MotionEvent.ACTION_MOVE, 1,
                    new MotionEvent.PointerProperties[]{props},
                    new MotionEvent.PointerCoords[]{coords},
                    0, 0, 1.0f, 1.0f, 0, 0,
                    InputDevice.SOURCE_TOUCHSCREEN, 0
                );
                
                controller.injectEvent(event);
                Thread.sleep(16); // 模拟60fps
            }
            
            // 抬起
            MotionEvent.PointerCoords endCoords = new MotionEvent.PointerCoords();
            endCoords.x = endX;
            endCoords.y = endY;
            
            event = MotionEvent.obtain(
                startTime + duration, startTime + duration, MotionEvent.ACTION_UP, 1,
                new MotionEvent.PointerProperties[]{props},
                new MotionEvent.PointerCoords[]{endCoords},
                0, 0, 1.0f, 1.0f, 0, 0,
                InputDevice.SOURCE_TOUCHSCREEN, 0
            );
            
            controller.injectEvent(event);
            
            return true;
        } catch (Exception e) {
            Ln.e("执行滑动失败", e);
            return false;
        }
    }
    
    private TextCompareResult executeTextCompare(InstructionProto.TextCompare textCompare) {
        synchronized (ocrLock) {
            if (!ocrInitialized) {
                Ln.w("OCR引擎未初始化");
                return new TextCompareResult(false, "OCR引擎未初始化");
            }
            
            try {
                // 截图指定区域
                Bitmap screenshot = device.screenshot();
                if (screenshot == null) {
                    return new TextCompareResult(false, "截图失败");
                }
                
                // 裁剪指定区域
                Rect rect = new Rect(
                    textCompare.getX(),
                    textCompare.getY(),
                    textCompare.getX() + textCompare.getWidth(),
                    textCompare.getY() + textCompare.getHeight()
                );
                
                if (rect.right > screenshot.getWidth() || rect.bottom > screenshot.getHeight()) {
                    return new TextCompareResult(false, "区域超出屏幕范围");
                }
                
                Bitmap region = Bitmap.createBitmap(
                    screenshot, rect.left, rect.top, rect.width(), rect.height()
                );
                
                // 设置OCR语言
//                tessBaseAPI.setLanguage(textCompare.getLanguage());
                
                // 设置页面分割模式
                tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
                
                // 识别文字
                tessBaseAPI.setImage(region);
                String recognizedText = tessBaseAPI.getUTF8Text().trim();
                
                Ln.d("OCR识别结果: " + recognizedText + ", 期望: " + textCompare.getExpectedText());
                
                // 对比文字
                boolean match = recognizedText.equalsIgnoreCase(textCompare.getExpectedText()) ||
                               recognizedText.contains(textCompare.getExpectedText());
                
                return new TextCompareResult(match, recognizedText);
                
            } catch (Exception e) {
                Ln.e("文字识别失败", e);
                return new TextCompareResult(false, "识别失败: " + e.getMessage());
            }
        }
    }
    
    private void sendResult(InstructionProto.InstructionResult result) {
        try {
            // 这里需要通过socket发送结果给客户端
            // 需要修改Server类来支持发送自定义消息
            // 暂时打印到日志
            Ln.i("指令执行结果: sequenceId=" + result.getSequenceId() + 
                 ", success=" + result.isSuccess());
        } catch (Exception e) {
            Ln.e("发送结果失败", e);
        }
    }
    
    public void destroy() {
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        executor.shutdownNow();
        
        synchronized (ocrLock) {
            if (tessBaseAPI != null) {
                tessBaseAPI.recycle();
            }
        }
    }
    
    private static class TextCompareResult {
        private final boolean match;
        private final String recognizedText;
        
        public TextCompareResult(boolean match, String recognizedText) {
            this.match = match;
            this.recognizedText = recognizedText;
        }
        
        public boolean isMatch() { return match; }
        public String getRecognizedText() { return recognizedText; }
    }
}
// Server.java (部分修改)
package com.genymobile.scrcpy.custom.controller;

import com.genymobile.scrcpy.control.Controller;
import com.genymobile.scrcpy.device.Device;
import com.genymobile.scrcpy.util.Ln;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class InstructReader {
    private Device device;
    private Controller controller;
    private InstructionEngine instructionEngine;
    
    // 在构造函数或初始化方法中添加
    private void initializeInstructionEngine() {
        instructionEngine = new InstructionEngine(device, controller);
    }
    
    // 处理新的控制消息
    private void handleInstructionSequence(ByteBuffer buffer) {
        try {
            // 解析JSON格式的指令序列
            int length = buffer.getInt();
            byte[] jsonBytes = new byte[length];
            buffer.get(jsonBytes);
            
            String jsonStr = new String(jsonBytes, "UTF-8");
            JSONObject json = new JSONObject(jsonStr);
            
            InstructionProto.InstructionSequence sequence = 
                parseInstructionSequence(json);
            
            // 执行指令序列
            instructionEngine.executeSequence(sequence);
            
        } catch (Exception e) {
            Ln.e("解析指令序列失败", e);
        }
    }
    
    private InstructionProto.InstructionSequence parseInstructionSequence(JSONObject json) throws Exception {
        InstructionProto.InstructionSequence sequence = 
            new InstructionProto.InstructionSequence();
        
        sequence.setSequenceId(json.getInt("sequenceId"));
        
        JSONArray instructionsJson = json.getJSONArray("instructions");
        List<InstructionProto.Instruction> instructions = new ArrayList<>();
        
        for (int i = 0; i < instructionsJson.length(); i++) {
            JSONObject instructionJson = instructionsJson.getJSONObject(i);
            InstructionProto.Instruction instruction = new InstructionProto.Instruction();
            
            instruction.setId(instructionJson.getInt("id"));
            instruction.setDelayMs(instructionJson.getInt("delayMs"));
            
            JSONObject operationJson = instructionJson.getJSONObject("operation");
            String type = operationJson.getString("type");
            
            InstructionProto.Operation operation;
            switch (type) {
                case "click":
                    InstructionProto.Click click = new InstructionProto.Click();
                    click.setX(operationJson.getInt("x"));
                    click.setY(operationJson.getInt("y"));
                    operation = click;
                    break;
                    
                case "doubleClick":
                    InstructionProto.DoubleClick doubleClick = new InstructionProto.DoubleClick();
                    doubleClick.setX(operationJson.getInt("x"));
                    doubleClick.setY(operationJson.getInt("y"));
                    operation = doubleClick;
                    break;
                    
                case "swipe":
                    InstructionProto.Swipe swipe = new InstructionProto.Swipe();
                    swipe.setStartX(operationJson.getInt("startX"));
                    swipe.setStartY(operationJson.getInt("startY"));
                    swipe.setEndX(operationJson.getInt("endX"));
                    swipe.setEndY(operationJson.getInt("endY"));
                    if (operationJson.has("durationMs")) {
                        swipe.setDurationMs(operationJson.getInt("durationMs"));
                    }
                    operation = swipe;
                    break;
                    
                case "textCompare":
                    InstructionProto.TextCompare textCompare = new InstructionProto.TextCompare();
                    textCompare.setX(operationJson.getInt("x"));
                    textCompare.setY(operationJson.getInt("y"));
                    textCompare.setWidth(operationJson.getInt("width"));
                    textCompare.setHeight(operationJson.getInt("height"));
                    textCompare.setExpectedText(operationJson.getString("expectedText"));
                    
                    if (operationJson.has("language")) {
                        textCompare.setLanguage(operationJson.getString("language"));
                    }
                    if (operationJson.has("confidence")) {
                        textCompare.setConfidence((float) operationJson.getDouble("confidence"));
                    }
                    operation = textCompare;
                    break;
                    
                default:
                    throw new IllegalArgumentException("未知操作类型: " + type);
            }
            
            instruction.setOperation(operation);
            instructions.add(instruction);
        }
        
        sequence.setInstructions(instructions);
        return sequence;
    }
    
    // 在cleanup方法中添加
    private void cleanup() {
        if (instructionEngine != null) {
            instructionEngine.destroy();
        }
        // ... 其他清理代码
    }
}
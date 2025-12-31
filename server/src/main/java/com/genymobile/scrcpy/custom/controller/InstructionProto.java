// InstructionProto.java
package com.genymobile.scrcpy.custom.controller;

import java.util.List;

public class InstructionProto {
    
    public static class InstructionSequence {
        private int sequenceId;
        private List<Instruction> instructions;
        
        // Getters and setters
        public int getSequenceId() { return sequenceId; }
        public void setSequenceId(int sequenceId) { this.sequenceId = sequenceId; }
        public List<Instruction> getInstructions() { return instructions; }
        public void setInstructions(List<Instruction> instructions) { this.instructions = instructions; }
    }
    
    public static class Instruction {
        private int id;
        private int delayMs;
        private Operation operation;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getDelayMs() { return delayMs; }
        public void setDelayMs(int delayMs) { this.delayMs = delayMs; }
        public Operation getOperation() { return operation; }
        public void setOperation(Operation operation) { this.operation = operation; }
    }
    
    public static abstract class Operation {
        public enum Type {
            CLICK, DOUBLE_CLICK, SWIPE, TEXT_COMPARE
        }
        
        public abstract Type getType();
    }
    
    public static class Click extends Operation {
        private int x;
        private int y;
        
        @Override
        public Type getType() { return Type.CLICK; }
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }
    
    public static class DoubleClick extends Operation {
        private int x;
        private int y;
        
        @Override
        public Type getType() { return Type.DOUBLE_CLICK; }
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }
    
    public static class Swipe extends Operation {
        private int startX;
        private int startY;
        private int endX;
        private int endY;
        private int durationMs = 300; // 默认300ms
        
        @Override
        public Type getType() { return Type.SWIPE; }
        
        // Getters and setters
        public int getStartX() { return startX; }
        public void setStartX(int startX) { this.startX = startX; }
        public int getStartY() { return startY; }
        public void setStartY(int startY) { this.startY = startY; }
        public int getEndX() { return endX; }
        public void setEndX(int endX) { this.endX = endX; }
        public int getEndY() { return endY; }
        public void setEndY(int endY) { this.endY = endY; }
        public int getDurationMs() { return durationMs; }
        public void setDurationMs(int durationMs) { this.durationMs = durationMs; }
    }
    
    public static class TextCompare extends Operation {
        private int x;
        private int y;
        private int width;
        private int height;
        private String expectedText;
        private String language = "eng"; // 默认英语
        private float confidence = 0.7f; // 默认置信度0.7
        
        @Override
        public Type getType() { return Type.TEXT_COMPARE; }
        
        // Getters and setters
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public String getExpectedText() { return expectedText; }
        public void setExpectedText(String expectedText) { this.expectedText = expectedText; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public float getConfidence() { return confidence; }
        public void setConfidence(float confidence) { this.confidence = confidence; }
    }
    
    public static class InstructionResult {
        private int sequenceId;
        private boolean success;
        private List<InstructionExecutionResult> results;
        private String errorMessage;
        
        // Getters and setters
        public int getSequenceId() { return sequenceId; }
        public void setSequenceId(int sequenceId) { this.sequenceId = sequenceId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public List<InstructionExecutionResult> getResults() { return results; }
        public void setResults(List<InstructionExecutionResult> results) { this.results = results; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    public static class InstructionExecutionResult {
        private int instructionId;
        private boolean success;
        private String resultData;
        private long executionTime;
        
        // Getters and setters
        public int getInstructionId() { return instructionId; }
        public void setInstructionId(int instructionId) { this.instructionId = instructionId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getResultData() { return resultData; }
        public void setResultData(String resultData) { this.resultData = resultData; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    }
}
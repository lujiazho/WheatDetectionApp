package com.xyqlx.paddydoctor.data.model;

// 识别记录
public class HistoryItem {
    private String fileId;
    private String detectType;
    private double confidence;

    public HistoryItem(String fileId, String detectType, double confidence) {
        this.fileId = fileId;
        this.detectType = detectType;
        this.confidence = confidence;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getDetectType() {
        return detectType;
    }

    public void setDetectType(String detectType) {
        this.detectType = detectType;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}

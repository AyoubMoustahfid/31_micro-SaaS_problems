package com.example.microSaas.upload.enums;

public final class UploadEnums {

    private UploadEnums() {}

    public enum ScanStatus {
        PENDING,
        PROCESSING,
        CLEAN,
        INFECTED,
        ERROR
    }

    public enum UploadStatus {
        PROCESSING,
        COMPLETED,
        PARTIALLY_FAILED,
        FAILED
    }

    public enum ProcessingStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}

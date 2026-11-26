package com.example.microSaas.platform.entity;

import java.util.UUID;
import java.time.Instant;

import com.example.microSaas.upload.enums.UploadEnums.ScanStatus;
import com.example.microSaas.upload.enums.UploadEnums.ProcessingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name="uploaded_files", schema = "platform")
public class UploadedFile {
    

    @Id 
    private UUID id;

    @Column(name="upload_batch_id", nullable = false)
    private UUID uploadBatchId;
    
    @Column(name = "challenge_id", nullable = false)
    private String challengeId;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "stored_name")
    private String storedName;

    @Column(name = "dataset_name")
    private String datasetName;

    @Column(name= "storage_path")
    private String storagePath;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "file_size")
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_status")
    private ScanStatus scanStatus;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processingStatus;

    protected UploadedFile() {}

    public UploadedFile(
        UUID id,
        UUID batchId,
        String challengeId,
        String originalName,
        String storedName,
        String datasetName,
        String storagePath,
        long fileSize
    ) {
        this(
            id,
            batchId,
            challengeId,
            originalName,
            storedName,
            datasetName,
            storagePath,
            fileSize,
            ScanStatus.PENDING
        );
    }

    public UploadedFile(
        UUID id,
        UUID batchId,
        String challengeId,
        String originalName,
        String storedName,
        String datasetName,
        String storagePath,
        long fileSize,
        ScanStatus scanStatus
    ) {
        this.id = id;
        this.uploadBatchId = batchId;
        this.challengeId = challengeId;
        this.originalName = originalName;
        this.storedName = storedName;
        this.datasetName = datasetName;
        this.storagePath = storagePath;
        this.fileExtension = "csv";
        this.fileSize = fileSize;
        this.scanStatus = scanStatus;
        this.processingStatus = ProcessingStatus.PENDING;
    }

    public UploadedFile(
        UUID batchId,
        String challengeId,
        String originalName,
        String storedName,
        String datasetName,
        String storagePath,
        long fileSize
    ) {
        this(
            UUID.randomUUID(),
            batchId,
            challengeId,
            originalName,
            storedName,
            datasetName,
            storagePath,
            fileSize
        );
    }

    public UUID getId() {return id;}
    public UUID getUploadBatchId() {return uploadBatchId;}
    public String getChallengeId() { return challengeId; }
    public String getOriginalName() { return originalName; }
    public String getDatasetName() { return datasetName; }
    public String getStoragePath() { return storagePath; }
    public long getFileSize() { return fileSize; }
    public ScanStatus getScanStatus() { return scanStatus; }
    public ProcessingStatus getProcessingStatus() { return processingStatus; }
    public Instant getProcessingStartedAt() { return processingStartedAt; }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public void setScanStatus(ScanStatus scanStatus) {
        this.scanStatus = scanStatus;
    }

    public void setProcessingStartedAt(Instant processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }
}

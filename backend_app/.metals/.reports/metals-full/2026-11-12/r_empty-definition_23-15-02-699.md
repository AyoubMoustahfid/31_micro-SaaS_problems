error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/platform/entity/UploadBatch.java:_empty_/UploadStatus#PROCESSING#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/platform/entity/UploadBatch.java
empty definition using pc, found symbol in pc: _empty_/UploadStatus#PROCESSING#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1039
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/platform/entity/UploadBatch.java
text:
```scala
package com.example.microSaas.platform.entity;

import java.time.Instant;
import java.util.UUID;

import com.example.microSaas.upload.enums.UploadStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "upload_batches", schema = "platform")
public class UploadBatch {

    @Id 
    private UUID id;

    @Column(name="challenge_id", nullable = false)
    private String challengeId;

    @Enumerated(EnumType.STRING)
    private UploadStatus status;

    @Column(name="total_files")
    private int totalFiles;

    @Column(name="created_at")
    private Instant createdAt;

    protected UploadBatch() {}

    public UploadBatch(UUID id, String challengeId, int totalFiles) {
        this.id = id;
        this.challengeId = challengeId;
        this.totalFiles = totalFiles;
        this.status = UploadStatus.@@PROCESSING;
        this.createdAt = Instant.now();
    }

    public UUID getId() {return id;}
    public String getChallengeId() {return challengeId;}
    public UploadStatus geStatus() {return status;}
    public int getTotalFiles() {return totalFiles;}
    public Instant getCreatedAt() {return createdAt;}

    public void setStatus(UploadStatus status) {
        this.status = status;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/UploadStatus#PROCESSING#
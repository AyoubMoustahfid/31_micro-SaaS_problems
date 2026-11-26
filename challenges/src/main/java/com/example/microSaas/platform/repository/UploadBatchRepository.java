package com.example.microSaas.platform.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.microSaas.platform.entity.UploadBatch;
import com.example.microSaas.upload.enums.UploadEnums.UploadStatus;

public interface UploadBatchRepository extends JpaRepository<UploadBatch, UUID> {

    List<UploadBatch> findByChallengeId(String challengeId);

    List<UploadBatch> findByStatus(UploadStatus status);
}

package com.example.microSaas.platform.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.microSaas.platform.entity.UploadedFile;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID>{
    
    List<UploadedFile> findByUploadBatchId(UUID batchId);

    List<UploadedFile> findByChallengeId(String challengeId);
}

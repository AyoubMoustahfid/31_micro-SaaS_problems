package com.example.microSaas.upload.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.microSaas.platform.entity.UploadBatch;
import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.repository.UploadBatchRepository;
import com.example.microSaas.platform.repository.UploadedFileRepository;
import com.example.microSaas.upload.enums.UploadEnums.ProcessingStatus;
import com.example.microSaas.upload.enums.UploadEnums.UploadStatus;

@Service
public class UploadJobStateService {

    private final JdbcTemplate jdbcTemplate;
    private final UploadBatchRepository batchRepository;
    private final UploadedFileRepository fileRepository;

    public UploadJobStateService(
        JdbcTemplate jdbcTemplate,
        UploadBatchRepository batchRepository,
        UploadedFileRepository fileRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.batchRepository = batchRepository;
        this.fileRepository = fileRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(UUID fileId) {
        return jdbcTemplate.update(
            """
            UPDATE platform.uploaded_files AS queued_file
               SET processing_status = 'PROCESSING',
                   processing_started_at = ?
             WHERE queued_file.id = ?
               AND queued_file.processing_status = 'PENDING'
               AND NOT EXISTS (
                   SELECT 1
                     FROM platform.uploaded_files AS prerequisite
                    WHERE prerequisite.upload_batch_id =
                          queued_file.upload_batch_id
                      AND prerequisite.processing_status IN ('PENDING', 'PROCESSING')
                      AND (
                          (queued_file.dataset_name = 'contract'
                           AND prerequisite.dataset_name = 'vendor')
                          OR
                          (queued_file.dataset_name = 'purchase_order'
                           AND prerequisite.dataset_name IN ('vendor', 'contract'))
                      )
               )
            """,
            Timestamp.from(Instant.now()),
            fileId
        ) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID fileId) {
        fileRepository.findById(fileId).ifPresent(file -> {
            file.setProcessingStatus(ProcessingStatus.FAILED);
            file.setProcessingStartedAt(null);
            fileRepository.saveAndFlush(file);
            refreshBatchStatus(file.getUploadBatchId());
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshBatchStatus(UUID batchId) {
        UploadBatch batch = batchRepository.findById(batchId).orElse(null);
        if (batch == null) {
            return;
        }

        List<ProcessingStatus> statuses = fileRepository
            .findByUploadBatchId(batchId)
            .stream()
            .map(UploadedFile::getProcessingStatus)
            .toList();
        if (statuses.isEmpty()) {
            batch.setStatus(UploadStatus.FAILED);
        } else if (statuses.stream().anyMatch(status ->
            status == ProcessingStatus.PENDING
                || status == ProcessingStatus.PROCESSING
        )) {
            batch.setStatus(UploadStatus.PROCESSING);
        } else {
            long cleanFiles = statuses.stream()
                .filter(status -> status == ProcessingStatus.COMPLETED)
                .count();
            if (cleanFiles == statuses.size()) {
                batch.setStatus(UploadStatus.COMPLETED);
            } else if (cleanFiles > 0) {
                batch.setStatus(UploadStatus.PARTIALLY_FAILED);
            } else {
                batch.setStatus(UploadStatus.FAILED);
            }
        }
        batchRepository.saveAndFlush(batch);
    }

    @Transactional
    public int requeueStaleProcessingFiles(long leaseSeconds) {
        return jdbcTemplate.update(
            """
            UPDATE platform.uploaded_files
               SET processing_status = 'PENDING',
                   processing_started_at = NULL
             WHERE processing_status = 'PROCESSING'
               AND processing_started_at
                   < NOW() - (? * INTERVAL '1 second')
            """,
            leaseSeconds
        );
    }
}

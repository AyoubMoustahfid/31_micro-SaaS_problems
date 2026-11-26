package com.example.microSaas.upload.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.microSaas.platform.entity.UploadBatch;
import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.repository.UploadBatchRepository;
import com.example.microSaas.platform.repository.UploadedFileRepository;
import com.example.microSaas.upload.dto.UploadResponse.DeleteChallengeUploadsResponse;
import com.example.microSaas.upload.registry.DatasetRegistry;

@Service
public class ChallengeUploadDeletionService {

    private static final Logger logger =
        LoggerFactory.getLogger(ChallengeUploadDeletionService.class);

    private final UploadBatchRepository batchRepository;
    private final UploadedFileRepository fileRepository;
    private final JdbcTemplate jdbcTemplate;
    private final FileStorageService storageService;
    private final DatasetRegistry registry;

    public ChallengeUploadDeletionService(
        UploadBatchRepository batchRepository,
        UploadedFileRepository fileRepository,
        JdbcTemplate jdbcTemplate,
        FileStorageService storageService,
        DatasetRegistry registry
    ) {
        this.batchRepository = batchRepository;
        this.fileRepository = fileRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.storageService = storageService;
        this.registry = registry;
    }

    @Transactional
    public DeleteChallengeUploadsResponse deleteChallengeUploads(
        String challengeId
    ) {
        if (!registry.supportsChallenge(challengeId)) {
            throw new IllegalArgumentException(
                "Unknown challenge: " + challengeId
            );
        }

        List<UploadedFile> files =
            fileRepository.findByChallengeId(challengeId);
        List<UploadBatch> batches =
            batchRepository.findByChallengeId(challengeId);

        deleteDatasetRows(challengeId, files);
        fileRepository.deleteAll(files);
        fileRepository.flush();
        batchRepository.deleteAll(batches);
        batchRepository.flush();

        if (!files.isEmpty()) {
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                throw new IllegalStateException(
                    "Transaction synchronization is required to delete uploaded files"
                );
            }
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        deleteStoredFiles(challengeId, files);
                    }
                }
            );
        }

        return new DeleteChallengeUploadsResponse(
            challengeId,
            files.size()
        );
    }

    private void deleteDatasetRows(
        String challengeId,
        List<UploadedFile> files
    ) {
        files.stream()
            .sorted(Comparator.comparingInt(file ->
                datasetDeletePriority(
                    registry.getDataset(
                        challengeId,
                        file.getDatasetName()
                    ).datasetType()
                )
            ))
            .forEach(file -> {
                var dataset = registry.getDataset(
                    challengeId,
                    file.getDatasetName()
                );
                jdbcTemplate.update(
                    "DELETE FROM " + dataset.schemaName() + "."
                        + dataset.tableName()
                        + " WHERE uploaded_file_id = ?",
                    file.getId()
                );
            });
    }

    private int datasetDeletePriority(String datasetType) {
        return switch (datasetType) {
            case "purchase_order" -> 0;
            case "contract" -> 1;
            case "vendor" -> 2;
            default -> 0;
        };
    }

    private void deleteStoredFiles(
        String challengeId,
        List<UploadedFile> files
    ) {
        IOException failure = null;
        for (UploadedFile file : files) {
            try {
                storageService.deleteUploadFile(
                    challengeId,
                    file.getStoragePath()
                );
            } catch (IOException exception) {
                logger.error(
                    "Could not delete stored upload {} for challenge {}",
                    file.getId(),
                    challengeId,
                    exception
                );
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }

        if (failure != null) {
            throw new IllegalStateException(
                "Uploaded file records were deleted, but one or more stored files could not be removed",
                failure
            );
        }
    }
}

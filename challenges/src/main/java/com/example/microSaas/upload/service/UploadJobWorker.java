package com.example.microSaas.upload.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.repository.UploadedFileRepository;

@Service
public class UploadJobWorker {

    private static final Logger logger =
        LoggerFactory.getLogger(UploadJobWorker.class);

    private final UploadedFileRepository fileRepository;
    private final UploadFileProcessor fileProcessor;
    private final UploadJobStateService jobStateService;
    private final FileStorageService storageService;

    public UploadJobWorker(
        UploadedFileRepository fileRepository,
        UploadFileProcessor fileProcessor,
        UploadJobStateService jobStateService,
        FileStorageService storageService
    ) {
        this.fileRepository = fileRepository;
        this.fileProcessor = fileProcessor;
        this.jobStateService = jobStateService;
        this.storageService = storageService;
    }

    public void processBatch(UUID batchId) {
        List<UploadedFile> files = fileRepository.findByUploadBatchId(batchId)
            .stream()
            .sorted(Comparator.comparingInt(file ->
                datasetPriority(file.getDatasetName())
            ))
            .toList();
        for (UploadedFile file : files) {
            try {
                fileProcessor.process(file.getId());
            } catch (Exception exception) {
                logger.error(
                    "Upload processing failed for file {} in batch {}",
                    file.getId(),
                    batchId,
                    exception
                );
                cleanupFailedFile(file, exception);
                jobStateService.markFailed(file.getId());
            }
        }
        jobStateService.refreshBatchStatus(batchId);
    }

    private int datasetPriority(String datasetName) {
        return switch (datasetName) {
            case "vendor" -> 0;
            case "contract" -> 1;
            default -> 2;
        };
    }

    private void cleanupFailedFile(UploadedFile file, Exception processingFailure) {
        try {
            storageService.delete(Path.of(file.getStoragePath()));
        } catch (IOException | SecurityException cleanupFailure) {
            processingFailure.addSuppressed(cleanupFailure);
            logger.error(
                "Could not remove staged upload file {}",
                file.getId(),
                cleanupFailure
            );
        }
        try {
            storageService.deleteCleanFile(
                file.getChallengeId(),
                storageService.cleanFilePath(
                    file.getChallengeId(),
                    file.getId()
                ).toString()
            );
        } catch (IOException | SecurityException cleanupFailure) {
            processingFailure.addSuppressed(cleanupFailure);
            logger.error(
                "Could not remove processed upload file {}",
                file.getId(),
                cleanupFailure
            );
        }
    }
}

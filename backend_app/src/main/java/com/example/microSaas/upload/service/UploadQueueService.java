package com.example.microSaas.upload.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.microSaas.platform.entity.UploadBatch;
import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.exception.ApiException;
import com.example.microSaas.platform.repository.UploadBatchRepository;
import com.example.microSaas.platform.repository.UploadedFileRepository;
import com.example.microSaas.upload.dto.UploadResponse;
import com.example.microSaas.upload.dto.UploadResponse.UploadedFileResponse;

@Service
public class UploadQueueService {

    private final UploadBatchRepository batchRepository;
    private final UploadedFileRepository fileRepository;
    private final UploadJobDispatcher jobDispatcher;

    public UploadQueueService(
        UploadBatchRepository batchRepository,
        UploadedFileRepository fileRepository,
        UploadJobDispatcher jobDispatcher
    ) {
        this.batchRepository = batchRepository;
        this.fileRepository = fileRepository;
        this.jobDispatcher = jobDispatcher;
    }

    @Transactional(rollbackFor = Exception.class)
    public UploadResponse enqueue(
        UUID batchId,
        String challengeId,
        List<UploadedFile> files
    ) {
        UploadBatch batch = new UploadBatch(
            batchId,
            challengeId,
            files.size()
        );
        batchRepository.saveAndFlush(batch);
        fileRepository.saveAllAndFlush(files);
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    jobDispatcher.dispatch(batchId);
                }
            }
        );
        return response(batch, files);
    }

    @Transactional(readOnly = true)
    public UploadResponse getBatchStatus(UUID batchId) {
        UploadBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Upload batch not found"
            ));
        List<UploadedFile> files = fileRepository
            .findByUploadBatchId(batchId)
            .stream()
            .sorted(Comparator.comparing(UploadedFile::getOriginalName))
            .toList();
        return response(batch, files);
    }

    private UploadResponse response(
        UploadBatch batch,
        List<UploadedFile> files
    ) {
        return new UploadResponse(
            batch.getId(),
            batch.getChallengeId(),
            batch.getStatus().name(),
            files.stream()
                .map(file -> new UploadedFileResponse(
                    file.getId(),
                    file.getOriginalName(),
                    file.getDatasetName(),
                    file.getProcessingStatus().name()
                ))
                .toList()
        );
    }
}

package com.example.microSaas.upload.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.repository.UploadedFileRepository;
import com.example.microSaas.upload.enums.UploadEnums.ProcessingStatus;

@Service
public class UploadFileProcessor {

    private final UploadedFileRepository fileRepository;
    private final UploadJobStateService jobStateService;
    private final FileValidationService validationService;
    private final FileStorageService storageService;
    private final ChallengeDatasetPersistenceService datasetPersistenceService;

    public UploadFileProcessor(
        UploadedFileRepository fileRepository,
        UploadJobStateService jobStateService,
        FileValidationService validationService,
        FileStorageService storageService,
        ChallengeDatasetPersistenceService datasetPersistenceService
    ) {
        this.fileRepository = fileRepository;
        this.jobStateService = jobStateService;
        this.validationService = validationService;
        this.storageService = storageService;
        this.datasetPersistenceService = datasetPersistenceService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void process(UUID fileId) throws IOException {
        if (!jobStateService.claim(fileId)) {
            return;
        }

        UploadedFile file = fileRepository.findById(fileId)
            .orElseThrow(() -> new IllegalStateException(
                "Queued upload file no longer exists: " + fileId
            ));
        Path stagedFile = Path.of(file.getStoragePath());
        Path recoveredCleanFile = storageService.cleanFilePath(
            file.getChallengeId(),
            fileId
        );
        Path sourceFile = Files.exists(stagedFile)
            ? stagedFile
            : recoveredCleanFile;
        if (!Files.isRegularFile(sourceFile)) {
            throw new IOException(
                "Staged upload file is missing for queued file " + fileId
            );
        }

        validationService.validateStaged(
            sourceFile,
            file.getOriginalName(),
            file.getChallengeId(),
            file.getDatasetName()
        );

        Path storedFile = sourceFile.equals(recoveredCleanFile)
            ? sourceFile
            : storageService.storeCleanFile(
                sourceFile,
                file.getChallengeId(),
                fileId
            );
        file.setStoragePath(storedFile.toString());
        datasetPersistenceService.persist(
            storedFile,
            file.getId(),
            file.getChallengeId(),
            file.getDatasetName()
        );
        file.setProcessingStatus(ProcessingStatus.COMPLETED);
        file.setProcessingStartedAt(null);
        fileRepository.saveAndFlush(file);
    }
}

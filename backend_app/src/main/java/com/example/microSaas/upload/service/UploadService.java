package com.example.microSaas.upload.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.exception.ApiException;
import com.example.microSaas.upload.dto.UploadResponse;
import com.example.microSaas.upload.enums.UploadEnums.ScanStatus;
import com.example.microSaas.upload.registry.DatasetRegistry;
import com.example.microSaas.upload.scanner.AntivirusScanner;

@Service
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private static final long MAX_TOTAL_SIZE = 50L * 1024 * 1024;
    private static final int MAX_FILES = 10;

    private final FileValidationService validationService;
    private final FileStorageService storageService;
    private final DatasetRegistry registry;
    private final AntivirusScanner antivirusScanner;
    private final UploadQueueService uploadQueueService;

    public UploadService(
        FileValidationService validationService,
        FileStorageService storageService,
        DatasetRegistry registry,
        AntivirusScanner antivirusScanner,
        UploadQueueService uploadQueueService
    ) {
        this.validationService = validationService;
        this.storageService = storageService;
        this.registry = registry;
        this.antivirusScanner = antivirusScanner;
        this.uploadQueueService = uploadQueueService;
    }

    public UploadResponse upload(
        String challengeId,
        List<MultipartFile> files,
        List<String> datasetNames
    ) throws IOException {
        validateRequest(challengeId, files, datasetNames);

        UUID batchId = UUID.randomUUID();
        List<Path> stagedFiles = new ArrayList<>(files.size());
        List<UploadedFile> entities = new ArrayList<>(files.size());
        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String datasetName = datasetNames.get(i);
                Path stagedFile = storageService.createTemporaryFile();
                stagedFiles.add(stagedFile);
                try (InputStream input = file.getInputStream()) {
                    Files.copy(
                        input,
                        stagedFile,
                        StandardCopyOption.REPLACE_EXISTING
                    );
                }
                if (!Files.isRegularFile(stagedFile)
                    || Files.size(stagedFile) != file.getSize()) {
                    throw new IOException(
                        "Could not completely stage uploaded file "
                            + file.getOriginalFilename()
                    );
                }

                scanBeforeQueueing(stagedFile, file.getOriginalFilename());

                UUID fileId = UUID.randomUUID();
                entities.add(new UploadedFile(
                    fileId,
                    batchId,
                    challengeId,
                    file.getOriginalFilename(),
                    fileId + ".csv",
                    datasetName,
                    stagedFile.toString(),
                    file.getSize(),
                    ScanStatus.CLEAN
                ));
            }

            return uploadQueueService.enqueue(batchId, challengeId, entities);
        } catch (IOException | RuntimeException exception) {
            cleanupStagedFiles(stagedFiles, exception);
            throw exception;
        }
    }

    public UploadResponse getBatchStatus(UUID batchId) {
        return uploadQueueService.getBatchStatus(batchId);
    }

    private void validateRequest(
        String challengeId,
        List<MultipartFile> files,
        List<String> datasetNames
    ) {
        if (!registry.supportsChallenge(challengeId)) {
            throw new IllegalArgumentException("Unknown challenge");
        }
        if (files == null || files.isEmpty() || datasetNames == null
            || files.size() != datasetNames.size()) {
            throw new IllegalArgumentException(
                "Files and datasetNames must have matching entries"
            );
        }
        if (files.size() > MAX_FILES) {
            throw new IllegalArgumentException(
                "Maximum 10 files per upload"
            );
        }
        if (files.stream().anyMatch(file -> file == null)) {
            throw new IllegalArgumentException(
                "Uploaded files must not contain empty entries"
            );
        }
        if (files.stream().mapToLong(MultipartFile::getSize).sum()
            > MAX_TOTAL_SIZE) {
            throw new IllegalArgumentException("Total upload size exceeded");
        }

        Set<String> uniqueNames = new HashSet<>();
        if (datasetNames.stream().anyMatch(name ->
            name == null || name.isBlank() || !uniqueNames.add(name)
        )) {
            throw new IllegalArgumentException(
                "Dataset names must be non-empty and unique"
            );
        }
        for (int i = 0; i < files.size(); i++) {
            validationService.validateMetadata(
                files.get(i),
                challengeId,
                datasetNames.get(i)
            );
        }
    }

    private void scanBeforeQueueing(
        Path stagedFile,
        String originalName
    ) throws IOException {
        Path scanCopy = storageService.createScanCopy(stagedFile);
        AntivirusScanner.ScanResult scan;
        try {
            scan = antivirusScanner.scan(scanCopy);
        } finally {
            storageService.delete(scanCopy);
        }
        if (scan == AntivirusScanner.ScanResult.INFECTED) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "File rejected by antivirus scanning: " + originalName
            );
        }
        if (scan != AntivirusScanner.ScanResult.CLEAN) {
            throw new ApiException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Antivirus scanning failed for " + originalName
                    + ". The file was not queued or saved."
            );
        }
    }

    private void cleanupStagedFiles(
        List<Path> stagedFiles,
        Exception originalFailure
    ) {
        for (Path path : stagedFiles) {
            try {
                storageService.delete(path);
            } catch (IOException cleanupFailure) {
                originalFailure.addSuppressed(cleanupFailure);
                logger.error(
                    "Could not remove temporary upload file {}",
                    path,
                    cleanupFailure
                );
            }
        }
    }
}

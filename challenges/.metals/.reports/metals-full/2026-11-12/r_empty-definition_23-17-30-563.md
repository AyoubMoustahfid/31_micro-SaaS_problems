error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/service/UploadService.java:_empty_/UploadResponse#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/service/UploadService.java
empty definition using pc, found symbol in pc: _empty_/UploadResponse#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 6056
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/service/UploadService.java
text:
```scala
package com.example.microSaas.upload.service;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.microSaas.platform.entity.UploadBatch;
import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.repository.UploadBatchRepository;
import com.example.microSaas.platform.repository.UploadedFileRepository;
import com.example.microSaas.upload.dto.UploadResponse;
import com.example.microSaas.upload.dto.UploadedFileResponse;
import com.example.microSaas.upload.enums.UploadStatus;
import com.example.microSaas.upload.registry.DatasetRegistry;
import com.example.microSaas.upload.scanner.AntivirusScanner;

@Service
public class UploadService {

    private final UploadBatchRepository batchRepository;
    private final UploadedFileRepository fileRepository;
    private final FileValidationService validationService;
    private final FileStorageService storageService;
    private final AntivirusScanner antivirusScanner;
    private final DatasetRegistry registry;

    public UploadService(
        UploadBatchRepository batchRepository,
        UploadedFileRepository fileRepository,
        FileValidationService validationService,
        FileStorageService storageService,
        AntivirusScanner antivirusScanner,
        DatasetRegistry registry
    ) {
        this.batchRepository = batchRepository;
        this.fileRepository = fileRepository;
        this.validationService = validationService;
        this.storageService = storageService;
        this.antivirusScanner = antivirusScanner;
        this.registry = registry;
    }

    @Transactional
    public UploadResponse upload(
        String challengeId,
        List<MultipartFile> files,
        List<String> datasetNames
    ) throws Exception {

        if (!registry.supportsChallenge(challengeId)) {
            throw new IllegalArgumentException(
                "Unknown challenge"
            );
        }

        if (files == null || files.isEmpty() ||
            files.size() != datasetNames.size()) {
            throw new IllegalArgumentException(
                "Files and datasetNames must have matching entries"
            );
        }

        if (files.size() > 10) {
            throw new IllegalArgumentException(
                "Maximum 10 files per upload"
            );
        }

        long totalSize = files.stream()
            .mapToLong(MultipartFile::getSize)
            .sum();

        if (totalSize > 50L * 1024 * 1024) {
            throw new IllegalArgumentException(
                "Total upload size exceeded"
            );
        }

        Set<String> uniqueNames = new HashSet<>();

        if (!datasetNames.stream().allMatch(uniqueNames::add)) {
            throw new IllegalArgumentException(
                "Duplicate dataset names are not allowed"
            );
        }

        List<Path> temporaryFiles = new ArrayList<>();
        List<Path> storedFiles = new ArrayList<>();
        List<UploadedFile> entities = new ArrayList<>();

        UUID batchId = UUID.randomUUID();

        try {
            for (int i = 0; i < files.size(); i++) {

                MultipartFile file = files.get(i);
                String datasetName = datasetNames.get(i);

                validationService.validate(
                    file,
                    challengeId,
                    datasetName
                );

                Path temporary =
                    storageService.createTemporaryFile();

                temporaryFiles.add(temporary);

                file.transferTo(temporary);

                AntivirusScanner.ScanResult scan =
                    antivirusScanner.scan(temporary);

                if (scan == AntivirusScanner.ScanResult.INFECTED) {
                    throw new IllegalArgumentException(
                        "File rejected: antivirus detection"
                    );
                }

                if (scan != AntivirusScanner.ScanResult.CLEAN) {
                    throw new IllegalStateException(
                        "Antivirus scanning unavailable"
                    );
                }

                String sha256 = calculateSha256(temporary);

                UUID fileId = UUID.randomUUID();

                Path stored = storageService.storeCleanFile(
                    temporary,
                    challengeId,
                    fileId
                );

                storedFiles.add(stored);

                String storedName =
                    stored.getFileName().toString();

                var definition =
                    registry.getDataset(challengeId, datasetName);

                UploadedFile entity = new UploadedFile(
                    batchId,
                    challengeId,
                    file.getOriginalFilename(),
                    storedName,
                    datasetName,
                    definition.datasetType(),
                    stored.toString(),
                    file.getContentType(),
                    Files.size(stored),
                    sha256
                );

                entity = fileRepository.save(entity);
                entities.add(entity);
            }

            UploadBatch batch = new UploadBatch(
                batchId,
                challengeId,
                files.size()
            );

            batch.setStatus(UploadStatus.COMPLETED);
            batchRepository.save(batch);

            return new UploadResponse@@(
                batch.getId(),
                challengeId,
                UploadStatus.COMPLETED.name(),
                entities.stream()
                    .map(file -> new UploadedFileResponse(
                        file.getId(),
                        file.getOriginalName(),
                        file.getDatasetName(),
                        file.getStatus().name()
                    ))
                    .toList()
            );

        } catch (Exception exception) {

            for (Path path : temporaryFiles) {
                try {
                    storageService.delete(path);
                } catch (IOException ignored) {}
            }

            for (Path path : storedFiles) {
                try {
                    storageService.delete(path);
                } catch (IOException ignored) {}
            }

            throw exception;
        }
    }

    private String calculateSha256(Path file) throws Exception {

        MessageDigest digest =
            MessageDigest.getInstance("SHA-256");

        try (var input = Files.newInputStream(file)) {

            byte[] buffer = new byte[8192];

            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/UploadResponse#
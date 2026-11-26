package com.example.microSaas.upload.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.repository.UploadedFileRepository;
import com.example.microSaas.upload.enums.UploadEnums.ScanStatus;
import com.example.microSaas.upload.enums.UploadEnums.ProcessingStatus;

import tools.jackson.databind.ObjectMapper;

class UploadFileProcessorTest {

    @TempDir
    Path tempDirectory;

    @Test
    void persistsAnAlreadyScannedStagedUploadAsynchronously() throws Exception {
        UUID fileId = UUID.randomUUID();
        Path stagedFile = tempDirectory.resolve("queued.tmp");
        Files.writeString(stagedFile, "vendor_id,vendor_name\nV1,Example\n");
        UploadedFile file = new UploadedFile(
            fileId,
            UUID.randomUUID(),
            "MS_01",
            "vendors.csv",
            fileId + ".csv",
            "vendor",
            stagedFile.toString(),
            Files.size(stagedFile),
            ScanStatus.CLEAN
        );

        UploadedFileRepository fileRepository =
            mock(UploadedFileRepository.class);
        UploadJobStateService jobStateService =
            mock(UploadJobStateService.class);
        FileValidationService validationService =
            mock(FileValidationService.class);
        ChallengeDatasetPersistenceService persistenceService =
            mock(ChallengeDatasetPersistenceService.class);
        FileStorageService storageService =
            new FileStorageService(tempDirectory.resolve("storage").toString());

        when(jobStateService.claim(fileId)).thenReturn(true);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        UploadFileProcessor processor = new UploadFileProcessor(
            fileRepository,
            jobStateService,
            validationService,
            storageService,
            persistenceService
        );
        processor.process(fileId);

        assertFalse(Files.exists(stagedFile));
        assertTrue(
            Files.exists(storageService.cleanFilePath("MS_01", fileId))
        );
        verify(persistenceService).persist(
            eq(storageService.cleanFilePath("MS_01", fileId)),
            eq(fileId),
            eq("MS_01"),
            eq("vendor")
        );
        verify(fileRepository).saveAndFlush(file);
        org.junit.jupiter.api.Assertions.assertEquals(
            ScanStatus.CLEAN,
            file.getScanStatus()
        );
        assertEquals(ProcessingStatus.COMPLETED, file.getProcessingStatus());
    }
}

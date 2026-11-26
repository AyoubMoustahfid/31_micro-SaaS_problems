package com.example.microSaas.upload.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.example.microSaas.platform.exception.ApiException;
import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.upload.dto.UploadResponse;
import com.example.microSaas.upload.registry.DatasetRegistry;
import com.example.microSaas.upload.scanner.AntivirusScanner;

class UploadServiceTest {

    @TempDir
    Path tempDirectory;

    private final AntivirusScanner antivirusScanner =
        mock(AntivirusScanner.class);
    private final UploadQueueService uploadQueueService =
        mock(UploadQueueService.class);

    @Test
    void scansFilesBeforePersistingTheUploadQueue() throws Exception {
        FileStorageService storageService =
            new FileStorageService(tempDirectory.toString());
        FileValidationService validationService =
            new FileValidationService(new DatasetRegistry());
        UploadService service = new UploadService(
            validationService,
            storageService,
            new DatasetRegistry(),
            antivirusScanner,
            uploadQueueService
        );
        MockMultipartFile file = vendorCsv();
        UUID batchId = UUID.randomUUID();
        when(antivirusScanner.scan(any(Path.class)))
            .thenReturn(AntivirusScanner.ScanResult.CLEAN);
        when(uploadQueueService.enqueue(
            any(UUID.class),
            org.mockito.ArgumentMatchers.eq("MS_01"),
            anyList()
        )).thenReturn(new UploadResponse(batchId, "MS_01", "PROCESSING", List.of()));

        UploadResponse response = service.upload(
            "MS_01",
            List.of(file),
            List.of("vendor")
        );

        assertEquals(batchId, response.uploadId());
        InOrder order = inOrder(antivirusScanner, uploadQueueService);
        order.verify(antivirusScanner).scan(any(Path.class));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UploadedFile>> filesCaptor =
            ArgumentCaptor.forClass(List.class);
        order.verify(uploadQueueService).enqueue(
            any(UUID.class),
            org.mockito.ArgumentMatchers.eq("MS_01"),
            filesCaptor.capture()
        );

        UploadedFile queuedFile = filesCaptor.getValue().get(0);
        assertEquals("CLEAN", queuedFile.getScanStatus().name());
        assertTrue(Files.exists(Path.of(queuedFile.getStoragePath())));
    }

    @Test
    void rejectsInfectedFileWithoutSavingItOrCreatingAJob() throws Exception {
        FileStorageService storageService =
            new FileStorageService(tempDirectory.toString());
        UploadService service = new UploadService(
            new FileValidationService(new DatasetRegistry()),
            storageService,
            new DatasetRegistry(),
            antivirusScanner,
            uploadQueueService
        );
        when(antivirusScanner.scan(any(Path.class)))
            .thenReturn(AntivirusScanner.ScanResult.INFECTED);

        ApiException exception = org.junit.jupiter.api.Assertions.assertThrows(
            ApiException.class,
            () -> service.upload(
                "MS_01",
                List.of(vendorCsv()),
                List.of("vendor")
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(uploadQueueService, never()).enqueue(
            any(UUID.class),
            org.mockito.ArgumentMatchers.eq("MS_01"),
            anyList()
        );
        try (var staging = Files.list(tempDirectory.resolve("staging"));
             var processing = Files.list(tempDirectory.resolve("processing"))) {
            assertTrue(staging.findAny().isEmpty());
            assertTrue(processing.findAny().isEmpty());
        }
    }

    @Test
    void doesNotQueueFilesWhenAntivirusScanningFails() throws Exception {
        UploadService service = new UploadService(
            new FileValidationService(new DatasetRegistry()),
            new FileStorageService(tempDirectory.toString()),
            new DatasetRegistry(),
            antivirusScanner,
            uploadQueueService
        );
        when(antivirusScanner.scan(any(Path.class)))
            .thenReturn(AntivirusScanner.ScanResult.ERROR);

        ApiException exception = org.junit.jupiter.api.Assertions.assertThrows(
            ApiException.class,
            () -> service.upload(
                "MS_01",
                List.of(vendorCsv()),
                List.of("vendor")
            )
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        verify(uploadQueueService, never()).enqueue(
            any(UUID.class),
            org.mockito.ArgumentMatchers.eq("MS_01"),
            anyList()
        );
        try (var staging = Files.list(tempDirectory.resolve("staging"))) {
            assertTrue(staging.findAny().isEmpty());
        }
    }

    @Test
    void returnsPersistedBatchStatus() throws Exception {
        UUID batchId = UUID.randomUUID();
        UploadResponse status = new UploadResponse(
            batchId,
            "MS_01",
            "PROCESSING",
            List.of()
        );
        when(uploadQueueService.getBatchStatus(batchId)).thenReturn(status);

        UploadService service = new UploadService(
            new FileValidationService(new DatasetRegistry()),
            new FileStorageService(tempDirectory.toString()),
            new DatasetRegistry(),
            antivirusScanner,
            uploadQueueService
        );

        assertEquals(status, service.getBatchStatus(batchId));
        verify(uploadQueueService).getBatchStatus(batchId);
    }

    private MockMultipartFile vendorCsv() {
        return new MockMultipartFile(
            "files",
            "vendors.csv",
            "text/csv",
            ("country,risk_tier,vendor_id,vendor_name,created_date,"
                + "payment_terms,vendor_status\n"
                + "Morocco,Low,V001,Atlas,2023-03-06,Net 15,Active\n")
                .getBytes()
        );
    }
}

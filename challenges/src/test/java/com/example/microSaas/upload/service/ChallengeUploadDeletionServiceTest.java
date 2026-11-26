package com.example.microSaas.upload.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.inOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.microSaas.platform.entity.UploadBatch;
import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.repository.UploadBatchRepository;
import com.example.microSaas.platform.repository.UploadedFileRepository;
import com.example.microSaas.upload.registry.DatasetRegistry;

class ChallengeUploadDeletionServiceTest {

    @TempDir
    Path tempDirectory;

    private final UploadBatchRepository batchRepository =
        mock(UploadBatchRepository.class);
    private final UploadedFileRepository fileRepository =
        mock(UploadedFileRepository.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void deletesChallengeRecordsAndStoredFilesAfterCommit() throws Exception {
        FileStorageService storageService =
            new FileStorageService(tempDirectory.toString());
        Path vendorFilePath = storedFile("vendors.csv");
        Path contractFilePath = storedFile("contracts.csv");
        Path purchaseOrderFilePath = storedFile("purchase_orders.csv");
        UploadedFile vendorFile = uploadedFile(vendorFilePath, "vendor");
        UploadedFile contractFile = uploadedFile(contractFilePath, "contract");
        UploadedFile purchaseOrderFile =
            uploadedFile(purchaseOrderFilePath, "purchase_order");
        List<UploadedFile> files =
            List.of(vendorFile, contractFile, purchaseOrderFile);
        List<UploadBatch> batches = List.of(mock(UploadBatch.class));
        when(fileRepository.findByChallengeId("MS_01")).thenReturn(files);
        when(batchRepository.findByChallengeId("MS_01")).thenReturn(batches);

        TransactionSynchronizationManager.initSynchronization();
        ChallengeUploadDeletionService service =
            new ChallengeUploadDeletionService(
                batchRepository,
                fileRepository,
                jdbcTemplate,
                storageService,
                new DatasetRegistry()
            );

        var response = service.deleteChallengeUploads("MS_01");

        assertEquals("MS_01", response.challengeId());
        assertEquals(3, response.deletedFiles());
        assertTrue(Files.exists(vendorFilePath));
        assertTrue(Files.exists(contractFilePath));
        assertTrue(Files.exists(purchaseOrderFilePath));

        InOrder deletionOrder = inOrder(jdbcTemplate, fileRepository);
        deletionOrder.verify(jdbcTemplate).update(
            contains("challenge_01.purchase_orders"),
            any(java.util.UUID.class)
        );
        deletionOrder.verify(jdbcTemplate).update(
            contains("challenge_01.contracts"),
            any(java.util.UUID.class)
        );
        deletionOrder.verify(jdbcTemplate).update(
            contains("challenge_01.vendors"),
            any(java.util.UUID.class)
        );
        deletionOrder.verify(fileRepository).deleteAll(files);
        deletionOrder.verify(fileRepository).flush();
        verify(batchRepository).deleteAll(batches);
        verify(batchRepository).flush();

        for (TransactionSynchronization synchronization :
            TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        assertFalse(Files.exists(vendorFilePath));
        assertFalse(Files.exists(contractFilePath));
        assertFalse(Files.exists(purchaseOrderFilePath));
    }

    @Test
    void rejectsUnknownChallenge() throws Exception {
        ChallengeUploadDeletionService service =
            new ChallengeUploadDeletionService(
                batchRepository,
                fileRepository,
                jdbcTemplate,
                new FileStorageService(tempDirectory.toString()),
                new DatasetRegistry()
            );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.deleteChallengeUploads("UNKNOWN")
        );
    }

    private Path storedFile(String name) throws Exception {
        Path path = tempDirectory
            .resolve("clean")
            .resolve("MS_01")
            .resolve(name);
        Files.createDirectories(path.getParent());
        Files.writeString(path, "id\n1\n");
        return path;
    }

    private UploadedFile uploadedFile(Path path, String datasetName) {
        return new UploadedFile(
            java.util.UUID.randomUUID(),
            "MS_01",
            path.getFileName().toString(),
            path.getFileName().toString(),
            datasetName,
            path.toString(),
            10
        );
    }
}

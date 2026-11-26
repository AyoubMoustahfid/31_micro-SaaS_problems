package com.example.microSaas.upload.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(
        @Value("${app.storage.root}") String root
    ) throws IOException {

        this.root = Path.of(root)
            .toAbsolutePath()
            .normalize();

        Files.createDirectories(this.root);
    }

    public Path createTemporaryFile() throws IOException {
        Path staging = root.resolve("staging");

        Files.createDirectories(staging);

        return Files.createTempFile(
            staging,
            "upload-",
            ".tmp"
        );
    }

    public Path createScanCopy(Path sourceFile) throws IOException {
        Path processing = root.resolve("processing").normalize();
        if (!processing.startsWith(root)) {
            throw new SecurityException("Invalid processing path");
        }
        Files.createDirectories(processing);
        Path scanCopy = Files.createTempFile(
            processing,
            "scan-",
            ".tmp"
        );
        try {
            Files.copy(
                sourceFile,
                scanCopy,
                StandardCopyOption.REPLACE_EXISTING
            );
            if (!Files.isRegularFile(scanCopy)
                || Files.size(scanCopy) != Files.size(sourceFile)) {
                throw new IOException(
                    "Could not create a complete antivirus scan copy"
                );
            }
            return scanCopy;
        } catch (IOException | RuntimeException exception) {
            try {
                Files.deleteIfExists(scanCopy);
            } catch (IOException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            throw exception;
        }
    }

    public Path storeCleanFile(
        Path temporaryFile,
        String challengeId,
        UUID fileId
    ) throws IOException {

        Path destination = cleanFilePath(challengeId, fileId);
        Path directory = destination.getParent();
        Files.createDirectories(directory);

        return Files.move(
            temporaryFile,
            destination,
            StandardCopyOption.ATOMIC_MOVE
        );
    }

    public Path cleanFilePath(String challengeId, UUID fileId) {
        Path directory = root
            .resolve("clean")
            .resolve(challengeId)
            .normalize();
        if (!directory.startsWith(root)) {
            throw new SecurityException("Invalid storage path");
        }
        return directory.resolve(fileId + ".csv");
    }

    public void delete(Path file) throws IOException {
        Files.deleteIfExists(file);
    }

    public void deleteCleanFile(
        String challengeId,
        String storagePath
    ) throws IOException {
        Path cleanRoot = root.resolve("clean").normalize();
        Path challengeDirectory = cleanRoot.resolve(challengeId).normalize();
        Path file = Path.of(storagePath).toAbsolutePath().normalize();

        if (!challengeDirectory.startsWith(cleanRoot) ||
            !challengeDirectory.equals(file.getParent())) {
            throw new SecurityException("Invalid stored upload path");
        }

        Files.deleteIfExists(file);
    }

    public void deleteUploadFile(
        String challengeId,
        String storagePath
    ) throws IOException {
        Path file = Path.of(storagePath).toAbsolutePath().normalize();
        Path staging = root.resolve("staging").normalize();
        Path cleanChallenge = root
            .resolve("clean")
            .resolve(challengeId)
            .normalize();

        if (!(file.startsWith(staging) || file.startsWith(cleanChallenge))) {
            throw new SecurityException("Invalid stored upload path");
        }
        Files.deleteIfExists(file);
    }
}
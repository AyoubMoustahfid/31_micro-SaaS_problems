package com.example.microSaas.upload.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.microSaas.upload.registry.DatasetRegistry;

@Service
public class FileValidationService {

    private static final Pattern SAFE_NAME =
        Pattern.compile("^[a-zA-Z0-9_-]{1,100}$");

    private final DatasetRegistry registry;

    public FileValidationService(DatasetRegistry registry) {
        this.registry = registry;
    }

    public void validateMetadata(
        MultipartFile file,
        String challengeId,
        String datasetName
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        validateMetadata(
            file.getOriginalFilename(),
            challengeId,
            datasetName
        );
    }

    public void validateStaged(
        Path csvPath,
        String originalName,
        String challengeId,
        String datasetName
    ) throws IOException {
        validateMetadata(originalName, challengeId, datasetName);
        try (
            Reader reader = new BufferedReader(
                new InputStreamReader(
                    Files.newInputStream(csvPath),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            validateContent(reader, challengeId, datasetName);
        }
    }

    private void validateMetadata(
        String originalName,
        String challengeId,
        String datasetName
    ) {
        if (!SAFE_NAME.matcher(datasetName).matches()) {
            throw new IllegalArgumentException(
                "Invalid dataset name"
            );
        }

        if (originalName == null ||
            !originalName.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException(
                "Only CSV files are accepted"
            );
        }

        registry.getDataset(challengeId, datasetName);
    }

    private void validateContent(
        Reader reader,
        String challengeId,
        String datasetName
    ) throws IOException {
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .get()
            .parse(reader)) {
            List<String> headerNames = parser.getHeaderNames();

            if (headerNames.isEmpty()
                || headerNames.stream().anyMatch(String::isBlank)) {
                throw new IllegalArgumentException(
                    "CSV must contain non-empty column names"
                );
            }

            List<String> normalizedHeaders = headerNames.stream()
                .map(DatasetRegistry::normalizeHeader)
                .toList();
            Set<String> headers = new HashSet<>(normalizedHeaders);
            if (normalizedHeaders.size() != headers.size()) {
                throw new IllegalArgumentException(
                    "Duplicate CSV headers after normalization"
                );
            }

            List<String> requiredHeaders = registry
                .getDataset(challengeId, datasetName)
                .requiredHeaders();
            Set<String> missingHeaders = new HashSet<>(requiredHeaders);
            missingHeaders.removeAll(headers);
            Set<String> unexpectedHeaders = new HashSet<>(headers);
            unexpectedHeaders.removeAll(requiredHeaders);
            if (!missingHeaders.isEmpty() || !unexpectedHeaders.isEmpty()) {
                throw new IllegalArgumentException(
                    "CSV headers do not match dataset '" + datasetName
                        + "'. Missing: " + missingHeaders
                        + "; unexpected: " + unexpectedHeaders
                );
            }

            long rowCount = 0;
            for (CSVRecord record : parser) {
                rowCount++;
                if (!record.isConsistent()) {
                    throw new IllegalArgumentException(
                        "Invalid CSV row: " + record.getRecordNumber()
                    );
                }
                if (rowCount > 1_000_000) {
                    throw new IllegalArgumentException(
                        "CSV row limit exceeded"
                    );
                }
            }

            if (rowCount == 0) {
                throw new IllegalArgumentException(
                    "CSV contains no data rows"
                );
            }
        }
    }
}
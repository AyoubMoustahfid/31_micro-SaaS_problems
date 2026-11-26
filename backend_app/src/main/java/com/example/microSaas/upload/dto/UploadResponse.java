package com.example.microSaas.upload.dto;

import java.util.List;
import java.util.UUID;

import tools.jackson.databind.JsonNode;

public record UploadResponse(
    UUID uploadId,
    String challengeId,
    String status,
    List<UploadedFileResponse> files
) {

    public record UploadedFileResponse(
        UUID fileId,
        String originalName,
        String datasetName,
        String status
    ) {}

    public record DeleteChallengeUploadsResponse(
        String challengeId,
        int deletedFiles
    ) {}

    public record UploadedFileDataResponse(
        UUID fileId,
        String originalName,
        String datasetName,
        int size,
        long totalRows,
        long totalPages,
        List<JsonNode> rows
    ) {}

    public record GetFilesChallengeResponse(
        String challengeId,
        List<UploadedFileDataResponse> files
    ) {}
}

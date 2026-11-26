error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/service/UploadedFileDataService.java:_empty_/UploadedFileRepository#findByChallengeId#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/service/UploadedFileDataService.java
empty definition using pc, found symbol in pc: _empty_/UploadedFileRepository#findByChallengeId#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 2849
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/service/UploadedFileDataService.java
text:
```scala
package com.example.microSaas.upload.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.exception.ApiException;
import com.example.microSaas.platform.repository.UploadedFileRepository;
import com.example.microSaas.upload.dto.GetFilesChallengeResponse;
import com.example.microSaas.upload.dto.UploadedFileDataResponse;
import com.example.microSaas.upload.registry.DatasetRegistry;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class UploadedFileDataService {

    private static final int MAX_PAGE_SIZE = 500;
    private static final String COUNT_ROWS_SQL = """
        SELECT COUNT(*)
        FROM platform.uploaded_file_rows
        WHERE uploaded_file_id = ?
        """;
    private static final String SELECT_ROWS_SQL = """
        SELECT row_data::text
        FROM platform.uploaded_file_rows
        WHERE uploaded_file_id = ?
        ORDER BY row_number
        """;

    private final UploadedFileRepository fileRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final DatasetRegistry registry;

    public UploadedFileDataService(
        UploadedFileRepository fileRepository,
        JdbcTemplate jdbcTemplate,
        ObjectMapper objectMapper,
        DatasetRegistry registry
    ) {
        this.fileRepository = fileRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.registry = registry;
    }

    public UploadedFileDataResponse getData(
        UUID fileId,
        int size
    ) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                "size must be between 1 and " + MAX_PAGE_SIZE
            );
        }

        UploadedFile file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Uploaded file not found"
            ));

        return getData(file, size);
    }

    public GetFilesChallengeResponse getChallengeData(
        String challengeId,
        int size
    ) {
        validateSize(size);
        if (!registry.supportsChallenge(challengeId)) {
            throw new IllegalArgumentException(
                "Unknown challenge: " + challengeId
            );
        }

        List<UploadedFileDataResponse> files =
            fileRepository.findByChal@@lengeId(challengeId)
                .stream()
                .map(file -> getData(file, size))
                .toList();

        return new GetFilesChallengeResponse(challengeId, files);
    }

    private UploadedFileDataResponse getData(UploadedFile file, int size) {
        Long totalRows = jdbcTemplate.queryForObject(
            COUNT_ROWS_SQL,
            Long.class,
            file.getId()
        );
        List<JsonNode> rows = jdbcTemplate.query(
            SELECT_ROWS_SQL,
            this::mapRow,
            file.getId()
        );
        long rowCount = totalRows == null ? 0 : totalRows;
        long totalPages = rowCount == 0 ? 0 : 1 + (rowCount - 1) / size;

        return new UploadedFileDataResponse(
            file.getId(),
            file.getOriginalName(),
            file.getDatasetName(),
            size,
            rowCount,
            totalPages,
            rows
        );
    }

    private void validateSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                "size must be between 1 and " + MAX_PAGE_SIZE
            );
        }
    }

    private JsonNode mapRow(ResultSet resultSet, int rowNumber)
        throws SQLException {
        try {
            return objectMapper.readTree(resultSet.getString(1));
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                "Could not read persisted CSV row",
                exception
            );
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/UploadedFileRepository#findByChallengeId#
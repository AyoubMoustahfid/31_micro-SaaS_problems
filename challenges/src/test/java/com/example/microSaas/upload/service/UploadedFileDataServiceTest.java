package com.example.microSaas.upload.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.example.microSaas.platform.entity.UploadedFile;
import com.example.microSaas.platform.exception.ApiException;
import com.example.microSaas.platform.repository.UploadedFileRepository;
import com.example.microSaas.upload.registry.DatasetRegistry;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class UploadedFileDataServiceTest {

    private final UploadedFileRepository fileRepository =
        mock(UploadedFileRepository.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final UploadedFileDataService service =
        new UploadedFileDataService(
            fileRepository,
            jdbcTemplate,
            new ObjectMapper(),
            new DatasetRegistry()
        );

    @Test
    void returnsChallengeRowsWithDerivedPageCount()
        throws Exception {
        UploadedFile file = uploadedFile();
        UUID fileId = file.getId();
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(jdbcTemplate.query(
            anyString(),
            org.mockito.ArgumentMatchers.<RowMapper<JsonNode>>any(),
            eq(fileId)
        )).thenAnswer(invocation -> {
            RowMapper<JsonNode> rowMapper = invocation.getArgument(1);
            List<JsonNode> rows = new java.util.ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                ResultSet resultSet = mock(ResultSet.class);
                when(resultSet.getString(1)).thenReturn(
                    "{\"po_id\":\"PO-" + i + "\",\"status\":\"Open\"}"
                );
                rows.add(rowMapper.mapRow(resultSet, i - 1));
            }
            return rows;
        });

        var response = service.getData(fileId, 2);

        assertEquals(file.getId(), response.fileId());
        assertEquals("orders.csv", response.originalName());
        assertEquals("purchase_order", response.datasetName());
        assertEquals(2, response.size());
        assertEquals(3L, response.totalRows());
        assertEquals(2L, response.totalPages());
        assertEquals(3, response.rows().size());
        assertEquals("PO-3", response.rows().get(2).get("po_id").asText());
    }

    @Test
    void rejectsMissingUploadedFile() {
        UUID fileId = UUID.randomUUID();
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.getData(fileId, 100));
    }

    @Test
    void rejectsPageSizesAboveMaximum() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getData(UUID.randomUUID(), 501)
        );
    }

    @Test
    void returnsEveryUploadedFileAndItsRowsForChallenge() throws Exception {
        UploadedFile file = uploadedFile();
        when(fileRepository.findByChallengeId("MS_01"))
            .thenReturn(List.of(file));
        when(jdbcTemplate.query(
            anyString(),
            org.mockito.ArgumentMatchers.<RowMapper<JsonNode>>any(),
            eq(file.getId())
        )).thenAnswer(invocation -> {
            RowMapper<JsonNode> rowMapper = invocation.getArgument(1);
            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getString(1)).thenReturn(
                "{\"po_id\":\"PO-1\",\"status\":\"Open\"}"
            );
            return List.of(rowMapper.mapRow(resultSet, 0));
        });

        var response = service.getChallengeData("MS_01", 100);

        assertEquals("MS_01", response.challengeId());
        assertEquals(1, response.files().size());
        assertEquals(file.getId(), response.files().get(0).fileId());
        assertEquals(1L, response.files().get(0).totalRows());
        assertEquals(
            "PO-1",
            response.files().get(0).rows().get(0).get("po_id").asText()
        );
    }

    @Test
    void rejectsUnknownChallengeForFileDataLookup() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getChallengeData("UNKNOWN", 100)
        );
    }

    private UploadedFile uploadedFile() {
        return new UploadedFile(
            UUID.randomUUID(),
            "MS_01",
            "orders.csv",
            "stored.csv",
            "purchase_order",
            "path",
            1
        );
    }
}

package com.example.microSaas.upload.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import tools.jackson.databind.ObjectMapper;

class ChallengeDatasetPersistenceServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void persistsNormalizedVendorDataIntoChallengeSchema() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChallengeDatasetPersistenceService service =
                new ChallengeDatasetPersistenceService(
                    jdbcTemplate,
                    new com.example.microSaas.upload.registry.DatasetRegistry(),
                    new ObjectMapper()
                );
        Path csv = tempDirectory.resolve("vendors.csv");
        Files.writeString(
                csv,
                "Country,Risk Tier,Vendor ID,Vendor Name,Created Date,"
                        + "Payment Terms,Vendor Status\n"
                        + "Morocco,Low,V001,Atlas Industrial Supplies,"
                        + "2023-03-06,Net 15,Active\n",
                StandardCharsets.UTF_8);

        service.persist(csv, UUID.randomUUID(), "MS_01", "vendor");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object[]>> batchCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), batchCaptor.capture());
        List<Object[]> rows = batchCaptor.getValue();
        assertEquals(1, rows.size());
        assertTrue(sqlCaptor.getValue().contains("challenge_01.vendors"));
        assertTrue(sqlCaptor.getValue().contains("vendor_id"));
        assertEquals("V001", rows.get(0)[0]);
        assertEquals("Atlas Industrial Supplies", rows.get(0)[1]);
    }

    @Test
    void insertsContractRowsUsingVendorExternalIds() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        org.mockito.Mockito.when(
            jdbcTemplate.batchUpdate(anyString(), anyList())
        ).thenReturn(new int[] {1});
        ChallengeDatasetPersistenceService service =
            new ChallengeDatasetPersistenceService(
                jdbcTemplate,
                new com.example.microSaas.upload.registry.DatasetRegistry(),
                new ObjectMapper()
            );
        Path csv = tempDirectory.resolve("contracts.csv");
        Files.writeString(
            csv,
            "category,currency,owner_id,vendor_id,department,contract_id,"
                + "vendor_name,contract_title,contract_value,contract_status,"
                + "renewal_required,contract_end_date,contract_start_date\n"
                + "Raw Materials,EUR,EMP-0055,V008,Manufacturing,CTR-0001,"
                + "Summit Logistics Parts,Supply Agreement,805323.75,"
                + "Under Review,No,2027-04-04,2026-04-04\n",
            StandardCharsets.UTF_8
        );

        service.persist(csv, UUID.randomUUID(), "MS_01", "contract");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object[]>> batchCaptor =
            ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> sqlCaptor =
            ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), batchCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("challenge_01.contracts"));
        assertTrue(sqlCaptor.getValue().contains("vendor_id"));
        assertEquals("CTR-0001", batchCaptor.getValue().get(0)[0]);
        assertEquals("V008", batchCaptor.getValue().get(0)[4]);
    }

    @Test
    void rejectsUnknownVendorReferences() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        org.mockito.Mockito.when(
            jdbcTemplate.batchUpdate(anyString(), anyList())
        ).thenThrow(new DataIntegrityViolationException(
            "foreign key violation",
            new SQLException("foreign key violation", "23503")
        ));
        ChallengeDatasetPersistenceService service =
            new ChallengeDatasetPersistenceService(
                jdbcTemplate,
                new com.example.microSaas.upload.registry.DatasetRegistry(),
                new ObjectMapper()
            );
        Path csv = tempDirectory.resolve("contracts-missing-vendor.csv");
        Files.writeString(
            csv,
            "category,currency,owner_id,vendor_id,department,contract_id,"
                + "vendor_name,contract_title,contract_value,contract_status,"
                + "renewal_required,contract_end_date,contract_start_date\n"
                + "Raw Materials,EUR,EMP-0055,V008,Manufacturing,CTR-0001,"
                + "Summit Logistics Parts,Supply Agreement,805323.75,"
                + "Under Review,No,2027-04-04,2026-04-04\n",
            StandardCharsets.UTF_8
        );

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions
            .assertThrows(
                IllegalArgumentException.class,
                () -> service.persist(csv, UUID.randomUUID(), "MS_01", "contract")
            );

        assertTrue(exception.getMessage().contains("unknown vendor_id"));
    }

    @Test
    void reportsDuplicateDatasetIdentifiers() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        org.mockito.Mockito.when(
            jdbcTemplate.batchUpdate(anyString(), anyList())
        ).thenThrow(new DataIntegrityViolationException(
            "duplicate key",
            new SQLException("duplicate key", "23505")
        ));
        ChallengeDatasetPersistenceService service =
            new ChallengeDatasetPersistenceService(
                jdbcTemplate,
                new com.example.microSaas.upload.registry.DatasetRegistry(),
                new ObjectMapper()
            );
        Path csv = tempDirectory.resolve("duplicate-vendors.csv");
        Files.writeString(
            csv,
            "country,risk_tier,vendor_id,vendor_name,created_date,"
                + "payment_terms,vendor_status\n"
                + "Morocco,Low,V001,Atlas,2023-03-06,Net 15,Active\n",
            StandardCharsets.UTF_8
        );

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions
            .assertThrows(
                IllegalArgumentException.class,
                () -> service.persist(csv, UUID.randomUUID(), "MS_01", "vendor")
            );

        assertTrue(exception.getMessage().contains("identifier that already exists"));
    }
}

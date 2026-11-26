package com.example.microSaas.upload.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.microSaas.upload.registry.DatasetRegistry;
import com.example.microSaas.upload.registry.DatasetRegistry.DatasetDefinition;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class ChallengeDatasetPersistenceService {

    private static final int BATCH_SIZE = 500;

    private static final String VENDOR_INSERT_SQL = """
        INSERT INTO challenge_01.vendors (
            vendor_id, vendor_name, country, risk_tier, created_date,
            payment_terms, vendor_status, uploaded_file_id, row_number
        ) VALUES (?, ?, ?, ?, NULLIF(?, '')::date, ?, ?, ?, ?)
        """;
    private static final String CONTRACT_INSERT_SQL = """
        INSERT INTO challenge_01.contracts (
            contract_id, category, currency, owner_id, vendor_id, department,
            vendor_name, contract_title, contract_value, contract_status,
            renewal_required, contract_end_date, contract_start_date,
            uploaded_file_id, row_number
        ) VALUES (?, ?, ?, ?, NULLIF(?, ''), ?, ?, ?, CAST(? AS numeric), ?,
                  ?, NULLIF(?, '')::date, NULLIF(?, '')::date, ?, ?)
        """;
    private static final String PURCHASE_ORDER_INSERT_SQL = """
        INSERT INTO challenge_01.purchase_orders (
            po_id, status, po_date, category, currency, quantity, subtotal,
            tax_rate, unit_cost, vendor_id, department, tax_amount, approver_id,
            contract_id, cost_center, vendor_name, requester_id, total_amount,
            payment_terms, vendor_country, discount_amount, discount_percent,
            item_description, invoice_reference, expected_delivery_date,
            uploaded_file_id, row_number
        ) VALUES (?, ?, NULLIF(?, '')::date, ?, ?, NULLIF(?, '')::numeric,
                  NULLIF(?, '')::numeric, NULLIF(?, '')::numeric,
                  NULLIF(?, '')::numeric, NULLIF(?, ''), ?,
                  NULLIF(?, '')::numeric, ?, NULLIF(?, ''), ?, ?, ?,
                  CAST(? AS numeric), ?, ?, NULLIF(?, '')::numeric,
                  NULLIF(?, '')::numeric, ?, ?, NULLIF(?, '')::date, ?, ?)
        """;
    private static final String INVOICE_INSERT_SQL = """
        INSERT INTO challenge_02.invoices (
            invoice_number, vendor_reference, invoice_data, amount, currency,
            uploaded_file_id, row_number
        ) VALUES (?, ?, CAST(? AS jsonb), CAST(? AS numeric), ?, ?, ?)
        """;

    private final JdbcTemplate jdbcTemplate;
    private final DatasetRegistry registry;
    private final ObjectMapper objectMapper;

    public ChallengeDatasetPersistenceService(
        JdbcTemplate jdbcTemplate,
        DatasetRegistry registry,
        ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    public void persist(
        Path csvPath,
        UUID uploadedFileId,
        String challengeId,
        String datasetName
    ) throws IOException {
        DatasetDefinition definition =
            registry.getDataset(challengeId, datasetName);

        try (Reader reader = new BufferedReader(
                new InputStreamReader(
                    Files.newInputStream(csvPath),
                    StandardCharsets.UTF_8
                ));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get()
                .parse(reader)) {
            List<String> headers = parser.getHeaderNames().stream()
                .map(DatasetRegistry::normalizeHeader)
                .toList();
            List<PendingRow> batch = new ArrayList<>(BATCH_SIZE);

            for (CSVRecord record : parser) {
                if (!record.isConsistent()) {
                    throw new IllegalArgumentException(
                        "Invalid CSV row: " + record.getRecordNumber()
                    );
                }

                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), record.get(i));
                }
                validateRow(
                    definition.datasetType(),
                    row,
                    record.getRecordNumber()
                );
                batch.add(new PendingRow(record.getRecordNumber(), row));

                if (batch.size() == BATCH_SIZE) {
                    persistBatch(definition.datasetType(), uploadedFileId, batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                persistBatch(definition.datasetType(), uploadedFileId, batch);
            }
        }
    }

    private void persistBatch(
        String datasetType,
        UUID uploadedFileId,
        List<PendingRow> rows
    ) {
        List<Object[]> parameters = rows.stream()
            .map(row -> parameters(datasetType, uploadedFileId, row))
            .toList();
        try {
            jdbcTemplate.batchUpdate(insertSql(datasetType), parameters);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(
                integrityViolationMessage(
                    datasetType,
                    sqlState(exception)
                ),
                exception
            );
        }
    }

    private String integrityViolationMessage(
        String datasetType,
        String sqlState
    ) {
        return switch (sqlState == null ? "" : sqlState) {
            case "23505" ->
                "Dataset '" + datasetType
                    + "' contains an identifier that already exists. "
                    + "Use unique IDs or remove the existing challenge data before retrying.";
            case "23503" -> {
                String references = switch (datasetType) {
                    case "contract" -> "vendor_id";
                    case "purchase_order" -> "vendor_id or contract_id";
                    default -> "a referenced record";
                };
                yield "Dataset '" + datasetType + "' references an unknown "
                    + references + ". Upload referenced datasets first.";
            }
            case "23502" ->
                "Dataset '" + datasetType
                    + "' is missing a required database value.";
            case "22001", "22003" ->
                "Dataset '" + datasetType
                    + "' contains a value that exceeds a database column limit.";
            default ->
                "Dataset '" + datasetType
                    + "' violates database constraints. Check required values, "
                    + "references, and duplicate identifiers.";
        };
    }

    private String sqlState(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException) {
                if (sqlException.getSQLState() != null) {
                    return sqlException.getSQLState();
                }
                for (SQLException next = sqlException.getNextException();
                     next != null;
                     next = next.getNextException()) {
                    if (next.getSQLState() != null) {
                        return next.getSQLState();
                    }
                }
            }
        }
        return null;
    }

    private String insertSql(String datasetType) {
        return switch (datasetType) {
            case "vendor" -> VENDOR_INSERT_SQL;
            case "contract" -> CONTRACT_INSERT_SQL;
            case "purchase_order" -> PURCHASE_ORDER_INSERT_SQL;
            case "invoice" -> INVOICE_INSERT_SQL;
            default -> throw new IllegalArgumentException(
                "Unsupported dataset type: " + datasetType
            );
        };
    }

    private Object[] parameters(
        String datasetType,
        UUID uploadedFileId,
        PendingRow pendingRow
    ) {
        Map<String, String> row = pendingRow.values();
        return switch (datasetType) {
            case "vendor" -> new Object[] {
                row.get("vendor_id"),
                row.get("vendor_name"),
                row.get("country"),
                row.get("risk_tier"),
                row.get("created_date"),
                row.get("payment_terms"),
                row.get("vendor_status"),
                uploadedFileId,
                pendingRow.recordNumber()
            };
            case "contract" -> new Object[] {
                row.get("contract_id"),
                row.get("category"),
                row.get("currency"),
                row.get("owner_id"),
                row.get("vendor_id"),
                row.get("department"),
                row.get("vendor_name"),
                row.get("contract_title"),
                row.get("contract_value"),
                row.get("contract_status"),
                row.get("renewal_required"),
                row.get("contract_end_date"),
                row.get("contract_start_date"),
                uploadedFileId,
                pendingRow.recordNumber()
            };
            case "purchase_order" -> new Object[] {
                row.get("po_id"),
                row.get("status"),
                row.get("po_date"),
                row.get("category"),
                row.get("currency"),
                row.get("quantity"),
                row.get("subtotal"),
                row.get("tax_rate"),
                row.get("unit_cost"),
                row.get("vendor_id"),
                row.get("department"),
                row.get("tax_amount"),
                row.get("approver_id"),
                row.get("contract_id"),
                row.get("cost_center"),
                row.get("vendor_name"),
                row.get("requester_id"),
                row.get("total_amount"),
                row.get("payment_terms"),
                row.get("vendor_country"),
                row.get("discount_amount"),
                row.get("discount_percent"),
                row.get("item_description"),
                row.get("invoice_reference"),
                row.get("expected_delivery_date"),
                uploadedFileId,
                pendingRow.recordNumber()
            };
            case "invoice" -> new Object[] {
                row.get("invoice_number"),
                row.get("vendor_reference"),
                toJson(row),
                row.get("amount"),
                row.get("currency"),
                uploadedFileId,
                pendingRow.recordNumber()
            };
            default -> throw new IllegalArgumentException(
                "Unsupported dataset type: " + datasetType
            );
        };
    }

    private String toJson(Map<String, String> row) {
        try {
            return objectMapper.writeValueAsString(row);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                "Could not serialize challenge dataset row",
                exception
            );
        }
    }

    private void validateRow(
        String datasetType,
        Map<String, String> row,
        long recordNumber
    ) {
        switch (datasetType) {
            case "vendor" -> {
                requireValue(row, "vendor_id", recordNumber);
                requireValue(row, "vendor_name", recordNumber);
                validateDate(row, "created_date", recordNumber);
            }
            case "contract" -> {
                requireValue(row, "contract_id", recordNumber);
                requireValue(row, "contract_value", recordNumber);
                requireValue(row, "currency", recordNumber);
                validateDecimal(row, "contract_value", recordNumber);
                validateDate(row, "contract_start_date", recordNumber);
                validateDate(row, "contract_end_date", recordNumber);
            }
            case "purchase_order" -> {
                requireValue(row, "po_id", recordNumber);
                requireValue(row, "po_date", recordNumber);
                requireValue(row, "total_amount", recordNumber);
                requireValue(row, "currency", recordNumber);
                validateDate(row, "po_date", recordNumber);
                validateDate(row, "expected_delivery_date", recordNumber);
                for (String column : List.of(
                    "quantity", "subtotal", "tax_rate", "unit_cost",
                    "tax_amount", "total_amount", "discount_amount",
                    "discount_percent"
                )) {
                    validateDecimal(row, column, recordNumber);
                }
            }
            case "invoice" -> {
                requireValue(row, "invoice_number", recordNumber);
                requireValue(row, "vendor_reference", recordNumber);
                requireValue(row, "invoice_data", recordNumber);
                requireValue(row, "amount", recordNumber);
                requireValue(row, "currency", recordNumber);
                validateDecimal(row, "amount", recordNumber);
            }
            default -> throw new IllegalArgumentException(
                "Unsupported dataset type: " + datasetType
            );
        }
    }

    private void requireValue(
        Map<String, String> row,
        String column,
        long recordNumber
    ) {
        if (row.get(column) == null || row.get(column).isBlank()) {
            throw new IllegalArgumentException(
                "CSV column '" + column + "' is required at row " + recordNumber
            );
        }
    }

    private void validateDecimal(
        Map<String, String> row,
        String column,
        long recordNumber
    ) {
        String value = row.get(column);
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                "Invalid number in column '" + column + "' at row " + recordNumber,
                exception
            );
        }
    }

    private void validateDate(
        Map<String, String> row,
        String column,
        long recordNumber
    ) {
        String value = row.get(column);
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                "Invalid date in column '" + column + "' at row " + recordNumber,
                exception
            );
        }
    }

    private record PendingRow(long recordNumber, Map<String, String> values) {}
}

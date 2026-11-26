package com.example.microSaas.challenges.MS_01.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.microSaas.challenges.MS_01.importer.ContractSplittingDetectionResponse;
import com.example.microSaas.challenges.MS_01.importer.ContractSplittingDetectionResponse.AlertResponse;
import com.example.microSaas.challenges.MS_01.importer.ContractSplittingDetectionResponse.OrdersResponse;

@Service
public class ContractSplittingDetectionService {

    private static final String CHALLENGE_ID = "MS_01";
    private static final String PURCHASE_ORDER_QUERY = """
        SELECT po.po_id,
               po.vendor_id,
               po.requester_id,
               po.approver_id,
               po.po_date,
               po.total_amount,
               po.currency,
               po.department,
               COALESCE(
                   NULLIF(BTRIM(v.vendor_name), ''),
                   NULLIF(BTRIM(po.vendor_name), ''),
                   NULLIF(BTRIM(c.vendor_name), ''),
                   po.vendor_id
               ) AS vendor_name,
               uf.original_name AS file_name
        FROM challenge_01.purchase_orders po
        JOIN platform.uploaded_files uf
          ON uf.id = po.uploaded_file_id
         AND uf.challenge_id = ?
         AND uf.dataset_name = 'purchase_order'
        LEFT JOIN challenge_01.vendors v
          ON v.vendor_id = po.vendor_id
        LEFT JOIN challenge_01.contracts c
          ON c.contract_id = po.contract_id
        ORDER BY po.po_date, po.po_id
        """;

    private final JdbcTemplate jdbcTemplate;

    public ContractSplittingDetectionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ContractSplittingDetectionResponse configureAndScan(
        BigDecimal thresholdNumber,
        int period,
        int minOrders
    ) {
        if (thresholdNumber == null || thresholdNumber.signum() <= 0) {
            throw new IllegalArgumentException(
                "thresholdNumber must be greater than zero"
            );
        }
        if (period < 1) {
            throw new IllegalArgumentException(
                "period must be at least 1 day"
            );
        }
        if (minOrders < 2) {
            throw new IllegalArgumentException(
                "minOrders must be at least 2"
            );
        }

        ScanConfiguration config = new ScanConfiguration(
            normalizeAmount(thresholdNumber),
            period,
            minOrders
        );
        List<PurchaseOrderRow> rows = jdbcTemplate.query(
            PURCHASE_ORDER_QUERY,
            this::mapPurchaseOrder,
            CHALLENGE_ID
        );
        return buildReport(config, rows);
    }

    private ContractSplittingDetectionResponse buildReport(
        ScanConfiguration config,
        List<PurchaseOrderRow> rows
    ) {
        Map<MatchKey, List<PurchaseOrderRow>> groups = new LinkedHashMap<>();
        for (PurchaseOrderRow row : rows) {
            String vendorKey = normalizeKey(row.vendorId());
            String requesterKey = normalizeKey(row.requesterId());
            String currencyKey = normalizeKey(row.currency());
            if (vendorKey.isEmpty() || requesterKey.isEmpty()
                || currencyKey.isEmpty() || row.orderDate() == null
                || row.amount() == null) {
                continue;
            }

            groups.computeIfAbsent(
                new MatchKey(vendorKey, requesterKey, currencyKey),
                ignored -> new ArrayList<>()
            ).add(row);
        }

        List<AlertResponse> alerts = new ArrayList<>();
        Map<String, OrdersResponse> evidenceOrders = new LinkedHashMap<>();
        for (List<PurchaseOrderRow> group : groups.values()) {
            addAlerts(config, group, alerts, evidenceOrders);
        }

        String fileNames = rows.stream()
            .map(PurchaseOrderRow::fileName)
            .map(ContractSplittingDetectionService::clean)
            .filter(name -> !name.isEmpty())
            .distinct()
            .collect(Collectors.joining(", "));

        return new ContractSplittingDetectionResponse(
            List.copyOf(alerts),
            List.copyOf(evidenceOrders.values()),
            fileNames,
            config.thresholdNumber(),
            Integer.toString(config.period()),
            Integer.toString(config.minOrders())
        );
    }

    private void addAlerts(
        ScanConfiguration config,
        List<PurchaseOrderRow> group,
        List<AlertResponse> alerts,
        Map<String, OrdersResponse> evidenceOrders
    ) {
        int start = 0;
        while (start < group.size()) {
            LocalDate startDate = group.get(start).orderDate();
            int end = start + 1;
            while (end < group.size()
                && ChronoUnit.DAYS.between(
                    startDate,
                    group.get(end).orderDate()
                ) < config.period()) {
                end++;
            }

            List<PurchaseOrderRow> window = group.subList(start, end);
            BigDecimal combined = window.stream()
                .map(PurchaseOrderRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (window.size() >= config.minOrders()
                && combined.compareTo(config.thresholdNumber()) >= 0) {
                PurchaseOrderRow first = window.get(0);
                PurchaseOrderRow last = window.get(window.size() - 1);
                alerts.add(new AlertResponse(
                    displayVendor(first),
                    clean(first.requesterId()),
                    first.orderDate() + " \u2013 " + last.orderDate(),
                    normalizeAmount(combined),
                    config.thresholdNumber(),
                    window.stream().map(PurchaseOrderRow::orderId).toList(),
                    "Needs review"
                ));
                for (PurchaseOrderRow row : window) {
                    evidenceOrders.putIfAbsent(
                        row.orderId(),
                        toResponse(row)
                    );
                }
                start = end;
            } else {
                start++;
            }
        }
    }

    private OrdersResponse toResponse(PurchaseOrderRow row) {
        return new OrdersResponse(
            row.orderId(),
            displayVendor(row),
            clean(row.requesterId()),
            clean(row.approverId()),
            row.orderDate(),
            normalizeAmount(row.amount()),
            clean(row.currency()),
            clean(row.department())
        );
    }

    private String displayVendor(PurchaseOrderRow row) {
        String vendorName = clean(row.vendorName());
        return vendorName.isEmpty() ? clean(row.vendorId()) : vendorName;
    }

    private PurchaseOrderRow mapPurchaseOrder(
        ResultSet resultSet,
        int rowNumber
    ) throws SQLException {
        return new PurchaseOrderRow(
            resultSet.getString("po_id"),
            resultSet.getString("vendor_id"),
            resultSet.getString("requester_id"),
            resultSet.getString("approver_id"),
            resultSet.getObject("po_date", LocalDate.class),
            resultSet.getBigDecimal("total_amount"),
            resultSet.getString("currency"),
            resultSet.getString("department"),
            resultSet.getString("vendor_name"),
            resultSet.getString("file_name")
        );
    }

    private static String normalizeKey(String value) {
        return clean(value)
            .replaceAll("\\s+", " ")
            .toLowerCase(Locale.ROOT);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static BigDecimal normalizeAmount(BigDecimal value) {
        return new BigDecimal(value.stripTrailingZeros().toPlainString());
    }

    private record ScanConfiguration(
        BigDecimal thresholdNumber,
        int period,
        int minOrders
    ) {}

    private record MatchKey(
        String vendorId,
        String requesterId,
        String currency
    ) {}

    record PurchaseOrderRow(
        String orderId,
        String vendorId,
        String requesterId,
        String approverId,
        LocalDate orderDate,
        BigDecimal amount,
        String currency,
        String department,
        String vendorName,
        String fileName
    ) {}
}

package com.example.microSaas.challenges.MS_01.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class ContractSplittingDetectionServiceTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final ContractSplittingDetectionService service =
        new ContractSplittingDetectionService(jdbcTemplate);

    @Test
    void reportsPurchaseOrdersGroupedWithinPeriodAndCurrency() {
        List<ContractSplittingDetectionService.PurchaseOrderRow> rows =
            List.of(
                order("PO-1041", "V001", "EMP-1", "2026-04-03", "6200", "EUR"),
                order("PO-1048", "V001", "EMP-1", "2026-04-07", "12200", "EUR"),
                order("PO-1052", "V001", "EMP-1", "2026-04-10", "9000", "EUR"),
                order("PO-1053", "V001", "EMP-1", "2026-04-04", "9000", "USD"),
                order("PO-1054", "V001", "EMP-2", "2026-04-04", "9000", "EUR")
            );
        when(jdbcTemplate.query(
            anyString(),
            org.mockito.ArgumentMatchers
                .<RowMapper<ContractSplittingDetectionService.PurchaseOrderRow>>any(),
            eq("MS_01")
        )).thenReturn(rows);

        var response = service.configureAndScan(
            new BigDecimal("10000"),
            7,
            2
        );

        assertEquals(1, response.alerts().size());
        var alert = response.alerts().get(0);
        assertEquals("Acme Ltd", alert.vendor());
        assertEquals("EMP-1", alert.requester());
        assertEquals("2026-04-03 \u2013 2026-04-07", alert.period());
        assertEquals(new BigDecimal("18400"), alert.combined());
        assertEquals(new BigDecimal("10000"), alert.threshold());
        assertEquals(List.of("PO-1041", "PO-1048"), alert.orders());
        assertEquals(2, response.orders().size());
        assertEquals("Acme Ltd", response.orders().get(0).vendor_id());
        assertEquals(new BigDecimal("6200"), response.orders().get(0).amount());
        assertEquals("purchase_orders_sample.csv", response.fileName());
        assertEquals(new BigDecimal("10000"), response.thresholdNumber());
        assertEquals("7", response.period());
        assertEquals("2", response.minOrders());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.configureAndScan(BigDecimal.ZERO, 7, 2)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> service.configureAndScan(BigDecimal.TEN, 0, 2)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> service.configureAndScan(BigDecimal.TEN, 7, 1)
        );
    }

    private ContractSplittingDetectionService.PurchaseOrderRow order(
        String orderId,
        String vendorId,
        String requesterId,
        String orderDate,
        String amount,
        String currency
    ) {
        return new ContractSplittingDetectionService.PurchaseOrderRow(
            orderId,
            vendorId,
            requesterId,
            "A. Patel",
            LocalDate.parse(orderDate),
            new BigDecimal(amount),
            currency,
            "Operations",
            "Acme Ltd",
            "purchase_orders_sample.csv"
        );
    }
}

package com.example.microSaas.upload.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.microSaas.upload.registry.DatasetRegistry;

class FileValidationServiceTest {

    @TempDir
    Path tempDirectory;

    private final FileValidationService validationService =
        new FileValidationService(new DatasetRegistry());

    @Test
    void acceptsNormalizedRequiredVendorColumns() throws IOException {
        Path file = csv(
            "Country,Risk Tier,Vendor ID,Vendor Name,Created Date,"
                + "Payment Terms,Vendor Status\n"
                + "Morocco,Low,V001,Atlas,2023-03-06,Net 15,Active\n"
        );

        assertDoesNotThrow(() ->
            validationService.validateStaged(
                file,
                "vendors.csv",
                "MS_01",
                "vendor"
            )
        );
    }

    @Test
    void rejectsMissingRequiredDatasetHeaders() throws IOException {
        Path file = csv(
            "vendor_id,vendor_name,country\n"
                + "V001,Atlas,Morocco\n"
        );

        assertThrows(IllegalArgumentException.class, () ->
            validationService.validateStaged(
                file,
                "vendors.csv",
                "MS_01",
                "vendor"
            )
        );
    }

    @Test
    void rejectsExtraDatasetHeaders() throws IOException {
        Path file = csv(
            "country,risk_tier,vendor_id,vendor_name,created_date,"
                + "payment_terms,vendor_status,extra\n"
                + "Morocco,Low,V001,Atlas,2023-03-06,Net 15,Active,x\n"
        );

        assertThrows(IllegalArgumentException.class, () ->
            validationService.validateStaged(
                file,
                "vendors.csv",
                "MS_01",
                "vendor"
            )
        );
    }

    @Test
    void acceptsPurchaseOrderVendorNameHeader() throws IOException {
        List<String> headers = List.of(
            "po_id", "status", "po_date", "category", "currency",
            "quantity", "subtotal", "tax_rate", "unit_cost", "vendor_id",
            "department", "tax_amount", "approver_id", "contract_id",
            "cost_center", "vendor_name", "requester_id", "total_amount",
            "payment_terms", "vendor_country", "discount_amount",
            "discount_percent", "item_description", "invoice_reference",
            "expected_delivery_date"
        );
        String content = String.join(",", headers)
            + "\n"
            + String.join(",", Collections.nCopies(headers.size(), "value"))
            + "\n";

        assertDoesNotThrow(() ->
            validationService.validateStaged(
                csv(content),
                "purchase_orders.csv",
                "MS_01",
                "purchase_order"
            )
        );
    }

    @Test
    void rejectsRowsWithDifferentNumberOfColumns() throws IOException {
        Path file = csv(
            "country,risk_tier,vendor_id,vendor_name,created_date,"
                + "payment_terms,vendor_status\n"
                + "Morocco,Low,V001,Atlas,2023-03-06,Net 15\n"
        );

        assertThrows(IllegalArgumentException.class, () ->
            validationService.validateStaged(
                file,
                "vendors.csv",
                "MS_01",
                "vendor"
            )
        );
    }

    private Path csv(String content) throws IOException {
        Path path = Files.createTempFile(tempDirectory, "upload-", ".csv");
        return Files.writeString(path, content, StandardCharsets.UTF_8);
    }
}

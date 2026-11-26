package com.example.microSaas.upload.registry;

import java.util.Map;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component 
public class DatasetRegistry {

    public record DatasetDefinition(
        String datasetType,
        String schemaName,
        String tableName,
        List<String> requiredHeaders
    ) {}

    private static DatasetDefinition dataset(
        String type,
        String schema,
        String table,
        String... headers
    ) {
        return new DatasetDefinition(type, schema, table, List.of(headers));
    }

    private final Map<String, Map<String, DatasetDefinition>> registry = 
        Map.of(
            "MS_01", Map.of(
                "contract", dataset(
                    "contract", "challenge_01", "contracts",
                    "category", "currency", "owner_id", "vendor_id",
                    "department", "contract_id", "vendor_name",
                    "contract_title", "contract_value", "contract_status",
                    "renewal_required", "contract_end_date",
                    "contract_start_date"
                ),
                "vendor", dataset(
                    "vendor", "challenge_01", "vendors",
                    "country", "risk_tier", "vendor_id", "vendor_name",
                    "created_date", "payment_terms", "vendor_status"
                ),
                "purchase_order", dataset(
                    "purchase_order", "challenge_01", "purchase_orders",
                    "po_id", "status", "po_date", "category", "currency",
                    "quantity", "subtotal", "tax_rate", "unit_cost",
                    "vendor_id", "department", "tax_amount", "approver_id",
                    "contract_id", "cost_center", "vendor_name", "requester_id",
                    "total_amount", "payment_terms", "vendor_country",
                    "discount_amount", "discount_percent", "item_description",
                    "invoice_reference", "expected_delivery_date"
                )
            ),
            "MS_02", Map.of(
                "invoice", dataset(
                    "invoice", "challenge_02", "invoices",
                    "invoice_number", "vendor_reference", "invoice_data",
                    "amount", "currency"
                )
            )
        );

        public DatasetDefinition getDataset(
            String challengeId,
            String datasetName
        ) {
            var challenge = registry.get(challengeId);

            if(challenge == null) {
                throw new IllegalArgumentException(
                    "Unknown challenge: " + challengeId
                );
            }

            var dataset = challenge.get(datasetName);

            if(dataset == null) {
                throw new IllegalArgumentException(
                    "Dataset is not registered: " + datasetName
                );
            }

            return dataset;
        }

        public boolean supportsChallenge(String challengeId) {
            return registry.containsKey(challengeId);
        }

        public static String normalizeHeader(String header) {
            return header.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "_");
        }
}

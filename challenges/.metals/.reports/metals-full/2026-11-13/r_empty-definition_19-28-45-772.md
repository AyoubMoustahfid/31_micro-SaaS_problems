error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/registry/DatasetRegistry.java:_empty_/Component#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/registry/DatasetRegistry.java
empty definition using pc, found symbol in pc: _empty_/Component#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 159
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/upload/registry/DatasetRegistry.java
text:
```scala
package com.example.microSaas.upload.registry;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Componen@@t 
public class DatasetRegistry {
    
    public record DatasetDefinition (
        String datasetType,
        Set<String> requiredColumns
    ) {}

    private final Map<String, Map<String, DatasetDefinition>> registry = 
        Map.of(
            "MS_01", Map.of(
                "contract", new DatasetDefinition(
                        "contract",
                        Set.of(
                            "contract_id",
                            "vendor_id",
                            "contract_date",
                            "amount",
                            "currency"
                        )
                    ),
                "vendor", new DatasetDefinition(
                    "vendor",
                    Set.of(
                        "vendor_id",
                        "vendor_name"
                    )
                ),
                "purchase_order", new DatasetDefinition(
                    "purchase_order",
                    Set.of(
                        "order_id",
                        "vendor_id",
                        "order_date",
                        "amount"
                    )
                )    
            ),
            "MS_02", Map.of(
                "invoice", new DatasetDefinition(
                   "invoice",
                   Set.of(
                    "invoice_number",
                    "vendor_reference",
                    "invoice_date",
                    "amount",
                    "currency"
                   ) 
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
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Component#
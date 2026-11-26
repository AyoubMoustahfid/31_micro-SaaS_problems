error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/challenges/challenge01/processor/ContractSplittingDetector.java:_empty_/PurchaseOrderRepository#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/challenges/challenge01/processor/ContractSplittingDetector.java
empty definition using pc, found symbol in pc: _empty_/PurchaseOrderRepository#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 566
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/challenges/challenge01/processor/ContractSplittingDetector.java
text:
```scala
package com.example.microSaas.challenges.challenge01.processor;

import com.microSaas.challenges.challenge01.entity.ContractSplittingAlertEntity;
import com.microSaas.challenges.challenge01.repository.AlertRepository;
import com.microSaas.challenges.challenge01.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class ContractSplittingDetector {
    private final Purch@@aseOrderRepository orders;
    private final AlertRepository alerts;
    public ContractSplittingDetector(PurchaseOrderRepository orders, AlertRepository alerts) {
        this.orders=orders; this.alerts=alerts;
    }

    // Starter rule: same vendor + requester + currency, rolling calendar month, total above configured limit.
    // The limit is passed in explicitly; do not mix currencies without FX normalization.
    @Transactional
    public List<ContractSplittingAlertEntity> detect(UUID uploadId, BigDecimal approvalLimit) {
        List<ContractSplittingAlertEntity> found=new ArrayList<>();
        Map<String,List<OrderView>> groups=new HashMap<>();
        // A production implementation should query only rows for uploadId and stream/group in SQL.
        // This method is kept intentionally small for the first challenge iteration.
        for (var order: orders.findAll()) {
            // entity getters are intentionally not exposed in this starter; see note in README.
        }
        return found;
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/PurchaseOrderRepository#
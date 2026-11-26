error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/challenges/challenge01/service/Ms01ImportService.java:java/lang/String#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/challenges/challenge01/service/Ms01ImportService.java
empty definition using pc, found symbol in pc: java/lang/String#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1118
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/challenges/challenge01/service/Ms01ImportService.java
text:
```scala
package com.example.microSaas.challenges.challenge01.service;

import com.microSaas.challenges.challenge01.entity.PurchaseOrderEntity;
import com.microSaas.challenges.challenge01.repository.PurchaseOrderRepository;
import com.microSaas.shared.exception.ApiException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Ms01ImportService {
    private final PurchaseOrderRepository repository;
    public Ms01ImportService(PurchaseOrderRepository repository) { this.repository=repository; }

    @Transactional
    public void importDatasets(UUID uploadId, Map<String,List<Map<String,String>>> datasets) {
        List<Map<String,String>> orders=datasets.get("purchase_orders");
        if (orders==null || orders.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST,"purchase_orders dataset is required");
        List<PurchaseOrderEntity> batch=new ArrayList<>();
        for (Map<String@@,String> row:orders) {
            try {
                batch.add(new PurchaseOrderEntity(UUID.randomUUID(),uploadId,required(row,"order_reference"),
                    required(row,"vendor_id"),required(row,"requester_id"),row.get("approver_id"),
                    LocalDate.parse(required(row,"order_date")),new BigDecimal(required(row,"amount")),
                    required(row,"currency").toUpperCase(Locale.ROOT)));
            } catch (RuntimeException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST,"Invalid purchase_orders row: "+ex.getMessage());
            }
        }
        repository.saveAll(batch);
    }
    private String required(Map<String,String> row,String key) {
        String value=row.get(key);
        if (value==null || value.isBlank()) throw new IllegalArgumentException("missing column/value: "+key);
        return value.trim();
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: java/lang/String#
error id: file:///C:/Users/pc/Downloads/challenges/src/test/java/com/example/challenges/day_01/services/impl/PurchaseOrderServiceImplTest.java:_empty_/PurchaseOrderServiceImpl#
file:///C:/Users/pc/Downloads/challenges/src/test/java/com/example/challenges/day_01/services/impl/PurchaseOrderServiceImplTest.java
empty definition using pc, found symbol in pc: _empty_/PurchaseOrderServiceImpl#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1646
uri: file:///C:/Users/pc/Downloads/challenges/src/test/java/com/example/challenges/day_01/services/impl/PurchaseOrderServiceImplTest.java
text:
```scala
package com.example.challenges.day_01.services.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import com.example.microSaas.challenges.challenge01.repository.PurchaseOrderRepository;


class PurchaseOrderServiceImplTest {

    @Test
    void importCsvPersistsOriginalFileAndParsedOrders() {
        PurchaseOrderRepository orderRepository = mock(PurchaseOrderRepository.class);
        PurchaseOrderImportRepository importRepository = mock(PurchaseOrderImportRepository.class);
        when(orderRepository.existsByOrderId("PO-1")).thenReturn(false);
        when(importRepository.save(any(PurchaseOrderImport.class))).thenAnswer(invocation -> {
            PurchaseOrderImport savedImport = invocation.getArgument(0);
            savedImport.setId(17L);
            return savedImport;
        });

        String csv = "order_id,vendor_id,requester_id,approver_id,order_date,amount,currency,department\n"
                + "PO-1,V-1,R-1,A-1,2026-11-04,25.50,USD,IT\n";
        byte[] fileContent = csv.getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "orders.csv", "text/csv", fileContent);

        var response = new PurchaseOrderSe@@rviceImpl(orderRepository, importRepository).importCsv(file);

        assertEquals(17L, response.importId());
        assertEquals(1, response.totalRows());
        assertEquals(1, response.importedRows());
        assertEquals("USD", response.currency());
        verify(orderRepository).saveAll(any());
        var savedImportCaptor = ArgumentCaptor.forClass(PurchaseOrderImport.class);
        verify(importRepository).save(savedImportCaptor.capture());
        assertEquals("orders.csv", savedImportCaptor.getValue().getOriginalFilename());
        assertArrayEquals(fileContent, savedImportCaptor.getValue().getFileContent());
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/PurchaseOrderServiceImpl#
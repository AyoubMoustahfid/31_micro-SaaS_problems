error id: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/challenges/challenge01/controller/Ms01Controller.java:_empty_/ContractSplittingDetector#
file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/challenges/challenge01/controller/Ms01Controller.java
empty definition using pc, found symbol in pc: _empty_/ContractSplittingDetector#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 434
uri: file:///C:/Users/pc/Downloads/challenges/src/main/java/com/example/microSaas/challenges/challenge01/controller/Ms01Controller.java
text:
```scala
package com.example.microSaas.challenges.challenge01.controller;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenges/MS-01")
public class Ms01Controller {
    private final ContractSplittingDetector detector;
    private final AlertRepository alerts;
    public Ms01Controller(@@ContractSplittingDetector detector, AlertRepository alerts) {
        this.detector=detector; this.alerts=alerts;
    }
    @PostMapping("/uploads/{uploadId}/detect")
    public List<AlertResponse> detect(@PathVariable UUID uploadId,
        @RequestParam(defaultValue="10000.00") BigDecimal approvalLimit) {
        if (approvalLimit.signum()<=0) throw new ApiException(HttpStatus.BAD_REQUEST,"approvalLimit must be positive");
        return detector.detect(uploadId,approvalLimit).stream().map(a -> new AlertResponse(a.getId(),a.getVendorId(),
            a.getRequesterId(),a.getPeriodStart(),a.getPeriodEnd(),a.getCombinedAmount(),a.getApprovalLimit(),
            a.getOrderCount(),a.getCurrency(),a.getCombinedAmount().subtract(a.getApprovalLimit()))).toList();
    }
    @GetMapping("/uploads/{uploadId}/alerts")
    public List<AlertResponse> alerts(@PathVariable UUID uploadId) {
        return alerts.findByUploadId(uploadId).stream().map(a -> new AlertResponse(a.getId(),a.getVendorId(),
            a.getRequesterId(),a.getPeriodStart(),a.getPeriodEnd(),a.getCombinedAmount(),a.getApprovalLimit(),
            a.getOrderCount(),a.getCurrency(),a.getCombinedAmount().subtract(a.getApprovalLimit()))).toList();
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/ContractSplittingDetector#
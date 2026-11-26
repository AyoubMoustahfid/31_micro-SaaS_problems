package com.example.microSaas.challenges.MS_01.importer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContractSplittingDetectionResponse (
    List<AlertResponse> alerts,
    List<OrdersResponse> orders,
    String fileName,
    BigDecimal thresholdNumber,
    String period,
    String minOrders
){

    public record AlertResponse(
        String vendor,
        String requester,
        String period,
        BigDecimal combined,
        BigDecimal threshold,
        List<String> orders,
        String status
    ){}


    public record OrdersResponse(
        String order_id,
        String vendor_id,
        String requester_id,
        String approver_id,
        LocalDate order_date,
        BigDecimal amount,
        String currency,
        String department
    ){}
}

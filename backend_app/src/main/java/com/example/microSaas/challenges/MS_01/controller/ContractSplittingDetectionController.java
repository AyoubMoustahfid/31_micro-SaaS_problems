package com.example.microSaas.challenges.MS_01.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.microSaas.challenges.MS_01.importer.ContractSplittingDetectionResponse;
import com.example.microSaas.challenges.MS_01.service.ContractSplittingDetectionService;

@RestController
@RequestMapping("/api/v1/contract-splitting")
public class ContractSplittingDetectionController {

    private final ContractSplittingDetectionService detectionService;

    public ContractSplittingDetectionController(
        ContractSplittingDetectionService detectionService
    ) {
        this.detectionService = detectionService;
    }

    @PostMapping("/config")
    public ContractSplittingDetectionResponse configure(
        @RequestParam("thresholdNumber") BigDecimal thresholdNumber,
        @RequestParam("period") int period,
        @RequestParam("minOrders") int minOrders
    ) {
        return detectionService.configureAndScan(
            thresholdNumber,
            period,
            minOrders
        );
    }
}

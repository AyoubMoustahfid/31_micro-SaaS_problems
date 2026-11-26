package com.example.microSaas.upload.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.microSaas.platform.repository.UploadBatchRepository;
import com.example.microSaas.upload.enums.UploadEnums.UploadStatus;

@Component
public class UploadJobQueuePoller {

    private static final Logger logger =
        LoggerFactory.getLogger(UploadJobQueuePoller.class);

    private final UploadBatchRepository batchRepository;
    private final UploadJobStateService jobStateService;
    private final UploadJobDispatcher dispatcher;
    private final long processingLeaseSeconds;

    public UploadJobQueuePoller(
        UploadBatchRepository batchRepository,
        UploadJobStateService jobStateService,
        UploadJobDispatcher dispatcher,
        @Value("${app.upload.job-lease-seconds:1800}")
        long processingLeaseSeconds
    ) {
        this.batchRepository = batchRepository;
        this.jobStateService = jobStateService;
        this.dispatcher = dispatcher;
        this.processingLeaseSeconds = processingLeaseSeconds;
    }

    @Scheduled(fixedDelayString = "${app.upload.queue-poll-ms:5000}")
    public void dispatchPendingBatches() {
        int recovered = jobStateService.requeueStaleProcessingFiles(
            processingLeaseSeconds
        );
        if (recovered > 0) {
            logger.warn("Requeued {} stale upload file job(s)", recovered);
        }

        batchRepository.findByStatus(UploadStatus.PROCESSING)
            .forEach(batch -> dispatcher.dispatch(batch.getId()));
    }
}

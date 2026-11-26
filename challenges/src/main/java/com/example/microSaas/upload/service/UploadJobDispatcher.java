package com.example.microSaas.upload.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

@Service
public class UploadJobDispatcher {

    private static final Logger logger =
        LoggerFactory.getLogger(UploadJobDispatcher.class);

    private final Executor uploadTaskExecutor;
    private final UploadJobWorker jobWorker;
    private final Set<UUID> activeBatches = ConcurrentHashMap.newKeySet();

    public UploadJobDispatcher(
        @Qualifier("uploadTaskExecutor") Executor uploadTaskExecutor,
        UploadJobWorker jobWorker
    ) {
        this.uploadTaskExecutor = uploadTaskExecutor;
        this.jobWorker = jobWorker;
    }

    public void dispatch(UUID batchId) {
        if (!activeBatches.add(batchId)) {
            return;
        }
        try {
            uploadTaskExecutor.execute(() -> {
                try {
                    jobWorker.processBatch(batchId);
                } catch (RuntimeException exception) {
                    logger.error(
                        "Could not process upload batch {}",
                        batchId,
                        exception
                    );
                } finally {
                    activeBatches.remove(batchId);
                }
            });
        } catch (TaskRejectedException exception) {
            activeBatches.remove(batchId);
            logger.warn(
                "Upload queue is full; batch {} will be retried by the database queue poller",
                batchId,
                exception
            );
        }
    }
}

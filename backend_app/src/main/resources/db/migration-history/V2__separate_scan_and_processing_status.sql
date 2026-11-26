ALTER TABLE platform.uploaded_files
    ADD COLUMN processing_status VARCHAR(30) NOT NULL DEFAULT 'PENDING';

UPDATE platform.uploaded_files
   SET processing_status = CASE scan_status
       WHEN 'CLEAN' THEN 'COMPLETED'
       WHEN 'INFECTED' THEN 'FAILED'
       WHEN 'ERROR' THEN 'FAILED'
       ELSE 'PENDING'
   END;

DROP INDEX IF EXISTS platform.idx_uploaded_file_job_state;
CREATE INDEX idx_uploaded_file_job_state
    ON platform.uploaded_files(processing_status, processing_started_at);

DROP INDEX IF EXISTS platform.idx_uploaded_file_batch_dataset_state;
CREATE INDEX idx_uploaded_file_batch_dataset_state
    ON platform.uploaded_files(
        upload_batch_id,
        dataset_name,
        processing_status
    );

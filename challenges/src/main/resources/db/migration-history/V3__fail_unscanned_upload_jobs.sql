UPDATE platform.uploaded_files
   SET processing_status = 'FAILED',
       processing_started_at = NULL
 WHERE scan_status IS DISTINCT FROM 'CLEAN';

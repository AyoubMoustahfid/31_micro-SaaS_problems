CREATE SCHEMA IF NOT EXISTS platform;
CREATE SCHEMA IF NOT EXISTS challenge_01;
CREATE SCHEMA IF NOT EXISTS challenge_02;

CREATE TABLE platform.upload_batches (
    id UUID PRIMARY KEY,
    challenge_id VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_files INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE platform.uploaded_files (
    id UUID PRIMARY KEY,
    upload_batch_id UUID NOT NULL
        REFERENCES platform.upload_batches(id) ON DELETE CASCADE,
    challenge_id VARCHAR(30) NOT NULL,
    scan_status VARCHAR(30) NOT NULL,
    processing_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    processing_started_at TIMESTAMPTZ,
    file_size BIGINT NOT NULL,
    file_extension VARCHAR(20) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    dataset_name VARCHAR(100) NOT NULL,
    storage_path TEXT NOT NULL
);

CREATE INDEX idx_upload_batch
    ON platform.uploaded_files(upload_batch_id);
CREATE INDEX idx_upload_challenge
    ON platform.uploaded_files(challenge_id);
CREATE INDEX idx_upload_batch_status
    ON platform.upload_batches(status);
CREATE INDEX idx_uploaded_file_job_state
    ON platform.uploaded_files(processing_status, processing_started_at);
CREATE INDEX idx_uploaded_file_batch_dataset_state
    ON platform.uploaded_files(
        upload_batch_id,
        dataset_name,
        processing_status
    );

CREATE TABLE challenge_01.vendors (
    vendor_id VARCHAR(100) PRIMARY KEY,
    vendor_name VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    risk_tier VARCHAR(50),
    created_date DATE,
    payment_terms VARCHAR(100),
    vendor_status VARCHAR(50),
    uploaded_file_id UUID NOT NULL
        REFERENCES platform.uploaded_files(id) ON DELETE CASCADE,
    row_number BIGINT NOT NULL,
    CONSTRAINT uq_challenge_01_vendor_file_row
        UNIQUE (uploaded_file_id, row_number)
);

CREATE TABLE challenge_01.contracts (
    contract_id VARCHAR(100) PRIMARY KEY,
    category VARCHAR(100),
    currency CHAR(3) NOT NULL,
    owner_id VARCHAR(100),
    vendor_id VARCHAR(100)
        REFERENCES challenge_01.vendors(vendor_id),
    department VARCHAR(100),
    vendor_name VARCHAR(255),
    contract_title VARCHAR(255),
    contract_value NUMERIC(18, 2) NOT NULL,
    contract_status VARCHAR(50),
    renewal_required VARCHAR(10),
    contract_end_date DATE,
    contract_start_date DATE,
    uploaded_file_id UUID NOT NULL
        REFERENCES platform.uploaded_files(id) ON DELETE CASCADE,
    row_number BIGINT NOT NULL,
    CONSTRAINT uq_challenge_01_contract_file_row
        UNIQUE (uploaded_file_id, row_number)
);

CREATE TABLE challenge_01.purchase_orders (
    po_id VARCHAR(100) PRIMARY KEY,
    status VARCHAR(50),
    po_date DATE NOT NULL,
    category VARCHAR(100),
    currency CHAR(3) NOT NULL,
    quantity NUMERIC(18, 4),
    subtotal NUMERIC(18, 2),
    tax_rate NUMERIC(8, 4),
    unit_cost NUMERIC(18, 2),
    vendor_id VARCHAR(100)
        REFERENCES challenge_01.vendors(vendor_id),
    department VARCHAR(100),
    tax_amount NUMERIC(18, 2),
    approver_id VARCHAR(100),
    contract_id VARCHAR(100)
        REFERENCES challenge_01.contracts(contract_id),
    cost_center VARCHAR(100),
    vendor_name VARCHAR(255),
    requester_id VARCHAR(100),
    total_amount NUMERIC(18, 2) NOT NULL,
    payment_terms VARCHAR(100),
    vendor_country VARCHAR(100),
    discount_amount NUMERIC(18, 2),
    discount_percent NUMERIC(8, 4),
    item_description TEXT,
    invoice_reference VARCHAR(100),
    expected_delivery_date DATE,
    uploaded_file_id UUID NOT NULL
        REFERENCES platform.uploaded_files(id) ON DELETE CASCADE,
    row_number BIGINT NOT NULL,
    CONSTRAINT uq_challenge_01_purchase_order_file_row
        UNIQUE (uploaded_file_id, row_number)
);

CREATE TABLE challenge_02.invoices (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    invoice_number VARCHAR(100) NOT NULL,
    vendor_reference VARCHAR(100) NOT NULL,
    invoice_data JSONB NOT NULL,
    amount NUMERIC(18, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    uploaded_file_id UUID NOT NULL
        REFERENCES platform.uploaded_files(id) ON DELETE CASCADE,
    row_number BIGINT NOT NULL,
    CONSTRAINT uq_challenge_02_invoice_file_row
        UNIQUE (uploaded_file_id, row_number)
);

CREATE TABLE challenge_02.duplicate_alerts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    invoice_id BIGINT NOT NULL
        REFERENCES challenge_02.invoices(id) ON DELETE CASCADE,
    matched_invoice_id BIGINT NOT NULL
        REFERENCES challenge_02.invoices(id) ON DELETE CASCADE,
    similarity_score NUMERIC(6, 5),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

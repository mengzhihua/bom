CREATE TABLE IF NOT EXISTS bom_sequence (
    prefix VARCHAR(16),
    day_key VARCHAR(8),
    seq_value INT NOT NULL,
    PRIMARY KEY(prefix, day_key)
);

CREATE TABLE IF NOT EXISTS bom_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(64),
    role VARCHAR(16) NOT NULL,
    supplier_code VARCHAR(32),
    status INT DEFAULT 1,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_op_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64),
    method VARCHAR(8),
    path VARCHAR(255),
    query VARCHAR(255),
    http_status INT,
    cost_ms INT,
    client_ip VARCHAR(64),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_integration_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    direction VARCHAR(8),
    system VARCHAR(16),
    action VARCHAR(64),
    biz_type VARCHAR(32),
    biz_id BIGINT,
    biz_code VARCHAR(64),
    request TEXT,
    response TEXT,
    status VARCHAR(16),
    error_msg VARCHAR(512),
    duration_ms INT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_vehicle_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_code VARCHAR(32) UNIQUE NOT NULL,
    model_name VARCHAR(128),
    platform VARCHAR(128),
    program VARCHAR(16),
    sop_date DATE,
    status VARCHAR(16),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_plant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plant_code VARCHAR(32) UNIQUE NOT NULL,
    plant_name VARCHAR(128),
    sap_plant VARCHAR(32),
    address VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128),
    sap_vendor VARCHAR(32),
    srm_code VARCHAR(32),
    status VARCHAR(16),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_feature (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_model_id BIGINT,
    feature VARCHAR(32),
    name VARCHAR(64),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_feature_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feature_id BIGINT,
    option_code VARCHAR(64),
    option_name VARCHAR(128),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_part (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_no VARCHAR(32) NOT NULL,
    revision VARCHAR(8) NOT NULL,
    part_name VARCHAR(128),
    part_name_en VARCHAR(128),
    part_type VARCHAR(32),
    category VARCHAR(32),
    uom VARCHAR(8),
    material VARCHAR(128),
    weight_kg DECIMAL(18, 4),
    make_buy VARCHAR(8),
    supplier_id BIGINT,
    drawing_no VARCHAR(64),
    drawing_rev VARCHAR(16),
    color VARCHAR(32),
    surface_treatment VARCHAR(128),
    unit_cost DECIMAL(18, 4),
    lead_time_days INT,
    lifecycle VARCHAR(16),
    sap_material VARCHAR(64),
    is_phantom BOOLEAN DEFAULT FALSE,
    safety_part BOOLEAN DEFAULT FALSE,
    regulatory_part BOOLEAN DEFAULT FALSE,
    remark VARCHAR(512),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_bom_part UNIQUE(part_no, revision)
);

CREATE TABLE IF NOT EXISTS bom_part_revision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_id BIGINT,
    revision VARCHAR(8),
    change_no VARCHAR(64),
    released_by VARCHAR(64),
    released_at TIMESTAMP,
    description VARCHAR(512),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_part_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_id BIGINT,
    doc_type VARCHAR(16),
    doc_no VARCHAR(64),
    version VARCHAR(16),
    url VARCHAR(512),
    remark VARCHAR(512),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_header (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bom_no VARCHAR(32) NOT NULL,
    bom_type VARCHAR(16),
    root_part_id BIGINT,
    vehicle_model_id BIGINT,
    plant_id BIGINT,
    version INT,
    status VARCHAR(16),
    effective_from DATE,
    effective_to DATE,
    description VARCHAR(512),
    source_bom_id BIGINT,
    released_by VARCHAR(64),
    released_at TIMESTAMP,
    sap_bom_no VARCHAR(64),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bom_id BIGINT,
    parent_part_id BIGINT,
    child_part_id BIGINT,
    find_no INT,
    qty DECIMAL(18, 4),
    uom VARCHAR(8),
    usage_type VARCHAR(16),
    usage_condition VARCHAR(512),
    station_code VARCHAR(32),
    operation_seq INT,
    alternate_group VARCHAR(32),
    alternate_priority INT,
    effective_from DATE,
    effective_to DATE,
    position_desc VARCHAR(128),
    remark VARCHAR(512),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_ecr (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ecr_no VARCHAR(32) UNIQUE NOT NULL,
    title VARCHAR(256),
    reason VARCHAR(32),
    priority VARCHAR(16),
    vehicle_model_id BIGINT,
    affected_part_ids VARCHAR(4000),
    description VARCHAR(2000),
    status VARCHAR(16),
    requester VARCHAR(64),
    approved_by VARCHAR(64),
    approved_at TIMESTAMP,
    reject_reason VARCHAR(512),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_ecn (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ecn_no VARCHAR(32) UNIQUE NOT NULL,
    ecr_id BIGINT,
    title VARCHAR(256),
    bom_id BIGINT,
    change_type VARCHAR(32),
    effective_type VARCHAR(16),
    effective_date DATE,
    effective_vin VARCHAR(64),
    status VARCHAR(16),
    implemented_bom_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_ecn_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ecn_id BIGINT,
    action VARCHAR(16),
    parent_part_id BIGINT,
    old_child_part_id BIGINT,
    new_child_part_id BIGINT,
    old_qty DECIMAL(18, 4),
    new_qty DECIMAL(18, 4),
    find_no INT,
    usage_condition VARCHAR(512),
    station_code VARCHAR(32),
    remark VARCHAR(512),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bom_work_station (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plant_id BIGINT,
    line_code VARCHAR(32),
    station_code VARCHAR(32),
    station_name VARCHAR(128),
    seq INT,
    takt INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bom_item_bom
    ON bom_item(bom_id);

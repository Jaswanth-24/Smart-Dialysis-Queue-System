-- V1__init_schema.sql
-- Smart Dialysis Center Management System — initial schema

CREATE TABLE centers (
    id            CHAR(36)     PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    address       TEXT,
    phone         VARCHAR(20),
    machine_count INT          NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id            CHAR(36)     PRIMARY KEY,
    center_id     CHAR(36)     NOT NULL,
    name          VARCHAR(200) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('RECEPTIONIST','TECHNICIAN','DOCTOR','ADMIN') NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_center FOREIGN KEY (center_id) REFERENCES centers(id)
);

CREATE TABLE patients (
    id                           CHAR(36)    PRIMARY KEY,
    center_id                    CHAR(36)    NOT NULL,
    doctor_id                    CHAR(36),
    name                         VARCHAR(200) NOT NULL,
    phone                        VARCHAR(20)  NOT NULL,
    email                        VARCHAR(150) UNIQUE,
    password_hash                VARCHAR(255),
    date_of_birth                DATE,
    blood_group                  VARCHAR(5),
    diagnosis                    TEXT,
    emergency_contact_name       VARCHAR(200),
    emergency_contact_phone      VARCHAR(20),
    total_sessions               INT         NOT NULL DEFAULT 0,
    last_consult_trigger_session INT         NOT NULL DEFAULT 0,
    is_active                    BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at                   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_patient_center FOREIGN KEY (center_id) REFERENCES centers(id),
    CONSTRAINT fk_patient_doctor FOREIGN KEY (doctor_id) REFERENCES users(id)
);

CREATE TABLE machines (
    id             CHAR(36)    PRIMARY KEY,
    center_id      CHAR(36)    NOT NULL,
    machine_number VARCHAR(20) NOT NULL,
    status         ENUM('AVAILABLE','IN_USE','CLEANING','MAINTENANCE','OUT_OF_SERVICE') NOT NULL DEFAULT 'AVAILABLE',
    last_serviced  DATE,
    is_active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_machine_center FOREIGN KEY (center_id) REFERENCES centers(id)
);

CREATE TABLE queue_entries (
    id             CHAR(36)   PRIMARY KEY,
    patient_id     CHAR(36)   NOT NULL,
    center_id      CHAR(36)   NOT NULL,
    machine_id     CHAR(36),
    token_number   INT        NOT NULL,
    priority_score DOUBLE     NOT NULL DEFAULT 0,
    is_emergency   BOOLEAN    NOT NULL DEFAULT FALSE,
    status         ENUM('WAITING','CALLED','IN_SESSION','COMPLETED','CANCELLED','EMERGENCY') NOT NULL DEFAULT 'WAITING',
    queue_date     DATE       NOT NULL,
    checked_in_at  DATETIME   NOT NULL,
    created_at     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_queue_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_queue_center  FOREIGN KEY (center_id)  REFERENCES centers(id),
    CONSTRAINT fk_queue_machine FOREIGN KEY (machine_id) REFERENCES machines(id),
    CONSTRAINT uq_patient_queue_date UNIQUE (patient_id, queue_date)
);

CREATE TABLE dialysis_sessions (
    id             CHAR(36)   PRIMARY KEY,
    patient_id     CHAR(36)   NOT NULL,
    machine_id     CHAR(36),
    technician_id  CHAR(36),
    session_date   DATE       NOT NULL,
    start_time     DATETIME,
    end_time       DATETIME,
    status         ENUM('SCHEDULED','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    session_number INT        NOT NULL,
    created_at     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_patient    FOREIGN KEY (patient_id)    REFERENCES patients(id),
    CONSTRAINT fk_session_machine    FOREIGN KEY (machine_id)    REFERENCES machines(id),
    CONSTRAINT fk_session_technician FOREIGN KEY (technician_id) REFERENCES users(id)
);

CREATE TABLE session_values (
    id                  CHAR(36)       PRIMARY KEY,
    session_id          CHAR(36)       NOT NULL UNIQUE,
    pre_bp              VARCHAR(20),
    pre_weight          DECIMAL(5,2),
    pre_complaints      TEXT,
    uf_rate             DECIMAL(5,2),
    blood_flow_rate     DECIMAL(5,2),
    dialysate_flow_rate DECIMAL(6,2),
    post_bp             VARCHAR(20),
    post_weight         DECIMAL(5,2),
    uf_removed          DECIMAL(5,2),
    complications       TEXT,
    has_abnormal_values BOOLEAN        NOT NULL DEFAULT FALSE,
    updated_at          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_values_session FOREIGN KEY (session_id) REFERENCES dialysis_sessions(id)
);

CREATE TABLE doctor_consultations (
    id               CHAR(36)   PRIMARY KEY,
    patient_id       CHAR(36)   NOT NULL,
    doctor_id        CHAR(36),
    consult_date     DATE,
    trigger_session  INT,
    status           ENUM('PENDING','SCHEDULED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    doctor_notes     TEXT,
    prescription     TEXT,
    created_at       DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_consult_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_consult_doctor  FOREIGN KEY (doctor_id)  REFERENCES users(id)
);

CREATE TABLE test_reports (
    id              CHAR(36)    PRIMARY KEY,
    patient_id      CHAR(36)    NOT NULL,
    consultation_id CHAR(36),
    uploaded_by     CHAR(36)    NOT NULL,
    test_type       ENUM('RFT','CBP','KFT','SERUM_ELECTROLYTES','HBA1C','OTHER') NOT NULL,
    report_date     DATE        NOT NULL,
    file_url        VARCHAR(500) NOT NULL,
    file_name       VARCHAR(255),
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_patient  FOREIGN KEY (patient_id)      REFERENCES patients(id),
    CONSTRAINT fk_report_consult  FOREIGN KEY (consultation_id) REFERENCES doctor_consultations(id),
    CONSTRAINT fk_report_uploader FOREIGN KEY (uploaded_by)     REFERENCES users(id)
);

-- Indexes for common lookups
CREATE INDEX idx_queue_center_date  ON queue_entries(center_id, queue_date);
CREATE INDEX idx_queue_status       ON queue_entries(status);
CREATE INDEX idx_session_patient    ON dialysis_sessions(patient_id, session_date);
CREATE INDEX idx_consult_patient    ON doctor_consultations(patient_id, status);
CREATE INDEX idx_report_patient     ON test_reports(patient_id, report_date);

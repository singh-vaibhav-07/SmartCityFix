-- department-service/src/main/resources/db/migration/V1__create_departments_tables.sql
CREATE TABLE departments (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    zone VARCHAR(50),
    contact_email VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20),
    endpoint VARCHAR(255),
    capacity INTEGER,
    current_workload INTEGER DEFAULT 0,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    address VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE department_categories (
    department_id UUID NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    category VARCHAR(20) NOT NULL,
    PRIMARY KEY (department_id, category)
);

CREATE INDEX idx_departments_name ON departments(name);
CREATE INDEX idx_departments_zone ON departments(zone);
CREATE INDEX idx_department_categories_category ON department_categories(category);
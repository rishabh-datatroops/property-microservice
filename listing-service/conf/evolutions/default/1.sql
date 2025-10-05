# --- !Ups

CREATE TABLE properties (
    id UUID PRIMARY KEY,
    broker_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    property_type VARCHAR(100) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    location VARCHAR(255) NOT NULL,
    area DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- --- !Downs
DROP TABLE IF EXISTS properties;

# --- !Ups
ALTER TABLE properties ALTER COLUMN area DROP NOT NULL;

# --- !Downs
ALTER TABLE properties ALTER COLUMN area SET NOT NULL;

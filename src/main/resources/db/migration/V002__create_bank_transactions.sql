CREATE TABLE IF NOT EXISTS bank_transactions (
    transaction_uuid        UUID                        PRIMARY KEY,
    account_uuid            UUID                        NOT NULL REFERENCES bank_accounts (account_uuid) ON DELETE RESTRICT,
    transaction_timestamp   TIMESTAMP WITH TIME ZONE    NOT NULL,
    amount                  NUMERIC(15, 2)              NOT NULL,
    transaction_type        VARCHAR(25)                 NOT NULL,
    notes                   TEXT,
    created_at              TIMESTAMP WITH TIME ZONE,
    updated_at              TIMESTAMP WITH TIME ZONE
);
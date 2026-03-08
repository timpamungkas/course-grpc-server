CREATE TABLE IF NOT EXISTS bank_transfers (
    transfer_uuid       UUID                        PRIMARY KEY,
    from_account_uuid   UUID                        REFERENCES bank_accounts (account_uuid) ON DELETE SET NULL,
    to_account_uuid     UUID                        REFERENCES bank_accounts (account_uuid) ON DELETE SET NULL,
    currency            VARCHAR(5)                  NOT NULL,
    amount              NUMERIC(15, 2)              NOT NULL,
    transfer_timestamp  TIMESTAMP WITH TIME ZONE    NOT NULL,
    transfer_success    BOOLEAN                     NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE
);
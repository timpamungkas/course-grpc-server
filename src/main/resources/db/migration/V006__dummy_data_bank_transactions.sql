MERGE INTO bank_transactions (
    transaction_uuid,
    account_uuid,
    transaction_timestamp,
    amount,
    transaction_type,
    notes,
    created_at,
    updated_at
) KEY (transaction_uuid)
VALUES
    (
        '1590ff7a-3980-4f4a-87f2-0e09ccc9439e',
        'c49d4752-c3da-4d3b-816d-cfd5ddf2c3d3',
        CURRENT_TIMESTAMP,
        10.00,
        'ADDITION',
        'Initial deposit',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

MERGE INTO bank_transactions (
    transaction_uuid,
    account_uuid,
    transaction_timestamp,
    amount,
    transaction_type,
    notes,
    created_at,
    updated_at
) KEY (transaction_uuid)
VALUES
    (
        'dcc3ebc1-20f9-4e22-bf0c-38ad2d89f459',
        '1fc76647-b82b-41f4-8336-1de7278c7e88',
        CURRENT_TIMESTAMP,
        10.00,
        'ADDITION',
        'Initial deposit',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

MERGE INTO bank_transactions (
    transaction_uuid,
    account_uuid,
    transaction_timestamp,
    amount,
    transaction_type,
    notes,
    created_at,
    updated_at
) KEY (transaction_uuid)
VALUES
    (
        '61eaf64c-3af9-4dfa-a53c-60c1d12b5deb',
        'b6487d36-ced6-4768-aa1c-9219836e52cb',
        CURRENT_TIMESTAMP,
        10.00,
        'ADDITION',
        'Initial deposit',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

MERGE INTO bank_transactions (
    transaction_uuid,
    account_uuid,
    transaction_timestamp,
    amount,
    transaction_type,
    notes,
    created_at,
    updated_at
) KEY (transaction_uuid)
VALUES
    (
        'c9031f1c-f1ca-4511-89bc-e66e426c63f6',
        '2a630f89-73af-4771-97c1-ae354b70cecd',
        CURRENT_TIMESTAMP,
        10.00,
        'ADDITION',
        'Initial deposit',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

MERGE INTO bank_transactions (
    transaction_uuid,
    account_uuid,
    transaction_timestamp,
    amount,
    transaction_type,
    notes,
    created_at,
    updated_at
) KEY (transaction_uuid)
VALUES
    (
        '24d213ab-4a95-4964-a20f-6f86a01dd18c',
        'f190f2c2-d8ae-4ed8-8566-bc57233a14b1',
        CURRENT_TIMESTAMP,
        10.00,
        'ADDITION',
        'Initial deposit',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

MERGE INTO bank_accounts (
    account_uuid,
    account_number,
    account_name,
    currency,
    current_balance,
    created_at,
    updated_at
) KEY (account_uuid)
VALUES
    (
        'c49d4752-c3da-4d3b-816d-cfd5ddf2c3d3',
        '11111111',
        'Selina Kyle',
        'USD',
        10.00,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

MERGE INTO bank_accounts (
    account_uuid,
    account_number,
    account_name,
    currency,
    current_balance,
    created_at,
    updated_at
) KEY (account_uuid)
VALUES
    (
        '1fc76647-b82b-41f4-8336-1de7278c7e88',
        '22222222',
        'Arthur Curry',
        'USD',
        10.00,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

MERGE INTO bank_accounts (
    account_uuid,
    account_number,
    account_name,
    currency,
    current_balance,
    created_at,
    updated_at
) KEY (account_uuid)
VALUES
    (
        'b6487d36-ced6-4768-aa1c-9219836e52cb',
        '33333333',
        'Lois Lane',
        'USD',
        10.00,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

MERGE INTO bank_accounts (
    account_uuid,
    account_number,
    account_name,
    currency,
    current_balance,
    created_at,
    updated_at
) KEY (account_uuid)
VALUES
    (
        '2a630f89-73af-4771-97c1-ae354b70cecd',
        '44444444',
        'Harleen Quinzel',
        'USD',
        10.00,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

MERGE INTO bank_accounts (
    account_uuid,
    account_number,
    account_name,
    currency,
    current_balance,
    created_at,
    updated_at
) KEY (account_uuid)
VALUES
    (
        'f190f2c2-d8ae-4ed8-8566-bc57233a14b1',
        '55555555',
        'Barry Allen',
        'USD',
        10.00,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
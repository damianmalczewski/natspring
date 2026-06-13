CREATE
    TABLE
        IF NOT EXISTS orders(
            order_id UUID NOT NULL,
            order_customer_name VARCHAR(255) NOT NULL,
            PRIMARY KEY(order_id)
        );

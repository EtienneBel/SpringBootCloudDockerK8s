-- Create the orderDb database if it doesn't exist
CREATE DATABASE IF NOT EXISTS orderDb;

-- Use the orderDb database
USE orderDb;

-- Drop existing orders table if it exists
DROP TABLE IF EXISTS orders;

-- Create the orders table
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    order_status VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL
);

-- Insert data into the orders table
INSERT INTO orders (product_id, quantity, order_date, order_status, amount) VALUES
(1, 2, '2023-06-26 10:00:00', 'Pending', 500),
(2, 1, '2023-06-26 11:00:00', 'Shipped', 250),
(3, 5, '2023-06-26 12:00:00', 'Delivered', 1250),
(4, 3, '2023-06-26 13:00:00', 'Cancelled', 750);


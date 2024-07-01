-- Create the paymentDb database
CREATE DATABASE IF NOT EXISTS paymentDb;

-- Use the paymentDb database
USE paymentDb;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS payments;

-- Create the payments table
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    payment_mode VARCHAR(50) NOT NULL,
    reference_number VARCHAR(255) NOT NULL,
    payment_date DATETIME NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    amount BIGINT NOT NULL
);

-- Insert data into the payments table
INSERT INTO payments (order_id, payment_mode, reference_number, payment_date, payment_status, amount) VALUES
(1, 'CREDIT_CARD', 'REF123456', '2023-06-26 10:30:00', 'Completed', 500),
(3, 'CASH', 'REF123458', '2023-06-26 12:30:00', 'Completed', 1250),
(4, 'CREDIT_CARD', 'REF123459', '2023-06-26 13:30:00', 'Failed', 750),
(2, 'PAYPAL', 'REF123460', '2023-06-26 14:30:00', 'Pending', 1000);
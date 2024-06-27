-- Create the paymentDb database
CREATE DATABASE IF NOT EXISTS paymentDb;

-- Use the paymentDb database
USE paymentDb;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS payments;

-- Create the payments table
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    orderId BIGINT NOT NULL,
    paymentMode VARCHAR(50) NOT NULL,
    referenceNumber VARCHAR(255) NOT NULL,
    paymentDate DATETIME NOT NULL,
    paymentStatus VARCHAR(50) NOT NULL,
    amount BIGINT NOT NULL
);

-- Insert data into the payments table
INSERT INTO payments (orderId, paymentMode, referenceNumber, paymentDate, paymentStatus, amount) VALUES
(1, 'Credit Card', 'REF123456', '2023-06-26 10:30:00', 'Completed', 500),
(3, 'Bank Transfer', 'REF123458', '2023-06-26 12:30:00', 'Completed', 1250),
(4, 'Credit Card', 'REF123459', '2023-06-26 13:30:00', 'Failed', 750),
(2, 'PayPal', 'REF123460', '2023-06-26 14:30:00', 'Pending', 1000);
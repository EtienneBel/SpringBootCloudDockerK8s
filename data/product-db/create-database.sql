-- Create the productDb database
CREATE DATABASE IF NOT EXISTS productDb;

-- Use the productDb database
USE productDb;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS products;

-- Create the products table
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(255) NOT NULL,
    price BIGINT NOT NULL,
    quantity BIGINT NOT NULL
);

-- Insert data into the products table
INSERT INTO products (product_name, price, quantity) VALUES
('iPhone 15', 250, 10),
('Dermaroller', 250, 5),
('Tesla Motor', 250, 20),
('Keyboard', 250, 15);
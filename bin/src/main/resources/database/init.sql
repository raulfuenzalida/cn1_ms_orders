CREATE DATABASE IF NOT EXISTS orders_db;
USE orders_db;

CREATE TABLE IF NOT EXISTS orders (
    id_order BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL,
    customer_email VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    confirmed_at DATETIME,
    completed_at DATETIME,
    cancelled_at DATETIME,
    CONSTRAINT chk_total_positive CHECK (total >= 0),
    CONSTRAINT chk_status_valid CHECK (status IN ('CREATED', 'CONFIRMED', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_items (
    id_order_item BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_order BIGINT NOT NULL,
    id_product BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    UNIQUE KEY unique_order_product (id_order, id_product),
    CONSTRAINT fk_order FOREIGN KEY (id_order) REFERENCES orders(id_order) ON DELETE CASCADE,
    CONSTRAINT chk_quantity_range CHECK (quantity BETWEEN 1 AND 99),
    CONSTRAINT chk_unit_price_positive CHECK (unit_price > 0),
    CONSTRAINT chk_subtotal_positive CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

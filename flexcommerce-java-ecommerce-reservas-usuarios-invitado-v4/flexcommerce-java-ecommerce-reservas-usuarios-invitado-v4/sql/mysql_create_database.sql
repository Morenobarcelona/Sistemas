CREATE DATABASE IF NOT EXISTS flexcommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE flexcommerce;

-- Hibernate/Spring Data JPA crea y actualiza las tablas automáticamente con:
-- spring.jpa.hibernate.ddl-auto=update
-- Tablas principales esperadas:
-- app_users, products, customer_orders, order_line, appointment, service_item

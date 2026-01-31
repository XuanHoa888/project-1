-- 1. Tạo database mới
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'OrderAnalytics')
BEGIN
    CREATE DATABASE OrderAnalytics;
    PRINT 'Database OrderAnalytics đã được tạo thành công!';
END
ELSE
BEGIN
    PRINT 'Database OrderAnalytics đã tồn tại!';
END

GO

-- 2. Chuyển sang database
USE OrderAnalytics;
GO

-- 3. Tạo bảng orders


CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_code NVARCHAR(50) NOT NULL,
    customer_name NVARCHAR(100) NOT NULL,
    customer_email NVARCHAR(100),
    product_name NVARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    status NVARCHAR(20) NOT NULL,
    order_date DATETIME2 NOT NULL,
    category NVARCHAR(100),
    region NVARCHAR(100)
);

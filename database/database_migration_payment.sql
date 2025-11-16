-- =====================================================
-- VNPAY PAYMENT INTEGRATION - DATABASE MIGRATION SCRIPT
-- =====================================================
-- This script creates tables for VNPay payment integration
-- Run this script on your SQL Server database

USE joblink2025;
GO

-- =====================================================
-- 1. Create SubscriptionPackage Table
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SubscriptionPackage]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[SubscriptionPackage] (
        [package_id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [package_name] NVARCHAR(100) NOT NULL,
        [description] NVARCHAR(MAX),
        [price] BIGINT NOT NULL,
        [duration_days] INT NOT NULL,
        [tier_level] INT NOT NULL,
        [features] NVARCHAR(MAX),
        [is_active] BIT NOT NULL DEFAULT 1,
        [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
        [updated_at] DATETIME
    );
    PRINT 'Table SubscriptionPackage created successfully.';
END
ELSE
BEGIN
    PRINT 'Table SubscriptionPackage already exists.';
END
GO

-- =====================================================
-- 2. Create PaymentTransaction Table
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[PaymentTransaction]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[PaymentTransaction] (
        [transaction_id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [user_id] INT NOT NULL,
        [package_id] BIGINT NOT NULL,
        [vnpay_txn_ref] NVARCHAR(100) UNIQUE,
        [amount] BIGINT NOT NULL,
        [payment_status] NVARCHAR(30) NOT NULL,
        [vnpay_response_code] NVARCHAR(10),
        [payment_method] NVARCHAR(50),
        [bank_code] NVARCHAR(20),
        [transaction_info] NVARCHAR(500),
        [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
        [paid_at] DATETIME,
        [tier_upgraded_to] INT,
        CONSTRAINT [FK_PaymentTransaction_User] FOREIGN KEY ([user_id]) 
            REFERENCES [dbo].[Users]([user_id]) ON DELETE CASCADE,
        CONSTRAINT [FK_PaymentTransaction_Package] FOREIGN KEY ([package_id]) 
            REFERENCES [dbo].[SubscriptionPackage]([package_id])
    );
    PRINT 'Table PaymentTransaction created successfully.';
END
ELSE
BEGIN
    PRINT 'Table PaymentTransaction already exists.';
END
GO

-- =====================================================
-- 3. Add Tier Columns to EmployerProfile Table
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[EmployerProfile]') 
               AND name = 'tier_level')
BEGIN
    ALTER TABLE [dbo].[EmployerProfile]
    ADD [tier_level] INT DEFAULT 1;
    PRINT 'Column tier_level added to EmployerProfile table.';
END
ELSE
BEGIN
    PRINT 'Column tier_level already exists in EmployerProfile table.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[EmployerProfile]') 
               AND name = 'subscription_expires_at')
BEGIN
    ALTER TABLE [dbo].[EmployerProfile]
    ADD [subscription_expires_at] DATETIME;
    PRINT 'Column subscription_expires_at added to EmployerProfile table.';
END
ELSE
BEGIN
    PRINT 'Column subscription_expires_at already exists in EmployerProfile table.';
END
GO

-- =====================================================
-- 4. Insert Sample Subscription Packages
-- =====================================================
IF NOT EXISTS (SELECT * FROM [dbo].[SubscriptionPackage] WHERE [package_name] = N'Basic')
BEGIN
    INSERT INTO [dbo].[SubscriptionPackage] 
    ([package_name], [description], [price], [duration_days], [tier_level], [features], [is_active], [created_at])
    VALUES 
    (N'Basic', 
     N'Gói cơ bản dành cho doanh nghiệp nhỏ', 
     199000, 
     30, 
     1, 
     N'{"max_jobs": 100, "basic_support": true, "view_candidates": true}',
     1,
     GETDATE());
    PRINT 'Basic package inserted.';
END
GO

IF NOT EXISTS (SELECT * FROM [dbo].[SubscriptionPackage] WHERE [package_name] = N'Premium')
BEGIN
    INSERT INTO [dbo].[SubscriptionPackage] 
    ([package_name], [description], [price], [duration_days], [tier_level], [features], [is_active], [created_at])
    VALUES 
    (N'Premium', 
     N'Gói phổ biến nhất dành cho doanh nghiệp vừa', 
     499000, 
     30, 
     2, 
     N'{"max_jobs": 1000, "featured_jobs": true, "priority_search": true, "analytics": true, "basic_support": true}',
     1,
     GETDATE());
    PRINT 'Premium package inserted.';
END
GO

IF NOT EXISTS (SELECT * FROM [dbo].[SubscriptionPackage] WHERE [package_name] = N'Enterprise')
BEGIN
    INSERT INTO [dbo].[SubscriptionPackage] 
    ([package_name], [description], [price], [duration_days], [tier_level], [features], [is_active], [created_at])
    VALUES 
    (N'Enterprise', 
     N'Gói cao cấp dành cho doanh nghiệp lớn', 
     999000, 
     30, 
     3, 
     N'{"max_jobs": 10000, "featured_jobs": true, "priority_search": true, "ai_matching": true, "multi_branch": true, "vip_support": true, "analytics": true}',
     1,
     GETDATE());
    PRINT 'Enterprise package inserted.';
END
GO

-- =====================================================
-- 5. Create Indexes for Better Performance
-- =====================================================
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_PaymentTransaction_VnpayTxnRef')
BEGIN
    CREATE INDEX IX_PaymentTransaction_VnpayTxnRef 
    ON [dbo].[PaymentTransaction] ([vnpay_txn_ref]);
    PRINT 'Index IX_PaymentTransaction_VnpayTxnRef created.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_PaymentTransaction_UserId')
BEGIN
    CREATE INDEX IX_PaymentTransaction_UserId 
    ON [dbo].[PaymentTransaction] ([user_id]);
    PRINT 'Index IX_PaymentTransaction_UserId created.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_PaymentTransaction_Status')
BEGIN
    CREATE INDEX IX_PaymentTransaction_Status 
    ON [dbo].[PaymentTransaction] ([payment_status]);
    PRINT 'Index IX_PaymentTransaction_Status created.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_SubscriptionPackage_Active')
BEGIN
    CREATE INDEX IX_SubscriptionPackage_Active 
    ON [dbo].[SubscriptionPackage] ([is_active]);
    PRINT 'Index IX_SubscriptionPackage_Active created.';
END
GO

-- =====================================================
-- 6. Update Existing Employers to have default tier
-- =====================================================
UPDATE [dbo].[EmployerProfile]
SET [tier_level] = 1
WHERE [tier_level] IS NULL;
PRINT 'Existing employers updated with default tier level.';
GO

-- =====================================================
-- MIGRATION COMPLETE
-- =====================================================
PRINT '========================================';
PRINT 'VNPay Payment Integration Migration Complete!';
PRINT '========================================';
PRINT 'Next Steps:';
PRINT '1. Update VNPay credentials in application.properties';
PRINT '2. Test payment flow in sandbox environment';
PRINT '3. Configure security for production';
GO

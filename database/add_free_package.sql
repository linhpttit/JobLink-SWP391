-- =====================================================
-- ADD FREE TIER PACKAGE
-- =====================================================
-- This script adds a Free tier package (tier 0)
-- Run this script on your joblink database

USE joblink;
GO

-- Insert Free Package
IF NOT EXISTS (SELECT * FROM [dbo].[SubscriptionPackage] WHERE [package_name] = N'Free')
BEGIN
    INSERT INTO [dbo].[SubscriptionPackage] 
    ([package_name], [description], [price], [duration_days], [tier_level], [features], [is_active], [created_at])
    VALUES 
    (N'Free', 
     N'Gói miễn phí cho người mới bắt đầu', 
     0, 
     365, 
     0, 
     N'{"max_jobs": 10, "basic_support": false, "view_candidates": true, "limited_features": true}',
     1,
     GETDATE());
    PRINT 'Free package inserted successfully.';
END
ELSE
BEGIN
    PRINT 'Free package already exists.';
END
GO

-- Update tier_level default to 0 for new employers
IF EXISTS (SELECT * FROM sys.columns 
           WHERE object_id = OBJECT_ID(N'[dbo].[EmployerProfile]') 
           AND name = 'tier_level')
BEGIN
    -- Update default constraint
    DECLARE @ConstraintName nvarchar(200)
    SELECT @ConstraintName = Name 
    FROM sys.default_constraints
    WHERE parent_object_id = OBJECT_ID(N'[dbo].[EmployerProfile]')
    AND parent_column_id = (
        SELECT column_id 
        FROM sys.columns 
        WHERE object_id = OBJECT_ID(N'[dbo].[EmployerProfile]') 
        AND name = 'tier_level'
    )
    
    IF @ConstraintName IS NOT NULL
    BEGIN
        EXEC('ALTER TABLE [dbo].[EmployerProfile] DROP CONSTRAINT ' + @ConstraintName)
        PRINT 'Dropped old default constraint on tier_level.';
    END
    
    ALTER TABLE [dbo].[EmployerProfile]
    ADD CONSTRAINT DF_EmployerProfile_TierLevel DEFAULT 0 FOR [tier_level];
    PRINT 'New default constraint set to tier 0 (Free).';
END
GO

-- Update existing employers with tier 1 to keep them at Basic
-- (They've been using the system, so they get Basic tier)
PRINT 'Existing employers remain at their current tier level.';
GO

PRINT '========================================';
PRINT 'Free package added successfully!';
PRINT 'New employers will start with Free tier (tier 0)';
PRINT '========================================';
GO

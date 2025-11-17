-- =====================================================
-- UPDATE PACKAGE JOB POSTING LIMITS
-- =====================================================
-- This script updates the max_jobs for all packages
-- Free: 10, Basic: 100, Premium: 1000, Enterprise: 10000

USE joblink;
GO

-- Update Free package (tier 0)
IF EXISTS (SELECT * FROM [dbo].[SubscriptionPackage] WHERE [tier_level] = 0)
BEGIN
    UPDATE [dbo].[SubscriptionPackage]
    SET [features] = N'{"max_jobs": 10, "basic_support": false, "view_candidates": true, "limited_features": true}',
        [updated_at] = GETDATE()
    WHERE [tier_level] = 0;
    PRINT 'Free package updated: max_jobs = 10';
END
GO

-- Update Basic package (tier 1)
IF EXISTS (SELECT * FROM [dbo].[SubscriptionPackage] WHERE [tier_level] = 1)
BEGIN
    UPDATE [dbo].[SubscriptionPackage]
    SET [features] = N'{"max_jobs": 100, "basic_support": true, "view_candidates": true}',
        [updated_at] = GETDATE()
    WHERE [tier_level] = 1;
    PRINT 'Basic package updated: max_jobs = 100';
END
GO

-- Update Premium package (tier 2)
IF EXISTS (SELECT * FROM [dbo].[SubscriptionPackage] WHERE [tier_level] = 2)
BEGIN
    UPDATE [dbo].[SubscriptionPackage]
    SET [features] = N'{"max_jobs": 1000, "featured_jobs": true, "priority_search": true, "analytics": true, "basic_support": true}',
        [updated_at] = GETDATE()
    WHERE [tier_level] = 2;
    PRINT 'Premium package updated: max_jobs = 1000';
END
GO

-- Update Enterprise package (tier 3)
IF EXISTS (SELECT * FROM [dbo].[SubscriptionPackage] WHERE [tier_level] = 3)
BEGIN
    UPDATE [dbo].[SubscriptionPackage]
    SET [features] = N'{"max_jobs": 10000, "featured_jobs": true, "priority_search": true, "ai_matching": true, "multi_branch": true, "vip_support": true, "analytics": true}',
        [updated_at] = GETDATE()
    WHERE [tier_level] = 3;
    PRINT 'Enterprise package updated: max_jobs = 10000';
END
GO

-- Verify updates
PRINT '========================================';
PRINT 'Verification - Current package limits:';
PRINT '========================================';

SELECT 
    [package_name],
    [tier_level],
    [price],
    [features],
    [updated_at]
FROM [dbo].[SubscriptionPackage]
ORDER BY [tier_level];
GO

PRINT '========================================';
PRINT 'Package limits updated successfully!';
PRINT 'Free: 10 jobs';
PRINT 'Basic: 100 jobs';
PRINT 'Premium: 1,000 jobs';
PRINT 'Enterprise: 10,000 jobs';
PRINT '========================================';
GO

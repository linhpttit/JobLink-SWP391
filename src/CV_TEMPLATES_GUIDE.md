# CV Templates Implementation Guide

## Overview
The CV Templates feature allows premium users (LegendStar-Premium package) to access, view, and export professional CV templates.

## User Flow

### 1. Non-Premium Users
- Click "CV Templates" in user menu
- **Redirected to Premium Titanium page** (`/jobseeker/premium`)
- Must purchase LegendStar-Premium package to access CV templates

### 2. Premium Users (LegendStar-Premium)
- Click "CV Templates" in user menu
- View gallery of available CV templates (`/jobseeker/cv-templates`)
- Click "View & Edit" to see template with their profile data
- Click export button to generate PDF (demo implementation)

## Routes

| Route | Description | Access |
|-------|-------------|--------|
| `/jobseeker/cv-templates` | CV templates gallery | Premium only |
| `/jobseeker/cv-templates/{id}` | View specific template | Premium only |
| `/jobseeker/cv-templates/{id}/export` | Export CV as PDF | Premium only |

## Premium Check Logic

\`\`\`java
boolean hasCVAccess = premiumService.hasFeature(user.getUserId(), "cv_templates");
if (!hasCVAccess) {
return "redirect:/jobseeker/premium";
}
\`\`\`

## Database Tables Used

- **CVTemplates**: Stores template designs (HTML/CSS)
- **CVExports**: Records export history
- **PremiumSubscriptions**: Checks user's premium status
- **PremiumPackages**: Defines which packages have CV access

## Features Implemented

1. **Premium Access Control**: Only LegendStar-Premium users can access
2. **Template Gallery**: Display all available CV templates
3. **Template Preview**: View template with user's actual profile data
4. **Export Functionality**: Generate and download CV as PDF (demo)
5. **Export History**: Track all CV exports per user

## Testing

### Test Accounts (from demo data)
- **seeker1@test.com** / Password123! - Has LegendStar-Premium
- **seeker2@test.com** / Password123! - No premium (should redirect)
- **seeker3@test.com** / Password123! - No premium (should redirect)

### Test Flow
1. Login as seeker2 (non-premium)
2. Click "CV Templates" → Should redirect to Premium page
3. Purchase LegendStar-Premium package
4. Click "CV Templates" → Should see template gallery
5. Click "View & Edit" on any template
6. Click "Export as PDF" → Should show success message

## Notes

- PDF generation is currently a demo implementation
- In production, integrate a proper PDF library (iText, Flying Saucer, etc.)
- Export URLs are placeholders for demo purposes
- Real implementation should store PDFs in cloud storage (S3, Azure Blob, etc.)

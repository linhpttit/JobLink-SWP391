-- =====================================================
-- MOCKUP HOÀN CHỈNH: 10 Jobs + 50 Candidates với đầy đủ thông tin
-- =====================================================
USE [JobLink_DB];
GO

PRINT N'========================================';
PRINT N'🚀 BẮT ĐẦU TẠO MOCKUP DATA';
PRINT N'========================================';
PRINT N'';

-- =====================================================
-- BƯỚC 1: XÓA DỮ LIỆU CŨ
-- =====================================================
PRINT N'🔄 Xóa dữ liệu mockup cũ...';

DELETE FROM Applications WHERE seeker_id >= 1001;
DELETE FROM SeekerSkills WHERE seeker_id >= 1001;
DELETE FROM Certificates WHERE seeker_id >= 1001;
DELETE FROM Experience WHERE seeker_id >= 1001;
DELETE FROM Education WHERE seeker_id >= 1001;
DELETE FROM JobSeekerProfile WHERE seeker_id >= 1001;
DELETE FROM Users WHERE user_id >= 1001 AND role = 'seeker';

DELETE FROM Applications WHERE job_id IN (SELECT job_id FROM JobsPosting WHERE employer_id = 1);
DELETE FROM JobSkills WHERE job_id IN (SELECT job_id FROM JobsPosting WHERE employer_id = 1);
DELETE FROM JobsPosting WHERE employer_id = 1;

PRINT N'✅ Đã xóa dữ liệu cũ';
PRINT N'';
GO

-- =====================================================
-- BƯỚC 2: TẠO SKILLS CẦN THIẾT
-- =====================================================
PRINT N'🔧 Tạo Skills...';

DECLARE @SkillsNeeded TABLE (skill_name NVARCHAR(100));
INSERT INTO @SkillsNeeded VALUES 
(N'React'), (N'Node.js'), (N'MongoDB'), (N'JavaScript'), (N'TypeScript'),
(N'Vue.js'), (N'Angular'), (N'HTML/CSS'), (N'Responsive Design'),
(N'Java'), (N'Spring Boot'), (N'Microservices'), (N'PostgreSQL'), (N'MySQL'),
(N'React Native'), (N'Flutter'), (N'iOS'), (N'Android'), (N'Mobile UI/UX'),
(N'AWS'), (N'Docker'), (N'Kubernetes'), (N'CI/CD'), (N'Jenkins'),
(N'Figma'), (N'Adobe XD'), (N'Sketch'), (N'UI Design'), (N'UX Research'),
(N'Python'), (N'SQL'), (N'Power BI'), (N'Tableau'), (N'Data Analysis'),
(N'Selenium'), (N'Cypress'), (N'JUnit'), (N'Test Automation'), (N'Manual Testing'),
(N'Agile'), (N'Scrum'), (N'Product Management'), (N'Roadmap Planning'),
(N'Business Analysis'), (N'Requirements Gathering'), (N'Documentation');

DECLARE @SkillName NVARCHAR(100);
DECLARE skill_cursor CURSOR FOR SELECT skill_name FROM @SkillsNeeded;
OPEN skill_cursor;
FETCH NEXT FROM skill_cursor INTO @SkillName;

WHILE @@FETCH_STATUS = 0
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Skills WHERE [name] = @SkillName)
    BEGIN
        INSERT INTO Skills ([name]) VALUES (@SkillName);
    END
    FETCH NEXT FROM skill_cursor INTO @SkillName;
END

CLOSE skill_cursor;
DEALLOCATE skill_cursor;

PRINT N'✅ Đã tạo Skills';
PRINT N'';
GO

-- =====================================================
-- BƯỚC 3: TẠO 10 JOB POSTINGS
-- =====================================================
PRINT N'📋 Tạo 10 Job Postings...';

DECLARE @EmployerId INT = 1;
DECLARE @CategoryId INT;
DECLARE @SkillId INT;

SELECT TOP 1 @CategoryId = category_id FROM Categories;
IF @CategoryId IS NULL
BEGIN
    INSERT INTO Categories ([name], [description]) VALUES (N'Công nghệ thông tin', N'IT');
    SET @CategoryId = SCOPE_IDENTITY();
END

SELECT TOP 1 @SkillId = skill_id FROM Skills;

-- Job 1: Full-stack Developer
INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'Full-stack Developer', N'Tuyển Full-stack Developer', @SkillId, @CategoryId, 1,
    15000000, 30000000, N'Full-time', N'2-3', N'ACTIVE', DATEADD(DAY, -10, GETDATE()), DATEADD(DAY, 30, GETDATE()),
    N'Phát triển ứng dụng web full-stack với React, Node.js, MongoDB',
    N'- 2-3 năm kinh nghiệm\n- React, Node.js, MongoDB\n- JavaScript/TypeScript\n- Git, Agile',
    N'- Lương 15-30 triệu\n- Bảo hiểm\n- Thưởng dự án',
    3, N'HR Dept', 'hr@company.com', '0987654321');

-- Job 2-10 tương tự...
INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'Frontend Developer', N'Tuyển Frontend Developer', @SkillId, @CategoryId, 2,
    12000000, 25000000, N'Full-time', N'1-3', N'ACTIVE', DATEADD(DAY, -8, GETDATE()), DATEADD(DAY, 35, GETDATE()),
    N'Xây dựng giao diện web với React/Vue.js',
    N'- 1-3 năm kinh nghiệm\n- React/Vue.js\n- HTML/CSS\n- UI/UX',
    N'- Lương 12-25 triệu\n- Bảo hiểm\n- Review 6 tháng',
    2, N'HR Dept', 'hr@company.com', '0987654321');

INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'Backend Developer', N'Tuyển Backend Developer', @SkillId, @CategoryId, 1,
    18000000, 35000000, N'Full-time', N'3-5', N'ACTIVE', DATEADD(DAY, -15, GETDATE()), DATEADD(DAY, 25, GETDATE()),
    N'Phát triển backend với Java Spring Boot',
    N'- 3-5 năm kinh nghiệm\n- Java, Spring Boot\n- Microservices\n- PostgreSQL',
    N'- Lương 18-35 triệu\n- Bảo hiểm cao cấp\n- Thưởng hiệu suất',
    2, N'HR Dept', 'hr@company.com', '0987654321');

INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'Mobile Developer', N'Tuyển Mobile Developer', @SkillId, @CategoryId, 2,
    16000000, 32000000, N'Full-time', N'2-4', N'ACTIVE', DATEADD(DAY, -12, GETDATE()), DATEADD(DAY, 28, GETDATE()),
    N'Phát triển app mobile với React Native/Flutter',
    N'- 2-4 năm kinh nghiệm\n- React Native/Flutter\n- Mobile UI/UX\n- App Store',
    N'- Lương 16-32 triệu\n- Bảo hiểm\n- Thiết bị',
    2, N'HR Dept', 'hr@company.com', '0987654321');

INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'DevOps Engineer', N'Tuyển DevOps Engineer', @SkillId, @CategoryId, 1,
    20000000, 40000000, N'Full-time', N'3-5', N'ACTIVE', DATEADD(DAY, -20, GETDATE()), DATEADD(DAY, 20, GETDATE()),
    N'Quản lý hạ tầng cloud, CI/CD với AWS, Docker',
    N'- 3-5 năm kinh nghiệm\n- AWS/Azure\n- Docker, Kubernetes\n- CI/CD, Jenkins',
    N'- Lương 20-40 triệu\n- Bảo hiểm cao cấp\n- Chứng chỉ AWS',
    1, N'HR Dept', 'hr@company.com', '0987654321');

INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'UI/UX Designer', N'Tuyển UI/UX Designer', @SkillId, @CategoryId, 3,
    14000000, 28000000, N'Full-time', N'2-3', N'ACTIVE', DATEADD(DAY, -7, GETDATE()), DATEADD(DAY, 33, GETDATE()),
    N'Thiết kế giao diện cho web/mobile app',
    N'- 2-3 năm kinh nghiệm\n- Figma, Adobe XD\n- Portfolio\n- UX Research',
    N'- Lương 14-28 triệu\n- Bảo hiểm\n- Thiết bị Apple',
    2, N'HR Dept', 'hr@company.com', '0987654321');

INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'Data Analyst', N'Tuyển Data Analyst', @SkillId, @CategoryId, 1,
    15000000, 30000000, N'Full-time', N'2-4', N'ACTIVE', DATEADD(DAY, -5, GETDATE()), DATEADD(DAY, 35, GETDATE()),
    N'Phân tích dữ liệu với Python, SQL, Power BI',
    N'- 2-4 năm kinh nghiệm\n- Python, SQL\n- Power BI/Tableau\n- Statistics',
    N'- Lương 15-30 triệu\n- Bảo hiểm\n- Đào tạo Data Science',
    2, N'HR Dept', 'hr@company.com', '0987654321');

INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'QA Engineer', N'Tuyển QA Engineer', @SkillId, @CategoryId, 2,
    13000000, 26000000, N'Full-time', N'1-3', N'ACTIVE', DATEADD(DAY, -6, GETDATE()), DATEADD(DAY, 34, GETDATE()),
    N'Kiểm thử phần mềm, automation testing',
    N'- 1-3 năm kinh nghiệm\n- Selenium/Cypress\n- Manual Testing\n- JUnit',
    N'- Lương 13-26 triệu\n- Bảo hiểm\n- Đào tạo Automation',
    3, N'HR Dept', 'hr@company.com', '0987654321');

INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'Product Manager', N'Tuyển Product Manager', @SkillId, @CategoryId, 1,
    22000000, 45000000, N'Full-time', N'4-6', N'ACTIVE', DATEADD(DAY, -18, GETDATE()), DATEADD(DAY, 22, GETDATE()),
    N'Quản lý sản phẩm, roadmap planning',
    N'- 4-6 năm kinh nghiệm\n- Agile/Scrum\n- Fintech\n- Leadership',
    N'- Lương 22-45 triệu\n- Bảo hiểm cao cấp\n- Thưởng sản phẩm',
    1, N'HR Dept', 'hr@company.com', '0987654321');

INSERT INTO JobsPosting (employer_id, position, [title], skill_id, category_id, province_id, salary_min, salary_max, 
    work_type, year_experience, [status], posted_at, submission_deadline, 
    job_desc, job_requirements, benefits, hiring_number, contact_name, contact_email, contact_phone)
VALUES (@EmployerId, N'Business Analyst', N'Tuyển Business Analyst', @SkillId, @CategoryId, 2,
    14000000, 28000000, N'Full-time', N'2-4', N'ACTIVE', DATEADD(DAY, -9, GETDATE()), DATEADD(DAY, 31, GETDATE()),
    N'Phân tích nghiệp vụ, thu thập yêu cầu',
    N'- 2-4 năm kinh nghiệm\n- Requirements Gathering\n- Documentation\n- UML, BPMN',
    N'- Lương 14-28 triệu\n- Bảo hiểm\n- Đào tạo BA',
    2, N'HR Dept', 'hr@company.com', '0987654321');

PRINT N'✅ Đã tạo 10 Job Postings';
PRINT N'';
GO

-- =====================================================
-- BƯỚC 4: TẠO 50 USERS
-- =====================================================
PRINT N'👥 Tạo 50 Users...';

DECLARE @i INT = 1;
WHILE @i <= 50
BEGIN
    INSERT INTO Users (email, username, password_hash, [role], [enabled], created_at)
    VALUES (
        CONCAT('candidate', @i, '@joblink.com'),
        CONCAT('candidate', @i),
        '$2a$10$dummyHashForTesting',
        'seeker', 1, GETDATE()
    );
    SET @i = @i + 1;
END

PRINT N'✅ Đã tạo 50 Users';
PRINT N'';
GO

-- =====================================================
-- BƯỚC 5: TẠO 50 JOB SEEKER PROFILES
-- =====================================================
PRINT N'📝 Tạo 50 Job Seeker Profiles...';

DECLARE @FirstNames TABLE (name NVARCHAR(50));
INSERT INTO @FirstNames VALUES 
(N'Nguyễn Văn'), (N'Trần Thị'), (N'Lê Văn'), (N'Phạm Thị'), (N'Hoàng Văn'),
(N'Phan Thị'), (N'Vũ Văn'), (N'Đặng Thị'), (N'Bùi Văn'), (N'Đỗ Thị');

DECLARE @LastNames TABLE (name NVARCHAR(50));
INSERT INTO @LastNames VALUES 
(N'An'), (N'Bình'), (N'Cường'), (N'Dũng'), (N'Hải'), (N'Hùng'), (N'Khoa'), (N'Linh'),
(N'Long'), (N'Minh'), (N'Nam'), (N'Phong'), (N'Quân'), (N'Sơn'), (N'Tài');

DECLARE @Locations TABLE (name NVARCHAR(100));
INSERT INTO @Locations VALUES (N'Hà Nội'), (N'Hồ Chí Minh'), (N'Đà Nẵng');

DECLARE @i INT = 1;
WHILE @i <= 50
BEGIN
    DECLARE @UserId INT;
    SELECT @UserId = user_id FROM Users WHERE email = CONCAT('candidate', @i, '@joblink.com');
    
    IF @UserId IS NOT NULL
    BEGIN
        INSERT INTO JobSeekerProfile (
            user_id, fullname, gender, [location], headline,
            experience_years, email, phone, dob, degree,
            completion_percentage, updated_at
        )
        VALUES (
            @UserId,
            (SELECT TOP 1 name FROM @FirstNames ORDER BY NEWID()) + ' ' + 
            (SELECT TOP 1 name FROM @LastNames ORDER BY NEWID()),
            CASE @i % 3 WHEN 0 THEN 'female' WHEN 1 THEN 'male' ELSE 'other' END,
            (SELECT TOP 1 name FROM @Locations ORDER BY NEWID()),
            CASE 
                WHEN @i <= 10 THEN N'Full-stack Developer'
                WHEN @i <= 20 THEN N'Frontend Developer'
                WHEN @i <= 25 THEN N'Backend Developer'
                WHEN @i <= 30 THEN N'Mobile Developer'
                WHEN @i <= 33 THEN N'DevOps Engineer'
                WHEN @i <= 38 THEN N'UI/UX Designer'
                WHEN @i <= 43 THEN N'Data Analyst'
                WHEN @i <= 48 THEN N'QA Engineer'
                WHEN @i = 49 THEN N'Product Manager'
                ELSE N'Business Analyst'
            END,
            (@i % 6) + 1,
            CONCAT('candidate', @i, '@joblink.com'),
            CONCAT('0912345', RIGHT('000' + CAST(@i AS VARCHAR), 3)),
            DATEADD(YEAR, -(25 + @i % 10), GETDATE()),
            CASE 
                WHEN @i % 4 = 0 THEN N'Thạc sĩ Công nghệ Thông tin'
                WHEN @i % 4 = 1 THEN N'Cử nhân Khoa học Máy tính'
                WHEN @i % 4 = 2 THEN N'Kỹ sư Phần mềm'
                ELSE N'Cử nhân Công nghệ Thông tin'
            END,
            85, GETDATE()
        );
    END
    
    SET @i = @i + 1;
END

PRINT N'✅ Đã tạo 50 Profiles';
PRINT N'';
GO

-- =====================================================
-- BƯỚC 6: TẠO EDUCATION
-- =====================================================
PRINT N'🎓 Tạo Education...';

DECLARE @Universities TABLE (name NVARCHAR(255));
INSERT INTO @Universities VALUES 
(N'Đại học Bách Khoa Hà Nội'), (N'Đại học Công nghệ - ĐHQGHN'),
(N'Đại học FPT'), (N'Đại học Bách Khoa TP.HCM');

DECLARE @i INT = 1;
WHILE @i <= 50
BEGIN
    DECLARE @SeekerId INT;
    SELECT @SeekerId = seeker_id FROM JobSeekerProfile 
    WHERE user_id = (SELECT user_id FROM Users WHERE email = CONCAT('candidate', @i, '@joblink.com'));
    
    IF @SeekerId IS NOT NULL
    BEGIN
        INSERT INTO Education (seeker_id, [university], degree_level, start_date, graduation_date, [description])
        VALUES (
            @SeekerId,
            (SELECT TOP 1 name FROM @Universities ORDER BY NEWID()),
            CASE WHEN @i % 4 = 0 THEN N'Thạc sĩ' ELSE N'Cử nhân' END,
            DATEADD(YEAR, -(8 + @i % 5), GETDATE()),
            DATEADD(YEAR, -(4 + @i % 5), GETDATE()),
            N'Chuyên ngành Công nghệ Thông tin, GPA: ' + CAST((3.0 + (@i % 10) * 0.1) AS NVARCHAR(3))
        );
    END
    
    SET @i = @i + 1;
END

PRINT N'✅ Đã tạo Education';
PRINT N'';
GO

-- =====================================================
-- BƯỚC 7: TẠO EXPERIENCE
-- =====================================================
PRINT N'💼 Tạo Experience...';

DECLARE @Companies TABLE (name NVARCHAR(255));
INSERT INTO @Companies VALUES 
(N'FPT Software'), (N'Viettel Solutions'), (N'VNG Corporation'),
(N'Tiki'), (N'Shopee Vietnam'), (N'Grab Vietnam');

DECLARE @i INT = 1;
WHILE @i <= 50
BEGIN
    DECLARE @SeekerId INT;
    SELECT @SeekerId = seeker_id FROM JobSeekerProfile 
    WHERE user_id = (SELECT user_id FROM Users WHERE email = CONCAT('candidate', @i, '@joblink.com'));
    
    IF @SeekerId IS NOT NULL
    BEGIN
        DECLARE @ExpYears INT = (@i % 6) + 1;
        
        INSERT INTO Experience (seeker_id, job_title, company_name, start_date, end_date, project_link)
        VALUES (
            @SeekerId,
            CASE 
                WHEN @i <= 10 THEN N'Full-stack Developer'
                WHEN @i <= 20 THEN N'Frontend Developer'
                WHEN @i <= 25 THEN N'Backend Developer'
                WHEN @i <= 30 THEN N'Mobile Developer'
                WHEN @i <= 33 THEN N'DevOps Engineer'
                WHEN @i <= 38 THEN N'UI/UX Designer'
                WHEN @i <= 43 THEN N'Data Analyst'
                WHEN @i <= 48 THEN N'QA Engineer'
                WHEN @i = 49 THEN N'Product Manager'
                ELSE N'Business Analyst'
            END,
            (SELECT TOP 1 name FROM @Companies ORDER BY NEWID()),
            DATEADD(YEAR, -@ExpYears, GETDATE()),
            CASE WHEN @i % 5 = 0 THEN NULL ELSE DATEADD(MONTH, -3, GETDATE()) END,
            CASE WHEN @i % 3 = 0 THEN CONCAT('https://github.com/candidate', @i) ELSE NULL END
        );
    END
    
    SET @i = @i + 1;
END

PRINT N'✅ Đã tạo Experience';
PRINT N'';
GO

-- =====================================================
-- BƯỚC 8: TẠO CERTIFICATES
-- =====================================================
PRINT N'📜 Tạo Certificates...';

DECLARE @Orgs TABLE (name NVARCHAR(255));
INSERT INTO @Orgs VALUES 
(N'AWS'), (N'Google'), (N'Microsoft'), (N'Oracle'), (N'Coursera'), (N'Udemy');

DECLARE @i INT = 1;
WHILE @i <= 50
BEGIN
    DECLARE @SeekerId INT;
    SELECT @SeekerId = seeker_id FROM JobSeekerProfile 
    WHERE user_id = (SELECT user_id FROM Users WHERE email = CONCAT('candidate', @i, '@joblink.com'));
    
    IF @SeekerId IS NOT NULL
    BEGIN
        IF @i % 2 = 0
        BEGIN
            INSERT INTO Certificates (seeker_id, issuing_organization, certificate_image_url, year_of_completion)
            VALUES (
                @SeekerId,
                (SELECT TOP 1 name FROM @Orgs ORDER BY NEWID()),
                CONCAT('https://certificates.com/cert_', @i, '.jpg'),
                YEAR(GETDATE()) - (@i % 3)
            );
        END
    END
    
    SET @i = @i + 1;
END

PRINT N'✅ Đã tạo Certificates';
PRINT N'';
GO

-- =====================================================
-- BƯỚC 9: TẠO SEEKER SKILLS
-- =====================================================
PRINT N'🛠️ Tạo Seeker Skills...';

DECLARE @i INT = 1;
WHILE @i <= 50
BEGIN
    DECLARE @SeekerId INT;
    SELECT @SeekerId = seeker_id FROM JobSeekerProfile 
    WHERE user_id = (SELECT user_id FROM Users WHERE email = CONCAT('candidate', @i, '@joblink.com'));
    
    IF @SeekerId IS NOT NULL
    BEGIN
        -- Skill 1
        INSERT INTO SeekerSkills (seeker_id, skill_name, years_of_experience, [description])
        VALUES (
            @SeekerId,
            CASE 
                WHEN @i <= 10 THEN N'React'
                WHEN @i <= 20 THEN N'Vue.js'
                WHEN @i <= 25 THEN N'Java'
                WHEN @i <= 30 THEN N'React Native'
                WHEN @i <= 33 THEN N'AWS'
                WHEN @i <= 38 THEN N'Figma'
                WHEN @i <= 43 THEN N'Python'
                WHEN @i <= 48 THEN N'Selenium'
                WHEN @i = 49 THEN N'Agile'
                ELSE N'Business Analysis'
            END,
            (@i % 5) + 1,
            N'Thành thạo và có nhiều dự án thực tế'
        );
        
        -- Skill 2
        INSERT INTO SeekerSkills (seeker_id, skill_name, years_of_experience, [description])
        VALUES (
            @SeekerId,
            CASE 
                WHEN @i <= 10 THEN N'Node.js'
                WHEN @i <= 20 THEN N'HTML/CSS'
                WHEN @i <= 25 THEN N'Spring Boot'
                WHEN @i <= 30 THEN N'Flutter'
                WHEN @i <= 33 THEN N'Docker'
                WHEN @i <= 38 THEN N'Adobe XD'
                WHEN @i <= 43 THEN N'SQL'
                WHEN @i <= 48 THEN N'Cypress'
                WHEN @i = 49 THEN N'Scrum'
                ELSE N'Documentation'
            END,
            (@i % 4) + 1,
            N'Có kinh nghiệm thực tế'
        );
    END
    
    SET @i = @i + 1;
END

PRINT N'✅ Đã tạo Seeker Skills';
PRINT N'';
GO

-- =====================================================
-- BƯỚC 10: TẠO APPLICATIONS
-- =====================================================
PRINT N'📨 Tạo Applications...';

DECLARE @JobIds TABLE (job_id INT, position NVARCHAR(150));
INSERT INTO @JobIds 
SELECT job_id, position FROM JobsPosting WHERE employer_id = 1 AND [status] = 'ACTIVE';

DECLARE @i INT = 1;
WHILE @i <= 50
BEGIN
    DECLARE @SeekerId INT;
    SELECT @SeekerId = seeker_id FROM JobSeekerProfile 
    WHERE user_id = (SELECT user_id FROM Users WHERE email = CONCAT('candidate', @i, '@joblink.com'));
    
    IF @SeekerId IS NOT NULL
    BEGIN
        -- Mỗi ứng viên apply 2-4 jobs ngẫu nhiên
        DECLARE @NumApps INT = 2 + (@i % 3);
        DECLARE @AppCount INT = 0;
        
        WHILE @AppCount < @NumApps
        BEGIN
            DECLARE @JobId INT;
            DECLARE @JobPos NVARCHAR(150);
            
            SELECT TOP 1 @JobId = job_id, @JobPos = position 
            FROM @JobIds 
            ORDER BY NEWID();
            
            IF NOT EXISTS (SELECT 1 FROM Applications WHERE job_id = @JobId AND seeker_id = @SeekerId)
            BEGIN
                DECLARE @Status NVARCHAR(20);
                DECLARE @Rand INT = ABS(CHECKSUM(NEWID())) % 100;
                
                SET @Status = CASE 
                    WHEN @Rand < 30 THEN 'submitted'
                    WHEN @Rand < 55 THEN 'reviewed'
                    WHEN @Rand < 70 THEN 'interviewed'
                    WHEN @Rand < 90 THEN 'hired'
                    ELSE 'rejected'
                END;
                
                DECLARE @AppliedDate DATETIME = DATEADD(DAY, -(ABS(CHECKSUM(NEWID())) % 60), GETDATE());
                
                INSERT INTO Applications (job_id, seeker_id, status, applied_at, last_status_at, cv_url, note)
                VALUES (
                    @JobId, @SeekerId, @Status, @AppliedDate,
                    CASE WHEN @Status IN ('reviewed','interviewed','hired','rejected') 
                    THEN DATEADD(DAY, ABS(CHECKSUM(NEWID())) % 7, @AppliedDate) ELSE NULL END,
                    CONCAT('cv_candidate', @i, '_', @JobPos, '.pdf'),
                    CASE @Status 
                        WHEN 'hired' THEN N'✅ Xuất sắc - ' + @JobPos
                        WHEN 'rejected' THEN N'❌ Không phù hợp - ' + @JobPos
                        WHEN 'interviewed' THEN N'⏳ Đang phỏng vấn - ' + @JobPos
                        WHEN 'reviewed' THEN N'👀 Đã xem CV - ' + @JobPos
                        ELSE N'📝 Đã nộp - ' + @JobPos
                    END
                );
                
                SET @AppCount = @AppCount + 1;
            END
        END
    END
    
    SET @i = @i + 1;
END

PRINT N'✅ Đã tạo Applications';
PRINT N'';
GO

-- =====================================================
-- THỐNG KÊ KẾT QUẢ
-- =====================================================
PRINT N'========================================';
PRINT N'📊 THỐNG KÊ KẾT QUẢ';
PRINT N'========================================';

DECLARE @U INT, @P INT, @A INT, @E INT, @Ex INT, @C INT, @S INT;
SELECT @U = COUNT(*) FROM Users WHERE user_id >= 1001;
SELECT @P = COUNT(*) FROM JobSeekerProfile WHERE seeker_id >= 1001;
SELECT @A = COUNT(*) FROM Applications WHERE seeker_id >= 1001;
SELECT @E = COUNT(*) FROM Education WHERE seeker_id >= 1001;
SELECT @Ex = COUNT(*) FROM Experience WHERE seeker_id >= 1001;
SELECT @C = COUNT(*) FROM Certificates WHERE seeker_id >= 1001;
SELECT @S = COUNT(*) FROM SeekerSkills WHERE seeker_id >= 1001;

PRINT N'✅ Users: ' + CAST(@U AS NVARCHAR);
PRINT N'✅ Profiles: ' + CAST(@P AS NVARCHAR);
PRINT N'✅ Applications: ' + CAST(@A AS NVARCHAR);
PRINT N'✅ Education: ' + CAST(@E AS NVARCHAR);
PRINT N'✅ Experience: ' + CAST(@Ex AS NVARCHAR);
PRINT N'✅ Certificates: ' + CAST(@C AS NVARCHAR);
PRINT N'✅ Skills: ' + CAST(@S AS NVARCHAR);
PRINT N'';

DECLARE @Sub INT, @Rev INT, @Int INT, @Hir INT, @Rej INT;
SELECT @Sub = COUNT(*) FROM Applications WHERE status = 'submitted' AND seeker_id >= 1001;
SELECT @Rev = COUNT(*) FROM Applications WHERE status = 'reviewed' AND seeker_id >= 1001;
SELECT @Int = COUNT(*) FROM Applications WHERE status = 'interviewed' AND seeker_id >= 1001;
SELECT @Hir = COUNT(*) FROM Applications WHERE status = 'hired' AND seeker_id >= 1001;
SELECT @Rej = COUNT(*) FROM Applications WHERE status = 'rejected' AND seeker_id >= 1001;

PRINT N'📈 Phân bố Status:';
PRINT N'   - Submitted: ' + CAST(@Sub AS NVARCHAR);
PRINT N'   - Reviewed: ' + CAST(@Rev AS NVARCHAR);
PRINT N'   - Interviewed: ' + CAST(@Int AS NVARCHAR);
PRINT N'   - Hired: ' + CAST(@Hir AS NVARCHAR) + N' ⭐';
PRINT N'   - Rejected: ' + CAST(@Rej AS NVARCHAR);
PRINT N'';
PRINT N'========================================';
PRINT N'🎉 HOÀN THÀNH!';
PRINT N'========================================';
GO

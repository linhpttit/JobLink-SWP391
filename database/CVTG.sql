

/* ==== Core: Users ==== */
IF OBJECT_ID('dbo.Users','U') IS NULL
BEGIN
  CREATE TABLE dbo.Users(
    user_id       INT IDENTITY(1,1) PRIMARY KEY,
    email         NVARCHAR(255) NOT NULL,
    username      NVARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    [role]        VARCHAR(30) NOT NULL,
    url_avt       NVARCHAR(500) NULL,
    [enabled]     BIT NOT NULL CONSTRAINT DF_Users_enabled DEFAULT (1),
    created_at    DATETIME2(3) NOT NULL CONSTRAINT DF_Users_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_Users_Email UNIQUE (email),
    CONSTRAINT UQ_Users_Username UNIQUE (username)
  );
END
GO

/* ==== Lookup: Categories, Skills ==== */
IF OBJECT_ID('dbo.Categories','U') IS NULL
BEGIN
  CREATE TABLE dbo.Categories(
    category_id  INT IDENTITY(1,1) PRIMARY KEY,
    [name]       NVARCHAR(100) NOT NULL,
    [description] NVARCHAR(255) NULL,
    CONSTRAINT UQ_Categories_Name UNIQUE([name])
  );
END
GO

IF OBJECT_ID('dbo.Skills','U') IS NULL
BEGIN
  CREATE TABLE dbo.Skills(
    skill_id INT IDENTITY(1,1) PRIMARY KEY,
    [name]   NVARCHAR(100) NOT NULL,
    CONSTRAINT UQ_Skills_Name UNIQUE([name])
  );
END
GO

/* ==== Profiles ==== */
IF OBJECT_ID('dbo.EmployerProfile','U') IS NULL
BEGIN
  CREATE TABLE dbo.EmployerProfile(
    employer_id   INT IDENTITY(1,1) PRIMARY KEY,
    user_id       INT NOT NULL,
    company_name  NVARCHAR(200) NOT NULL,
    industry      NVARCHAR(100) NULL,
    [location]    NVARCHAR(150) NULL,
    phone_number  NVARCHAR(20)  NULL,
    [description] NVARCHAR(MAX) NULL,
    CONSTRAINT UQ_EmployerProfile_User UNIQUE (user_id),
    CONSTRAINT FK_Employer_User FOREIGN KEY(user_id) REFERENCES dbo.Users(user_id)
  );
  CREATE INDEX IX_EmployerProfile_User ON dbo.EmployerProfile(user_id);
END
GO

IF OBJECT_ID('dbo.JobSeekerProfile','U') IS NULL
BEGIN
  CREATE TABLE dbo.JobSeekerProfile(
    seeker_id             INT IDENTITY(1,1) PRIMARY KEY,
    user_id               INT NOT NULL,
    fullname              NVARCHAR(150) NULL,
    gender                VARCHAR(10)  NULL,
    [location]            NVARCHAR(150) NULL,
    headline              NVARCHAR(200) NULL,
    experience_years      INT NULL,
    [about]               NVARCHAR(MAX) NULL,
    email                 NVARCHAR(150) NULL,
    phone                 NVARCHAR(30)  NULL,
    dob                   DATE NULL,
    avatar_url            NVARCHAR(300) NULL,
    -- (ĐÃ BỎ) educations/experiences/skills/languages/projects/certifications/awards
    updated_at            DATETIME2(3) NOT NULL CONSTRAINT DF_Seeker_updated_at DEFAULT SYSUTCDATETIME(),
    birthday              DATE NULL,
    degree                NVARCHAR(120) NULL,
    [address]             NVARCHAR(300) NULL,
    github_url            NVARCHAR(500) NULL,
    linkedin_url          NVARCHAR(500) NULL,
    website               NVARCHAR(500) NULL,
    completion_percentage INT NULL CONSTRAINT DF_Seeker_completion DEFAULT (0),
    CONSTRAINT UQ_Seeker_User UNIQUE(user_id),
    CONSTRAINT CK_Seeker_Exp CHECK (experience_years IS NULL OR experience_years >= 0),
    CONSTRAINT CK_Seeker_Completion CHECK (completion_percentage IS NULL OR completion_percentage BETWEEN 0 AND 100),
    CONSTRAINT CK_Seeker_Gender CHECK (gender IS NULL OR gender IN ('male','female','other')),
    CONSTRAINT FK_Seeker_User FOREIGN KEY(user_id) REFERENCES dbo.Users(user_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_Seeker_User ON dbo.JobSeekerProfile(user_id);
  CREATE INDEX IX_Seeker_Email ON dbo.JobSeekerProfile(email) WHERE email IS NOT NULL;
  CREATE INDEX IX_Seeker_Phone ON dbo.JobSeekerProfile(phone) WHERE phone IS NOT NULL;
END
GO

/* ==== Content: Blog / BlogPosts ==== */
IF OBJECT_ID('dbo.Blog','U') IS NULL
BEGIN
  CREATE TABLE dbo.Blog(
    blog_id        INT IDENTITY(1,1) PRIMARY KEY,
    category_id    INT NOT NULL,
    [title]        NVARCHAR(200) NOT NULL,
    [content]      NVARCHAR(MAX) NOT NULL,
    created_at     DATETIME2(3) NOT NULL CONSTRAINT DF_Blog_created DEFAULT SYSUTCDATETIME(),
    updated_at     DATETIME2(3) NULL,
    parent_blog_id INT NULL,
    CONSTRAINT FK_Blog_Category FOREIGN KEY(category_id) REFERENCES dbo.Categories(category_id),
    CONSTRAINT FK_Blog_Parent FOREIGN KEY(parent_blog_id) REFERENCES dbo.Blog(blog_id)
  );
  CREATE INDEX IX_Blog_Category ON dbo.Blog(category_id);
  CREATE INDEX IX_Blog_Parent ON dbo.Blog(parent_blog_id);
END
GO

IF OBJECT_ID('dbo.BlogPosts','U') IS NULL
BEGIN
  CREATE TABLE dbo.BlogPosts(
    post_id     INT IDENTITY(1,1) PRIMARY KEY,
    category_id INT NOT NULL,
    [title]     NVARCHAR(200) NOT NULL,
    [content]   NVARCHAR(MAX) NOT NULL,
    created_at  DATETIME2(3) NOT NULL CONSTRAINT DF_BlogPosts_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_BlogPosts_Category FOREIGN KEY(category_id) REFERENCES dbo.Categories(category_id)
  );
  CREATE INDEX IX_BlogPosts_Category ON dbo.BlogPosts(category_id);
END
GO

-- Bảng Provinces
CREATE TABLE dbo.Provinces (
    province_id INT IDENTITY(1,1) PRIMARY KEY,
    province_name NVARCHAR(100) NOT NULL,
    province_code NVARCHAR(20) NULL,
    created_at DATETIME2(3) DEFAULT SYSUTCDATETIME()
);

-- Bảng Districts
CREATE TABLE dbo.Districts (
    district_id INT IDENTITY(1,1) PRIMARY KEY,
    district_name NVARCHAR(100) NOT NULL,
    province_id INT NOT NULL,
    created_at DATETIME2(3) DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Districts_Provinces FOREIGN KEY (province_id)
        REFERENCES dbo.Provinces(province_id)
);
/* ==== Jobs & Skills mapping ==== */
IF OBJECT_ID('dbo.JobsPostings','U') IS NULL
BEGIN
  CREATE TABLE dbo.JobsPosting(
    job_id       INT IDENTITY(1,1) PRIMARY KEY,
    employer_id  INT NOT NULL,
	[status] NVARCHAR(50) DEFAULT 'ACTIVE',
    category_id  INT NULL,

    [title]      NVARCHAR(200) NOT NULL,
	skill_id  INT NOT NULL,

	province_id         INT NULL,
	district_id         INT NULL,
    street_address      NVARCHAR(255) NULL,

	year_experience NVARCHAR(150) NOT NULL,
	hiring_number INT NOT NULL,
	submission_deadline DATETIME NOT NULL,

	work_type  NVARCHAR(255) NOT NULL,
    salary_min   DECIMAL(12,2) NULL,
    salary_max   DECIMAL(12,2) NULL,

	job_desc NVARCHAR(MAX) NOT NULL,
	job_requirements NVARCHAR(MAX) NOT NULL,
	benefits NVARCHAR(MAX) NOT NULL,

	contact_name NVARCHAR(255) NOT NULL,
	contact_email VARCHAR(255) NOT NULL,
	contact_phone VARCHAR(10) NOT NULL,
    posted_at    DATETIME2(3) NOT NULL CONSTRAINT DF_Jobs_posted DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Jobs_Employer FOREIGN KEY(employer_id) REFERENCES dbo.EmployerProfile(employer_id) ON DELETE CASCADE,
    CONSTRAINT FK_Jobs_Category FOREIGN KEY(category_id) REFERENCES dbo.Categories(category_id),
    CONSTRAINT FK_Jobs_Skills FOREIGN KEY(skill_id) REFERENCES dbo.Skills(skill_id) ON DELETE CASCADE,
	CONSTRAINT FK_Jobs_Provinces FOREIGN KEY(province_id) REFERENCES dbo.Provinces(province_id) ON DELETE SET NULL,
	CONSTRAINT FK_Jobs_Districts FOREIGN KEY(district_id) REFERENCES dbo.Districts(district_id) ON DELETE SET NULL,

	CONSTRAINT CK_Jobs_SalaryMin CHECK (salary_min IS NULL OR salary_min >= 0),
    CONSTRAINT CK_Jobs_SalaryMax CHECK (salary_max IS NULL OR salary_max >= 0),
    CONSTRAINT CK_Jobs_SalaryRange CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_max >= salary_min)
  );
  CREATE INDEX IX_Jobs_Employer ON dbo.JobsPosting(employer_id);
  CREATE INDEX IX_Jobs_Category ON dbo.JobsPosting(category_id);
  CREATE INDEX IX_Jobs_PostedAt ON dbo.JobsPosting(posted_at);
END
GO
ALTER TABLE JobsPosting ADD position NVARCHAR(150) NULL;



IF OBJECT_ID('dbo.JobSkills','U') IS NULL
BEGIN
  CREATE TABLE dbo.JobSkills(
    job_id   INT NOT NULL,
    skill_id INT NOT NULL,
    CONSTRAINT PK_JobSkills PRIMARY KEY(job_id, skill_id),
    CONSTRAINT FK_JobSkills_Job   FOREIGN KEY(job_id)   REFERENCES dbo.JobsPosting(job_id),
    CONSTRAINT FK_JobSkills_Skill FOREIGN KEY(skill_id) REFERENCES dbo.Skills(skill_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_JobSkills_Skill ON dbo.JobSkills(skill_id);
END
GO

/* ==== Applications & Bookmarks ==== */
IF OBJECT_ID('dbo.Applications','U') IS NULL
BEGIN
  CREATE TABLE dbo.Applications(
    application_id INT IDENTITY(1,1) PRIMARY KEY,
    job_id         INT NOT NULL,
    seeker_id      INT NOT NULL,
    [status]       NVARCHAR(20) NOT NULL CONSTRAINT DF_Applications_Status DEFAULT N'submitted',
    applied_at     DATETIME2(3) NOT NULL CONSTRAINT DF_Applications_Applied DEFAULT SYSUTCDATETIME(),
    last_status_at DATETIME2(3) NULL,
    cv_url         NVARCHAR(500) NULL,
    [note]         NVARCHAR(500) NULL,
    status_log     NVARCHAR(MAX) NULL,
    CONSTRAINT UQ_Applications_Once UNIQUE(job_id, seeker_id),
    CONSTRAINT FK_Applications_Job    FOREIGN KEY(job_id)    REFERENCES dbo.JobsPosting(job_id) ON DELETE CASCADE,
    CONSTRAINT FK_Applications_Seeker FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE
    -- ,CONSTRAINT CK_Applications_Status CHECK ([status] IN (N'submitted',N'reviewed',N'rejected',N'hired'))
  );
  CREATE INDEX IX_Applications_Job ON dbo.Applications(job_id);
  CREATE INDEX IX_Applications_Seeker ON dbo.Applications(seeker_id);
END
GO



IF OBJECT_ID('dbo.JobBookmarks','U') IS NULL
BEGIN
  CREATE TABLE dbo.JobBookmarks(
    bookmark_id INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id   INT NOT NULL,
    job_id      INT NOT NULL,
    created_at  DATETIME2(3) NOT NULL CONSTRAINT DF_Bookmarks_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_JobBookmarks UNIQUE(seeker_id, job_id),
    CONSTRAINT FK_Bookmark_Job    FOREIGN KEY(job_id)    REFERENCES dbo.JobsPosting(job_id) ON DELETE CASCADE,
    CONSTRAINT FK_Bookmark_Seeker FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_Bookmarks_Seeker ON dbo.JobBookmarks(seeker_id);
  CREATE INDEX IX_Bookmarks_Job ON dbo.JobBookmarks(job_id);
END
GO
/*==BookMard for Employer==*/
IF OBJECT_ID('dbo.EmployerBookmarks','U') IS NULL
BEGIN
  CREATE TABLE dbo.EmployerBookmarks(
    bookmark_id INT IDENTITY(1,1) PRIMARY KEY,
    employer_id INT NOT NULL,
    application_id INT NOT NULL,
    created_at DATETIME2(3) NOT NULL CONSTRAINT DF_EmployerBookmarks_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_EmployerBookmarks UNIQUE(employer_id, application_id),
    CONSTRAINT FK_EmployerBookmarks_Employer FOREIGN KEY(employer_id) REFERENCES dbo.EmployerProfile(employer_id) ON DELETE CASCADE,
    CONSTRAINT FK_EmployerBookmarks_Application FOREIGN KEY(application_id) REFERENCES dbo.Applications(application_id) ON DELETE NO ACTION
  );
  CREATE INDEX IX_EmployerBookmarks_Employer ON dbo.EmployerBookmarks(employer_id);
  CREATE INDEX IX_EmployerBookmarks_Application ON dbo.EmployerBookmarks(application_id);
END
GO

/* ==== Follows / Reviews / Networking ==== */
IF OBJECT_ID('dbo.CompanyFollows','U') IS NULL
BEGIN
  CREATE TABLE dbo.CompanyFollows(
    follow_id   INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id   INT NOT NULL,
    employer_id INT NOT NULL,
    created_at  DATETIME2(3) NOT NULL CONSTRAINT DF_Follows_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_CompanyFollows UNIQUE(seeker_id, employer_id),
    CONSTRAINT FK_Follow_Seeker   FOREIGN KEY(seeker_id)   REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE,
    CONSTRAINT FK_Follow_Employer FOREIGN KEY(employer_id) REFERENCES dbo.EmployerProfile(employer_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_Follows_Seeker ON dbo.CompanyFollows(seeker_id);
  CREATE INDEX IX_Follows_Employer ON dbo.CompanyFollows(employer_id);
END
GO

IF OBJECT_ID('dbo.CompanyReviews','U') IS NULL
BEGIN
  CREATE TABLE dbo.CompanyReviews(
    review_id   INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id   INT NOT NULL,
    employer_id INT NOT NULL,
    rating      TINYINT NOT NULL,
    [comment]   NVARCHAR(1000) NULL,
    created_at  DATETIME2(3) NOT NULL CONSTRAINT DF_Reviews_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_Reviews_Rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT FK_Review_Seeker   FOREIGN KEY(seeker_id)   REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE,
    CONSTRAINT FK_Review_Employer FOREIGN KEY(employer_id) REFERENCES dbo.EmployerProfile(employer_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_Reviews_Employer ON dbo.CompanyReviews(employer_id);
  CREATE INDEX IX_Reviews_Seeker ON dbo.CompanyReviews(seeker_id);
END
GO

IF OBJECT_ID('dbo.ConnectionRequests','U') IS NULL
BEGIN
  CREATE TABLE dbo.ConnectionRequests(
    request_id          INT IDENTITY(1,1) PRIMARY KEY,
    requester_seeker_id INT NOT NULL,
    target_seeker_id    INT NOT NULL,
    [status]            VARCHAR(20) NOT NULL,
    [message]           NVARCHAR(500) NULL,
    common_skills       NVARCHAR(MAX) NULL,
    created_at          DATETIME2(3) NOT NULL CONSTRAINT DF_ConnReq_created DEFAULT SYSUTCDATETIME(),
    responded_at        DATETIME2(3) NULL,
    CONSTRAINT CHK_DifferentSeekers CHECK (requester_seeker_id <> target_seeker_id),
    CONSTRAINT CK_ConnReq_Status CHECK ([status] IN ('PENDING','ACCEPTED','REJECTED','CANCELLED')),
    CONSTRAINT FK_ConnReq_Requester FOREIGN KEY(requester_seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id),
    CONSTRAINT FK_ConnReq_Target    FOREIGN KEY(target_seeker_id)    REFERENCES dbo.JobSeekerProfile(seeker_id)
  );
  CREATE INDEX IX_ConnReq_Requester ON dbo.ConnectionRequests(requester_seeker_id);
  CREATE INDEX IX_ConnReq_Target ON dbo.ConnectionRequests(target_seeker_id);
END
GO

/* ==== Conversations & Messages ==== */
IF OBJECT_ID('dbo.Conversations','U') IS NULL
BEGIN
  CREATE TABLE dbo.Conversations(
    conversation_id   INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id         INT NOT NULL,
    employer_id       INT NULL,  -- NULL nếu SEEKER_SEEKER
    last_message_at   DATETIME2(3) NOT NULL CONSTRAINT DF_Conv_last DEFAULT SYSUTCDATETIME(),
    created_at        DATETIME2(3) NOT NULL CONSTRAINT DF_Conv_created DEFAULT SYSUTCDATETIME(),
    conversation_type VARCHAR(20) NOT NULL CONSTRAINT DF_Conv_type DEFAULT 'SEEKER_EMPLOYER',
    seeker_id_2       INT NULL,  -- dùng khi SEEKER_SEEKER
    CONSTRAINT CK_Conv_Shape CHECK (
      (conversation_type = 'SEEKER_EMPLOYER' AND employer_id IS NOT NULL AND seeker_id_2 IS NULL) OR
      (conversation_type = 'SEEKER_SEEKER'   AND employer_id IS NULL     AND seeker_id_2 IS NOT NULL)
    ),
    CONSTRAINT CK_Conv_SeekerOrder CHECK (
      conversation_type <> 'SEEKER_SEEKER' OR seeker_id < seeker_id_2
    ),
    CONSTRAINT FK_Conv_Seeker1 FOREIGN KEY(seeker_id)   REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE,
    CONSTRAINT FK_Conv_Seeker2 FOREIGN KEY(seeker_id_2) REFERENCES dbo.JobSeekerProfile(seeker_id),
    CONSTRAINT FK_Conv_Employer FOREIGN KEY(employer_id) REFERENCES dbo.EmployerProfile(employer_id) ON DELETE CASCADE
  );
  CREATE UNIQUE INDEX UQ_Conversation_SE ON dbo.Conversations(seeker_id, employer_id)
    WHERE conversation_type = 'SEEKER_EMPLOYER';
  CREATE UNIQUE INDEX UQ_Conversation_SS ON dbo.Conversations(seeker_id, seeker_id_2)
    WHERE conversation_type = 'SEEKER_SEEKER';
  CREATE INDEX IX_Conv_Employer ON dbo.Conversations(employer_id) WHERE employer_id IS NOT NULL;
  CREATE INDEX IX_Conv_Seeker ON dbo.Conversations(seeker_id);
  CREATE INDEX IX_Conv_Seeker2 ON dbo.Conversations(seeker_id_2) WHERE seeker_id_2 IS NOT NULL;
END
GO

IF OBJECT_ID('dbo.Messages','U') IS NULL
BEGIN
  CREATE TABLE dbo.Messages(
    message_id        INT IDENTITY(1,1) PRIMARY KEY,
    conversation_id   INT NOT NULL,
    sender_user_id    INT NOT NULL,
    receiver_user_id  INT NOT NULL,
    message_content   NVARCHAR(MAX) NOT NULL,
    message_type      VARCHAR(20) NOT NULL CONSTRAINT DF_Messages_type DEFAULT 'text',
    is_read           BIT NOT NULL CONSTRAINT DF_Messages_is_read DEFAULT (0),
    is_recalled       BIT NOT NULL CONSTRAINT DF_Messages_is_recalled DEFAULT (0),
    recalled_at       DATETIME2(3) NULL,
    sent_at           DATETIME2(3) NOT NULL CONSTRAINT DF_Messages_sent DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Messages_Conv     FOREIGN KEY(conversation_id)  REFERENCES dbo.Conversations(conversation_id) ON DELETE CASCADE,
    CONSTRAINT FK_Messages_Sender   FOREIGN KEY(sender_user_id)   REFERENCES dbo.Users(user_id),
    CONSTRAINT FK_Messages_Receiver FOREIGN KEY(receiver_user_id) REFERENCES dbo.Users(user_id)
  );
  CREATE INDEX IX_Messages_Conv ON dbo.Messages(conversation_id);
  CREATE INDEX IX_Messages_SentAt ON dbo.Messages(sent_at);
END
GO

/* ==== Education / Experience / Languages / Certificates / SeekerSkills ==== */
IF OBJECT_ID('dbo.Education','U') IS NULL
BEGIN
  CREATE TABLE dbo.Education(
    education_id    INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id       INT NOT NULL,
    [university]    NVARCHAR(255) NULL,
    degree_level    NVARCHAR(50)  NULL,
    start_date      DATE NULL,
    graduation_date DATE NULL,
    [description]   NVARCHAR(1000) NULL,
    created_at      DATETIME2(3) NOT NULL CONSTRAINT DF_Edu_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Edu_Seeker FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_Edu_Seeker ON dbo.Education(seeker_id);
END
GO

IF OBJECT_ID('dbo.Experience','U') IS NULL
BEGIN
  CREATE TABLE dbo.Experience(
    experience_id  INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id      INT NOT NULL,
    job_title      NVARCHAR(255) NULL,
    company_name   NVARCHAR(255) NULL,
    start_date     DATE NULL,
    end_date       DATE NULL,
    project_link   NVARCHAR(500) NULL,
    created_at     DATETIME2(3) NOT NULL CONSTRAINT DF_Exp_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_Exp_Dates CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT FK_Exp_Seeker FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_Exp_Seeker ON dbo.Experience(seeker_id);
END
GO

IF OBJECT_ID('dbo.Languages','U') IS NULL
BEGIN
  CREATE TABLE dbo.Languages(
    language_id      INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id        INT NOT NULL,
    language_name    NVARCHAR(100) NULL,
    certificate_type NVARCHAR(100) NULL,
    created_at       DATETIME2(3) NOT NULL CONSTRAINT DF_Lang_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Lang_Seeker FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_Lang_Seeker ON dbo.Languages(seeker_id);
END
GO

IF OBJECT_ID('dbo.Certificates','U') IS NULL
BEGIN
  CREATE TABLE dbo.Certificates(
    certificate_id        INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id             INT NOT NULL,
    issuing_organization  NVARCHAR(255) NULL,
    certificate_image_url NVARCHAR(500) NULL,
    year_of_completion    INT NULL,
    created_at            DATETIME2(3) NOT NULL CONSTRAINT DF_Cert_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_Cert_Seeker FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_Cert_Seeker ON dbo.Certificates(seeker_id);
END
GO

IF OBJECT_ID('dbo.SeekerSkills','U') IS NULL
BEGIN
  CREATE TABLE dbo.SeekerSkills(
    skill_id             INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id            INT NOT NULL,
    skill_name           NVARCHAR(255) NULL,
    years_of_experience  INT NULL,
    [description]        NVARCHAR(500) NULL,
    created_at           DATETIME2(3) NOT NULL CONSTRAINT DF_SeekerSkills_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_SeekerSkills_Seeker FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_SeekerSkills_Seeker ON dbo.SeekerSkills(seeker_id);
END
GO

/* ==== CV Templates / Upload / Exports ==== */
IF OBJECT_ID('dbo.CVTemplates','U') IS NULL
BEGIN
  CREATE TABLE dbo.CVTemplates(
    template_id    INT IDENTITY(1,1) PRIMARY KEY,
    template_name  NVARCHAR(200) NOT NULL,
    template_code  VARCHAR(50)   NOT NULL,
    [description]  NVARCHAR(500) NULL,
    thumbnail_url  NVARCHAR(500) NULL,
    html_content   NVARCHAR(MAX) NOT NULL,
    css_content    NVARCHAR(MAX) NULL,
    [category]     VARCHAR(50)   NULL,
    is_premium     BIT NOT NULL CONSTRAINT DF_Tpl_premium DEFAULT (1),
    is_active      BIT NOT NULL CONSTRAINT DF_Tpl_active  DEFAULT (1),
    display_order  INT NOT NULL  CONSTRAINT DF_Tpl_order   DEFAULT (0),
    created_at     DATETIME2(3) NOT NULL CONSTRAINT DF_Tpl_created DEFAULT SYSUTCDATETIME(),
    updated_at     DATETIME2(3) NULL,
    CONSTRAINT UQ_Tpl_Code UNIQUE (template_code)
  );
END
GO

IF OBJECT_ID('dbo.CVUpload','U') IS NULL
BEGIN
  CREATE TABLE dbo.CVUpload(
    cv_id               INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id           INT NOT NULL,
    full_name           NVARCHAR(255) NOT NULL,
    phone_number        NVARCHAR(20)  NOT NULL,
    email               NVARCHAR(255) NOT NULL,
    preferred_location  NVARCHAR(100) NOT NULL,
    years_of_experience INT NULL,
    current_job_level   NVARCHAR(100) NOT NULL,
    work_mode           NVARCHAR(50)  NOT NULL,
    expected_salary     NVARCHAR(100) NOT NULL,
    current_salary      NVARCHAR(100) NULL,
    cover_letter        NVARCHAR(MAX) NULL,
    cv_file_url         NVARCHAR(500) NOT NULL,
    cv_file_name        NVARCHAR(255) NOT NULL,
    uploaded_at         DATETIME2(3)  NOT NULL CONSTRAINT DF_CVUpload_uploaded DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_CVUpload_Seeker FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE
  );
  CREATE INDEX IX_CVUpload_Seeker ON dbo.CVUpload(seeker_id);
END
GO

IF OBJECT_ID('dbo.CVExports','U') IS NULL
BEGIN
  CREATE TABLE dbo.CVExports(
    export_id    INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id    INT NOT NULL,
    template_id  INT NOT NULL,
    file_name    NVARCHAR(500) NOT NULL,
    file_path    NVARCHAR(1000) NOT NULL,
    file_size_kb INT NULL,
    exported_at  DATETIME2(3) NOT NULL CONSTRAINT DF_CVExports_exported DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_CVExports_FileSize CHECK (file_size_kb IS NULL OR file_size_kb >= 0),
    CONSTRAINT FK_CVExports_Seeker   FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id),
    CONSTRAINT FK_CVExports_Template FOREIGN KEY(template_id) REFERENCES dbo.CVTemplates(template_id)
  );
  CREATE INDEX IX_CVExports_Seeker ON dbo.CVExports(seeker_id);
  CREATE INDEX IX_CVExports_Template ON dbo.CVExports(template_id);
END
GO

/* ==== Billing: PremiumPackages / Subscriptions / Invoice / Payment ==== */
IF OBJECT_ID('dbo.PremiumPackages','U') IS NULL
BEGIN
  CREATE TABLE dbo.PremiumPackages(
    package_id               INT IDENTITY(1,1) PRIMARY KEY,
    [code]                   VARCHAR(50)  NOT NULL,
    [name]                   NVARCHAR(200) NOT NULL,
    user_type                VARCHAR(20)  NOT NULL,  -- JOBSEEKER / EMPLOYER
    price                    DECIMAL(10,2) NOT NULL,
    duration_days            INT NOT NULL,
    max_active_jobs          INT NULL,
    boost_credits            INT NULL,
    candidate_views          INT NULL,
    [highlight]              BIT NULL,
    cv_templates_access      BIT NOT NULL CONSTRAINT DF_Pack_tpl_access DEFAULT (0),
    messaging_enabled        BIT NOT NULL CONSTRAINT DF_Pack_msg_enabled DEFAULT (0),
    seeker_networking_enabled BIT NOT NULL CONSTRAINT DF_Pack_net_enabled DEFAULT (0),
    pdf_export_limit         INT NULL,
    [features]               NVARCHAR(MAX) NULL,
    is_active                BIT NOT NULL CONSTRAINT DF_Pack_active DEFAULT (1),
    created_at               DATETIME2(3) NOT NULL CONSTRAINT DF_Pack_created DEFAULT SYSUTCDATETIME(),
    updated_at               DATETIME2(3) NULL,
    CONSTRAINT UQ_Pack_Code UNIQUE([code]),
    CONSTRAINT CK_Pack_UserType CHECK (user_type IN ('JOBSEEKER','EMPLOYER'))
  );
END
GO

IF OBJECT_ID('dbo.PremiumSubscriptions','U') IS NULL
BEGIN
  CREATE TABLE dbo.PremiumSubscriptions(
    subscription_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id         INT NOT NULL,
    employer_id     INT NULL,
    seeker_id       INT NULL,
    package_id      INT NOT NULL,
    [status]        VARCHAR(20) NOT NULL,
    start_date      DATETIME2(3) NOT NULL,
    end_date        DATETIME2(3) NOT NULL,
    is_active       BIT NOT NULL CONSTRAINT DF_Sub_active DEFAULT (1),
    created_at      DATETIME2(3) NOT NULL CONSTRAINT DF_Sub_created DEFAULT SYSUTCDATETIME(),
    updated_at      DATETIME2(3) NULL,
    CONSTRAINT CK_Sub_Status CHECK ([status] IN ('PENDING','ACTIVE','CANCELLED','EXPIRED')),
    CONSTRAINT CK_Sub_Party CHECK (
      (employer_id IS NOT NULL AND seeker_id IS NULL) OR
      (employer_id IS NULL     AND seeker_id IS NOT NULL)
    ),
    CONSTRAINT FK_Sub_User     FOREIGN KEY(user_id)    REFERENCES dbo.Users(user_id),
    CONSTRAINT FK_Sub_Employer FOREIGN KEY(employer_id) REFERENCES dbo.EmployerProfile(employer_id),
    CONSTRAINT FK_Sub_Seeker   FOREIGN KEY(seeker_id)   REFERENCES dbo.JobSeekerProfile(seeker_id),
    CONSTRAINT FK_Sub_Package  FOREIGN KEY(package_id)  REFERENCES dbo.PremiumPackages(package_id)
  );
  CREATE INDEX IX_Sub_User ON dbo.PremiumSubscriptions(user_id);
  CREATE INDEX IX_Sub_Package ON dbo.PremiumSubscriptions(package_id);
  CREATE INDEX IX_Sub_Employer ON dbo.PremiumSubscriptions(employer_id) WHERE employer_id IS NOT NULL;
  CREATE INDEX IX_Sub_Seeker ON dbo.PremiumSubscriptions(seeker_id) WHERE seeker_id IS NOT NULL;
END
GO

IF OBJECT_ID('dbo.Invoice','U') IS NULL
BEGIN
  CREATE TABLE dbo.Invoice(
    invoice_id      INT IDENTITY(1,1) PRIMARY KEY,
    user_id         INT NOT NULL,
    employer_id     INT NULL,
    seeker_id       INT NULL,
    subscription_id INT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    [status]        VARCHAR(20) NOT NULL,
    issued_at       DATETIME2(3) NOT NULL CONSTRAINT DF_Inv_issued DEFAULT SYSUTCDATETIME(),
    due_at          DATETIME2(3) NOT NULL,
    paid_at         DATETIME2(3) NULL,
    CONSTRAINT CK_Inv_Amount CHECK (amount >= 0),
    CONSTRAINT CK_Inv_Status CHECK ([status] IN ('PENDING','PAID','CANCELLED','REFUNDED')),
    CONSTRAINT CK_Inv_Dates CHECK (due_at >= issued_at AND (paid_at IS NULL OR paid_at >= issued_at)),
    CONSTRAINT CK_Inv_Party CHECK (employer_id IS NULL OR seeker_id IS NULL),
    CONSTRAINT FK_Inv_User  FOREIGN KEY(user_id) REFERENCES dbo.Users(user_id),
    CONSTRAINT FK_Inv_Emp   FOREIGN KEY(employer_id) REFERENCES dbo.EmployerProfile(employer_id),
    CONSTRAINT FK_Inv_Seek  FOREIGN KEY(seeker_id) REFERENCES dbo.JobSeekerProfile(seeker_id),
    CONSTRAINT FK_Inv_Sub   FOREIGN KEY(subscription_id) REFERENCES dbo.PremiumSubscriptions(subscription_id)
  );
  CREATE INDEX IX_Inv_User ON dbo.Invoice(user_id);
  CREATE INDEX IX_Inv_Emp ON dbo.Invoice(employer_id) WHERE employer_id IS NOT NULL;
  CREATE INDEX IX_Inv_Seek ON dbo.Invoice(seeker_id) WHERE seeker_id IS NOT NULL;
  CREATE INDEX IX_Inv_Sub ON dbo.Invoice(subscription_id) WHERE subscription_id IS NOT NULL;
END
GO

IF OBJECT_ID('dbo.Payment','U') IS NULL
BEGIN
  CREATE TABLE dbo.Payment(
    payment_id      INT IDENTITY(1,1) PRIMARY KEY,
    invoice_id      INT NOT NULL,
    provider        VARCHAR(50)  NOT NULL,
    tx_ref          VARCHAR(200) NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    [status]        VARCHAR(20)  NOT NULL,
    payment_method  VARCHAR(50)  NULL,
    payment_details NVARCHAR(MAX) NULL,
    created_at      DATETIME2(3) NOT NULL CONSTRAINT DF_Pay_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_Pay_TxRef UNIQUE(tx_ref),
    CONSTRAINT CK_Pay_Status CHECK ([status] IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
    CONSTRAINT FK_Pay_Invoice FOREIGN KEY(invoice_id) REFERENCES dbo.Invoice(invoice_id)
  );
  CREATE INDEX IX_Pay_Invoice ON dbo.Payment(invoice_id);
END
GO

/* ==== Messaging safety ==== */
IF OBJECT_ID('dbo.MessageBlocks','U') IS NULL
BEGIN
  CREATE TABLE dbo.MessageBlocks(
    block_id        INT IDENTITY(1,1) PRIMARY KEY,
    blocker_user_id INT NOT NULL,
    blocked_user_id INT NOT NULL,
    blocked_at      DATETIME2(3) NOT NULL CONSTRAINT DF_Block_created DEFAULT SYSUTCDATETIME(),
    [reason]        NVARCHAR(500) NULL,
    CONSTRAINT UQ_Block UNIQUE(blocker_user_id, blocked_user_id),
    CONSTRAINT CK_Block_NotSelf CHECK (blocker_user_id <> blocked_user_id),
    CONSTRAINT FK_Block_Blocker FOREIGN KEY(blocker_user_id) REFERENCES dbo.Users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Block_Blocked FOREIGN KEY(blocked_user_id) REFERENCES dbo.Users(user_id)
  );
  CREATE INDEX IX_Block_Blocked ON dbo.MessageBlocks(blocked_user_id);
END
GO

/* ==== Admin & Logs ==== */
IF OBJECT_ID('dbo.AdminLogs','U') IS NULL
BEGIN
  CREATE TABLE dbo.AdminLogs(
    log_id     INT IDENTITY(1,1) PRIMARY KEY,
    [action]   NVARCHAR(200) NOT NULL,
    created_at DATETIME2(3)  NOT NULL CONSTRAINT DF_Admin_created DEFAULT SYSUTCDATETIME()
  );
END
GO
-- procedure

--Tạo Application (chặn trùng, ghi status_log initial) 
--Trạng thái chưa chạy đucợ applications_create
-- Đổi đầu sp thành usp => nguyên nhân gây lỗi: 
--Prefix sp_ là đặc biệt: SQL Server luôn tìm trong master trước, rồi mới tới database hiện hành.
--Nếu CREATE OR ALTER trong user database với tên sp_..., nó có thể ghi đè nhầm hoặc bị bỏ qua.
DROP PROCEDURE IF EXISTS dbo.sp_Application_Create;
GO

CREATE OR ALTER PROCEDURE dbo.usp_Application_Create
  @job_id INT,
  @seeker_id INT,
  @cv_url NVARCHAR(500) = NULL,
  @note NVARCHAR(500) = NULL
AS
BEGIN
  SET NOCOUNT ON;

  IF EXISTS (SELECT 1 FROM dbo.Applications WHERE job_id = @job_id AND seeker_id = @seeker_id)
  BEGIN
     RAISERROR ('This seeker already applied this job.', 16, 1);
     RETURN;
  END;

  DECLARE @now DATETIME2 = SYSUTCDATETIME();
  DECLARE @initLog NVARCHAR(MAX) = N'[{"ts":"'+CONVERT(NVARCHAR(27),@now,126)+
                                     N'","action":"applied","new":"submitted"}]';

  INSERT INTO dbo.Applications(job_id,seeker_id,cv_url,note,applied_at,status,last_status_at,status_log)
  VALUES (@job_id,@seeker_id,@cv_url,@note,@now,'submitted',@now,@initLog);

  SELECT SCOPE_IDENTITY() AS application_id;
END
GO

--Đổi trạng thái Application (append JSON log)
GO
CREATE OR ALTER PROCEDURE dbo.usp_Application_SetStatus
  @application_id INT,
  @new_status NVARCHAR(20),
  @changed_by_id INT = NULL,                 -- Users.user_id
  @changed_by_type NVARCHAR(10) = 'system',  -- seeker|employer|admin|system
  @note NVARCHAR(500) = NULL
AS
BEGIN
  SET NOCOUNT ON;

  DECLARE @old_status NVARCHAR(20),
          @now DATETIME2 = SYSUTCDATETIME(),
          @entry NVARCHAR(MAX);

  SELECT @old_status = status
  FROM dbo.Applications WITH (UPDLOCK, ROWLOCK)
  WHERE application_id = @application_id;

  IF @old_status IS NULL
  BEGIN
     RAISERROR('Application not found.',16,1);
     RETURN;
  END;

  SET @entry = N'{"ts":"'+CONVERT(NVARCHAR(27),@now,126)+
               N'","action":"status_change","old":"'+ISNULL(@old_status,'')+
               N'","new":"'+@new_status+
               N'","by_id":'+COALESCE(CONVERT(NVARCHAR(12),@changed_by_id),'null')+
               N',"by_type":"'+@changed_by_type+
               N'","note":'+COALESCE('"' + REPLACE(@note,'"','\"') + '"','null')+N'}';

  UPDATE dbo.Applications
  SET status = @new_status,
      last_status_at = @now,
      status_log = CASE 
                     WHEN status_log IS NULL OR LTRIM(RTRIM(status_log)) = '' 
                       THEN N'[' + @entry + N']'
                     ELSE STUFF(status_log, LEN(status_log), 1, N',' + @entry + N']')
                   END
  WHERE application_id = @application_id;
END
GO

--Lấy lịch sử apply theo ngày (cho seeker)
GO
CREATE OR ALTER PROCEDURE dbo.usp_Seeker_ApplicationsByDay
  @seeker_id INT
AS
BEGIN
  SET NOCOUNT ON;

  SELECT CAST(applied_at AS date) AS applied_date,
         COUNT(*) AS total_jobs
  FROM dbo.Applications
  WHERE seeker_id = @seeker_id
  GROUP BY CAST(applied_at AS date)
  ORDER BY applied_date DESC;
END
GO

--Tạo Subscription + Invoice (sau khi employer chọn gói)
GO
CREATE OR ALTER PROCEDURE dbo.usp_Subscription_Create
  @employer_id INT,
  @package_id  INT
AS
BEGIN
  SET NOCOUNT ON;
  BEGIN TRAN;

  DECLARE @price DECIMAL(12,2);
  SELECT @price = price FROM dbo.PremiumPackages WHERE package_id = @package_id AND is_active = 1;
  IF @price IS NULL
  BEGIN
     ROLLBACK;
     RAISERROR('Package not found or inactive.',16,1);
     RETURN;
  END;

  INSERT INTO dbo.PremiumSubscriptions(employer_id,package_id,status,is_active)
  VALUES (@employer_id,@package_id,'pending',0);

  DECLARE @subscription_id INT = SCOPE_IDENTITY();

  INSERT INTO dbo.Invoice (user_id, employer_id, subscription_id, amount, status, due_at)
  VALUES (NULL, @employer_id, @subscription_id, @price, 'unpaid', DATEADD(DAY,3,SYSUTCDATETIME()));

  DECLARE @invoice_id INT = SCOPE_IDENTITY();

  COMMIT;

  SELECT @subscription_id AS subscription_id, @invoice_id AS invoice_id, @price AS amount;
END
GO

--Webhook thanh toán thành công (ghi Payment, kích hoạt Subscription)
GO
CREATE OR ALTER PROCEDURE dbo.usp_Payment_Webhook_Success
  @invoice_id INT,
  @provider   VARCHAR(30),
  @tx_ref     VARCHAR(120),
  @amount     DECIMAL(12,2)
AS
BEGIN
  SET NOCOUNT ON;
  BEGIN TRAN;

  IF EXISTS (SELECT 1 FROM dbo.Payment WHERE invoice_id = @invoice_id)
  BEGIN
     ROLLBACK;
     RAISERROR('Payment already recorded for this invoice.',16,1);
     RETURN;
  END

  INSERT INTO dbo.Payment(invoice_id, provider, tx_ref, amount, status)
  VALUES (@invoice_id, @provider, @tx_ref, @amount, 'success');

  UPDATE dbo.Invoice SET status = 'paid' WHERE invoice_id = @invoice_id;

  UPDATE s
  SET status = 'active',
      is_active = 1,
      start_date = COALESCE(start_date, SYSUTCDATETIME()),
      end_date   = DATEADD(DAY, p.duration_days, COALESCE(s.start_date, SYSUTCDATETIME()))
  FROM dbo.PremiumSubscriptions s
  JOIN dbo.Invoice i ON i.subscription_id = s.subscription_id
  JOIN dbo.PremiumPackages p ON p.package_id = s.package_id
  WHERE i.invoice_id = @invoice_id;

  COMMIT;
END
GO

--Gói đang có hiệu lực của employer (để check hạn mức)
GO
CREATE OR ALTER PROCEDURE dbo.usp_Employer_ActivePackage
  @employer_id INT
AS
BEGIN
  SET NOCOUNT ON;

  SELECT TOP (1)
         s.subscription_id, s.start_date, s.end_date,
         p.package_id, p.code, p.name, p.price, p.duration_days,
         p.max_active_jobs, p.boost_credits, p.candidate_views, p.highlight
  FROM dbo.PremiumSubscriptions s
  JOIN dbo.PremiumPackages p ON p.package_id = s.package_id
  WHERE s.employer_id = @employer_id
    AND s.is_active = 1
    AND (s.end_date IS NULL OR s.end_date >= SYSUTCDATETIME())
  ORDER BY s.end_date DESC;
END
GO

--Stored Procedure đăng ký có kiểm tra mật khẩu
CREATE OR ALTER PROCEDURE dbo.usp_User_Register
  @Email NVARCHAR(255),
  @RawPassword NVARCHAR(200),
  @Role VARCHAR(30)  -- admin|employer|seeker
AS
BEGIN
  SET NOCOUNT ON;

  -- 1) Kiểm tra policy
  IF LEN(@RawPassword) < 8 
     OR @RawPassword NOT LIKE '%[0-9]%'
     OR @RawPassword NOT LIKE '%[A-Z]%'
     OR @RawPassword NOT LIKE '%[^a-zA-Z0-9]%'
  BEGIN
     RAISERROR('Password does not meet policy (>=8, 1 uppercase, 1 digit, 1 special).',16,1);
     RETURN;
  END

  -- 2) Email unique
  IF EXISTS (SELECT 1 FROM dbo.Users WHERE email = @Email)
  BEGIN
     RAISERROR('Email already exists.',16,1);
     RETURN;
  END

  -- 3) Lưu hash SHA-256
  INSERT INTO dbo.Users(email, password_hash, role)
  VALUES (@Email, HASHBYTES('SHA2_256', @RawPassword), @Role);
END
GO

ALTER TABLE dbo.Users
ALTER COLUMN username NVARCHAR(100) NULL;

-- ===== Provinces =====
INSERT INTO dbo.Provinces (province_name, province_code)
VALUES
(N'Hà Nội', 'HN'),
(N'Hồ Chí Minh', 'HCM'),
(N'Đà Nẵng', 'DN'),
(N'Quảng Ninh', 'QN'),
(N'Nam Định', 'ND');

-- ===== Districts =====

-- 1. Hà Nội (province_id = 1)
INSERT INTO dbo.Districts (district_name, province_id)
VALUES
(N'Ba Đình', 1),
(N'Hoàn Kiếm', 1),
(N'Tây Hồ', 1),
(N'Cầu Giấy', 1),
(N'Đống Đa', 1),
(N'Hai Bà Trưng', 1),
(N'Hoàng Mai', 1),
(N'Thanh Xuân', 1),
(N'Long Biên', 1),
(N'Hà Đông', 1);

-- 2. Hồ Chí Minh (province_id = 2)
INSERT INTO dbo.Districts (district_name, province_id)
VALUES
(N'Quận 1', 2),
(N'Quận 3', 2),
(N'Quận 4', 2),
(N'Quận 5', 2),
(N'Quận 7', 2),
(N'Quận 10', 2),
(N'Quận 11', 2),
(N'Quận Bình Thạnh', 2),
(N'Quận Phú Nhuận', 2),
(N'Thành phố Thủ Đức', 2);

-- 3. Đà Nẵng (province_id = 3)
INSERT INTO dbo.Districts (district_name, province_id)
VALUES
(N'Hải Châu', 3),
(N'Thanh Khê', 3),
(N'Sơn Trà', 3),
(N'Ngũ Hành Sơn', 3),
(N'Liên Chiểu', 3),
(N'Cẩm Lệ', 3),
(N'Hòa Vang', 3),
(N'Hoàng Sa', 3),
(N'Hòa Nhơn', 3),
(N'Hòa Phong', 3);

-- 4. Quảng Ninh (province_id = 4)
INSERT INTO dbo.Districts (district_name, province_id)
VALUES
(N'Hạ Long', 4),
(N'Cẩm Phả', 4),
(N'Móng Cái', 4),
(N'Uông Bí', 4),
(N'Đông Triều', 4),
(N'Quảng Yên', 4),
(N'Vân Đồn', 4),
(N'Bình Liêu', 4),
(N'Tiên Yên', 4),
(N'Hải Hà', 4);

-- 5. Nam Định (province_id = 5)
INSERT INTO dbo.Districts (district_name, province_id)
VALUES
(N'TP Nam Định', 5),
(N'Giao Thủy', 5),
(N'Hải Hậu', 5),
(N'Mỹ Lộc', 5),
(N'Nam Trực', 5),
(N'Nghĩa Hưng', 5),
(N'Trực Ninh', 5),
(N'Vụ Bản', 5),
(N'Xuân Trường', 5),
(N'Ý Yên', 5);

INSERT INTO dbo.Skills ([name]) VALUES
(N'Java'),
(N'Spring Boot'),
(N'JavaScript'),
(N'HTML/CSS'),
(N'Python'),
(N'Node.js'),
(N'SQL'),
(N'ReactJS'),
(N'Angular'),
(N'Docker');

INSERT INTO dbo.Categories ([name], [description]) VALUES
(N'Công nghệ thông tin', N'Việc làm trong lĩnh vực phần mềm, lập trình, IT Helpdesk.'),
(N'Kinh doanh & Bán hàng', N'Công việc liên quan đến sale, marketing, phát triển thị trường.'),
(N'Tài chính - Kế toán', N'Ngành nghề về kế toán, kiểm toán, tài chính ngân hàng.'),
(N'Hành chính - Nhân sự', N'Nhân viên hành chính, tuyển dụng, đào tạo, quản trị nhân sự.'),
(N'Thiết kế & Sáng tạo', N'Ngành thiết kế đồ họa, sáng tạo nội dung, UI/UX.'),
(N'Xây dựng', N'Kỹ sư xây dựng, giám sát công trình, kiến trúc.'),
(N'Giáo dục & Đào tạo', N'Giảng dạy, huấn luyện, đào tạo chuyên môn.'),
(N'Sản xuất & Cơ khí', N'Công việc trong nhà máy, kỹ thuật viên cơ khí, bảo trì.'),
(N'Y tế & Chăm sóc sức khỏe', N'Bác sĩ, y tá, dược sĩ, nhân viên y tế.'),
(N'Du lịch & Nhà hàng - Khách sạn', N'Nhân viên phục vụ, lễ tân, quản lý khách sạn, hướng dẫn viên.');


--test
  INSERT INTO dbo.Users (username, email, password_hash, role, url_avt)
VALUES (
    N'testUser',
    N'test@example.com',
    '$2a$10$VhW8fpW1tM5KyzjXoSgnzOghzWtkMTr1b4Kkz6NwNq9OqDLEtkKx6', 
    'employer',
    N'/images/default.png'
);


-- 2. Lấy user_id vừa tạo (nếu đang dùng SSMS)

DECLARE @userId INT = SCOPE_IDENTITY();
INSERT INTO dbo.EmployerProfile (user_id, company_name, industry, location, description, phone_number)
VALUES (
    @userId,
    N'Công ty ABC',
    N'Công nghệ thông tin',
    N'Hà Nội',
    N'Công ty chuyên phát triển phần mềm',
    N'0123456789'
);

ALTER TABLE dbo.Users
DROP CONSTRAINT UQ_Users_Username;
GO

-- Bước 2: Sửa đổi cột username để cho phép giá trị NULL
ALTER TABLE dbo.Users
ALTER COLUMN username NVARCHAR(100) NULL;
GO

ALTER TABLE dbo.Users
ADD google_id VARCHAR(255) NULL;
GO

ALTER TABLE dbo.JobSeekerProfile
ADD receive_invitations BIT NOT NULL CONSTRAINT DF_Seeker_ReceiveInvites DEFAULT (1);
GO


IF OBJECT_ID('dbo.BlockedEmployers','U') IS NULL
BEGIN
  CREATE TABLE dbo.BlockedEmployers(
    block_id    INT IDENTITY(1,1) PRIMARY KEY,
    seeker_id   INT NOT NULL,
    employer_id INT NOT NULL,
    created_at  DATETIME2(3) NOT NULL CONSTRAINT DF_BlockedEmp_created DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_BlockedEmployers UNIQUE(seeker_id, employer_id),
    CONSTRAINT FK_Block_Seeker   FOREIGN KEY(seeker_id)   REFERENCES dbo.JobSeekerProfile(seeker_id) ON DELETE CASCADE,
    CONSTRAINT FK_Block_Employer FOREIGN KEY(employer_id) REFERENCES dbo.EmployerProfile(employer_id) ON DELETE CASCADE
  );
  PRINT N'Đã tạo bảng BlockedEmployers.';
END
GO


CREATE OR ALTER PROCEDURE dbo.sp_Job_GetSkills
    @JobId INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        s.skill_id,
        s.name
    FROM dbo.JobSkills js
    INNER JOIN dbo.Skills s ON js.skill_id = s.skill_id
    WHERE js.job_id = @JobId
    ORDER BY s.name;
END;
GO


CREATE OR ALTER PROCEDURE dbo.sp_Skills_GetPopular
    @TopN INT = 20
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@TopN)
        s.skill_id,
        s.name,
        COUNT(js.job_id) AS JobCount
    FROM dbo.Skills s
    LEFT JOIN dbo.JobSkills js ON s.skill_id = js.skill_id
    GROUP BY s.skill_id, s.name
    ORDER BY JobCount DESC, s.name;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_Employers_SearchOpen
    @Keyword NVARCHAR(100) = NULL,
    @Location NVARCHAR(100) = NULL,
    @Industry NVARCHAR(100) = NULL,
    @SortBy NVARCHAR(20) = 'most_jobs'  -- most_jobs | latest | name_az | name_za
AS
BEGIN
    SET NOCOUNT ON;

    ;WITH EmployerWithStats AS (
        SELECT e.employer_id,
               e.company_name,
               e.industry,
               e.location,
               e.description,
               e.phone_number,
               MAX(j.posted_at) AS latest_job_date,
               COUNT(j.job_id) AS open_positions
        FROM dbo.EmployerProfile e
        JOIN dbo.JobsPosting j ON e.employer_id = j.employer_id
        WHERE (@Keyword IS NULL OR e.company_name LIKE N'%' + @Keyword + N'%' OR e.industry LIKE N'%' + @Keyword + N'%')
          AND (@Location IS NULL OR e.location LIKE N'%' + @Location + N'%')
          AND (@Industry IS NULL OR e.industry LIKE N'%' + @Industry + N'%')
        GROUP BY e.employer_id, e.company_name, e.industry, e.location, e.description, e.phone_number
    )
    SELECT *
    FROM EmployerWithStats
    ORDER BY 
        CASE WHEN @SortBy = 'most_jobs' THEN open_positions END DESC,
        CASE WHEN @SortBy = 'latest' THEN latest_job_date END DESC,
        CASE WHEN @SortBy = 'name_az' THEN company_name END ASC,
        CASE WHEN @SortBy = 'name_za' THEN company_name END DESC;
END
GO


CREATE OR ALTER PROCEDURE dbo.usp_Application_Create
  @job_id INT,
  @seeker_id INT,
  @cv_url NVARCHAR(500) = NULL,
  @note NVARCHAR(500) = NULL
AS
BEGIN
  SET NOCOUNT ON;

  IF EXISTS (SELECT 1 FROM dbo.Applications WHERE job_id = @job_id AND seeker_id = @seeker_id)
  BEGIN
     RAISERROR ('This seeker already applied this job.', 16, 1);
     RETURN;
  END;

  DECLARE @now DATETIME2 = SYSUTCDATETIME();
  DECLARE @initLog NVARCHAR(MAX) = N'[{"ts":"'+CONVERT(NVARCHAR(27),@now,126)+
                                     N'","action":"applied","new":"submitted"}]';

  INSERT INTO dbo.Applications(job_id,seeker_id,cv_url,note,applied_at,status,last_status_at,status_log)
  VALUES (@job_id,@seeker_id,@cv_url,@note,@now,'submitted',@now,@initLog);

  SELECT SCOPE_IDENTITY() AS application_id;
END
GO



CREATE OR ALTER PROCEDURE dbo.usp_Application_SetStatus
  @application_id INT,
  @new_status NVARCHAR(20),
  @changed_by_id INT = NULL,                 -- Users.user_id
  @changed_by_type NVARCHAR(10) = 'system',  -- seeker|employer|admin|system
  @note NVARCHAR(500) = NULL
AS
BEGIN
  SET NOCOUNT ON;

  DECLARE @old_status NVARCHAR(20),
          @now DATETIME2 = SYSUTCDATETIME(),
          @entry NVARCHAR(MAX);

  SELECT @old_status = status
  FROM dbo.Applications WITH (UPDLOCK, ROWLOCK)
  WHERE application_id = @application_id;

  IF @old_status IS NULL
  BEGIN
     RAISERROR('Application not found.',16,1);
     RETURN;
  END;

  SET @entry = N'{"ts":"'+CONVERT(NVARCHAR(27),@now,126)+
               N'","action":"status_change","old":"'+ISNULL(@old_status,'')+
               N'","new":"'+@new_status+
               N'","by_id":'+COALESCE(CONVERT(NVARCHAR(12),@changed_by_id),'null')+
               N',"by_type":"'+@changed_by_type+
               N'","note":'+COALESCE('"' + REPLACE(@note,'"','\"') + '"','null')+N'}';

  UPDATE dbo.Applications
  SET status = @new_status,
      last_status_at = @now,
      status_log = CASE 
                     WHEN status_log IS NULL OR LTRIM(RTRIM(status_log)) = '' 
                       THEN N'[' + @entry + N']'
                     ELSE STUFF(status_log, LEN(status_log), 1, N',' + @entry + N']')
                   END
  WHERE application_id = @application_id;
END
GO



CREATE OR ALTER PROCEDURE dbo.usp_Seeker_ApplicationsByDay
  @seeker_id INT
AS
BEGIN
  SET NOCOUNT ON;

  SELECT CAST(applied_at AS date) AS applied_date,
         COUNT(*) AS total_jobs
  FROM dbo.Applications
  WHERE seeker_id = @seeker_id
  GROUP BY CAST(applied_at AS date)
  ORDER BY applied_date DESC;
END
GO



CREATE OR ALTER PROCEDURE dbo.usp_Subscription_Create
  @employer_id INT,
  @package_id  INT
AS
BEGIN
  SET NOCOUNT ON;
  BEGIN TRAN;

  DECLARE @price DECIMAL(12,2);
  SELECT @price = price FROM dbo.PremiumPackages WHERE package_id = @package_id AND is_active = 1;
  IF @price IS NULL
  BEGIN
     ROLLBACK;
     RAISERROR('Package not found or inactive.',16,1);
     RETURN;
  END;

  INSERT INTO dbo.PremiumSubscriptions(employer_id,package_id,status,is_active)
  VALUES (@employer_id,@package_id,'pending',0);

  DECLARE @subscription_id INT = SCOPE_IDENTITY();

  INSERT INTO dbo.Invoice (user_id, employer_id, subscription_id, amount, status, due_at)
  VALUES (NULL, @employer_id, @subscription_id, @price, 'unpaid', DATEADD(DAY,3,SYSUTCDATETIME()));

  DECLARE @invoice_id INT = SCOPE_IDENTITY();

  COMMIT;

  SELECT @subscription_id AS subscription_id, @invoice_id AS invoice_id, @price AS amount;
END
GO


CREATE OR ALTER PROCEDURE dbo.usp_Payment_Webhook_Success
  @invoice_id INT,
  @provider   VARCHAR(30),
  @tx_ref     VARCHAR(120),
  @amount     DECIMAL(12,2)
AS
BEGIN
  SET NOCOUNT ON;
  BEGIN TRAN;

  IF EXISTS (SELECT 1 FROM dbo.Payment WHERE invoice_id = @invoice_id)
  BEGIN
     ROLLBACK;
     RAISERROR('Payment already recorded for this invoice.',16,1);
     RETURN;
  END

  INSERT INTO dbo.Payment(invoice_id, provider, tx_ref, amount, status)
  VALUES (@invoice_id, @provider, @tx_ref, @amount, 'success');

  UPDATE dbo.Invoice SET status = 'paid' WHERE invoice_id = @invoice_id;

  UPDATE s
  SET status = 'active',
      is_active = 1,
      start_date = COALESCE(start_date, SYSUTCDATETIME()),
      end_date   = DATEADD(DAY, p.duration_days, COALESCE(s.start_date, SYSUTCDATETIME()))
  FROM dbo.PremiumSubscriptions s
  JOIN dbo.Invoice i ON i.subscription_id = s.subscription_id
  JOIN dbo.PremiumPackages p ON p.package_id = s.package_id
  WHERE i.invoice_id = @invoice_id;

  COMMIT;
END
GO



CREATE OR ALTER PROCEDURE dbo.usp_Employer_ActivePackage
  @employer_id INT
AS
BEGIN
  SET NOCOUNT ON;

  SELECT TOP (1)
         s.subscription_id, s.start_date, s.end_date,
         p.package_id, p.code, p.name, p.price, p.duration_days,
         p.max_active_jobs, p.boost_credits, p.candidate_views, p.highlight
  FROM dbo.PremiumSubscriptions s
  JOIN dbo.PremiumPackages p ON p.package_id = s.package_id
  WHERE s.employer_id = @employer_id
    AND s.is_active = 1
    AND (s.end_date IS NULL OR s.end_date >= SYSUTCDATETIME())
  ORDER BY s.end_date DESC;
END
GO










GO
CREATE OR ALTER PROCEDURE dbo.usp_Jobs_SearchBySkills
    @SkillNames NVARCHAR(1000) = NULL, -- Chuỗi các kỹ năng, cách nhau bởi dấu phẩy, vd: 'Java,SQL'
    @Location NVARCHAR(255) = NULL,
    @MinSalary DECIMAL(12, 2) = NULL,
    @MaxSalary DECIMAL(12, 2) = NULL,
    @PageNumber INT = 1,
    @PageSize INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    -- Bảng tạm để chứa các skill_id từ chuỗi đầu vào
    DECLARE @SkillIds TABLE (skill_id INT);
    IF @SkillNames IS NOT NULL AND LTRIM(RTRIM(@SkillNames)) <> ''
    BEGIN
        INSERT INTO @SkillIds (skill_id)
        SELECT s.skill_id
        FROM dbo.Skills s
        WHERE s.name IN (SELECT LTRIM(RTRIM(value)) FROM STRING_SPLIT(@SkillNames, ','));
    END;

    -- Sử dụng Common Table Expression (CTE) để lọc và tính toán
    ;WITH FilteredJobs AS (
        SELECT
            j.job_id,
            j.title,
            ep.company_name,
            -- Vẫn hiển thị địa điểm chi tiết của JOB (nếu có)
            CONCAT_WS(', ', d.district_name, p.province_name) AS location,
            (
                SELECT STRING_AGG(s.name, ', ')
                FROM dbo.JobSkills js
                JOIN dbo.Skills s ON js.skill_id = s.skill_id
                WHERE js.job_id = j.job_id
            ) AS all_skills,
            (
                SELECT COUNT(DISTINCT js.skill_id)
                FROM dbo.JobSkills js
                WHERE js.job_id = j.job_id AND js.skill_id IN (SELECT skill_id FROM @SkillIds)
            ) AS matching_skill_count,
            COUNT(*) OVER() AS TotalCount -- Đếm tổng số dòng trước khi phân trang
        FROM
            dbo.JobsPosting j
        JOIN
            dbo.EmployerProfile ep ON j.employer_id = ep.employer_id
        -- Vẫn join để lấy tên tỉnh, huyện cho việc hiển thị
        LEFT JOIN
            dbo.Provinces p ON j.province_id = p.province_id
        LEFT JOIN
            dbo.Districts d ON j.district_id = d.district_id
        WHERE
            -- SỬA LỖI: Lọc theo cột 'location' của bảng EmployerProfile
            (@Location IS NULL OR ep.location LIKE '%' + @Location + '%')
            -- Lọc theo mức lương (nếu có)
            AND (@MinSalary IS NULL OR j.salary_max >= @MinSalary)
            AND (@MaxSalary IS NULL OR j.salary_min <= @MaxSalary)
            -- Lọc theo skill: chỉ lấy những job có ít nhất 1 skill khớp
            AND (NOT EXISTS (SELECT 1 FROM @SkillIds) OR EXISTS (
                SELECT 1
                FROM dbo.JobSkills js_match
                WHERE js_match.job_id = j.job_id AND js_match.skill_id IN (SELECT skill_id FROM @SkillIds)
            ))
    )
    -- Trả về kết quả cuối cùng với phân trang
    SELECT
        job_id,
        title,
        company_name,
        location,
        all_skills,
        matching_skill_count,
        TotalCount
    FROM
        FilteredJobs
    ORDER BY
        matching_skill_count DESC, -- Ưu tiên job khớp nhiều skill nhất
        job_id -- Sắp xếp phụ để đảm bảo thứ tự nhất quán
    OFFSET (@PageNumber - 1) * @PageSize ROWS
    FETCH NEXT @PageSize ROWS ONLY;

END
GO
--Demo thử còn fix đăng nhập
INSERT INTO dbo.Users (email, username, password_hash, [role], url_avt)
VALUES (
  N'employer1@joblink.com',
  N'employer1',
  N'123456', -- chỉ demo, không dùng thật
  'employer',
  N'https://example.com/avatar/employer1.png'
);

-- 1. Chèn dữ liệu vào Users trước
-- 1. Chèn dữ liệu vào Users với URL online
-- Kiểm tra và update nếu tồn tại, insert nếu chưa có
MERGE INTO Users AS target
USING (VALUES 
    ('employer1@company.com', 'employer1', 'hashed_password_123', 'employer', 'https://i.pinimg.com/1200x/1e/cb/a9/1ecba98ac8bfd242436a87fb0996f14d.jpg?text=Employer', 1),
    ('nguyenvana@email.com', 'nguyenvana', 'hashed_password_123', 'seeker', 'https://i.pinimg.com/736x/d7/62/ea/d762eacadd9c9cc5e9c04244b711a972.jpg?text=User', 1),
    ('tranthib@email.com', 'tranthib', 'hashed_password_123', 'seeker', 'https://via.placeholder.com/150/28a745/ffffff?text=User', 1),
    ('levanc@email.com', 'levanc', 'hashed_password_123', 'seeker', 'https://via.placeholder.com/150/28a745/ffffff?text=User', 1)
) AS source (email, username, password_hash, role, url_avt, enabled)
ON target.email = source.email
WHEN MATCHED THEN
    UPDATE SET 
        username = source.username,
        password_hash = source.password_hash,
        role = source.role,
        url_avt = source.url_avt,
        enabled = source.enabled
WHEN NOT MATCHED THEN
    INSERT (email, username, password_hash, role, url_avt, enabled)
    VALUES (source.email, source.username, source.password_hash, source.role, source.url_avt, source.enabled);
-- 2. Kiểm tra user_id đã được tạo
SELECT user_id, username, role FROM Users;
-- 3. Chèn dữ liệu vào JobSeekerProfile với user_id từ bảng Users
INSERT INTO JobSeekerProfile (user_id, fullname, email, phone, avatar_url, location, experience_years) 
SELECT 
    user_id,
    CASE 
        WHEN username = 'nguyenvana' THEN N'Nguyễn Văn A'
        WHEN username = 'tranthib' THEN N'Trần Thị B' 
        WHEN username = 'levanc' THEN N'Lê Văn C'
    END,
    email,
    CASE 
        WHEN username = 'nguyenvana' THEN '0912345678'
        WHEN username = 'tranthib' THEN '0923456789'
        WHEN username = 'levanc' THEN '0934567890'
    END,
    url_avt,
    CASE 
        WHEN username = 'nguyenvana' THEN N'Hà Nội'
        WHEN username = 'tranthib' THEN N'Hồ Chí Minh'
        WHEN username = 'levanc' THEN N'Đà Nẵng'
    END,
    CASE 
        WHEN username = 'nguyenvana' THEN 3
        WHEN username = 'tranthib' THEN 5
        WHEN username = 'levanc' THEN 2
    END
FROM Users 
WHERE role = 'seeker';
-- 5. Kiểm tra JobSeekerProfile đã được tạo
SELECT seeker_id, user_id, fullname FROM JobSeekerProfile;

-- 2. Chèn dữ liệu vào JobsPosting
-- Lấy employer_id đầu tiên
DECLARE @employer_id INT;
SELECT TOP 1 @employer_id = employer_id FROM EmployerProfile;

-- Lấy các ID cần thiết
DECLARE @category_id INT, @skill_id INT, @province_id INT, @district_id INT;
SELECT TOP 1 @category_id = category_id FROM Categories;
SELECT TOP 1 @skill_id = skill_id FROM Skills;
SELECT TOP 1 @province_id = province_id FROM Provinces;
SELECT TOP 1 @district_id = district_id FROM Districts;

-- Chèn dữ liệu trực tiếp
INSERT INTO JobsPosting (
    employer_id, position, status, category_id, title, skill_id,
    province_id, district_id, street_address, year_experience, hiring_number,
    submission_deadline, work_type, salary_min, salary_max, job_desc,
    job_requirements, benefits, contact_name, contact_email, contact_phone, posted_at
) VALUES
(@employer_id, 'Backend Developer', 'ACTIVE', @category_id, 'Backend Developer Java', @skill_id,
 @province_id, @district_id, '123 Main Street', '1-3 years', 2,
 DATEADD(month, 1, GETDATE()), N'Toàn thời gian', 15000000, 25000000,
 N'Phát triển ứng dụng backend', N'Kinh nghiệm Java, Spring Boot', N'Bảo hiểm, thưởng',
 'HR Department', 'hr@company.com', '0987654321', GETDATE()),

(@employer_id, 'Frontend Developer', 'ACTIVE', @category_id, 'Frontend Developer React', @skill_id,
 @province_id, @district_id, '456 Central Ave', '2-5 years', 3,
 DATEADD(month, 1, GETDATE()), N'Toàn thời gian', 12000000, 20000000,
 N'Phát triển giao diện người dùng', N'Kinh nghiệm React, JavaScript', N'Linh hoạt, remote',
 'HR Department', 'hr@company.com', '0987654322', GETDATE()),

(@employer_id, 'Fullstack Developer', 'ACTIVE', @category_id, 'Fullstack Developer', @skill_id,
 @province_id, @district_id, '789 Park Road', '3-6 years', 1,
 DATEADD(month, 1, GETDATE()), N'Toàn thời gian', 18000000, 30000000,
 N'Phát triển fullstack', N'Kinh nghiệm cả frontend và backend', N'Đào tạo, thăng tiến',
 'HR Department', 'hr@company.com', '0987654323', GETDATE());

 SELECT job_id, position, title, employer_id FROM JobsPosting;

 -- 7. Chèn dữ liệu vào Education (cách đơn giản)
INSERT INTO Education (seeker_id, university, degree_level, start_date, graduation_date, description) 
SELECT 
    jsp.seeker_id,
    edu.university,
    edu.degree_level,
    edu.start_date,
    edu.graduation_date,
    edu.description
FROM (VALUES
    ('nguyenvana', 'Bachelor', N'Đại học Bách Khoa', '2018-09-01', '2022-06-30', N'Chuyên ngành Công nghệ thông tin'),
    ('tranthib', 'Master', N'Đại học Kinh tế', '2017-09-01', '2019-06-30', N'Chuyên ngành Quản trị kinh doanh'),
    ('levanc', 'Bachelor', N'Đại học Sư phạm', '2019-09-01', '2023-06-30', N'Chuyên ngành Toán tin'),
    ('nguyenvana', 'Master', N'Đại học Quốc gia', '2022-09-01', '2024-06-30', N'Chuyên ngành Khoa học máy tính')
) AS edu(username, degree_level, university, start_date, graduation_date, description)
INNER JOIN Users tu ON edu.username = tu.username
INNER JOIN JobSeekerProfile jsp ON tu.user_id = jsp.user_id;
SELECT 
    e.education_id,
    e.seeker_id,
    jsp.fullname,
    e.university,
    e.degree_level,
    e.start_date,
    e.graduation_date,
    e.description
FROM Education e
INNER JOIN JobSeekerProfile jsp ON e.seeker_id = jsp.seeker_id
ORDER BY e.seeker_id, e.start_date;

-- 4. Kiểm tra degree_level distinct (để test filter)
SELECT DISTINCT degree_level FROM Education;
-- 5. Kiểm tra subquery MAX(degree_level) dùng trong filter
SELECT 
    seeker_id, 
    MAX(degree_level) as highest_degree
FROM Education 
GROUP BY seeker_id;

-- 8. Chèn dữ liệu vào Applications (sửa lỗi syntax)
INSERT INTO Applications (job_id, seeker_id, status, applied_at, last_status_at, cv_url, note)
SELECT 
    jp.job_id,
    jsp.seeker_id,
    app.status,
    DATEADD(day, app.days_ago_applied, GETDATE()) as applied_at,
    DATEADD(day, app.days_ago_status, GETDATE()) as last_status_at,
    app.cv_url,
    app.note
FROM (VALUES
    ('Backend Developer', 'nguyenvana', 'submitted', 0, 0, '/cv/cv1.pdf', N'Ứng tuyển Backend'),
    ('Frontend Developer', 'tranthib', 'reviewed', -1, 0, '/cv/cv2.pdf', N'Đã review CV'),
    ('Fullstack Developer', 'levanc', 'interview', -2, 0, '/cv/cv3.pdf', N'Đang phỏng vấn')
) AS app(position, username, status, days_ago_applied, days_ago_status, cv_url, note)
INNER JOIN JobsPosting jp ON app.position = jp.position
INNER JOIN Users tu ON app.username = tu.username
INNER JOIN JobSeekerProfile jsp ON tu.user_id = jsp.user_id;

-- Kiểm tra Applications
SELECT * FROM Applications;

-- Kiểm tra join để xem filter có hoạt động không
SELECT 
    a.application_id,
    jsp.fullname as candidate_name,
    jp.position,
    a.status,
    edu.degree_level,
    jsp.location,
    jsp.experience_years,
    CASE WHEN eb.bookmark_id IS NOT NULL THEN 1 ELSE 0 END as saved
FROM Applications a
INNER JOIN JobSeekerProfile jsp ON a.seeker_id = jsp.seeker_id
INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
LEFT JOIN (
    SELECT seeker_id, MAX(degree_level) AS degree_level 
    FROM Education 
    GROUP BY seeker_id
) edu ON jsp.seeker_id = edu.seeker_id
LEFT JOIN EmployerBookmarks eb ON a.application_id = eb.application_id AND eb.employer_id = 1
ORDER BY a.applied_at DESC;
-- 5. Chèn dữ liệu vào EmployerBookmarks (nếu cần)
INSERT INTO EmployerBookmarks (employer_id, application_id) VALUES
(1, 1),
(1, 3);

-- Kiểm tra EmployerBookmarks
SELECT * FROM EmployerBookmarks;

-- Kiểm tra tất cả dữ liệu đã chèn
SELECT 
    'Users' as Table_Name, COUNT(*) as Count FROM Users
UNION ALL SELECT 'JobSeekerProfile', COUNT(*) FROM JobSeekerProfile
UNION ALL SELECT 'EmployerProfile', COUNT(*) FROM EmployerProfile  
UNION ALL SELECT 'JobsPosting', COUNT(*) FROM JobsPosting
UNION ALL SELECT 'Education', COUNT(*) FROM Education
UNION ALL SELECT 'Applications', COUNT(*) FROM Applications
UNION ALL SELECT 'EmployerBookmarks', COUNT(*) FROM EmployerBookmarks;

SELECT * FROM Applications
ORDER BY applied_at DESC, applied_at DESC;
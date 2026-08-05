-- School Competition Module Schema

CREATE TABLE IF NOT EXISTS school_competitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id VARCHAR(50) NOT NULL UNIQUE, -- e.g. COMP-2026-AUG-001
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL, -- Public Speaking, Science Project, Kojo, etc.
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    verification_code VARCHAR(50) NOT NULL, -- Single shared unique code for all participating schools
    is_live BOOLEAN NOT NULL DEFAULT TRUE,
    winner_announcement_mode VARCHAR(20) NOT NULL DEFAULT 'AUTOMATIC', -- AUTOMATIC or MANUAL
    status VARCHAR(20) NOT NULL DEFAULT 'LIVE', -- DRAFT, LIVE, EVALUATION, COMPLETED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS school_competition_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id VARCHAR(64) NOT NULL UNIQUE,
    competition_id VARCHAR(50) NOT NULL,
    student_name VARCHAR(150) NOT NULL,
    school_name VARCHAR(255) NOT NULL,
    class_grade VARCHAR(50) NOT NULL,
    roll_number VARCHAR(50) NOT NULL,
    group_category VARCHAR(100) NOT NULL, -- Age/Class Group
    entry_title VARCHAR(255),
    entry_description TEXT,
    video_url VARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED', -- SUBMITTED, UNDER_REVIEW, WINNER_ANNOUNCED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (competition_id) REFERENCES school_competitions(competition_id) ON DELETE CASCADE,
    UNIQUE KEY uk_comp_student (competition_id, school_name, roll_number)
);

CREATE TABLE IF NOT EXISTS judge_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    judge_id VARCHAR(64) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_JUDGE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS competition_judge_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id VARCHAR(50) NOT NULL,
    judge_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (competition_id) REFERENCES school_competitions(competition_id) ON DELETE CASCADE,
    FOREIGN KEY (judge_id) REFERENCES judge_accounts(judge_id) ON DELETE CASCADE,
    UNIQUE KEY uk_comp_judge (competition_id, judge_id)
);

CREATE TABLE IF NOT EXISTS judge_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id VARCHAR(64) NOT NULL,
    judge_id VARCHAR(64) NOT NULL,
    appearance_score INT NOT NULL DEFAULT 0,
    content_score INT NOT NULL DEFAULT 0,
    confidence_score INT NOT NULL DEFAULT 0,
    criteria4_score INT NOT NULL DEFAULT 0,
    criteria5_score INT NOT NULL DEFAULT 0,
    total_score INT NOT NULL DEFAULT 0,
    remarks TEXT NOT NULL, -- Mandatory remarks/description
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES school_competition_submissions(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (judge_id) REFERENCES judge_accounts(judge_id) ON DELETE CASCADE,
    UNIQUE KEY uk_submission_judge (submission_id, judge_id)
);

CREATE TABLE IF NOT EXISTS school_competition_winners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id VARCHAR(50) NOT NULL,
    group_category VARCHAR(100) NOT NULL,
    rank_position INT NOT NULL, -- 1, 2, 3
    submission_id VARCHAR(64) NOT NULL,
    total_aggregate_score INT NOT NULL,
    announced_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM', -- SYSTEM, SUPER_ADMIN, STATE_ADMIN
    announced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (competition_id) REFERENCES school_competitions(competition_id) ON DELETE CASCADE,
    FOREIGN KEY (submission_id) REFERENCES school_competition_submissions(submission_id) ON DELETE CASCADE,
    UNIQUE KEY uk_comp_group_rank (competition_id, group_category, rank_position)
);

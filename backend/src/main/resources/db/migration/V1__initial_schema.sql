-- ============================================================
-- V1: Initial schema
-- Tables: users, modules, lessons, module_lessons, user_lesson_progress
-- ============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- Custom enum types
-- ============================================================
CREATE TYPE content_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
CREATE TYPE lesson_progress_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE content_format AS ENUM ('HTML', 'MARKDOWN');

-- ============================================================
-- Tables
-- ============================================================

CREATE TABLE users (
    id               UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    google_id        TEXT        NOT NULL UNIQUE,
    email            TEXT        NOT NULL UNIQUE,
    display_name     TEXT,
    avatar_url       TEXT,
    email_verified   BOOLEAN     NOT NULL DEFAULT false,
    role             VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at       TIMESTAMPTZ
);

CREATE TABLE modules (
    id               UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
    title            TEXT             NOT NULL,
    description      TEXT,
    thumbnail_url    TEXT,
    status           content_status   NOT NULL DEFAULT 'DRAFT',
    sort_order       INTEGER          NOT NULL DEFAULT 0,
    if_free_preview  BOOLEAN          NOT NULL DEFAULT false,
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE TABLE lessons (
    id               UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
    title            TEXT             NOT NULL,
    content          TEXT,
    status           content_status   NOT NULL DEFAULT 'DRAFT',
    content_format   content_format   NOT NULL DEFAULT 'MARKDOWN',
    video_url        TEXT,
    if_free_preview  BOOLEAN          NOT NULL DEFAULT false,
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE TABLE module_lessons (
    module_id   UUID        NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    lesson_id   UUID        NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    sort_order  INTEGER     NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (module_id, lesson_id)
);

CREATE TABLE user_lesson_progress (
    user_id        UUID                    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id      UUID                    NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    progress_pct   DECIMAL(5, 2)           NOT NULL DEFAULT 0,
    status         lesson_progress_status  NOT NULL DEFAULT 'NOT_STARTED',
    started_at     TIMESTAMPTZ,
    completed_at   TIMESTAMPTZ,
    last_viewed_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ             NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ             NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, lesson_id)
);

-- ============================================================
-- Indexes on foreign key columns
-- ============================================================
CREATE INDEX idx_module_lessons_lesson_id    ON module_lessons(lesson_id);
CREATE INDEX idx_user_lesson_progress_lesson ON user_lesson_progress(lesson_id);

-- ============================================================
-- Auto-update updated_at trigger
-- ============================================================
CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at_users
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_modules
    BEFORE UPDATE ON modules
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_lessons
    BEFORE UPDATE ON lessons
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_user_lesson_progress
    BEFORE UPDATE ON user_lesson_progress
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

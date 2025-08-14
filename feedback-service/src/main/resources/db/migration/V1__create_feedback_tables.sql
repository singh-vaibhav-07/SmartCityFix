-- feedback-service/src/main/resources/db/migration/V1__create_feedback_tables.sql
CREATE TABLE feedbacks (
    id UUID PRIMARY KEY,
    complaint_id UUID NOT NULL,
    user_id UUID NOT NULL,
    department_id UUID NOT NULL,
    rating INTEGER NOT NULL,
    comment VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feedback_responses (
    id UUID PRIMARY KEY,
    feedback_id UUID NOT NULL REFERENCES feedbacks(id) ON DELETE CASCADE,
    responder_id UUID NOT NULL,
    response VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE department_ratings (
    department_id UUID PRIMARY KEY,
    average_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_ratings INTEGER NOT NULL DEFAULT 0,
    rating1_count INTEGER NOT NULL DEFAULT 0,
    rating2_count INTEGER NOT NULL DEFAULT 0,
    rating3_count INTEGER NOT NULL DEFAULT 0,
    rating4_count INTEGER NOT NULL DEFAULT 0,
    rating5_count INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feedbacks_complaint_id ON feedbacks(complaint_id);
CREATE INDEX idx_feedbacks_user_id ON feedbacks(user_id);
CREATE INDEX idx_feedbacks_department_id ON feedbacks(department_id);
CREATE INDEX idx_feedbacks_status ON feedbacks(status);
CREATE INDEX idx_feedback_responses_feedback_id ON feedback_responses(feedback_id);
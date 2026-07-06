-- Module: Event Approval Workflow (optional HOD step)
ALTER TABLE users DROP CONSTRAINT chk_users_role;
ALTER TABLE users ADD CONSTRAINT chk_users_role
    CHECK (role IN ('SUPER_ADMIN', 'FACULTY_COORDINATOR', 'HOD', 'STUDENT_ORGANIZER', 'STUDENT'));

ALTER TABLE departments ADD COLUMN hod_id BIGINT;
ALTER TABLE departments ADD COLUMN hod_approval_required BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE departments ADD CONSTRAINT fk_departments_hod FOREIGN KEY (hod_id) REFERENCES users (id);

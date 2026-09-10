INSERT INTO roles (id, code, name) VALUES
  (gen_random_uuid(), 'SUPER_ADMIN', 'Platform Super Administrator'),
  (gen_random_uuid(), 'SCHOOL_ADMIN', 'School Administrator'),
  (gen_random_uuid(), 'TEACHER', 'Teacher'),
  (gen_random_uuid(), 'ACCOUNTANT', 'Accountant'),
  (gen_random_uuid(), 'PARENT', 'Parent'),
  (gen_random_uuid(), 'STUDENT', 'Student')
ON CONFLICT (code) DO NOTHING;

INSERT INTO cybermanager.cm_users (
    id,
    email,
    first_name,
    last_name,
    password_hash,
    status
)
SELECT
    '11111111-1111-1111-1111-111111111111'::UUID,
    'admin@cybermanager.local',
    'Admin',
    'CyberManager',
    '$2a$10$Gd0r2BRovkxDepG0s8KeNOm7cNGF0gMxGXTB9IR8HTeGAtjttebce',
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM cybermanager.cm_users
    WHERE id = '11111111-1111-1111-1111-111111111111'::UUID
       OR email = 'admin@cybermanager.local'
);

INSERT INTO cybermanager.cm_user_roles (user_id, role)
SELECT users.id, 'ADMIN'
FROM cybermanager.cm_users users
WHERE users.email = 'admin@cybermanager.local'
  AND NOT EXISTS (
      SELECT 1
      FROM cybermanager.cm_user_roles roles
      WHERE roles.user_id = users.id
        AND roles.role = 'ADMIN'
  );

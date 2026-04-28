-- Optional local-only test user (password: DevLocal123!)
-- Run as JASOLAR_BUDGET after schema exists (same session as other seed scripts).
-- BCrypt hash generated with Spring BCryptPasswordEncoder (strength 10).

SET DEFINE OFF
WHENEVER SQLERROR EXIT SQL.SQLCODE

ALTER SESSION SET CURRENT_SCHEMA = JASOLAR_BUDGET;

MERGE INTO system_user t
USING (SELECT 'dev_local' user_name FROM dual) s
ON (t.user_name = s.user_name)
WHEN MATCHED THEN
  UPDATE SET
    t.pwd = '$2a$10$2HK8NAtaZIbKJrH/.4qIJu2vdt6yi1jKmmU1XmlMF87i6bPNv0Uxa',
    t.deleted = 0,
    t.status = '1',
    t.display_name = 'Local Dev',
    t.updater = 'system',
    t.update_time = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (
    id, user_name, display_name, pwd, status,
    creator, create_time, updater, update_time, deleted
  ) VALUES (
    99900000000000001,
    'dev_local',
    'Local Dev',
    '$2a$10$2HK8NAtaZIbKJrH/.4qIJu2vdt6yi1jKmmU1XmlMF87i6bPNv0Uxa',
    '1',
    'system',
    SYSTIMESTAMP,
    'system',
    SYSTIMESTAMP,
    0
  );

COMMIT;

-- 登录后需要菜单权限时，再执行（从 GBCS009 复制用户组关联）：
-- @005_copy_user_group_r_from_user.sql

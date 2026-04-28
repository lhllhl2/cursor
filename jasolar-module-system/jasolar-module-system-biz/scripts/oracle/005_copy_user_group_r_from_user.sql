-- 将源用户的 system_user_group_r（菜单/报表/组织等类型）复制到目标用户，用于本地/测试：
-- dev_local 登录后 currentUserInfo 依赖 type='1' 的用户组才有菜单，否则易跳 /home 404。
--
-- 默认：源 = GBCS009（种子 id 34246689082572800），目标 = dev_local（见 004_dev_local_login_user.sql）。
-- 若库中工号不同，可先改下面两个常量再执行。
--
-- sqlplus jasolar_budget/...@//host:1521/FREEPDB1 @005_copy_user_group_r_from_user.sql

SET DEFINE OFF
WHENEVER SQLERROR EXIT SQL.SQLCODE

-- sqlplus 会逐段执行：勿把 ALTER SESSION 与匿名块写在同一次 parse 里（部分客户端会 ORA-03405）。
ALTER SESSION SET CURRENT_SCHEMA = JASOLAR_BUDGET;

DECLARE
  c_src_user   CONSTANT VARCHAR2(64) := 'GBCS009';
  c_tgt_user   CONSTANT VARCHAR2(64) := 'dev_local';
  v_src_id     NUMBER(19);
  v_tgt_id     NUMBER(19);
  v_max_id     NUMBER(19);
BEGIN
  SELECT id INTO v_src_id FROM system_user
   WHERE user_name = c_src_user AND NVL(deleted, 0) = 0 AND ROWNUM = 1;

  SELECT id INTO v_tgt_id FROM system_user
   WHERE user_name = c_tgt_user AND NVL(deleted, 0) = 0 AND ROWNUM = 1;

  -- 目标用户旧关联软删（避免与源重复主键）
  UPDATE system_user_group_r
     SET deleted = 1,
         updater = 'system',
         update_time = SYSTIMESTAMP
   WHERE user_id = v_tgt_id AND NVL(deleted, 0) = 0;

  SELECT NVL(MAX(id), 0) INTO v_max_id FROM system_user_group_r;

  INSERT INTO system_user_group_r (
    id, user_id, group_id, type,
    creator, create_time, updater, update_time, deleted
  )
  SELECT
    v_max_id + ROW_NUMBER() OVER (ORDER BY r.group_id, r.type),
    v_tgt_id,
    r.group_id,
    r.type,
    'system',
    SYSTIMESTAMP,
    'system',
    SYSTIMESTAMP,
    0
  FROM system_user_group_r r
  WHERE r.user_id = v_src_id
    AND NVL(r.deleted, 0) = 0;

  COMMIT;
  DBMS_OUTPUT.PUT_LINE('OK: copied group_r from ' || c_src_user
    || ' (id=' || v_src_id || ') to ' || c_tgt_user || ' (id=' || v_tgt_id || ')');
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    ROLLBACK;
    RAISE_APPLICATION_ERROR(-20001,
      '源或目标用户不存在：src=' || c_src_user || ' tgt=' || c_tgt_user);
END;
/

EXIT;

-- Align JASOLAR_BUDGET with jasolar-module-system entities + resources/sql seed columns.
-- Run as JASOLAR_BUDGET: sqlplus jasolar_budget/pass@//host:1521/FREEPDB1 @this_file

WHENEVER SQLERROR EXIT SQL.SQLCODE

-- ========== SYSTEM_USER (SystemUserDo + BaseDO + columns used by seed INSERTs) ==========
BEGIN
  EXECUTE IMMEDIATE q'[
    CREATE TABLE system_user (
      id                  NUMBER(19) NOT NULL,
      user_name           VARCHAR2(128 CHAR),
      pwd                 VARCHAR2(256 CHAR),
      pwd_changed         VARCHAR2(32 CHAR),
      display_name        VARCHAR2(128 CHAR),
      gender              VARCHAR2(16 CHAR),
      email               VARCHAR2(256 CHAR),
      phone_region        VARCHAR2(32 CHAR),
      phone_number        VARCHAR2(64 CHAR),
      organization_code   VARCHAR2(128 CHAR),
      status              VARCHAR2(16 CHAR),
      direct_manager_code VARCHAR2(128 CHAR),
      card_no             VARCHAR2(128 CHAR),
      card_type           VARCHAR2(64 CHAR),
      company_code        VARCHAR2(128 CHAR),
      office_location     VARCHAR2(256 CHAR),
      induction_date      VARCHAR2(64 CHAR),
      leave_date          VARCHAR2(64 CHAR),
      post                VARCHAR2(128 CHAR),
      birthday            VARCHAR2(64 CHAR),
      leave_status        VARCHAR2(32 CHAR),
      emp_english_name    VARCHAR2(128 CHAR),
      employment_status   VARCHAR2(64 CHAR),
      creator             VARCHAR2(128 CHAR),
      create_time         TIMESTAMP(6),
      updater             VARCHAR2(128 CHAR),
      update_time         TIMESTAMP(6),
      deleted             NUMBER(1) DEFAULT 0,
      CONSTRAINT pk_system_user PRIMARY KEY (id)
    )
  ]';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -955 THEN RAISE; END IF; -- ORA-00955: name already used
END;
/

-- ========== SYSTEM_MANAGE_ORG (SystemManageOrgDO + BaseDO) ==========
BEGIN
  EXECUTE IMMEDIATE q'[
    CREATE TABLE system_manage_org (
      id                     NUMBER(19) NOT NULL,
      name                   VARCHAR2(256 CHAR),
      code                   VARCHAR2(128 CHAR),
      p_code                 VARCHAR2(128 CHAR),
      p_name                 VARCHAR2(256 CHAR),
      is_last_lvl            NUMBER(1),
      org_type               VARCHAR2(64 CHAR),
      is_approval_last_lvl   NUMBER(1),
      script_type            VARCHAR2(64 CHAR),
      employee_no            VARCHAR2(128 CHAR),
      creator                VARCHAR2(128 CHAR),
      create_time            TIMESTAMP(6),
      updater                VARCHAR2(128 CHAR),
      update_time            TIMESTAMP(6),
      deleted                NUMBER(1) DEFAULT 0,
      CONSTRAINT pk_system_manage_org PRIMARY KEY (id)
    )
  ]';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

-- ========== SYSTEM_PROJECT: add column for seed + SystemProject.parentProjectName ==========
DECLARE
  c NUMBER;
BEGIN
  SELECT COUNT(*) INTO c FROM user_tab_columns
   WHERE table_name = 'SYSTEM_PROJECT' AND column_name = 'PARENT_PROJECT_NAME';
  IF c = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE system_project ADD (parent_project_name VARCHAR2(512 CHAR))';
  END IF;
END;
/

PROMPT Done: SYSTEM_USER, SYSTEM_MANAGE_ORG, SYSTEM_PROJECT.PARENT_PROJECT_NAME
EXIT;

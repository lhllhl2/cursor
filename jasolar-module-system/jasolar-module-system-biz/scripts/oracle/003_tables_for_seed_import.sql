-- Tables required by resources/sql/*.sql (JASOLAR_BUDGET), from entity fields + BaseDO.
-- Run as: sqlplus jasolar_budget/pass@//host:1521/FREEPDB1 @003_tables_for_seed_import.sql

WHENEVER SQLERROR EXIT SQL.SQLCODE

-- system_dict
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_dict (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    code VARCHAR2(128 CHAR),
    title VARCHAR2(256 CHAR),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_dict_label
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_dict_label (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    dict_id NUMBER(19), field_key VARCHAR2(256 CHAR), field_label VARCHAR2(512 CHAR),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_role
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_role (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    name VARCHAR2(128 CHAR), code VARCHAR2(128 CHAR), status VARCHAR2(16 CHAR), remark VARCHAR2(512 CHAR),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_menu (MenuDO + BaseDO)
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_menu (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    name VARCHAR2(128 CHAR), path VARCHAR2(512 CHAR), type VARCHAR2(32 CHAR), pid NUMBER(19),
    auth_code VARCHAR2(256 CHAR), redirect VARCHAR2(512 CHAR), component VARCHAR2(512 CHAR),
    title VARCHAR2(256 CHAR), icon VARCHAR2(256 CHAR), active_icon VARCHAR2(256 CHAR),
    status NUMBER(10), menu_order NUMBER(10), active_path VARCHAR2(512 CHAR),
    affix_tab NUMBER(10), affix_tab_order NUMBER(10), badge VARCHAR2(128 CHAR),
    badge_type VARCHAR2(64 CHAR), badge_variants VARCHAR2(128 CHAR),
    hide_children_in_menu NUMBER(10), hide_in_breadcrumb NUMBER(10), hide_in_menu NUMBER(10),
    hide_in_tab NUMBER(10), iframe_src VARCHAR2(1024 CHAR), keep_alive NUMBER(10),
    link VARCHAR2(1024 CHAR), max_num_of_open_tab NUMBER(10), no_basic_layout NUMBER(10),
    open_in_new_window NUMBER(10), query_params CLOB, code VARCHAR2(256 CHAR),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_i18n_menu
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_i18n_menu (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    menu_id NUMBER(19), title VARCHAR2(256 CHAR), locale VARCHAR2(32 CHAR), json_data CLOB,
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_role_menu_r
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_role_menu_r (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    role_id NUMBER(19), menu_id NUMBER(19),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_user_group
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_user_group (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    code VARCHAR2(4000 CHAR), name VARCHAR2(4000 CHAR), type VARCHAR2(32 CHAR), remark VARCHAR2(512 CHAR),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_user_group_role_r
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_user_group_role_r (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    group_id NUMBER(19), role_id NUMBER(19),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_user_group_r
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_user_group_r (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    user_id NUMBER(19), group_id NUMBER(19), type VARCHAR2(32 CHAR),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_org
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_org (
    id VARCHAR2(128 CHAR) NOT NULL PRIMARY KEY,
    org_name VARCHAR2(256 CHAR), parent_id VARCHAR2(128 CHAR),
    company_code VARCHAR2(128 CHAR), company_name VARCHAR2(256 CHAR), cost_org_name VARCHAR2(256 CHAR),
    org_head_id VARCHAR2(128 CHAR), org_fg_id VARCHAR2(128 CHAR), org_attribute VARCHAR2(128 CHAR),
    org_full_path VARCHAR2(1024 CHAR), root_node VARCHAR2(64 CHAR),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
-- system_log
BEGIN EXECUTE IMMEDIATE q'[
  CREATE TABLE system_log (
    id NUMBER(19) NOT NULL PRIMARY KEY,
    user_name VARCHAR2(128 CHAR), display_name VARCHAR2(256 CHAR), ip VARCHAR2(128 CHAR), log_type VARCHAR2(64 CHAR),
    creator VARCHAR2(128 CHAR), create_time TIMESTAMP(6), updater VARCHAR2(128 CHAR),
    update_time TIMESTAMP(6), deleted NUMBER(1) DEFAULT 0
  )]'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/

PROMPT 003: seed support tables created (or already exist).
EXIT;

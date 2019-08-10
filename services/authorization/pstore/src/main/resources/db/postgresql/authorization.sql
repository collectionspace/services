CREATE TABLE IF NOT EXISTS accounts_roles (
	hjid INT8 NOT NULL PRIMARY KEY,
	account_id VARCHAR(128) NOT NULL,
	created_at TIMESTAMP NOT NULL,
	role_id VARCHAR(128) NOT NULL,
	role_name VARCHAR(255) NOT NULL,
	screen_name VARCHAR(255),
	user_id VARCHAR(128) NOT NULL,
	UNIQUE (account_id, role_id)
);

CREATE TABLE IF NOT EXISTS permissions (
	csid VARCHAR(128) NOT NULL PRIMARY KEY,
	action_group VARCHAR(128),
	attribute_name VARCHAR(128),
	created_at TIMESTAMP NOT NULL,
	description VARCHAR(255),
	effect VARCHAR(32) NOT NULL,
	metadata_protection VARCHAR(255),
	actions_protection VARCHAR(255),
	resource_name VARCHAR(128) NOT NULL,
	tenant_id VARCHAR(128) NOT NULL,
	updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permissions_actions (
	hjid INT8 NOT NULL PRIMARY KEY,
	name VARCHAR(128) NOT NULL,
	objectidentity VARCHAR(128) NOT NULL,
	objectidentityresource VARCHAR(128) NOT NULL,
	action__permission_csid VARCHAR(128),
	FOREIGN KEY (action__permission_csid) REFERENCES permissions
);

CREATE TABLE IF NOT EXISTS permissions_roles (
	hjid INT8 NOT NULL PRIMARY KEY,
	actiongroup VARCHAR(255),
	created_at TIMESTAMP NOT NULL,
	permission_id VARCHAR(128) NOT NULL,
	permission_resource VARCHAR(255),
	role_id VARCHAR(128) NOT NULL,
	role_name VARCHAR(255),
	UNIQUE (permission_id, role_id)
);

CREATE TABLE IF NOT EXISTS roles (
	csid VARCHAR(128) NOT NULL PRIMARY KEY,
	created_at TIMESTAMP NOT NULL,
	description VARCHAR(255),
	displayname VARCHAR(200) NOT NULL,
	rolegroup VARCHAR(255),
	rolename VARCHAR(200) NOT NULL,
	tenant_id VARCHAR(128) NOT NULL,
	metadata_protection VARCHAR(255),
	perms_protection VARCHAR(255),
	updated_at TIMESTAMP,
	UNIQUE (rolename, tenant_id),
	UNIQUE (displayname, tenant_id)
);

CREATE SEQUENCE IF NOT EXISTS hibernate_sequence;

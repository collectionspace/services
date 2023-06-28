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

-- Spring Security Authorization Server (OAuth) tables

CREATE TABLE IF NOT EXISTS oauth2_authorization (
	id varchar(100) NOT NULL,
	registered_client_id varchar(100) NOT NULL,
	principal_name varchar(200) NOT NULL,
	authorization_grant_type varchar(100) NOT NULL,
	authorized_scopes varchar(1000) DEFAULT NULL,
	attributes text DEFAULT NULL,
	state varchar(500) DEFAULT NULL,
	authorization_code_value text DEFAULT NULL,
	authorization_code_issued_at timestamp DEFAULT NULL,
	authorization_code_expires_at timestamp DEFAULT NULL,
	authorization_code_metadata text DEFAULT NULL,
	access_token_value text DEFAULT NULL,
	access_token_issued_at timestamp DEFAULT NULL,
	access_token_expires_at timestamp DEFAULT NULL,
	access_token_metadata text DEFAULT NULL,
	access_token_type varchar(100) DEFAULT NULL,
	access_token_scopes varchar(1000) DEFAULT NULL,
	oidc_id_token_value text DEFAULT NULL,
	oidc_id_token_issued_at timestamp DEFAULT NULL,
	oidc_id_token_expires_at timestamp DEFAULT NULL,
	oidc_id_token_metadata text DEFAULT NULL,
	refresh_token_value text DEFAULT NULL,
	refresh_token_issued_at timestamp DEFAULT NULL,
	refresh_token_expires_at timestamp DEFAULT NULL,
	refresh_token_metadata text DEFAULT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS oauth2_registered_client (
	id varchar(100) NOT NULL,
	client_id varchar(100) NOT NULL,
	client_id_issued_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	client_secret varchar(200) DEFAULT NULL,
	client_secret_expires_at timestamp DEFAULT NULL,
	client_name varchar(200) NOT NULL,
	client_authentication_methods varchar(1000) NOT NULL,
	authorization_grant_types varchar(1000) NOT NULL,
	redirect_uris varchar(1000) DEFAULT NULL,
	scopes varchar(1000) NOT NULL,
	client_settings varchar(2000) NOT NULL,
	token_settings varchar(2000) NOT NULL,
	PRIMARY KEY (id)
);

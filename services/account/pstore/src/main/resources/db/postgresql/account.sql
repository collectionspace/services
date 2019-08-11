CREATE TABLE IF NOT EXISTS accounts_common (
	csid VARCHAR(128) NOT NULL PRIMARY KEY,
	created_at TIMESTAMP NOT NULL,
	email VARCHAR(255) NOT NULL,
	mobile VARCHAR(255),
	person_ref_name VARCHAR(255),
	phone VARCHAR(255),
	screen_name VARCHAR(128) NOT NULL,
	status VARCHAR(15) NOT NULL,
	updated_at TIMESTAMP,
	userid VARCHAR(128) NOT NULL UNIQUE,
	metadata_protection VARCHAR(255),
	roles_protection VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS accounts_tenants (
	hjid INT8 NOT NULL PRIMARY KEY,
	tenant_id VARCHAR(128) NOT NULL,
	tenants_accounts_common_csid VARCHAR(128),
	FOREIGN KEY (tenants_accounts_common_csid) REFERENCES accounts_common
);

CREATE TABLE IF NOT EXISTS tenants (
	id VARCHAR(128) NOT NULL PRIMARY KEY,
	created_at TIMESTAMP NOT NULL,
	name VARCHAR(255) NOT NULL,
	config_md5hash VARCHAR(255),
	authorities_initialized BOOLEAN NOT NULL,
	disabled BOOLEAN NOT NULL,
	updated_at TIMESTAMP
);

UPDATE tenants SET authorities_initialized = FALSE;

CREATE SEQUENCE IF NOT EXISTS hibernate_sequence;

CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(128) NOT NULL PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  lastlogin TIMESTAMP,
  passwd VARCHAR(128) NOT NULL,
  salt VARCHAR(128),
  updated_at TIMESTAMP
);

-- Upgrade older users tables to 6.0

ALTER TABLE users ADD COLUMN IF NOT EXISTS lastlogin TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS salt VARCHAR(128);

-- Upgrade older users tables to 8.0

UPDATE users SET passwd = concat('{SHA-256}', '{', salt, '}', passwd)  WHERE left(passwd, 1) <> '{';

-- Create tokens table required in 8.0

CREATE TABLE IF NOT EXISTS tokens (
  id VARCHAR(128) NOT NULL PRIMARY KEY,
  account_csid VARCHAR(128) NOT NULL,
  tenant_id VARCHAR(128) NOT NULL,
  expire_seconds INTEGER NOT NULL,
  enabled BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

-- Upgrade older acl_object_identity tables to 8.0

ALTER TABLE acl_object_identity ALTER COLUMN object_id_identity TYPE VARCHAR (36);

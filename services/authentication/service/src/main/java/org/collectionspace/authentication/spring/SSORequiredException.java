package org.collectionspace.authentication.spring;

import org.springframework.security.core.AuthenticationException;

public class SSORequiredException extends AuthenticationException {

  public SSORequiredException(String msg) {
    super(msg);
  } 
}

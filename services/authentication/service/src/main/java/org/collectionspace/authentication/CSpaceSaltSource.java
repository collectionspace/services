package org.collectionspace.authentication;

import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.core.userdetails.UserDetails;

public class CSpaceSaltSource extends ReflectionSaltSource {

	@Override
	public Object getSalt(UserDetails user) {
		return super.getSalt(user);
	}

}

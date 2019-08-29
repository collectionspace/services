package org.collectionspace.authentication;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

public class CSpaceAuthenticationSuccessEvent implements ApplicationListener<AuthenticationSuccessEvent> {

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		// TODO Auto-generated method stub
		System.out.println(); //org.springframework.security.authentication.UsernamePasswordAuthenticationToken@8a633e91: Principal: org.collectionspace.authentication.CSpaceUser@b122ec20: Username: admin@core.collectionspace.org; Password: [PROTECTED]; Enabled: true; AccountNonExpired: true; credentialsNonExpired: true; AccountNonLocked: true; Granted Authorities: ROLE_1_TENANT_ADMINISTRATOR,ROLE_SPRING_ADMIN; Credentials: [PROTECTED]; Authenticated: true; Details: {grant_type=password, username=admin@core.collectionspace.org}; Granted Authorities: ROLE_1_TENANT_ADMINISTRATOR, ROLE_SPRING_ADMIN
	}

}

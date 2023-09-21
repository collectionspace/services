package org.collectionspace.services.common.security;

import java.net.MalformedURLException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.collectionspace.authentication.CSpaceUser;
import org.collectionspace.authentication.spring.CSpaceJwtAuthenticationToken;
import org.collectionspace.authentication.spring.CSpaceLogoutSuccessHandler;
import org.collectionspace.authentication.spring.CSpacePasswordEncoderFactory;
import org.collectionspace.authentication.spring.CSpaceUserAttributeFilter;
import org.collectionspace.authentication.spring.CSpaceUserDetailsService;
import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.config.OAuthAuthorizationGrantTypeEnum;
import org.collectionspace.services.config.OAuthClientAuthenticationMethodEnum;
import org.collectionspace.services.config.OAuthClientSettingsType;
import org.collectionspace.services.config.OAuthClientType;
import org.collectionspace.services.config.OAuthScopeEnum;
import org.collectionspace.services.config.OAuthTokenSettingsType;
import org.collectionspace.services.config.OAuthType;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.authentication.realm.db.CSpaceDbRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AnonymousConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	public static final String LOGIN_FORM_URL = "/login";
	public static final String LOGOUT_FORM_URL = "/logout";

	// The default login success URL, handled by LoginResource.
	public static final String DEFAULT_LOGIN_SUCCESS_URL = "/";

	private CorsConfiguration defaultCorsConfiguration = null;
	private CorsConfiguration oauthServerCorsConfiguration = null;

	private void initializeCorsConfigurations() {
		ServiceConfig serviceConfig = ServiceMain.getInstance().getServiceConfig();
		Duration maxAge = ConfigUtils.getCorsMaxAge(serviceConfig);
		List<String> allowedOrigins = ConfigUtils.getCorsAllowedOrigins(serviceConfig);

		if (this.defaultCorsConfiguration == null) {
			this.defaultCorsConfiguration = defaultCorsConfiguration(allowedOrigins, maxAge);
		}

		if (this.oauthServerCorsConfiguration == null) {
			this.oauthServerCorsConfiguration = oauthServerCorsConfiguration(allowedOrigins, maxAge);
		}
	}

	private CorsConfiguration defaultCorsConfiguration(List<String> allowedOrigins, Duration maxAge) {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(allowedOrigins);

		if (maxAge != null) {
			configuration.setMaxAge(maxAge);
		}

		configuration.setAllowedHeaders(Arrays.asList(
			"Authorization",
			"Content-Type"
		));

		configuration.setAllowedMethods(Arrays.asList(
			HttpMethod.POST.toString(),
			HttpMethod.GET.toString(),
			HttpMethod.PUT.toString(),
			HttpMethod.DELETE.toString()
		));

		configuration.setExposedHeaders(Arrays.asList(
			"Location",
			"Content-Disposition",
			"Www-Authenticate"
		));

		return configuration;
	}

	private CorsConfiguration oauthServerCorsConfiguration(List<String> allowedOrigins, Duration maxAge) {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(allowedOrigins);

		if (maxAge != null) {
			configuration.setMaxAge(maxAge);
		}

		configuration.setAllowedMethods(Arrays.asList(
			HttpMethod.POST.toString(),
			HttpMethod.GET.toString()
		));

		return configuration;
	}

	@Bean
	public JdbcOperations jdbcOperations(DataSource cspaceDataSource) {
		return new JdbcTemplate(cspaceDataSource);
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}

	@Bean
	public OAuth2AuthorizationService authorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		this.initializeCorsConfigurations();

		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

		return http
			.exceptionHandling(new Customizer<ExceptionHandlingConfigurer<HttpSecurity>>() {
				@Override
				public void customize(ExceptionHandlingConfigurer<HttpSecurity> configurer) {
					configurer.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(LOGIN_FORM_URL));
				}
			})
			.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
				@Override
				public void customize(CorsConfigurer<HttpSecurity> configurer) {
					configurer.configurationSource(new CorsConfigurationSource() {
						@Override
						@Nullable
						public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
							return SecurityConfig.this.oauthServerCorsConfiguration;
						}
					});
				}
			})
			.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(
		HttpSecurity http,
		final AuthenticationManager authenticationManager,
		final UserDetailsService userDetailsService,
		final RegisteredClientRepository registeredClientRepository,
		final ApplicationEventPublisher appEventPublisher
	) throws Exception {

		this.initializeCorsConfigurations();

		http
			.authorizeHttpRequests(new Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>() {
				@Override
				public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry configurer) {
					configurer
						// Exclude the login form, which needs to be accessible anonymously.
						.requestMatchers(LOGIN_FORM_URL).permitAll()

						// Exclude the logout form, since it's harmless to log out when you're not logged in.
						.requestMatchers(LOGOUT_FORM_URL).permitAll()

						// Exclude the resource path to public items' content from AuthN and AuthZ. Lets us publish resources with anonymous access.
						.requestMatchers("/publicitems/*/*/content").permitAll()

						// Exclude the resource path to handle an account password reset request from AuthN and AuthZ. Lets us process password resets anonymous access.
						.requestMatchers("/accounts/requestpasswordreset").permitAll()

						// Exclude the resource path to account process a password resets from AuthN and AuthZ. Lets us process password resets anonymous access.
						.requestMatchers("/accounts/processpasswordreset").permitAll()

						// Exclude the resource path to request system info.
						.requestMatchers("/systeminfo").permitAll()

						// Handle CORS (preflight OPTIONS requests must be anonymous).
						.requestMatchers(HttpMethod.OPTIONS).permitAll()

						// All other paths must be authenticated.
						.anyRequest().fullyAuthenticated();
				}
			})
			.oauth2ResourceServer(new Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>>() {
				@Override
				public void customize(OAuth2ResourceServerConfigurer<HttpSecurity> configurer) {
					configurer.jwt(new Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer>() {
						@Override
						public void customize(OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer jwtConfigurer) {
							// By default, authentication results in a JwtAuthenticationToken, where the principal is a Jwt instance.
							// We want the principal to be a CSpaceUser instance, so that authentication functions will continue to
							// work as they do with basic auth and session auth. This conversion code is based on comments in
							// https://github.com/spring-projects/spring-security/issues/7834

							jwtConfigurer.jwtAuthenticationConverter(new Converter<Jwt,CSpaceJwtAuthenticationToken>() {
								@Override
								@Nullable
								public CSpaceJwtAuthenticationToken convert(Jwt jwt) {
									CSpaceUser user = null;
									String username = (String) jwt.getClaims().get("sub");

									try {
										user = (CSpaceUser) userDetailsService.loadUserByUsername(username);
									} catch (UsernameNotFoundException e) {
										user = null;
									}

									return new CSpaceJwtAuthenticationToken(jwt, user);
								}
							});
						}
					});
				}
			})
			.httpBasic(new Customizer<HttpBasicConfigurer<HttpSecurity>>() {
				@Override
				public void customize(HttpBasicConfigurer<HttpSecurity> configurer) {}
			})
			.formLogin(new Customizer<FormLoginConfigurer<HttpSecurity>>() {
				@Override
				public void customize(FormLoginConfigurer<HttpSecurity> configurer) {
					configurer
						.loginPage(LOGIN_FORM_URL)
						.defaultSuccessUrl(DEFAULT_LOGIN_SUCCESS_URL);
				}
			})
			.logout(new Customizer<LogoutConfigurer<HttpSecurity>>() {
				@Override
				public void customize(LogoutConfigurer<HttpSecurity> configurer) {
					// Add a custom logout success handler that redirects to a URL (passed as a parameter)
					// after logout.

					// TODO: This seems to be automatic in Spring Authorization Server 1.1, so it should be
					// possible to remove this when we upgrade.
					// See https://docs.spring.io/spring-authorization-server/docs/current/api/org/springframework/security/oauth2/server/authorization/client/RegisteredClient.Builder.html#postLogoutRedirectUri(java.lang.String)

					ServiceConfig serviceConfig = ServiceMain.getInstance().getServiceConfig();
					List<OAuthClientType> clientsConfig = ConfigUtils.getOAuthClientRegistrations(serviceConfig);
					Set<String> permittedRedirectUris = new HashSet<>();

					for (OAuthClientType clientConfig : clientsConfig) {
						String clientId = clientConfig.getId();
						RegisteredClient client = registeredClientRepository.findByClientId(clientId);

						permittedRedirectUris.addAll(client.getRedirectUris());
					}

					configurer
						.logoutSuccessHandler(new CSpaceLogoutSuccessHandler(LOGIN_FORM_URL + "?logout", permittedRedirectUris));
				}
			})
			.csrf(new Customizer<CsrfConfigurer<HttpSecurity>>() {
				@Override
				public void customize(CsrfConfigurer<HttpSecurity> configurer) {
					configurer.requireCsrfProtectionMatcher(new OrRequestMatcher(
						new AntPathRequestMatcher(LOGIN_FORM_URL, HttpMethod.POST.toString()),
						new AntPathRequestMatcher(AccountClient.PASSWORD_RESET_PATH, HttpMethod.POST.toString()),
						new AntPathRequestMatcher(AccountClient.PROCESS_PASSWORD_RESET_PATH, HttpMethod.POST.toString())
					));
				}
			})
			.anonymous(new Customizer<AnonymousConfigurer<HttpSecurity>>() {
				@Override
				public void customize(AnonymousConfigurer<HttpSecurity> configurer) {
					configurer.principal("anonymous");
				}
			})
			.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
				@Override
				public void customize(CorsConfigurer<HttpSecurity> configurer) {
					configurer.configurationSource(new CorsConfigurationSource() {
						@Override
						@Nullable
						public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
							return SecurityConfig.this.defaultCorsConfiguration;
						}
					});
				}
			})
			// Insert the username from the security context into a request attribute for logging.
			.addFilterBefore(new CSpaceUserAttributeFilter(), LogoutFilter.class);

		return http.build();
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(CSpacePasswordEncoderFactory.createDefaultPasswordEncoder());

		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
		return new ProviderManager(provider);
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository(JdbcOperations jdbcOperations) {
		JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcOperations);
		ServiceConfig serviceConfig = ServiceMain.getInstance().getServiceConfig();
		OAuthType oauthConfig = ConfigUtils.getOAuth(serviceConfig);
		List<OAuthClientType> clientsConfig = ConfigUtils.getOAuthClientRegistrations(serviceConfig);

		Duration defaultAccessTokenTimeToLive = Duration.parse(oauthConfig.getDefaultAccessTokenTimeToLive());

		for (OAuthClientType clientConfig : clientsConfig) {
			RegisteredClient.Builder registeredClientBuilder = RegisteredClient.withId(clientConfig.getId());

			if (clientConfig.getClientId() != null) {
				registeredClientBuilder.clientId(clientConfig.getClientId());
			}

			if (clientConfig.getClientName() != null) {
				registeredClientBuilder.clientName(clientConfig.getClientName());
			}

			if (clientConfig.getClientAuthenticationMethod() != null) {
				for (OAuthClientAuthenticationMethodEnum method : clientConfig.getClientAuthenticationMethod()) {
					registeredClientBuilder.clientAuthenticationMethod(new ClientAuthenticationMethod(method.value()));
				}
			}

			if (clientConfig.getAuthorizationGrantType() != null) {
				for (OAuthAuthorizationGrantTypeEnum type : clientConfig.getAuthorizationGrantType()) {
					registeredClientBuilder.authorizationGrantType(new AuthorizationGrantType(type.value()));
				}
			}

			if (clientConfig.getScope() != null) {
				for (OAuthScopeEnum scope : clientConfig.getScope()) {
					registeredClientBuilder.scope(scope.value());
				}
			}

			OAuthClientSettingsType clientSettingsConfig = clientConfig.getClientSettings();

			if (clientSettingsConfig != null) {
				ClientSettings.Builder clientSettingsBuilder = ClientSettings.builder();

				if (clientSettingsConfig.isRequireAuthorizationConsent() != null) {
					clientSettingsBuilder.requireAuthorizationConsent(clientSettingsConfig.isRequireAuthorizationConsent());
				}

				registeredClientBuilder.clientSettings(clientSettingsBuilder.build());
			}

			OAuthTokenSettingsType tokenSettingsConfig = clientConfig.getTokenSettings();

			if (tokenSettingsConfig != null) {
				TokenSettings.Builder tokenSettingsBuilder = TokenSettings.builder();

				if (tokenSettingsConfig.getAccessTokenTimeToLive() != null) {
					tokenSettingsBuilder.accessTokenTimeToLive(Duration.parse(tokenSettingsConfig.getAccessTokenTimeToLive()));
				} else {
					tokenSettingsBuilder.accessTokenTimeToLive(defaultAccessTokenTimeToLive);
				}

				registeredClientBuilder.tokenSettings(tokenSettingsBuilder.build());
			}

			if (clientConfig.getRedirectUri() != null) {
				for (String redirectUri : clientConfig.getRedirectUri()) {
					registeredClientBuilder.redirectUri(redirectUri);
				}
			}

			if (clientConfig.getId().equals("cspace-ui")) {
				populateUIRedirectUris(registeredClientBuilder);
			}

			registeredClientRepository.save(registeredClientBuilder.build());
		}

		return registeredClientRepository;
	}

	private void populateUIRedirectUris(RegisteredClient.Builder registeredClientBuilder) {
		// Add the configured authorization success and logout success URLs for each active tenant
		// to the allowed redirect URIs for the OAuth client.

    TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();

		for (TenantBindingType tenantBinding : tenantBindingConfigReader.getTenantBindings().values()) {
				try {
					// Add allowed post-authorization redirects from tenant config.

					registeredClientBuilder.redirectUri(ConfigUtils.getUIAuthorizationSuccessUrl(tenantBinding));
				} catch (MalformedURLException e) {
					logger.warn(
						"Malformed authorizationSuccessUrl in tenant bindings config: name={} id={}",
						tenantBinding.getName(),
						tenantBinding.getId()
					);
				}

				try {
					// Add allowed post-logout redirects from tenant config.

					// TODO: RegisteredClient.Builder#postLogoutRedirectUri is available in Spring Authorization
					// Server 1.1, and should be used for this when we upgrade. For now we store the allowed
					// post-logout redirects alongside the allowed post-authorization redirects.

					registeredClientBuilder.redirectUri(ConfigUtils.getUILogoutSuccessUrl(tenantBinding));
				} catch (MalformedURLException e) {
					logger.warn(
						"Malformed logoutSuccessUrl in tenant bindings config: name={} id={}",
						tenantBinding.getName(),
						tenantBinding.getId()
					);
				}
		}
	}

	private static KeyPair generateRsaKey() {
	    KeyPair keyPair;

		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}

		return keyPair;
  }

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		KeyPair keyPair = generateRsaKey();

		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();

		JWKSet jwkSet = new JWKSet(rsaKey);

		return new ImmutableJWKSet<>(jwkSet);
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	public UserDetailsService userDetailsService() {
		Map<String, Object> options = new HashMap<String, Object>();

		options.put("dsJndiName", "CspaceDS");
		options.put("principalsQuery", "select passwd from users where username=?");
		options.put("saltQuery", "select salt from users where username=?");
		options.put("rolesQuery", "select r.rolename from roles as r, accounts_roles as ar where ar.user_id=? and ar.role_id=r.csid");
		options.put("tenantsQueryWithDisabled", "select t.id, t.name from accounts_common as a, accounts_tenants as at, tenants as t where a.userid=? and a.csid = at.TENANTS_ACCOUNTS_COMMON_CSID and at.tenant_id = t.id order by t.id");
		options.put("tenantsQueryNoDisabled", "select t.id, t.name from accounts_common as a, accounts_tenants as at, tenants as t where a.userid=? and a.csid = at.TENANTS_ACCOUNTS_COMMON_CSID and at.tenant_id = t.id and NOT t.disabled order by t.id");
		options.put("maxRetrySeconds", 5000);
		options.put("delayBetweenAttemptsMillis", 200);

		return new CSpaceUserDetailsService(new CSpaceDbRealm(options));
	}
}

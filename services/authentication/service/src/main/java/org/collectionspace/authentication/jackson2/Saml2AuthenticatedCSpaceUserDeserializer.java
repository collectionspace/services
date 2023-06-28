package org.collectionspace.authentication.jackson2;

import java.io.IOException;
import java.util.Set;

import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.authentication.spring.Saml2AuthenticatedCSpaceUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

public class Saml2AuthenticatedCSpaceUserDeserializer extends JsonDeserializer<Saml2AuthenticatedCSpaceUser> {
	private static final TypeReference<Set<SimpleGrantedAuthority>> SIMPLE_GRANTED_AUTHORITY_SET = new TypeReference<Set<SimpleGrantedAuthority>>() {
	};

  private static final TypeReference<Set<CSpaceTenant>> CSPACE_TENANT_SET = new TypeReference<Set<CSpaceTenant>>() {
	};

  @Override
	public Saml2AuthenticatedCSpaceUser deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		ObjectMapper mapper = (ObjectMapper) parser.getCodec();
		JsonNode jsonNode = mapper.readTree(parser);

		Set<? extends GrantedAuthority> authorities = mapper.convertValue(jsonNode.get("authorities"), SIMPLE_GRANTED_AUTHORITY_SET);
		Set<CSpaceTenant> tenants = mapper.convertValue(jsonNode.get("tenants"), CSPACE_TENANT_SET);

    Saml2AuthenticatedPrincipal principal = mapper.convertValue(readJsonNode(jsonNode, "principal"), Saml2AuthenticatedPrincipal.class);
		JsonNode passwordNode = readJsonNode(jsonNode, "password");
		String username = readJsonNode(jsonNode, "username").asText();
		String password = passwordNode.asText("");
		boolean requireSSO = readJsonNode(jsonNode, "requireSSO").asBoolean();
		String salt = readJsonNode(jsonNode, "salt").asText();

		Saml2AuthenticatedCSpaceUser result = new Saml2AuthenticatedCSpaceUser(principal, username, password, salt, requireSSO, tenants, authorities);

		if (passwordNode.asText(null) == null) {
			result.eraseCredentials();
		}

		return result;
	}

	private JsonNode readJsonNode(JsonNode jsonNode, String field) {
		return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
	}
}

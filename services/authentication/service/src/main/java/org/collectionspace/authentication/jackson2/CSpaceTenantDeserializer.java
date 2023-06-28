package org.collectionspace.authentication.jackson2;

import java.io.IOException;

import org.collectionspace.authentication.CSpaceTenant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

public class CSpaceTenantDeserializer extends JsonDeserializer<CSpaceTenant> {

	@Override
	public CSpaceTenant deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		ObjectMapper mapper = (ObjectMapper) parser.getCodec();
		JsonNode jsonNode = mapper.readTree(parser);

		String id = readJsonNode(jsonNode, "id").asText();
		String name = readJsonNode(jsonNode, "name").asText();

		return new CSpaceTenant(id, name);
	}

	private JsonNode readJsonNode(JsonNode jsonNode, String field) {
		return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
	}
}

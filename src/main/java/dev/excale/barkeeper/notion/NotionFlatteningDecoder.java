package dev.excale.barkeeper.notion;

import feign.Response;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

@RequiredArgsConstructor
public class NotionFlatteningDecoder implements Decoder {

	private final Decoder delegate;
	private final ObjectMapper mapper;

	@Override
	public Object decode(Response response, Type type) throws IOException {

		// Fallback for other API calls
		if(!type.getTypeName().equals(GamePage.class.getName()))
			return delegate.decode(response, type);

		ObjectNode root = mapper.readValue(response.body().asInputStream(), ObjectNode.class);

		// Your flattening logic
		JsonNode propertiesNode = root.remove("properties");
		if(propertiesNode != null && propertiesNode.isObject()) {
			ObjectNode properties = (ObjectNode) propertiesNode;
			for(Map.Entry<String, JsonNode> field : properties.properties()) {
				JsonNode propData = field.getValue();
				JsonNode idNode = propData.get("id");
				if(idNode != null && !idNode.isNull())
					root.set(idNode.asString(), propData);
			}
		}

		// Convert the flattened tree into the target object
		return mapper.treeToValue(root, mapper.constructType(type));
	}

}

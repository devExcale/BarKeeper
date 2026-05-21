package dev.excale.barkeeper.notion.codec;

import dev.excale.barkeeper.notion.model.GamePage;
import feign.Response;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
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

		JavaType javaType = mapper.constructType(type);

		// Check for GamePage class
		boolean isGamePage = javaType.getRawClass()
			.equals(GamePage.class);

		// Check for GamePage collection
		boolean isGamePageCollection = javaType.isCollectionLikeType() &&
			javaType.getContentType() != null &&
			javaType.getContentType().getRawClass().equals(GamePage.class);

		// Not GamePage related: fallback to delegate decoder
		if(!isGamePage && !isGamePageCollection)
			return delegate.decode(response, type);

		// Get json from response
		ObjectNode root = mapper.readValue(response.body().asInputStream(), ObjectNode.class);

		return isGamePage ? flattenPageSingle(root, javaType) : flattenPageCollection(root, javaType);
	}

	private Object flattenPageCollection(ObjectNode root, JavaType javaType) {

		// Extract the results array from the Notion list wrapper
		JsonNode resultsNode = root.get("results");

		// Empty results (or missing)
		if(resultsNode == null || !resultsNode.isArray())
			return mapper.treeToValue(mapper.createArrayNode(), javaType);

		// Flatten each page inside the array
		ArrayNode results = (ArrayNode) resultsNode;
		for(JsonNode item : resultsNode)
			if(item.isObject())
				flattenProperties((ObjectNode) item);

		// Map the flattened array directly to the List<GamePage>
		return mapper.treeToValue(results, javaType);
	}

	private Object flattenPageSingle(ObjectNode root, JavaType javaType) {

		// Direct GamePage
		flattenProperties(root);

		return mapper.treeToValue(root, javaType);
	}

	/**
	 * Extracts the "properties" node and flattens its children into the root of the page node.
	 *
	 * @param pageNode The root node of a Notion page JSON object, which may contain a "properties" field.
	 */
	private void flattenProperties(ObjectNode pageNode) {

		// Pop properties node
		JsonNode propertiesNode = pageNode.remove("properties");

		// Do nothing if missing
		if(propertiesNode == null || !propertiesNode.isObject())
			return;

		// Loop all properties
		for(Map.Entry<String, JsonNode> field : propertiesNode.properties()) {

			// Get property id and object data
			JsonNode propData = field.getValue();
			JsonNode idNode = propData.get("id");

			// Add it to page root node (id-indexed)
			if(idNode != null && !idNode.isNull())
				pageNode.set(idNode.asString(), propData);

		}

	}

}

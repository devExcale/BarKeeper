package dev.excale.barkeeper.notion.codec;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotionUnflatteningEncoder implements Encoder {

    private final ObjectMapper mapper;

    // A whitelist of standard Notion API root fields. 
    // Anything NOT in this list will be pushed into the "properties" object.
    private static final Set<String> NOTION_ROOT_FIELDS = Set.of(
            "id", "object", "parent", "archived", "cover", "icon",
            "created_time", "last_edited_time", "created_by", "last_edited_by",
            "url", "public_url"
    );

    public NotionUnflatteningEncoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        try {
            // 1. Serialize the Java object into a Jackson JSON tree using your configured serializers
            JsonNode tree = mapper.valueToTree(object);

            // If it's not an object (e.g., a simple string/number payload), just pass it through
            if (!tree.isObject()) {
                byte[] json = mapper.writeValueAsBytes(object);
                writeToTemplate(template, json);
                return;
            }

			ObjectNode root = (ObjectNode) tree;
			ObjectNode propertiesNode = mapper.createObjectNode();
			List<String> keysToMove = new ArrayList<>();

			// 2. Iterate over the root properties using Jackson 3's .properties()
			for (Map.Entry<String, JsonNode> field : root.properties()) {
				String key = field.getKey();

				// If it's a custom field, stage it to be moved
				if (!NOTION_ROOT_FIELDS.contains(key)) {
					propertiesNode.set(key, field.getValue());
					keysToMove.add(key);
				}
			}

			// 3. Remove the custom fields from the root
			for (String key : keysToMove) {
				root.remove(key);
			}

			// 4. Attach the grouped "properties" node if it's not empty
			if (!propertiesNode.isEmpty()) {
				root.set("properties", propertiesNode);
			}

            // 5. Write the unflattened tree to the Feign request
            byte[] finalJson = mapper.writeValueAsBytes(root);
            writeToTemplate(template, finalJson);

        } catch (Exception e) {
            throw new EncodeException("Failed to unflatten and encode payload via Jackson 3", e);
        }
    }

    private void writeToTemplate(RequestTemplate template, byte[] json) {
        template.body(json, StandardCharsets.UTF_8);
        template.header("Content-Type", "application/json");
    }
}
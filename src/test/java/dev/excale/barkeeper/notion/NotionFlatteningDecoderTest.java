package dev.excale.barkeeper.notion;

import dev.excale.barkeeper.config.NotionFeignConfig;
import feign.Request;
import feign.Response;
import feign.codec.Decoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotionFlatteningDecoderTest {

	@Mock
	private Decoder delegateDecoder;

	private NotionFlatteningDecoder decoder;

	@BeforeEach
	void setUp() {
		// Initialize Jackson 3 Mapper with our Introspector
		ObjectMapper mapper = new NotionFeignConfig().objectMapper();
		decoder = new NotionFlatteningDecoder(delegateDecoder, mapper);
	}

	@Test
	void givenRawNotionResponse_whenDecodedToGamePage_thenPropertiesAreFlattened() throws Exception {
		// Given (Mocking the contents of notion-properties-in.json)
		String rawJsonIn = """
            {
                "object": "page",
                "id": "21f0c276-a8de-8042-b777-d2fe829a054f",
                "properties": {
                    "Store": {
                        "id": "byYs",
                        "type": "select",
                        "select": { "name": "Steam" }
                    },
                    "Prezzo": {
                        "id": "rkK%3D",
                        "type": "number",
                        "number": 5.89
                    }
                }
            }
            """;

		Response response = buildFeignResponse(rawJsonIn);

		// When
		Object result = decoder.decode(response, GamePage.class);

		// Then
		assertInstanceOf(GamePage.class, result);
		GamePage page = (GamePage) result;

		// Assert the mapping worked because the JSON was successfully flattened
		// (If not flattened, Jackson wouldn't find "byYs" or "rkK%3D" at the root)
		assertEquals("Steam", page.getStore());
		assertEquals(5.89, page.getFullPrice());
		verifyNoInteractions(delegateDecoder);
	}

	@Test
	void givenRawNotionListResponse_whenDecodedToGamePageList_thenPropertiesAreFlattened() throws Exception {
		// Given a Notion list response
		String rawJsonListIn = """
            {
                "object": "list",
                "results": [
                    {
                        "object": "page",
                        "id": "21f0c276-a8de-8042-b777-d2fe829a054f",
                        "properties": {
                            "Store": {
                                "id": "byYs",
                                "type": "select",
                                "select": { "name": "Steam" }
                            },
                            "Prezzo": {
                                "id": "rkK%3D",
                                "type": "number",
                                "number": 5.89
                            }
                        }
                    },
                    {
                        "object": "page",
                        "id": "32a1b489-c9ef-9153-c888-e3ab930b165c",
                        "properties": {
                            "Store": {
                                "id": "byYs",
                                "type": "select",
                                "select": { "name": "Epic Games" }
                            },
                            "Prezzo": {
                                "id": "rkK%3D",
                                "type": "number",
                                "number": 19.99
                            }
                        }
                    }
                ]
            }
            """;

		Response response = buildFeignResponse(rawJsonListIn);

		// Extract the reflective Type for List<GamePage> to simulate OpenFeign's target type
		Type listType = new TypeReference<List<GamePage>>() {}.getType();

		// When
		Object result = decoder.decode(response, listType);

		// Then
		assertInstanceOf(List.class, result);

		@SuppressWarnings("unchecked")
		List<GamePage> pages = (List<GamePage>) result;
		assertEquals(2, pages.size());

		// Assert first item
		GamePage page1 = pages.get(0);
		assertEquals("Steam", page1.getStore());
		assertEquals(5.89, page1.getFullPrice());

		// Assert second item
		GamePage page2 = pages.get(1);
		assertEquals("Epic Games", page2.getStore());
		assertEquals(19.99, page2.getFullPrice());

		verifyNoInteractions(delegateDecoder);
	}

	@Test
	void givenNonGamePageTarget_whenDecoded_thenDelegatesToDefault() throws Exception {
		// Given
		Response response = buildFeignResponse("{}");

		// When
		decoder.decode(response, String.class);

		// Then
		verify(delegateDecoder, times(1)).decode(any(Response.class), eq(String.class));
	}

	private Response buildFeignResponse(String body) {
		Request request = Request.create(Request.HttpMethod.GET, "/notion", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
		return Response.builder()
			.request(request)
			.status(200)
			.reason("OK")
			.headers(Collections.emptyMap())
			.body(body, StandardCharsets.UTF_8)
			.build();
	}
}

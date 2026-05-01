package dev.excale.barkeeper.notion;

import feign.Request;
import feign.Response;
import feign.codec.Decoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
class NotionFlatteningDecoderTest {

	@Mock
	private Decoder delegateDecoder;

	private NotionFlatteningDecoder decoder;

	@BeforeEach
	void setUp() {
		// Initialize Jackson 3 Mapper with our Introspector
		ObjectMapper mapper = JsonMapper.builder()
			.annotationIntrospector(new NotionPropertyIntrospector())
			.build();
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

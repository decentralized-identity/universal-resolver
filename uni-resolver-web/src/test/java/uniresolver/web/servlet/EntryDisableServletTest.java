package uniresolver.web.servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uniresolver.local.LocalUniResolver;
import uniresolver.testutil.StubHttpDriver;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EntryDisableServletTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void methodsEndpointExcludesDisabledEntryContributions() throws Exception {
		MethodsServlet servlet = new MethodsServlet();
		servlet.setUniResolver(resolver());

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.doGet(request, response);

		Set<String> methods = OBJECT_MAPPER.readValue(response.getContentAsString(), new TypeReference<>() {});

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(methods).containsExactlyInAnyOrder("key", "web");
	}

	@Test
	void testIdentifiersEndpointExcludesDisabledEntryContributions() throws Exception {
		TestIdentifiersServlet servlet = new TestIdentifiersServlet();
		servlet.setUniResolver(resolver());

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.doGet(request, response);

		Map<String, List<String>> testIdentifiers = OBJECT_MAPPER.readValue(response.getContentAsString(), new TypeReference<>() {});

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(testIdentifiers).containsExactly(
				Map.entry("key", List.of("did:key:z6Mkshow")),
				Map.entry("web", List.of("did:web:example.com")));
	}

	@Test
	void resolveEndpointDoesNotForceDisabledEntriesOrExposeEntryIdMetadata() throws Exception {
		ResolveServlet servlet = new ResolveServlet();
		servlet.setUniResolver(resolveResolver());

		MockHttpServletRequest request = resolveRequest();
		request.setQueryString("_entryId=entry-disabled");
		request.addParameter("_entryId", "entry-disabled");
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.doGet(request, response);

		Map<String, Object> result = OBJECT_MAPPER.readValue(response.getContentAsString(), new TypeReference<>() {});
		Map<String, Object> didDocumentMetadata = (Map<String, Object>) result.get("didDocumentMetadata");
		Map<String, Object> didResolutionMetadata = (Map<String, Object>) result.get("didResolutionMetadata");

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(didDocumentMetadata).containsEntry("marker", "active");
		assertThat(didResolutionMetadata).doesNotContainKey("entryId");
	}

	private static MockHttpServletRequest resolveRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/1.0/identifiers/did%3Aexample%3A123");
		request.setServletPath("/1.0/identifiers");
		request.addHeader("Accept", "application/did-resolution");
		return request;
	}

	private static LocalUniResolver resolver() {
		return new LocalUniResolver(List.of(
				StubHttpDriver.resolving("entry-key-disabled", "^(did:key:.+)$", true, List.of("did:key:z6Mkhide"), "hidden"),
				StubHttpDriver.resolving("entry-key-active", "^(did:key:.+)$", false, List.of("did:key:z6Mkshow"), "shown"),
				StubHttpDriver.resolving("entry-web", "^(did:web:.+)$", false, List.of("did:web:example.com"), "web")
		));
	}

	private static LocalUniResolver resolveResolver() {
		return new LocalUniResolver(List.of(
				StubHttpDriver.resolving("entry-disabled", "^(did:example:.+)$", true, List.of("did:example:hidden"), "disabled"),
				StubHttpDriver.resolving("entry-active", "^(did:example:.+)$", false, List.of("did:example:shown"), "active")
		));
	}
}

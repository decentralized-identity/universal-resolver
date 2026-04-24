package uniresolver.local;

import org.junit.jupiter.api.Test;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;
import uniresolver.testutil.StubHttpDriver;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalUniResolverDisabledEntriesTest {

	@Test
	void skipsDisabledEntryAndUsesLaterMatchingEntry() throws Exception {
		LocalUniResolver resolver = new LocalUniResolver(List.of(
				StubHttpDriver.resolving("entry-disabled", "^(did:example:.+)$", true, List.of("did:example:disabled"), "disabled"),
				StubHttpDriver.resolving("entry-active", "^(did:example:.+)$", false, List.of("did:example:active"), "active")
		));

		ResolveResult resolveResult = resolver.resolve("did:example:123", Map.of("accept", ResolveResult.MEDIA_TYPE));

		assertThat(resolveResult.getDidResolutionMetadata()).containsEntry("entryId", "entry-active");
		assertThat(resolveResult.getDidDocumentMetadata()).containsEntry("marker", "active");
	}

	@Test
	void entryIdOverrideIsIgnoredWithoutProbeToken() throws Exception {
		LocalUniResolver resolver = new LocalUniResolver(List.of(
				StubHttpDriver.resolving("entry-disabled", "^(did:example:.+)$", true, List.of("did:example:disabled"), "disabled"),
				StubHttpDriver.resolving("entry-active", "^(did:example:.+)$", false, List.of("did:example:active"), "active")
		));

		ResolveResult resolveResult = resolver.resolve("did:example:123", Map.of(
				"accept", ResolveResult.MEDIA_TYPE,
				LocalUniResolver.ENTRY_ID_OPTION, "entry-disabled"));

		assertThat(resolveResult.getDidResolutionMetadata()).containsEntry("entryId", "entry-active");
		assertThat(resolveResult.getDidDocumentMetadata()).containsEntry("marker", "active");
	}

	@Test
	void entryIdOverrideCanProbeDisabledEntryWithToken() throws Exception {
		LocalUniResolver resolver = new LocalUniResolver(List.of(
				StubHttpDriver.resolving("entry-disabled", "^(did:example:.+)$", true, List.of("did:example:disabled"), "disabled"),
				StubHttpDriver.resolving("entry-active", "^(did:example:.+)$", false, List.of("did:example:active"), "active")
		));
		resolver.setEntryProbeToken("secret-token");

		ResolveResult resolveResult = resolver.resolve("did:example:123", Map.of(
				"accept", ResolveResult.MEDIA_TYPE,
				LocalUniResolver.ENTRY_ID_OPTION, "entry-disabled",
				LocalUniResolver.ENTRY_PROBE_TOKEN_OPTION, "secret-token"));

		assertThat(resolveResult.getDidResolutionMetadata()).containsEntry("entryId", "entry-disabled");
		assertThat(resolveResult.getDidDocumentMetadata()).containsEntry("marker", "disabled");
	}

	@Test
	void hidesDisabledEntriesFromMethodsAndTestIdentifiersWhileKeepingOverlaps() throws Exception {
		LocalUniResolver resolver = new LocalUniResolver(List.of(
				StubHttpDriver.resolving("entry-key-disabled", "^(did:key:.+)$", true, List.of("did:key:z6Mkhide"), "hidden"),
				StubHttpDriver.resolving("entry-key-active", "^(did:key:.+)$", false, List.of("did:key:z6Mkshow"), "shown"),
				StubHttpDriver.resolving("entry-web", "^(did:web:.+)$", false, List.of("did:web:example.com"), "web")
		));

		assertThat(resolver.methods()).containsExactly("key", "web");
		assertThat(resolver.testIdentifiers()).containsExactly(
				Map.entry("key", List.of("did:key:z6Mkshow")),
				Map.entry("web", List.of("did:web:example.com")));
		assertThat(resolver.properties()).hasSize(2);
		assertThat(resolver.traits()).hasSize(2);
	}

	@Test
	void returnsMethodNotSupportedWhenAllMatchingEntriesAreDisabled() {
		LocalUniResolver resolver = new LocalUniResolver(List.of(
				StubHttpDriver.resolving("entry-disabled", "^(did:example:.+)$", true, List.of("did:example:disabled"), "disabled")
		));

		assertThatThrownBy(() -> resolver.resolve("did:example:123", Map.of("accept", ResolveResult.MEDIA_TYPE)))
				.isInstanceOfSatisfying(ResolutionException.class, ex -> {
					assertThat(ex.getMessage()).contains("Method not supported");
					assertThat(ex.getErrorType()).isEqualTo(ResolutionException.ERROR_METHOD_NOT_SUPPORTED);
				});
	}
}

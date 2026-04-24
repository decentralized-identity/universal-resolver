package uniresolver.testutil;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.driver.http.HttpDriver;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class StubHttpDriver extends HttpDriver {

	private String resolveMarker;
	private String dereferenceMarker;
	private Map<String, Object> properties = Map.of();
	private Map<String, Object> traits = Map.of();

	public static StubHttpDriver resolving(String id, String pattern, boolean disabled, List<String> testIdentifiers, String marker) {
		StubHttpDriver driver = new StubHttpDriver();
		driver.setId(id);
		driver.setPattern(pattern);
		driver.setResolveUri("http://example.com/" + id + "/");
		driver.setDisabled(disabled);
		driver.setTestIdentifiers(testIdentifiers);
		driver.setResolveMarker(marker);
		driver.setProperties(Map.of("entryId", id));
		driver.setTraits(Map.of("entryId", id));
		return driver;
	}

	public static StubHttpDriver dereferencing(String id, String pattern, boolean disabled, List<String> testIdentifiers, String marker) {
		StubHttpDriver driver = resolving(id, pattern, disabled, testIdentifiers, null);
		driver.setSupportsDereference(true);
		driver.setDereferenceMarker(marker);
		return driver;
	}

	@Override
	public ResolveResult resolve(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (this.getPattern() == null || ! this.getPattern().matcher(did.getDidString()).matches()) return null;
		if (this.resolveMarker == null) return null;

		ResolveResult resolveResult = ResolveResult.build();
		resolveResult.setDidDocument(mock(DIDDocument.class));
		resolveResult.setContentType("application/did+ld+json");
		resolveResult.getDidDocumentMetadata().put("marker", this.resolveMarker);
		return resolveResult;
	}

	@Override
	public DereferenceResult dereference(DIDURL didUrl, Map<String, Object> dereferenceOptions) throws DereferencingException, ResolutionException {
		if (this.getPattern() == null || ! this.getPattern().matcher(didUrl.getDidUrlString()).matches()) return null;
		if (this.dereferenceMarker == null) return null;

		DereferenceResult dereferenceResult = DereferenceResult.build();
		dereferenceResult.setContentType("application/json");
		dereferenceResult.setContent(this.dereferenceMarker.getBytes(StandardCharsets.UTF_8));
		return dereferenceResult;
	}

	@Override
	public Map<String, Object> properties() {
		return this.properties;
	}

	@Override
	public Map<String, Object> traits() {
		return this.traits;
	}

	public void setResolveMarker(String resolveMarker) {
		this.resolveMarker = resolveMarker;
	}

	public void setDereferenceMarker(String dereferenceMarker) {
		this.dereferenceMarker = dereferenceMarker;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public void setTraits(Map<String, Object> traits) {
		this.traits = traits;
	}
}

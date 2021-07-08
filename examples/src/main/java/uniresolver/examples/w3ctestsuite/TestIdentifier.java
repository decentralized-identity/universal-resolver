package uniresolver.examples.w3ctestsuite;

import foundation.identity.did.DID;
import foundation.identity.did.DIDURL;
import foundation.identity.did.parameters.Parameters;
import uniresolver.ResolutionException;
import uniresolver.client.ClientUniResolver;
import uniresolver.result.ResolveResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestIdentifier {

	static final List<String> dids = new ArrayList<>();

	static final Map<String, String> didParameters = new LinkedHashMap<>();

	public static void main(String[] args) throws Exception {

		DID did = DID.fromString("did:sov:WRfXPg8dantKVubE3HX8pw");
		dids.add(did.toString());

		DIDURL didUrl1 = DIDURL.fromString(did + "?" + Parameters.DID_URL_PARAMETER_SERVICE + "=" + "files" + "&" + Parameters.DID_URL_PARAMETER_RELATIVEREF + "=" + URLEncoder.encode("/myresume/doc?version=latest#intro", StandardCharsets.UTF_8));
		DIDURL didUrl2 = DIDURL.fromString(did + "?" + Parameters.DID_URL_PARAMETER_HL + "=" + "zQmWvQxTqbG2Z9HPJgG57jjwR154cKhbtJenbyYTWkjgF3e");
		DIDURL didUrl3 = DIDURL.fromString(did + "?" + Parameters.DID_URL_PARAMETER_VERSIONID + "=" + "4");
		DIDURL didUrl4 = DIDURL.fromString(did + "?" + Parameters.DID_URL_PARAMETER_VERSIONTIME + "=" + "2016-10-17T02:41:00Z");

		didParameters.put(Parameters.DID_URL_PARAMETER_SERVICE.toString(), didUrl1.toString());
		didParameters.put(Parameters.DID_URL_PARAMETER_RELATIVEREF.toString(), didUrl1.toString());
		didParameters.put(Parameters.DID_URL_PARAMETER_HL.toString(), didUrl2.toString());
		didParameters.put(Parameters.DID_URL_PARAMETER_VERSIONID.toString(), didUrl3.toString());
		didParameters.put(Parameters.DID_URL_PARAMETER_VERSIONTIME.toString(), didUrl4.toString());

		System.out.println(TestSuiteUtil.makeIdentifierTestSuiteReport("sov", dids, didParameters));
	}
}

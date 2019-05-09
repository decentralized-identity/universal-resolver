package uniresolver.local;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.github.jsonldjava.utils.JsonUtils;

import did.DIDDocument;

public class VersionRepoService {

	@SuppressWarnings("unchecked")
	public static DIDDocument getDidDocumentByVersionTime(String did, String timestamp) throws IOException {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = "https://version-repo-service.uniresolver.io/api/DidRecords/getLatestRecord?did=" + did + "&timestamp=" + timestamp;
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(httpGet);

		String responseString;

		try {

			HttpEntity entity = response.getEntity();
			responseString = EntityUtils.toString(entity, "UTF-8");
			EntityUtils.consume(entity);
		} finally {

			response.close();
		}

		List<Object> jsonLdObject = (List<Object>) JsonUtils.fromString(responseString);
		Map<String, Object> jsonLdMap = jsonLdObject == null ? null : (Map<String, Object>) jsonLdObject.get(0);
		String did_doc = jsonLdMap == null ? null : (String) jsonLdMap.get("did_doc");

		return DIDDocument.fromJson(did_doc);
	}

}

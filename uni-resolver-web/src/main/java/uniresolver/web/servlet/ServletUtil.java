package uniresolver.web.servlet;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ServletUtil {

	private static final Logger log = LoggerFactory.getLogger(ServletUtil.class);

	public static void sendResponse(HttpServletResponse response, int status, Map<String, String> headers, byte[] body) throws IOException {

		if (log.isInfoEnabled()) log.info("Sending response with status " + status + " and headers " + headers + " and body (" + (body == null ? null : body.length) + ")");

		response.setStatus(status);
		if (headers != null) for (Map.Entry<String, String> header : headers.entrySet()) response.setHeader(header.getKey(), header.getValue());
		response.setHeader("Access-Control-Allow-Origin", "*");

		if (body != null) {
			OutputStream outputStream = response.getOutputStream();
			outputStream.write(body);
			outputStream.flush();
			outputStream.close();
		}

		response.flushBuffer();
	}

	public static void sendResponse(HttpServletResponse response, int status, String contentType, byte[] body) throws IOException {
		sendResponse(response, status, Map.of("Content-Type", contentType), body);
	}

	public static void sendResponse(HttpServletResponse response, int status, String contentType, String body) throws IOException {
		sendResponse(response, status, Map.of("Content-Type", contentType), body == null ? null : body.getBytes(StandardCharsets.UTF_8));
	}

	public static void sendResponse(HttpServletResponse response, int status, byte[] body) throws IOException {
		sendResponse(response, status, (Map<String, String>) null, body);
	}

	public static void sendResponse(HttpServletResponse response, int status, String body) throws IOException {
		sendResponse(response, status, (Map<String, String>) null, body == null ? null : body.getBytes(StandardCharsets.UTF_8));
	}
}

package uniresolver.driver.servlet;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

public class ServletUtil {

	private static final Logger log = LoggerFactory.getLogger(ServletUtil.class);

	static void sendResponse(HttpServletResponse response, int status, Map<String, String> headers, Object body) throws IOException {

		if (log.isDebugEnabled()) log.debug("Sending response with status " + status + " and headers " + headers + " and body (" + (body == null ? null : body.getClass()) + ")");

		response.setStatus(status);
		if (headers != null) for (Map.Entry<String, String> header : headers.entrySet()) response.setHeader(header.getKey(), header.getValue());
		response.setHeader("Access-Control-Allow-Origin", "*");

		if (body instanceof String) {

			PrintWriter printWriter = response.getWriter();
			printWriter.write((String) body);
			printWriter.flush();
			printWriter.close();
		} else if (body instanceof byte[]) {

			OutputStream outputStream = response.getOutputStream();
			outputStream.write((byte[]) body);
			outputStream.flush();
			outputStream.close();
		} else if (body != null) {

			PrintWriter printWriter = response.getWriter();
			printWriter.write(body.toString());
			printWriter.flush();
			printWriter.close();
		}

		response.flushBuffer();
	}

	static void sendResponse(HttpServletResponse response, int status, Map<String, String> headers) throws IOException {

		sendResponse(response, status, headers, null);
	}

	static void sendResponse(HttpServletResponse response, int status, String contentType, Object body) throws IOException {

		sendResponse(response, status, Map.of("Content-Type", contentType), body);
	}

	static void sendResponse(HttpServletResponse response, int status, Object body) throws IOException {

		sendResponse(response, status, (Map<String, String>) null, body);
	}
}

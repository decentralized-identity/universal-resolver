package uniresolver.driver.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ServletUtil {

	private static final Logger log = LoggerFactory.getLogger(ServletUtil.class);

	static void sendResponse(HttpServletResponse response, int status, String contentType, Object body) throws IOException {

		if (log.isDebugEnabled()) log.debug("Sending response with status " + status + " and content type " + contentType + " and body (" + (body == null ? null : body.getClass()) + ")");

		response.setStatus(status);
		if (contentType != null) response.setContentType(contentType);
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
	}
}

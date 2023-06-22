package uniresolver.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.UniResolver;
import uniresolver.web.WebUniResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class MethodsServlet extends WebUniResolver {

	protected static final Logger log = LoggerFactory.getLogger(MethodsServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		if (log.isInfoEnabled()) log.info("Incoming request.");

		// execute the request

		Set<String> methods;
		String methodsString;

		try {

			methods = this.methods();
			methodsString = methods == null ? null : objectMapper.writeValueAsString(methods);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Resolver reported: " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Resolver reported: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Methods: " + methods);

		// no result?

		if (methods == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "No methods.");
			return;
		}

		// write result

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, UniResolver.METHODS_MIME_TYPE, methodsString);
	}
}
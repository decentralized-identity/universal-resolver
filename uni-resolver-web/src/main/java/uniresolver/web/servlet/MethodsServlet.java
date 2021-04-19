package uniresolver.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.UniResolver;
import uniresolver.web.WebUniResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class MethodsServlet extends WebUniResolver {

	protected static Logger log = LoggerFactory.getLogger(MethodsServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
			WebUniResolver.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Resolver reported: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Methods: " + methods);

		// no result?

		if (methods == null) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No methods.");
			return;
		}

		// write result

		WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, UniResolver.METHODS_MIME_TYPE, methodsString);
	}
}
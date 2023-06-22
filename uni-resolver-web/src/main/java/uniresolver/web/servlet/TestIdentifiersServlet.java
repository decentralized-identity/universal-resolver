package uniresolver.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.UniResolver;
import uniresolver.web.WebUniResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestIdentifiersServlet extends WebUniResolver {

	protected static final Logger log = LoggerFactory.getLogger(TestIdentifiersServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		if (log.isInfoEnabled()) log.info("Incoming request.");

		// execute the request

		Map<String, List<String>> testIdentifiers;
		String testIdentifiersString;

		try {

			testIdentifiers = this.testIdentifiers();
			testIdentifiersString = testIdentifiers == null ? null : objectMapper.writeValueAsString(testIdentifiers);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Resolver reported: " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Resolver reported: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Test identifiers: " + testIdentifiers);

		// no result?

		if (testIdentifiers == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "No test identifiers.");
			return;
		}

		// write result

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, UniResolver.TEST_IDENTIFIER_MIME_TYPE, testIdentifiersString);
	}
}
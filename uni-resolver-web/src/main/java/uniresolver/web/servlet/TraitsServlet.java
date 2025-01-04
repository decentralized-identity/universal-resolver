package uniresolver.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.UniResolver;
import uniresolver.web.WebUniResolver;

import java.io.IOException;
import java.util.Map;

public class TraitsServlet extends WebUniResolver {

	protected static final Logger log = LoggerFactory.getLogger(TraitsServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		if (log.isInfoEnabled()) log.info("Incoming request.");

		// execute the request

		Map<String, Map<String, Object>> traits;
		String traitsString;

		try {

			traits = this.traits();
			traitsString = traits == null ? null : objectMapper.writeValueAsString(traits);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Resolver reported: " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Resolver reported: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Traits: " + traits);

		// no result?

		if (traits == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "No traits.");
			return;
		}

		// write result

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, UniResolver.TRAITS_MEDIA_TYPE, traitsString);
	}
}
package uniresolver.web.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import uniresolver.UniResolver;
import uniresolver.web.WebUniResolver;

public class PropertiesServlet extends WebUniResolver {

	private static final long serialVersionUID = 3865183054854163102L;

	protected static Logger log = LoggerFactory.getLogger(WebUniResolver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		if (log.isInfoEnabled()) log.info("Incoming request.");

		// execute the request

		Map<String, Map<String, Object>> properties;
		String propertiesString;

		try {

			properties = this.properties();
			propertiesString = properties == null ? null : objectMapper.writeValueAsString(properties);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver reported: " + ex.getMessage(), ex);
			WebUniResolver.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver reported: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Properties: " + properties);

		// no result?

		if (properties == null) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No properties.");
			return;
		}

		// write result

		WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, UniResolver.PROPERTIES_MIME_TYPE, propertiesString);
	}
}
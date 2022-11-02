package uniresolver.driver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.Driver;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class PropertiesServlet extends HttpServlet implements Servlet {

	private static final Logger log = LoggerFactory.getLogger(PropertiesServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public PropertiesServlet() {

		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		if (log.isInfoEnabled()) log.info("Incoming request.");

		// get properties

		Map<String, Object> properties;
		String propertiesString;

		try {

			properties = InitServlet.getDriver() == null ? null : InitServlet.getDriver().properties();
			propertiesString = properties == null ? null : objectMapper.writeValueAsString(properties);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Properties problem: " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Properties problem: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Properties: " + properties);

		// no properties?

		if (properties == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No properties.");
			return;
		}

		// write properties

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, Driver.PROPERTIES_MIME_TYPE, propertiesString);
	}
}

package uniresolver.driver.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import uniresolver.driver.Driver;

public class PropertiesServlet extends AbstractServlet implements Servlet {

	private static final long serialVersionUID = -2093931014950367385L;

	private static Logger log = LoggerFactory.getLogger(PropertiesServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public PropertiesServlet() {

		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Properties problem: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Properties: " + properties);

		// no properties?

		if (properties == null) {

			sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No properties.");
			return;
		}

		// write properties

		sendResponse(response, HttpServletResponse.SC_OK, Driver.PROPERTIES_MIME_TYPE, propertiesString);
	}
}

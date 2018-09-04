package uniresolver.driver.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import uniresolver.driver.Driver;

public class PropertiesServlet extends HttpServlet implements Servlet {

	private static final long serialVersionUID = -2093931014950367385L;

	private static Logger log = LoggerFactory.getLogger(PropertiesServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private Driver driver;

	public PropertiesServlet() {

		super();
	}

	public PropertiesServlet(Driver driver) {

		super();
		this.driver = driver;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		if (this.driver == null) {

			String driverClassName = config.getInitParameter("Driver");
			Class<? extends Driver> driverClass;

			try {

				driverClass = driverClassName == null ? null : (Class<? extends Driver>) Class.forName(driverClassName);
				this.driver = driverClass == null ? null : driverClass.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {

				throw new ServletException(ex.getMessage(), ex);
			}

			if (this.driver == null) throw new ServletException("Unable to load driver: " + driverClassName);

			if (log.isInfoEnabled()) log.info("Loaded driver: " + driverClass);
		}
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

			properties = this.getDriver().properties();
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

	/*
	 * Helper methods
	 */

	private static void sendResponse(HttpServletResponse response, int status, String contentType, String body) throws IOException {

		response.setStatus(status);

		if (contentType != null) response.setContentType(contentType);

		response.setHeader("Access-Control-Allow-Origin", "*");

		if (body != null) {

			PrintWriter writer = response.getWriter();
			writer.write(body);
			writer.flush();
			writer.close();
		}
	}

	/*
	 * Getters and setters
	 */

	public Driver getDriver() {

		return this.driver;
	}

	public void setDriver(Driver driver) {

		this.driver = driver;
	}
}

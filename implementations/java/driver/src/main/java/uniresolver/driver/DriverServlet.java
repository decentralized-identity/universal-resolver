package uniresolver.driver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniresolver.result.ResolutionResult;

public class DriverServlet extends HttpServlet implements Servlet {

	private static final long serialVersionUID = -531456245094927384L;

	private static Logger log = LoggerFactory.getLogger(DriverServlet.class);

	public static final String MIME_TYPE = "application/json";

	private Driver driver;

	public DriverServlet() {

		super();
	}

	public DriverServlet(Driver driver) {

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

		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String requestPath = request.getRequestURI();

		String identifier = requestPath.substring(contextPath.length() + servletPath.length());
		if (identifier.startsWith("/")) identifier = identifier.substring(1);

		if (log.isInfoEnabled()) log.info("Incoming request for identifier: " + identifier);

		if (identifier == null) {

			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No identifier found in resolution request.");
			return;
		}

		// resolve the identifier

		ResolutionResult resolutionResult;
		String resolutionResultString;

		try {

			resolutionResult = this.getDriver().resolve(identifier);
			resolutionResultString = resolutionResult == null ? null : resolutionResult.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver reported for " + identifier + ": " + ex.getMessage(), ex);
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver reported for " + identifier + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("DID document for identifier " + identifier + ": " + resolutionResult);

		// no resolution result?

		if (resolutionResult == null) {

			sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No result for " + identifier);
			return;
		}

		// write resolution result

		sendResponse(response, HttpServletResponse.SC_OK, MIME_TYPE, resolutionResultString);
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

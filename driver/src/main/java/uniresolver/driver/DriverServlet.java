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
import org.springframework.web.HttpRequestHandler;

import uniresolver.ResolutionException;
import uniresolver.ddo.DDO;

public class DriverServlet extends HttpServlet implements Servlet, HttpRequestHandler {

	private static final long serialVersionUID = -531456245094927384L;

	private static Logger log = LoggerFactory.getLogger(DriverServlet.class);

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

			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No identifier found in resolution request.");
			return;
		}

		// resolve the identifier

		DDO ddo;

		try {

			ddo = this.getDriver().resolve(identifier);
		} catch (ResolutionException ex) {

			if (log.isWarnEnabled()) log.warn("Resolution problem for " + identifier + ": " + ex.getMessage(), ex);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Resolution problem for " + identifier + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("DDO for identifier " + identifier + ": " + ddo);

		// no result?

		if (ddo == null) {

			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No result for " + identifier);
			return;
		}

		// write result

		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter writer = response.getWriter();
		writer.write(ddo.toString());
		writer.flush();
		writer.close();
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if ("GET".equals(request.getMethod())) this.doGet(request, response);
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

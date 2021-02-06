package uniresolver.driver.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniresolver.result.ResolveResult;

public class ResolveServlet extends AbstractServlet implements Servlet {

	private static final long serialVersionUID = -531456245094927384L;

	private static Logger log = LoggerFactory.getLogger(ResolveServlet.class);

	public ResolveServlet() {

		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String requestPath = request.getRequestURI();

		if (log.isDebugEnabled()) log.debug("contextPath: " + contextPath + ", servletPath: " + servletPath + ", requestPath: " + requestPath);

		String identifier = requestPath.substring(contextPath.length() + servletPath.length());
		if (log.isDebugEnabled()) log.debug("processing identifier (1): " + identifier);

		if (identifier.startsWith("/")) identifier = identifier.substring(1);
		if (log.isDebugEnabled()) log.debug("processing identifier (2): " + identifier);

		try {

			identifier = URLDecoder.decode(identifier, "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new IOException(ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Incoming resolve request for identifier: " + identifier);

		if (identifier == null) {

			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No identifier found in resolve request.");
			return;
		}

		// invoke the driver

		ResolveResult resolveResult;
		String resolveResultString;

		try {

			resolveResult = InitServlet.getDriver().resolve(identifier);
			resolveResultString = resolveResult == null ? null : resolveResult.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver reported for " + identifier + ": " + ex.getMessage(), ex);
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver reported for " + identifier + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Resolve result for " + identifier + ": " + resolveResultString);

		// no resolve result?

		if (resolveResultString == null) {

			sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No resolve result for " + identifier);
			return;
		}

		// write resolve result

		sendResponse(response, HttpServletResponse.SC_OK, ResolveResult.MIME_TYPE, resolveResultString);
	}
}

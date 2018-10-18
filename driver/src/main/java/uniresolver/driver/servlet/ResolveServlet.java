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

import uniresolver.result.ResolutionResult;

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

		String identifier = requestPath.substring(contextPath.length() + servletPath.length());
		if (identifier.startsWith("/")) identifier = identifier.substring(1);

		try {

			identifier = URLDecoder.decode(identifier, "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new IOException(ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Incoming resolution request for identifier: " + identifier);

		if (identifier == null) {

			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No identifier found in resolution request.");
			return;
		}

		// invoke the driver

		ResolutionResult resolutionResult;
		String resolutionResultString;

		try {

			resolutionResult = InitServlet.getDriver().resolve(identifier);
			resolutionResultString = resolutionResult == null ? null : resolutionResult.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver reported for " + identifier + ": " + ex.getMessage(), ex);
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver reported for " + identifier + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Resolution result for " + identifier + ": " + resolutionResultString);

		// no resolution result?

		if (resolutionResultString == null) {

			sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No resolution result for " + identifier);
			return;
		}

		// write resolution result

		sendResponse(response, HttpServletResponse.SC_OK, ResolutionResult.MIME_TYPE, resolutionResultString);
	}
}

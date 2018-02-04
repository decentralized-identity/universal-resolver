package uniresolver.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import uniresolver.ResolutionException;
import uniresolver.result.ResolutionResult;
import uniresolver.web.WebUniResolver;

public class ResolveServlet extends WebUniResolver {

	private static final long serialVersionUID = 1579362184113490816L;

	protected static Logger log = LoggerFactory.getLogger(WebUniResolver.class);

	public static final String MIME_TYPE = "application/json";

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

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No identifier found in resolution request.");
			return;
		}

		// execute the request

		ResolutionResult resolutionResult;

		try {

			resolutionResult = this.resolve(identifier);
		} catch (ResolutionException ex) {

			if (log.isWarnEnabled()) log.warn("Resolution problem for " + identifier + ": " + ex.getMessage(), ex);
			WebUniResolver.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Resolution problem for " + identifier + ": " + ex.getMessage());
			return;
		}

		// no result?

		if (resolutionResult == null) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No result for " + identifier + ".");
			return;
		}

		// write result

		try {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, MIME_TYPE, resolutionResult.toJson());
		} catch (JsonProcessingException ex) {

			throw new IOException("JSON processing error: " + ex.getMessage(), ex);
		}
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if ("GET".equals(request.getMethod())) this.doGet(request, response);
	}
}
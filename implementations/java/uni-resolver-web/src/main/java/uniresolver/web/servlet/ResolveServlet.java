package uniresolver.web.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		try {

			identifier = URLDecoder.decode(identifier, "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new IOException(ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Incoming request for identifier: " + identifier);

		if (identifier == null) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No identifier found in resolution request.");
			return;
		}

		// execute the request

		ResolutionResult resolutionResult;
		String resolutionResultString;

		try {

			resolutionResult = this.resolve(identifier);
			resolutionResultString = resolutionResult == null ? null : resolutionResult.toJson();
		} catch (ResolutionException ex) {

			if (log.isWarnEnabled()) log.warn("Resolution problem for " + identifier + ": " + ex.getMessage(), ex);
			WebUniResolver.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Resolution problem for " + identifier + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Resolution result for " + identifier + ": " + resolutionResultString);

		// no result?

		if (resolutionResultString == null) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No result for " + identifier + ".");
			return;
		}

		// write result

		WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, MIME_TYPE, resolutionResultString);
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if ("GET".equals(request.getMethod())) this.doGet(request, response);
	}
}
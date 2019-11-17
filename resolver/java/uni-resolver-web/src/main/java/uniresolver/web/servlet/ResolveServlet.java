package uniresolver.web.servlet;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import did.DIDDocument;
import uniresolver.result.ResolveResult;
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
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Request problem: " + ex.getMessage(), ex);
			WebUniResolver.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Request problem: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Incoming resolve request for identifier: " + identifier);

		if (identifier == null) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No identifier found in resolve request.");
			return;
		}

		// execute the request

		ResolveResult resolveResult;

		try {

			resolveResult = this.resolve(identifier);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Resolve problem for " + identifier + ": " + ex.getMessage(), ex);
			WebUniResolver.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Resolve problem for " + identifier + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Resolve result for " + identifier + ": " + resolveResult.toJson());

		// no resolve result?

		if (resolveResult == null || (resolveResult.getDidDocument() == null && resolveResult.getContent() == null)) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, resolveResult.toJson());
			return;
		}

		// write resolve result

		if (request.getHeader("Accept").contains(DIDDocument.MIME_TYPE) && resolveResult.getDidDocument() != null) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, DIDDocument.MIME_TYPE, resolveResult.getDidDocument().toJson());
			return;
		} else if (resolveResult.getContent() != null) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, resolveResult.getContentType(), resolveResult.getContent());
			return;
		} else {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, ResolveResult.MIME_TYPE, resolveResult.toJson());
			return;
		}
	}
}
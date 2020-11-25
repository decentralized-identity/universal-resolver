package uniresolver.web.servlet;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import foundation.identity.did.DIDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import uniresolver.result.ResolveResult;
import uniresolver.web.WebUniResolver;

public class ResolveServlet extends WebUniResolver {

	private static final long serialVersionUID = 1579362184113490816L;

	protected static Logger log = LoggerFactory.getLogger(ResolveServlet.class);

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

		if (log.isInfoEnabled()) log.info("Resolve result for " + identifier + ": " + resolveResult);

		// no resolve result?

		if (resolveResult == null || (resolveResult.getDidDocument() == null && resolveResult.getContent() == null)) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No resolve result for " + identifier + ": " + resolveResult);
			return;
		}

		// write resolve result

		String acceptHeader = request.getHeader("Accept");
		List<MediaType> acceptMediaTypes = MediaType.parseMediaTypes(acceptHeader != null ? acceptHeader : "*/*");
		MediaType.sortBySpecificityAndQuality(acceptMediaTypes);

		for (MediaType acceptMediaType : acceptMediaTypes) {

			if (log.isDebugEnabled()) log.warn("Trying to find content for media type " + acceptMediaType);

			if (acceptMediaType.includes(MediaType.valueOf(ResolveResult.MIME_TYPE))) {

				WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, ResolveResult.MIME_TYPE, resolveResult.toJson());
				return;
			} else if (acceptMediaType.includes(MediaType.valueOf(DIDDocument.MIME_TYPE_JSON_LD))) {

				if (resolveResult.getDidDocument() == null) {

					WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No DID document for " + identifier);
					return;
				}

				WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, DIDDocument.MIME_TYPE_JSON_LD, resolveResult.getDidDocument().toJson());
				return;
			} else if (resolveResult.getContentType() != null && acceptMediaType.includes(MediaType.valueOf(resolveResult.getContentType()))) {

				if (resolveResult.getContent() == null) {

					WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No content for " + identifier);
					return;
				}

				WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, resolveResult.getContentType(), resolveResult.getContent());
				return;
			}
		}

		WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, null, "Not acceptable media type " + acceptHeader);
		return;
	}
}
package uniresolver.web.servlet;

import foundation.identity.did.representations.Representations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;
import uniresolver.util.HttpBindingUtil;
import uniresolver.util.ResolveResultUtil;
import uniresolver.web.WebUniResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolveServlet extends WebUniResolver {

	protected static final Logger log = LoggerFactory.getLogger(ResolveServlet.class);

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
		identifier = URLDecoder.decode(identifier, StandardCharsets.UTF_8);

		if (log.isInfoEnabled()) log.info("Incoming resolve request for identifier: " + identifier);

		if (identifier == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No identifier found in resolve request.");
			return;
		}

		// assume identifier is a DID

		String didString = identifier;

		// prepare resolution options

		String acceptHeaderString = request.getHeader("Accept");
		if (log.isDebugEnabled()) log.info("Incoming Accept: header string: " + acceptHeaderString);
		List<MediaType> acceptMediaTypes = MediaType.parseMediaTypes(acceptHeaderString != null ? acceptHeaderString : "*/*");
		MediaType.sortBySpecificityAndQuality(acceptMediaTypes);

		String accept = acceptMediaTypes.size() > 0 ? acceptMediaTypes.get(0).toString() : null;
		if (accept == null || ! Representations.MEDIA_TYPES.contains(accept)) accept = Representations.DEFAULT_MEDIA_TYPE;

		Map<String, Object> resolutionOptions = new HashMap<>();
		resolutionOptions.put("accept", accept);

		// execute the request

		ResolveResult resolveResult;

		try {

			resolveResult = this.resolveRepresentation(didString, resolutionOptions);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Resolve problem for " + didString + ": " + ex.getMessage(), ex);

			if (ex instanceof ResolutionException && ((ResolutionException) ex).getResolveResult() != null) {
				resolveResult = ((ResolutionException) ex).getResolveResult();
			} else {
				resolveResult = ResolveResult.makeErrorResult(ResolveResult.Error.internalError, "Resolve problem for " + didString + ": " + ex.getMessage(), accept);
			}

			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, HttpBindingUtil.toHttpBodyResolveResult(resolveResult));
			return;
		}

		if (log.isInfoEnabled()) log.info("Resolve result for " + didString + ": " + resolveResult);

		// no resolve result?

		if (resolveResult == null || resolveResult.getDidDocumentStream() == null) {

			resolveResult = ResolveResult.makeErrorResult(ResolveResult.Error.notFound, "No resolve result for " + didString, accept);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null,  HttpBindingUtil.toHttpBodyResolveResult(resolveResult));
			return;
		}

		// write resolve result

		for (MediaType acceptMediaType : acceptMediaTypes) {

			String contentType = (String) resolveResult.getDidResolutionMetadata().get("contentType");
			if (log.isDebugEnabled()) log.debug("Trying to find content for media type " + acceptMediaType + " to satisfy " + contentType);

			if (acceptMediaType.includes(MediaType.valueOf(ResolveResult.MEDIA_TYPE))) {

				ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, ResolveResult.MEDIA_TYPE, HttpBindingUtil.toHttpBodyResolveResult(resolveResult));
				return;
			} else if (contentType != null && acceptMediaType.includes(MediaType.valueOf(contentType))) {

				ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, contentType, resolveResult.getDidDocumentStream());
				return;
			}
		}

		ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, null, "Not acceptable media type " + acceptHeaderString);
		return;
	}
}
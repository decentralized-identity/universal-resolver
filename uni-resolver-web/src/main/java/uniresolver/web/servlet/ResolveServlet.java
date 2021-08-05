package uniresolver.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.ResolutionException;
import uniresolver.driver.util.HttpBindingServerUtil;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;
import uniresolver.web.WebUniResolver;

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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String requestPath = request.getRequestURI();

		String path = requestPath.substring(contextPath.length() + servletPath.length());
		if (path.startsWith("/")) path = path.substring(1);

		if (log.isDebugEnabled()) log.debug("Incoming resolve request: " + requestPath);

		if (path.isEmpty()) {
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No identifier found in resolve request.");
			return;
		}

		// look at path

		String didString = null;
		Map<String, Object> resolutionOptions = new HashMap<>();

		if (path.contains("%")) {
			didString = URLDecoder.decode(path, StandardCharsets.UTF_8);
			if (request.getParameterMap() != null) {
				resolutionOptions.putAll(request.getParameterMap());
			}
		} else {
			didString = path;
		}

		if (log.isInfoEnabled()) log.info("Incoming DID string: " + didString);

		// prepare resolution options

		String httpAcceptHeader = request.getHeader("Accept");
		if (log.isInfoEnabled()) log.info("Incoming Accept: header string: " + httpAcceptHeader);

		List<MediaType> httpAcceptMediaTypes = MediaType.parseMediaTypes(httpAcceptHeader != null ? httpAcceptHeader : ResolveResult.MEDIA_TYPE);
		MediaType.sortBySpecificityAndQuality(httpAcceptMediaTypes);

		String accept = HttpBindingServerUtil.acceptForHttpAcceptMediaTypes(httpAcceptMediaTypes);
		resolutionOptions.put("accept", accept);

		if (log.isDebugEnabled()) log.debug("Using resolution options: " + resolutionOptions);

		// execute the request

		ResolveRepresentationResult resolveRepresentationResult;

		try {

			resolveRepresentationResult = this.resolveRepresentation(didString, resolutionOptions);
			if (resolveRepresentationResult == null) throw new ResolutionException(ResolveResult.ERROR_NOTFOUND, "No resolve result for " + didString);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Resolve problem for " + didString + ": " + ex.getMessage(), ex);

			if (! (ex instanceof ResolutionException)) {
				ex = new ResolutionException(ResolveResult.ERROR_INTERNALERROR, "Resolve problem for " + didString + ": " + ex.getMessage());
			}

			resolveRepresentationResult = ResolveRepresentationResult.makeErrorResult((ResolutionException) ex, accept);
		}

		if (log.isInfoEnabled()) log.info("Resolve result for " + didString + ": " + resolveRepresentationResult);

		// write resolve result

		for (MediaType acceptMediaType : httpAcceptMediaTypes) {

			if (HttpBindingServerUtil.isMediaTypeAcceptable(acceptMediaType, ResolveResult.MEDIA_TYPE)) {

				if (log.isDebugEnabled()) log.debug("Supporting HTTP media type " + acceptMediaType + " via content type " + ResolveResult.MEDIA_TYPE);

				ServletUtil.sendResponse(
						response,
						HttpBindingServerUtil.httpStatusCodeForResult(resolveRepresentationResult),
						ResolveResult.MEDIA_TYPE,
						HttpBindingServerUtil.toHttpBodyResolveRepresentationResult(resolveRepresentationResult));
				return;
			} else {

				// determine representation media type

				if (HttpBindingServerUtil.isMediaTypeAcceptable(acceptMediaType, resolveRepresentationResult.getContentType())) {
					if (log.isDebugEnabled()) log.debug("Supporting HTTP media type " + acceptMediaType + " via content type " + resolveRepresentationResult.getContentType());
				} else {
					if (log.isDebugEnabled()) log.debug("Not supporting HTTP media type " + acceptMediaType);
					continue;
				}

				ServletUtil.sendResponse(
						response,
						HttpBindingServerUtil.httpStatusCodeForResult(resolveRepresentationResult),
						resolveRepresentationResult.getContentType(),
						resolveRepresentationResult.getDidDocumentStream()
				);
				return;
			}
		}

		ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, null, "Not acceptable media types " + httpAcceptHeader);
		return;
	}
}
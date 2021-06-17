package uniresolver.web.servlet;

import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.ResolutionException;
import uniresolver.driver.util.HttpBindingServerUtil;
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

		String httpAcceptHeader = request.getHeader("Accept");
		if (log.isInfoEnabled()) log.info("Incoming Accept: header string: " + httpAcceptHeader);

		List<MediaType> httpAcceptMediaTypes = MediaType.parseMediaTypes(httpAcceptHeader != null ? httpAcceptHeader : ResolveResult.MEDIA_TYPE);
		MediaType.sortBySpecificityAndQuality(httpAcceptMediaTypes);

		String accept = HttpBindingServerUtil.acceptForHttpAcceptMediaTypes(httpAcceptMediaTypes);

		Map<String, Object> resolutionOptions = new HashMap<>();
		resolutionOptions.put("accept", accept);

		if (log.isDebugEnabled()) log.debug("Using resolution options: " + resolutionOptions);

		// execute the request

		ResolveResult resolveResult;

		try {

			resolveResult = this.resolveRepresentation(didString, resolutionOptions);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Resolve problem for " + didString + ": " + ex.getMessage(), ex);

			if (ex instanceof ResolutionException && ((ResolutionException) ex).getResolveResult() != null) {
				resolveResult = ((ResolutionException) ex).getResolveResult();
			} else {
				resolveResult = ResolveResult.makeErrorResolveRepresentationResult(ResolveResult.ERROR_INTERNALERROR, "Resolve problem for " + didString + ": " + ex.getMessage(), accept);
			}

			ServletUtil.sendResponse(response, HttpBindingServerUtil.httpStatusCodeForResolveResult(resolveResult), null, HttpBindingServerUtil.toHttpBodyResolveResult(resolveResult));
			return;
		}

		if (log.isInfoEnabled()) log.info("Resolve result for " + didString + ": " + resolveResult);

		// no resolve result?

		if (resolveResult == null || resolveResult.getDidDocumentStream() == null) {

			resolveResult = ResolveResult.makeErrorResolveRepresentationResult(ResolveResult.ERROR_NOTFOUND, "No resolve result for " + didString, accept);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null,  HttpBindingServerUtil.toHttpBodyResolveResult(resolveResult));
			return;
		}

		// write resolve result

		for (MediaType acceptMediaType : httpAcceptMediaTypes) {

			if (HttpBindingServerUtil.isMediaTypeAcceptable(acceptMediaType, MediaType.valueOf(ResolveResult.MEDIA_TYPE))) {

				ServletUtil.sendResponse(response, HttpBindingServerUtil.httpStatusCodeForResolveResult(resolveResult), ResolveResult.MEDIA_TYPE, HttpBindingServerUtil.toHttpBodyResolveResult(resolveResult));
				return;
			} else {

				if (!HttpBindingServerUtil.isMediaTypeAcceptable(acceptMediaType, MediaType.valueOf(resolveResult.getContentType()))) {

					// try to convert

					String sourceMediaType = resolveResult.getContentType();
					String targetMediaType = HttpBindingUtil.representationMediaTypeForMediaType(ContentType.parse(acceptMediaType.toString()).getMimeType());
					if (targetMediaType == null) {
						if (log.isDebugEnabled()) log.debug("Cannot convert resolve result from " + sourceMediaType + " to " + targetMediaType);
						continue;
					} else {
						if (log.isDebugEnabled()) log.debug("Attempting to convert resolve result from " + sourceMediaType + " to " + targetMediaType);
					}

					try {
						resolveResult = ResolveResultUtil.convertToResolveRepresentationResult(ResolveResultUtil.convertToResolveResult(resolveResult), targetMediaType);
						resolveResult.getDidResolutionMetadata().put("convertedFrom", sourceMediaType);
						resolveResult.getDidResolutionMetadata().put("convertedTo", targetMediaType);
					} catch (ResolutionException ex) {
						throw new IOException(ex.getMessage(), ex);
					}
				}

				ServletUtil.sendResponse(response, HttpBindingServerUtil.httpStatusCodeForResolveResult(resolveResult), resolveResult.getContentType(), resolveResult.getDidDocumentStream());
				return;
			}
		}

		ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, null, "Not acceptable media type " + httpAcceptHeader);
	}
}
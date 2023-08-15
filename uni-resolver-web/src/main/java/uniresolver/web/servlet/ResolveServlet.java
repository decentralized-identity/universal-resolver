package uniresolver.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.ResolutionException;
import uniresolver.driver.util.HttpBindingServerUtil;
import uniresolver.driver.util.MediaTypeUtil;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;
import uniresolver.web.WebUniResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
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
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No identifier found in resolve request.");
			return;
		}

		// incomding DID and resolution options

		String didString;
		Map<String, Object> resolutionOptions = new HashMap<>();

		if (path.startsWith("did%3A")) {
			didString = URLDecoder.decode(path, StandardCharsets.UTF_8);
			if (request.getParameterMap() != null) {
				for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
					String parameterName = e.nextElement();
					String parameterValue = request.getParameter(parameterName);
					resolutionOptions.put(parameterName, parameterValue);
				}
			}
		} else {
			didString = path;
			if (request.getQueryString() != null) didString += "?" + request.getQueryString();
		}

		if (log.isInfoEnabled()) log.info("Incoming DID string: " + didString + ", incoming DID resolution options: " + resolutionOptions);

		// prepare resolution options

		String httpAcceptHeader = request.getHeader("Accept");
		if (log.isInfoEnabled()) log.info("Incoming Accept: header string: " + httpAcceptHeader);

		List<MediaType> httpAcceptMediaTypes = MediaType.parseMediaTypes(httpAcceptHeader != null ? httpAcceptHeader : ResolveResult.MEDIA_TYPE);
		MediaType.sortBySpecificityAndQuality(httpAcceptMediaTypes);
		if (httpAcceptHeader != null) resolutionOptions.put("_http_accept", httpAcceptMediaTypes);

		String accept = HttpBindingServerUtil.acceptForHttpAccepts(httpAcceptMediaTypes);
		if (!resolutionOptions.containsKey("accept")) {
			resolutionOptions.put("accept", accept);
		}

		if (log.isDebugEnabled()) log.debug("Using resolution options: " + resolutionOptions);

		// execute the request

		ResolveRepresentationResult resolveRepresentationResult;

		try {

			resolveRepresentationResult = this.resolveRepresentation(didString, resolutionOptions);
			if (resolveRepresentationResult == null) throw new ResolutionException(ResolutionException.ERROR_NOTFOUND, "No resolve result for " + didString);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Resolve problem for " + didString + ": " + ex.getMessage(), ex);

			if (! (ex instanceof ResolutionException)) ex = new ResolutionException("Resolve problem for " + didString + ": " + ex.getMessage());
			resolveRepresentationResult = ((ResolutionException) ex).toErrorResult(accept);
		}

		if (log.isInfoEnabled()) log.info("Resolve representation result for " + didString + ": " + resolveRepresentationResult);

		// write resolve result

		for (MediaType httpAcceptMediaType : httpAcceptMediaTypes) {

			if (MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, ResolveResult.MEDIA_TYPE)) {

				if (log.isDebugEnabled()) log.debug("Supporting HTTP media type " + httpAcceptMediaType + " via default content type " + ResolveResult.MEDIA_TYPE);

				ServletUtil.sendResponse(
						response,
						HttpBindingServerUtil.httpStatusCodeForResult(resolveRepresentationResult),
						ResolveResult.MEDIA_TYPE,
						HttpBindingServerUtil.toHttpBodyStreamResult(resolveRepresentationResult));
				return;
			} else if (resolveRepresentationResult.getContentType() != null) {

				// determine representation media type

				if (MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, resolveRepresentationResult.getContentType())) {
					if (log.isDebugEnabled()) log.debug("Supporting HTTP media type " + httpAcceptMediaType + " via content type " + resolveRepresentationResult.getContentType());
				} else {
					if (log.isDebugEnabled()) log.debug("Not supporting HTTP media type " + httpAcceptMediaType);
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

		ServletUtil.sendResponse(
				response,
				HttpServletResponse.SC_NOT_ACCEPTABLE,
				"Not acceptable media types " + httpAcceptHeader);
	}
}
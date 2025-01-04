package uniresolver.driver.servlet;

import foundation.identity.did.DID;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.ResolutionException;
import uniresolver.driver.util.HttpBindingServerUtil;
import uniresolver.driver.util.MediaTypeUtil;
import uniresolver.result.ResolveResult;
import uniresolver.result.Result;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolveServlet extends HttpServlet implements Servlet {

	private static final Logger log = LoggerFactory.getLogger(ResolveServlet.class);

	public ResolveServlet() {

		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String requestPath = request.getRequestURI();

		String identifier = requestPath.substring(contextPath.length() + servletPath.length());
		if (identifier.startsWith("/")) identifier = identifier.substring(1);
		identifier = URLDecoder.decode(identifier, StandardCharsets.UTF_8);

		if (log.isInfoEnabled()) log.info("Driver: Incoming resolve request for identifier: " + identifier);

		if (identifier == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Driver: No identifier found in resolve request.");
			return;
		}

		// assume identifier is a DID

		String didString = identifier;

		// prepare resolution options

		String httpAcceptHeader = request.getHeader("Accept");
		if (log.isInfoEnabled()) log.info("Driver: Incoming Accept: header string: " + httpAcceptHeader);

		List<MediaType> httpAcceptMediaTypes = MediaType.parseMediaTypes(httpAcceptHeader != null ? httpAcceptHeader : ResolveResult.MEDIA_TYPE);
		MediaType.sortBySpecificityAndQuality(httpAcceptMediaTypes);

		String accept = HttpBindingServerUtil.acceptForHttpAccepts(httpAcceptMediaTypes);

		Map<String, Object> resolutionOptions = new HashMap<>();
		resolutionOptions.put("accept", accept);

		if (log.isDebugEnabled()) log.debug("Driver: Using resolution options: " + resolutionOptions);

		// invoke the driver

		Result result;

		try {
			result = InitServlet.getDriver().resolve(DID.fromString(didString), resolutionOptions);
			if (result == null) throw new ResolutionException(ResolutionException.ERROR_NOTFOUND, "Driver: No resolve result for " + didString);
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Driver: Resolve problem for " + didString + ": " + ex.getMessage(), ex);
			if (! (ex instanceof ResolutionException)) ex = new ResolutionException("Driver: Resolve problem for " + didString + ": " + ex.getMessage());
			result = ((ResolutionException) ex).toErrorResolveResult();
		}

		if (log.isInfoEnabled()) log.info("Driver: Result for " + didString + ": " + result);

		// write resolve result

		for (MediaType httpAcceptMediaType : httpAcceptMediaTypes) {

			int httpStatusCode = HttpBindingServerUtil.httpStatusCodeForResult(result);

			if (result instanceof ResolveResult && MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, ResolveResult.MEDIA_TYPE)) {
				if (log.isDebugEnabled()) log.debug("Driver: Supporting HTTP media type " + httpAcceptMediaType + " via default resolve result content type " + ResolveResult.MEDIA_TYPE);
				ServletUtil.sendResponse(
						response,
						httpStatusCode,
						ResolveResult.MEDIA_TYPE,
						HttpBindingServerUtil.httpBodyForResult(result));
				return;
			}


			// determine representation media type

			if (MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, result.getContentType())) {
				if (log.isDebugEnabled()) log.debug("Driver: Supporting HTTP media type " + httpAcceptMediaType + " via content type " + result.getContentType());
			} else {
				if (log.isDebugEnabled()) log.debug("Driver: Not supporting HTTP media type " + httpAcceptMediaType);
				continue;
			}

			ServletUtil.sendResponse(
					response,
					httpStatusCode,
					result.getContentType(),
					result.getFunctionContent()
			);
			return;
		}

		ServletUtil.sendResponse(
				response,
				HttpServletResponse.SC_NOT_ACCEPTABLE,
				"Not acceptable media types " + httpAcceptHeader);
	}
}

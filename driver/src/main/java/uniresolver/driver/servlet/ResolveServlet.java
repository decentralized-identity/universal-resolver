package uniresolver.driver.servlet;

import foundation.identity.did.DID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.ResolutionException;
import uniresolver.driver.util.HttpBindingServerUtil;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;
import uniresolver.util.HttpBindingUtil;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "Driver: No identifier found in resolve request.");
			return;
		}

		// assume identifier is a DID

		String didString = identifier;

		// prepare resolution options

		String httpAcceptHeader = request.getHeader("Accept");
		if (log.isInfoEnabled()) log.info("Driver: Incoming Accept: header string: " + httpAcceptHeader);

		List<MediaType> httpAcceptMediaTypes = MediaType.parseMediaTypes(httpAcceptHeader != null ? httpAcceptHeader : ResolveResult.MEDIA_TYPE);
		MediaType.sortBySpecificityAndQuality(httpAcceptMediaTypes);

		String accept = HttpBindingServerUtil.acceptForHttpAcceptMediaTypes(httpAcceptMediaTypes);

		Map<String, Object> resolutionOptions = new HashMap<>();
		resolutionOptions.put("accept", accept);

		if (log.isDebugEnabled()) log.debug("Driver: Using resolution options: " + resolutionOptions);

		// invoke the driver

		ResolveRepresentationResult resolveRepresentationResult;

		try {

			resolveRepresentationResult = InitServlet.getDriver().resolveRepresentation(DID.fromString(didString), resolutionOptions);
			if (resolveRepresentationResult == null) throw new ResolutionException(ResolveResult.ERROR_NOTFOUND, "Driver: No resolve result for " + didString);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver: Resolve problem for " + didString + ": " + ex.getMessage(), ex);

			if (! (ex instanceof ResolutionException)) {
				ex = new ResolutionException(ResolveResult.ERROR_INTERNALERROR, "Driver: Resolve problem for " + didString + ": " + ex.getMessage());
			}

			resolveRepresentationResult = ResolveRepresentationResult.makeErrorResult((ResolutionException) ex, accept);
		}

		if (log.isInfoEnabled()) log.info("Driver: Resolve result for " + didString + ": " + resolveRepresentationResult);

		// write resolve result

		for (MediaType acceptMediaType : httpAcceptMediaTypes) {

			if (HttpBindingServerUtil.isMediaTypeAcceptable(acceptMediaType, ResolveResult.MEDIA_TYPE)) {

				ServletUtil.sendResponse(
						response,
						HttpBindingServerUtil.httpStatusCodeForResult(resolveRepresentationResult),
						ResolveResult.MEDIA_TYPE,
						HttpBindingServerUtil.toHttpBodyResolveRepresentationResult(resolveRepresentationResult));
				return;
			} else {

				// determine representation media type

				String representationMediaType = HttpBindingUtil.representationMediaTypeForMediaType(acceptMediaType.toString());
				if (representationMediaType != null) {
					if (log.isDebugEnabled()) log.debug("Supporting HTTP media type " + acceptMediaType + " via DID document representation media type " + representationMediaType);
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

		ServletUtil.sendResponse(
				response,
				HttpServletResponse.SC_NOT_ACCEPTABLE,
				null,
				"Not acceptable media types " + httpAcceptHeader);
		return;
	}
}

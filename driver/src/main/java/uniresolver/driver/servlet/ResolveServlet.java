package uniresolver.driver.servlet;

import foundation.identity.did.DID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.ResolutionException;
import uniresolver.driver.util.HttpBindingServerUtil;
import uniresolver.result.ResolveResult;

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

		ResolveResult resolveResult;

		try {

			resolveResult = InitServlet.getDriver().resolveRepresentation(DID.fromString(didString), resolutionOptions);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver: Resolve problem for " + didString + ": " + ex.getMessage(), ex);

			if (ex instanceof ResolutionException && ((ResolutionException) ex).getResolveResult() != null) {
				resolveResult = ((ResolutionException) ex).getResolveResult();
			} else {
				resolveResult = ResolveResult.makeErrorResolveRepresentationResult(ResolveResult.ERROR_INTERNALERROR, "Driver: Resolve problem for " + didString + ": " + ex.getMessage(), accept);
			}

			ServletUtil.sendResponse(response, HttpBindingServerUtil.httpStatusCodeForResolveResult(resolveResult), null, HttpBindingServerUtil.toHttpBodyResolveResult(resolveResult));
			return;
		}

		if (log.isInfoEnabled()) log.info("Driver: Resolve result for " + didString + ": " + resolveResult);

		// no resolve result?

		if (resolveResult == null || resolveResult.getDidDocumentStream() == null) {

			resolveResult = ResolveResult.makeErrorResolveRepresentationResult(ResolveResult.ERROR_NOTFOUND, "Driver: No resolve result for " + didString, accept);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null,  HttpBindingServerUtil.toHttpBodyResolveResult(resolveResult));
			return;
		}

		// write resolve result

		ServletUtil.sendResponse(response, HttpBindingServerUtil.httpStatusCodeForResolveResult(resolveResult), ResolveResult.MEDIA_TYPE, HttpBindingServerUtil.toHttpBodyResolveResult(resolveResult));
	}
}

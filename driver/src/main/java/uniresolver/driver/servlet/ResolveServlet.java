package uniresolver.driver.servlet;

import foundation.identity.did.DID;
import foundation.identity.did.representations.Representations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

		Map<String, Object> resolutionOptions = new HashMap<>();
		resolutionOptions.put("accept", Representations.MEDIA_TYPE_JSONLD);

		// invoke the driver

		ResolveResult resolveResult;

		try {

			resolveResult = InitServlet.getDriver().resolveRepresentation(DID.fromString(didString), resolutionOptions);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver: Resolver problem for " + didString + ": " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver: Resolve problem for " + didString + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Driver: Resolve result for " + didString + ": " + resolveResult);

		// no resolve result?

		if (resolveResult == null || resolveResult.getDidDocumentStream() == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "Driver: No resolve result for " + didString);
			return;
		}

		// write resolve result

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, ResolveResult.MEDIA_TYPE, HttpBindingUtil.toHttpBodyResolveResult(resolveResult));
	}
}

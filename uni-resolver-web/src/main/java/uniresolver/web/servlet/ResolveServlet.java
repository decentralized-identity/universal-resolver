package uniresolver.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.driver.util.HttpBindingServerUtil;
import uniresolver.driver.util.MediaTypeUtil;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;
import uniresolver.result.Result;
import uniresolver.web.WebUniResolver;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResolveServlet extends WebUniResolver {

	protected static final Logger log = LoggerFactory.getLogger(ResolveServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String requestPath = request.getRequestURI();

		if (log.isDebugEnabled()) log.debug("Incoming dereference request: " + requestPath);

		String path = requestPath.substring(contextPath.length() + servletPath.length());
		if (path.startsWith("/")) path = path.substring(1);

		if (path.isEmpty()) {
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No identifier found in dereference request.");
			return;
		}

		// parse request

		String identifier;
		Map<String, Object> options = new LinkedHashMap<>();

		if (path.startsWith("did%3A")) {
			identifier = URLDecoder.decode(path, StandardCharsets.UTF_8);
			if (request.getParameterMap() != null) {
				for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
					String parameterName = e.nextElement();
					String parameterValue = request.getParameter(parameterName);
					options.put(parameterName, parameterValue);
				}
			}
		} else {
			identifier = path;
			if (request.getQueryString() != null) identifier += "?" + request.getQueryString();
		}

		if (log.isInfoEnabled()) log.info("Incoming identifier: " + identifier + ", incoming options: " + options);

		// prepare dereference options

		String httpXConfigHeader = request.getHeader("X-Config");
		if (log.isInfoEnabled()) log.info("Incoming X-Config: header string: " + httpXConfigHeader);
		Map<String, Object> httpXConfigHeaderMap = httpXConfigHeader == null ? null : (Map<String, Object>) objectMapper.readValue(httpXConfigHeader, Map.class);
		if (httpXConfigHeaderMap != null) options.put("_http_x_config", httpXConfigHeaderMap);

		String httpAcceptHeader = request.getHeader("Accept");
		if (log.isInfoEnabled()) log.info("Incoming Accept: header string: " + httpAcceptHeader);

		List<MediaType> httpAcceptMediaTypes = MediaType.parseMediaTypes(httpAcceptHeader != null ? httpAcceptHeader : ResolveResult.MEDIA_TYPE);
		MediaType.sortBySpecificityAndQuality(httpAcceptMediaTypes);
		if (httpAcceptHeader != null) options.put("_http_accept", httpAcceptMediaTypes);

		String accept = HttpBindingServerUtil.acceptForHttpAccepts(httpAcceptMediaTypes);
		if (! options.containsKey("accept")) {
			options.put("accept", accept);
		}

		if (log.isDebugEnabled()) log.debug("Using options: " + options);

		// execute the request

		Result result;

		if (isBareDid(identifier)) {
			try {
				result = this.resolve(identifier, options);
				if (result == null) throw new ResolutionException(DereferencingException.ERROR_NOTFOUND, "No resolve result for " + identifier);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Resolve problem for " + identifier + ": " + ex.getMessage(), ex);
				if (! (ex instanceof ResolutionException)) ex = new ResolutionException("Resolve problem for " + identifier + ": " + ex.getMessage(), ex);
				result = ((ResolutionException) ex).toErrorResolveResult();
			}
		} else {
			try {
				result = this.dereference(identifier, options);
				if (result == null) throw new DereferencingException(DereferencingException.ERROR_NOTFOUND, "No dereference result for " + identifier);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Dereference problem for " + identifier + ": " + ex.getMessage(), ex);
				if (ex instanceof ResolutionException) ex = new DereferencingException(((ResolutionException) ex).getError(), "Error " + ((ResolutionException) ex).getError() + " from resolver: " + ex.getMessage(), ((ResolutionException) ex).getDidResolutionMetadata(), ex);
				if (! (ex instanceof DereferencingException)) ex = new DereferencingException("Dereference problem for " + identifier + ": " + ex.getMessage(), ex);
				result = ((DereferencingException) ex).toErrorDereferenceResult();
			}
		}

		if (log.isInfoEnabled()) log.info("Result for " + identifier + ": " + result);

		// experimental: support for redirect

		if (Integer.valueOf(HttpServletResponse.SC_SEE_OTHER).equals(result.getFunctionMetadata().get("_http_code"))) {
			Map<String, String> httpHeaders = (Map<String, String>) result.getFunctionMetadata().get("_http_headers");
			if (log.isDebugEnabled()) log.debug("Redirecting with HTTP headers: " + httpHeaders);
			ServletUtil.sendResponse(
					response,
					HttpServletResponse.SC_SEE_OTHER,
					httpHeaders,
					null);
			return;
		}

		// write result

		for (MediaType httpAcceptMediaType : httpAcceptMediaTypes) {

			int httpStatusCode = HttpBindingServerUtil.httpStatusCodeForResult(result);

			if (result instanceof ResolveResult && MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, ResolveResult.MEDIA_TYPE)) {
				if (log.isDebugEnabled()) log.debug("Supporting HTTP media type " + httpAcceptMediaType + " via default resolve result content type " + ResolveResult.MEDIA_TYPE);
				String httpBody = HttpBindingServerUtil.httpBodyForResult(result);
				ServletUtil.sendResponse(
						response,
						httpStatusCode,
						ResolveResult.MEDIA_TYPE,
						httpBody);
				return;
			}

			if (result instanceof DereferenceResult && MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, DereferenceResult.MEDIA_TYPE)) {
				if (log.isDebugEnabled()) log.debug("Supporting HTTP media type " + httpAcceptMediaType + " via default dereference result content type " + DereferenceResult.MEDIA_TYPE);
				String httpBody = HttpBindingServerUtil.httpBodyForResult(result);
				ServletUtil.sendResponse(
						response,
						httpStatusCode,
						DereferenceResult.MEDIA_TYPE,
						httpBody);
				return;
			}

			// determine representation media type

			if (MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, result.getContentType())) {
				if (log.isDebugEnabled()) log.debug("Supporting HTTP media type " + httpAcceptMediaType + " via content type " + result.getContentType());
			} else {
				if (log.isDebugEnabled()) log.debug("Not supporting HTTP media type " + httpAcceptMediaType);
				continue;
			}

            byte[] httpBody = result.getFunctionContent();
            ServletUtil.sendResponse(
					response,
					httpStatusCode,
					result.getContentType(),
					httpBody);
			return;
		}

		ServletUtil.sendResponse(
				response,
				HttpServletResponse.SC_NOT_ACCEPTABLE,
				"Not acceptable media types " + httpAcceptHeader);
	}

	private static boolean isBareDid(String identifier) {
		return (!identifier.contains("/")) && (!identifier.contains("?")) && (!identifier.contains("#"));
	}
}
package uniresolver.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

import com.github.jsonldjava.core.JsonLdError;

import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.ddo.DDO;

public class WebUniResolver extends HttpServlet implements HttpRequestHandler, UniResolver {

	private static final long serialVersionUID = -8314214552475026363L;

	private static Logger log = LoggerFactory.getLogger(WebUniResolver.class);

	private UniResolver uniResolver;

	public WebUniResolver() {

		super();
	}

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

		if (log.isInfoEnabled()) log.info("Incoming request for identifier: " + identifier);

		if (identifier == null) {

			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No identifier found in resolution request.");
			return;
		}

		// resolve the identifier

		DDO ddo;

		try {

			ddo = this.resolve(identifier);
		} catch (ResolutionException ex) {

			if (log.isWarnEnabled()) log.warn("Resolution problem for " + identifier + ": " + ex.getMessage(), ex);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Resolution problem for " + identifier + ": " + ex.getMessage());
			return;
		}

		// no result?

		if (ddo == null) {

			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No result for " + identifier);
			return;
		}

		// write result

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(DDO.MIME_TYPE);
		PrintWriter writer = response.getWriter();

		try {

			writer.write(ddo.serialize());
		} catch (JsonLdError ex) {

			throw new IOException("JSON-LD error: " + ex.getMessage(), ex);
		}

		writer.flush();
		writer.close();
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if ("GET".equals(request.getMethod())) this.doGet(request, response);
	}

	@Override
	public DDO resolve(String identifier) throws ResolutionException {

		return this.getUniResolver() == null ? null : this.getUniResolver().resolve(identifier);
	}

	/*
	 * Getters and setters
	 */

	public UniResolver getUniResolver() {

		return this.uniResolver;
	}

	public void setUniResolver(UniResolver uniResolver) {

		this.uniResolver = uniResolver;
	}
}

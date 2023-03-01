package uniresolver.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.HttpRequestHandler;
import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveRepresentationResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
@WebServlet
public abstract class WebUniResolver extends HttpServlet implements HttpRequestHandler, UniResolver {

	@Autowired
	@Qualifier("UniResolver")
	private UniResolver uniResolver;

	protected WebUniResolver() {

		super();
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ("GET".equals(request.getMethod())) this.doGet(request, response);
		if ("POST".equals(request.getMethod())) this.doPost(request, response);
		if ("PUT".equals(request.getMethod())) this.doPut(request, response);
		if ("DELETE".equals(request.getMethod())) this.doDelete(request, response);
		if ("OPTIONS".equals(request.getMethod())) this.doOptions(request, response);
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type");
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	public ResolveDataModelResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		return this.getUniResolver() == null ? null : this.getUniResolver().resolve(didString, resolutionOptions);
	}

	@Override
	public ResolveRepresentationResult resolveRepresentation(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		return this.getUniResolver() == null ? null : this.getUniResolver().resolveRepresentation(didString, resolutionOptions);
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws ResolutionException {
		return this.getUniResolver() == null ? null : this.getUniResolver().properties();
	}

	@Override
	public Set<String> methods() throws ResolutionException {
		return this.getUniResolver() == null ? null : this.getUniResolver().methods();
	}

	@Override
	public Map<String, List<String>> testIdentifiers() throws ResolutionException {
		return this.getUniResolver() == null ? null : this.getUniResolver().testIdentifiers();
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

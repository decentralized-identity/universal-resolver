package uniresolver.web.servlet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import uniresolver.ResolutionException;
import uniresolver.web.WebUniResolver;

public class PropertiesServlet extends WebUniResolver {

	private static final long serialVersionUID = 3865183054854163102L;

	protected static Logger log = LoggerFactory.getLogger(WebUniResolver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		if (log.isInfoEnabled()) log.info("Incoming request.");

		// execute the request

		Map<String, Map<String, Object>> properties;

		try {

			properties = this.properties();
		} catch (ResolutionException ex) {

			if (log.isWarnEnabled()) log.warn("Resolution problem: " + ex.getMessage(), ex);
			WebUniResolver.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Resolution problem: " + ex.getMessage());
			return;
		}

		// no result?

		if (properties == null) {

			WebUniResolver.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No result.");
			return;
		}

		// write result

		StringWriter stringWriter = new StringWriter();
		objectMapper.writeValue(stringWriter, properties);
		WebUniResolver.sendResponse(response, HttpServletResponse.SC_OK, null, stringWriter.getBuffer().toString());
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if ("GET".equals(request.getMethod())) this.doGet(request, response);
	}
}
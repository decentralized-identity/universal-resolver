package uniresolver.driver.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.Driver;

import java.lang.reflect.InvocationTargetException;

public class InitServlet extends HttpServlet implements Servlet {

	private static final Logger log = LoggerFactory.getLogger(InitServlet.class);

	private static Driver driver = null;

	public InitServlet() {

		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		if (driver == null) {

			String driverClassName = config.getInitParameter("Driver");
			Class<? extends Driver> driverClass;

			try {

				driverClass = driverClassName == null ? null : (Class<? extends Driver>) Class.forName(driverClassName);
				driver = driverClass == null ? null : driverClass.getConstructor().newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {

				throw new ServletException(ex.getMessage(), ex);
			}

			if (driver == null) throw new ServletException("Unable to load driver: (no 'Driver' init parameter)");

			if (log.isInfoEnabled()) log.info("Loaded driver: " + driverClass);
		}
	}

	public static Driver getDriver() {
		return InitServlet.driver;
	}

	public static void setDriver(Driver driver) {
		InitServlet.driver = driver;
	}
}

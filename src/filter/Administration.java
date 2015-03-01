package filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebFilter;

/*import dao.LogDAO;
import dao.LogType;
import database.LogDB;*/
import budget.Permission;
import budget.User;

/**
 * Servlet Filter to protect servlets only for administrators
 */
@WebFilter({"/SendNewPassword", "/SetPolicy", "/SaveProduct", 
        	"/UpdatePrice", "/UploadProductImage", "/UploadCSV",
        	"/maintenance.jsp", "/RemoveCover"})
@MultipartConfig
public class Administration implements Filter {

    /**
     * Default constructor. 
     */
    public Administration() {
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, 
			FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		
		// servlet filter parameters
		javax.servlet.http.HttpServletRequest httpreq = 
				(javax.servlet.http.HttpServletRequest) request;
		javax.servlet.http.HttpServletResponse httpres =
			(javax.servlet.http.HttpServletResponse) response;
		
		User user = (User)httpreq.getSession(true).getAttribute("user");
		if (user != null && user.hasPermission(Permission.ADMINISTRATE))
			chain.doFilter(request, response);
		else {
			/*LogDAO logDAO = new LogDB();
			logDAO.add(LogType.WARNING, (user != null ? user.getName() : "") + 
						" ha intentado acceder a un recurso de administración sin permisos", 
						System.currentTimeMillis());*/
			httpres.sendRedirect("Logout");
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}

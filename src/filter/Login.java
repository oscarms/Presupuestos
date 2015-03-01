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

import budget.User;

/**
 * Servlet Filter to redirect users to login
 */
@WebFilter({"/*"})
@MultipartConfig
public class Login implements Filter {

    /**
     * Default constructor. 
     */
    public Login() {
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
		// servlet filter parameters
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		javax.servlet.http.HttpServletRequest httpreq = 
				(javax.servlet.http.HttpServletRequest) request;
		javax.servlet.http.HttpServletResponse httpres =
			(javax.servlet.http.HttpServletResponse) response;
		
		User user = (User)httpreq.getSession(true).getAttribute("user");
		
		String uri = httpreq.getRequestURI();
		uri = uri.substring(uri.lastIndexOf("/"));

		if (user != null || whitelist(uri))
			chain.doFilter(request, response);
		else
			httpres.sendRedirect("login.jsp");
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

	/**
	 * Checks if the requested page is in the whitelist
	 * 
	 * @param uri
	 * @return True if the uri is in the whitelist
	 */
	private boolean whitelist(String uri) {
		return ( uri != null && (
				uri.equals("/login.jsp") || 
				uri.equals("/Login") || 
				uri.equals("/styles.css") || 
				uri.equals("/logo.png") ||
				uri.equals("/favicon.png") ||
				uri.equals("/icon060.png") ||
				uri.equals("/icon076.png") ||
				uri.equals("/icon120.png") ||
				uri.equals("/icon152.png") ||
				uri.equals("/startupPs.png") ||
				uri.equals("/startupPr.png") ||
				uri.equals("/startupP5.png") ||
				uri.equals("/startupMh.png") ||
				uri.equals("/startupMv.png") ||
				uri.equals("/startupTh.png") ||
				uri.equals("/startupTv.png")
				) );
	}
}

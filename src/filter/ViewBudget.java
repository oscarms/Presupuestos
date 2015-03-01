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

import dao.BudgetDAO;
/*import dao.LogDAO;
import dao.LogType;
import database.LogDB;*/
import database.BudgetDB;
import budget.Budget;
import budget.Permission;
import budget.User;

/**
 * Servlet Filter to avoid unauthorized users from viewing or editing complements
 * (Attachments, Documents, Notes, Covers) of a budget
 */
@WebFilter({"/UploadCover", "/UploadDocument", "/UploadAttachment", "/AddNote", 
        	"/SelectCover", "/notes.jsp", "/covers.jsp", "/documents.jsp", 
        	"/attachments.jsp", "/EmailBudget"})
@MultipartConfig
public class ViewBudget implements Filter {

    /**
     * Default constructor. 
     */
    public ViewBudget() {
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
		
		String budgetId = request.getParameter("budgetId");
		User user = (User)httpreq.getSession(true).getAttribute("user");
		//LogDAO logDAO = new LogDB();
		
		// Check parameters
		if ( user == null || budgetId == null || budgetId.length() < 1 ) {
			// ERROR, log and logout user
			/*logDAO.add(LogType.WARNING, (user != null ? user.getName() : "") + 
					" ha intentado ver complementos de un presupuesto sin argumentos", 
					System.currentTimeMillis());*/
			httpres.sendRedirect("Logout");
		} else {
			BudgetDAO budgetDAO = new BudgetDB();
			Budget budget = budgetDAO.getBudget(budgetId);
	
			// Check if the budget exists
			if (budget == null) {
				// ERROR, log and logout user
				/*logDAO.add(LogType.WARNING, user.getName() + 
						" ha intentado acceder al complemento de un presupuesto inexistente", 
						System.currentTimeMillis());*/
				httpres.sendRedirect("Logout");
			} else {
	
				// check if the user is able to view the budget:
				// Can view all clients or is his client, and not is an offer and cannot view offers
				if((user.equals(budget.getSalesperson()) || user.hasPermission(Permission.ALLCLIENTS)) 
						&& ( (budget.isOffer() && user.hasPermission(Permission.VIEWOFFERS) )
						|| !budget.isOffer() ) ) {
					
					chain.doFilter(request, response);
					
				} else {
					/*logDAO.add(LogType.WARNING, user.getName() + 
					" ha intentado acceder al complemento de un presupuesto sin permisos", 
					System.currentTimeMillis());*/
			httpres.sendRedirect("Logout");
				}
		
			}
		}

	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}

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
 * Servlet Filter to avoid unauthorized users from editing a budget
 */
@WebFilter({"/SetClient", "/SetConstructionRef", "/SetExpirationDate", 
        	"/SetNote", "/AddSection", "/RenameSection", "/AddProduct", 
        	"/SetTaxRate", "/SetGlobalTotal", "/SetProductQuantity", 
        	"/CreateBudget", "/CheckBudget", "/SetDiscounts", "/SignOffer", 
        	"/createsections.jsp", "/createbudget.jsp"})
@MultipartConfig
public class EditBudget implements Filter {

    /**
     * Default constructor. 
     */
    public EditBudget() {
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
		// initializate and get parameters
		User user = (User)httpreq.getSession(true).getAttribute("user");
		BudgetDAO budgetDAO = new BudgetDB();
		/*LogDAO logDAO = new LogDB();*/
		String budgetId = request.getParameter("budgetId");
		if (user == null || budgetId == null) {
			/*logDAO.add(LogType.WARNING, (user != null ? user.getName() : "") 
			  		+ " ha intentado editar un presupuesto sin argumentos", 
					System.currentTimeMillis());*/
			httpres.sendRedirect("Logout");
		} else {
		
		Budget budget = budgetDAO.getBudget(budgetId);

			/*  
			 *  check if it is all the necessary data:
			 *  a budget in creation
			 *  or an offer in creation / not signed and the user is an administrator
			 *  and the budgetId is the same as the budget
			 */
			if (budget == null ||
				( !user.equals(budget.getAuthor()) && 
				!(user.hasPermission(Permission.ADMINISTRATE) && budget.isOffer() && 
						budget.getCreationDate() > 0 && budget.getSigner() == null) ) ||
				(budget.isOffer() && budget.getSigner() != null) || 
				(!budget.isOffer() && budget.getCreationDate() > 0)
				) {
				/*logDAO.add(LogType.WARNING, user.getName() 
				 		+ " ha provocado un error al editar un presupuesto", 
						System.currentTimeMillis());*/
				httpres.sendRedirect("Logout");
			} else { // data is correct and user has permissions
				chain.doFilter(request, response);
			}
		
		}

	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}

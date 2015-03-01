package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;
import mail.Mail;
import budget.Budget;
import budget.User;

/**
 * Servlet implementation class EmailBudget
 * 
 * Sends a budget by e-mail
 */
@WebServlet("/EmailBudget")
@MultipartConfig
public class EmailBudget extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmailBudget() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// retrieve parameters
		User user = (User)request.getSession(true).getAttribute("user");
		String budgetId = request.getParameter("budgetId");
		
		// filter ViewBudget ensures that the user can view the budget
		Mail mail = new Mail();
		BudgetDAO budgetDAO = new BudgetDB();
		Budget budget = budgetDAO.getBudget(budgetId);
		LogDAO logDAO = new LogDB();
		PrintWriter out = response.getWriter();
		out.println("<div style='color:#444'>Espere mientras se envía el mensaje de correo...<br /></div>");
		out.flush();
		if (mail.sendBudget(budget)) {
			// done
			logDAO.add(LogType.ACTION, user.getName() + " ha enviado por e-mail el presupuesto " + budgetId, 
					System.currentTimeMillis());
			out.println("<div style='color:#444'>Mensaje de correo electrónico enviado<br /></div>");
			out.flush();
		} else {
			// error
			logDAO.add(LogType.ERROR, user.getName() + " no ha podido enviar por e-mail el presupuesto " + budgetId, 
					System.currentTimeMillis());
			out.println("<div style='color:#C03'>Ha ocurrido un error durante el envío<br /></div>");
			out.flush();
		}
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}

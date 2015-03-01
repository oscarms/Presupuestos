/**
 * Objects to send e-mails
 */
package mail;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import dao.FileDAO;
import dao.FileType;
import pdf.PdfBox;
import storage.FileLocalStorage;
import budget.Budget;
import budget.User;

/**
 * This class provides the methods to send e-mails
 * 
 * @author oscar
 */
public class Mail {

	/*
	 * Attributes
	 */
	private static final String ERRORDESTINATION = "gestiondpresupuestos@gmail.com"; // email of the system administrator to send to him errors of the application
	private static final String EMAIL = "gestiondpresupuestos@gmail.com"; // default mail address for sending e-mails
	private static final String NAME = "Gestión de Presupuestos"; // default name for sending e-mails
	private static final String PASSWORD = "gdpP4s5w0rd"; // password of the default mail address
	private static final String SMTP = "smtp.gmail.com"; // mail server
	private Properties properties = new Properties();
	private static final String MAILSTART = 
			"<div style=\"width:auto;height:auto;margin:0px;padding:5px;overflow:hidden;\">"
			+ "<img src=\"cid:logo\" alt=\"Gestión de Presupuestos\" style=\"margin:0px;width:420px;height:85px;float:left;\"/>"
			+ "</div><div style=\"margin:0px;width:auto;height:0px;padding:1px;overflow:hidden;background:rgb(34, 34, 34);\"></div>"
			+ "<div style=\"display:inline-block;width:100%;line-height:24px;font-size:14px;margin:0px 0px 4px 0px;\"><div style=\"float:left;\">"
			+ "<h2 style=\"font-family: 'Helvetica', 'Arial', sans-serif;color:rgb(34, 34, 34);font-size:18px;padding:4px 0px 4px 0px;\">";
	private static final String MAILMIDDLE = 
			"</h2></div></div><div style=\"font-family: 'Helvetica', 'Arial', sans-serif;color:rgb(34, 34, 34);font-size:14px;float:left;\">";
	private static final String MAILEND = 
			"</div><div style=\"width:auto;height:auto;padding:8px;margin-top:20px;margin-right:auto;margin-left:auto;"
			+ "font-family: 'Helvetica', 'Arial', sans-serif;color:rgb(34, 34, 34);font-size:10px;text-align:center;clear:both;\">"
			+ "<p>© Gestión de Presupuestos, 2014</p></div>";
	
	/**
	 * Constructor
	 */
	public Mail() {
		// setup general properties
		properties.put("mail.smtp.host", SMTP);
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.port", "465");
	}
	
	/**
	 * Sends a budget to its Client from its Salesperson.
	 * If Salesperson not specifies his e-mail password,
	 * sends the email from the default EMAIL.
	 *
	 * @param budget The budget to send by email
	 * @return True if it is sent successfully
	 */
	public boolean sendBudget(Budget budget) {
		
		String from;
		String password;
		if (budget.getSalesperson().hasEmailPassword()) {
			from = budget.getSalesperson().getEmail();
			password = budget.getSalesperson().getEmailPassword();
		}
		else
			from = EMAIL;
			password = PASSWORD;
		
		String fromName = budget.getSalesperson().getName();
		String to = budget.getClient().getEmail();
		String toName = budget.getClient().getName();
		String cc = budget.getSalesperson().getEmail();
		String ccName = budget.getSalesperson().getName();

		String subject = (budget.isOffer() ? "Oferta " : "Presupuesto ") + budget.getBudgetId();
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date expiration = new Date( budget.getExpirationDate() );
		String title = "Su " + (budget.isOffer() ? "oferta" : "presupuesto");
		String content =
			"Adjunto " + (budget.isOffer() ? "la oferta" : "el presupuesto") + " que solicitó. "
			+ "Puede contactar con su comercial " + budget.getSalesperson().getName() 
			+ " para cualquier consulta, cambio o realizar el pedido antes de la fecha "
			+ "de vencimiento (" + dateFormat.format(expiration) + ").";
		return sendEmail(from, fromName, password, to, toName, cc, ccName, 
				subject, title, content, PdfBox.createPdf(budget));
		
	}
	
	/**
	 * Sends a notification to a user when an offer is signed, etc
	 *
	 * @param user The user that will receive the email
	 * @param budget The budget that generated the notification
	 * @return True if it is sent successfully
	 */
	public boolean sendNotification(User user, Budget budget) {
		String from = EMAIL;
		String fromName = NAME;
		String to = user.getEmail();
		String toName = user.getName();

		String subject = "Nuevas notificaciones";
		String title = "Notificación de" + ( budget.isOffer() ? " la oferta " : "l presupuesto ") + budget.getBudgetId();
		String content = "Tiene una nueva notificación sobre " 
				+ ( budget.isOffer() ? "la oferta " 
						+ ( budget.getSigner() != null ? 
								"que ha sido firmada por " + budget.getSigner().getName() + ", número " 
								: "pendiente de firma creada por "+ budget.getAuthor() + ", número ") 
						: "el presupuesto creado por " + budget.getAuthor() + ", número ") 
				+ budget.getBudgetId() 
				+ ", que corresponde al cliente " + budget.getClient().getName() 
				+ " y al comercial " + budget.getSalesperson().getName()
				+ ". Acceda a Gestión de Presupuestos > Notificaciones para ver más detalles.";

		return sendEmail(from, fromName, null, to, toName, null, null,
				subject, title, content, null);
	}
	
	/**
	 * Sends a new password to a user
	 *
	 * @param user The user that will receive the email
	 * @param password The new password
	 * @return True if it is sent successfully
	 */
	public boolean sendPassword(User user, String password) {
		String from = EMAIL;
		String fromName = NAME;
		String to = user.getEmail();
		String toName = user.getName();
		String subject = "Su nueva contraseña";
		String title = "Bienvenido a 'Gestión de Presupuestos'";
		String content =
			"Esta es la nueva contraseña que necesita para acceder a Gestión de presupuestos:<br /> " + password 
			+ "<br /><br />Puede cambiarla por una que le resulte más fácil de recordar.";
		return sendEmail(from, fromName, null, to, toName, null, null, subject, title, content, null);
	}
	
	/**
	 * Sends the error description to ERRORDESTINATION
	 *
	 * @param message The error description
	 * @return True if it is sent successfully
	 */
	public boolean sendError(String message) {
		String from = EMAIL;
		String fromName = NAME;
		String to = ERRORDESTINATION;
		String toName = NAME;
		String subject = "Ha ocurrido un error importante";
		String title = "Error en 'Gestión de Presupuestos'";
		String content =
			"A continuación se detalla el error que se ha producido "
			+ "en la aplicacion de Gestión de Presupuestos:<br /> "
			+ message;
		return sendEmail(from, fromName, null, to, toName, null, null, subject, title, content, null);
	}
	
	/**
	 * Sends the e-mail
	 *
	 * @param from The email of the sender
	 * @param fromName The name of the sender
	 * @param password The password of the email of the sender
	 * @param to The email of the receiver
	 * @param toName The name of the receiver
	 * @param cc The email of the receiver of a copy
	 * @param ccName The name of the receiver of a copy
	 * @param subject The subject of the email
	 * @param title The title of the content of the email
	 * @param content The message of the email
	 * @param attachment A file to attach to the email
	 * @return True if it is sent successfully
	 */
	private boolean sendEmail(final String from, String fromName, final String password,
			String to, String toName, String cc, String ccName, String subject,
			String title, String content, File attachment) {

		Session session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						if (password == null)
							return new PasswordAuthentication(EMAIL,PASSWORD);
						else
							return new PasswordAuthentication(from,password);
					}
				});
	 
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from, fromName));
			message.addRecipient(Message.RecipientType.TO,
					new InternetAddress(to, toName));
			if (cc != null) {
				message.addRecipient(Message.RecipientType.CC,
						new InternetAddress(cc, ccName));
			}
			message.setSubject(subject);

			Multipart mp = new MimeMultipart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(MAILSTART + title + MAILMIDDLE + content + MAILEND, "text/html; charset=UTF-8");
			Date timeStamp = new Date();
	        message.setSentDate(timeStamp);
			mp.addBodyPart(htmlPart);
			
			// add image of logo
			MimeBodyPart imgPart = new MimeBodyPart();
			try {
				FileDAO fileDAO = new FileLocalStorage();
				File logo = fileDAO.get(FileType.GLOBAL, "logo.png");
				if (logo != null)
					imgPart.attachFile(logo);
				else return false;
			} catch (IOException e) {
				return false;
			}
	        imgPart.setHeader("Content-ID", "<logo>");
	        mp.addBodyPart(imgPart);
	        
			// add attachment 
			// from www.codejava.net/java-ee/jsp/send-attachments-with-e-mail-using-jsp-servlet-and-javamail#EmailUtility
			if (attachment != null) {
				MimeBodyPart attachmentPart= new MimeBodyPart();
				
                try {
                    attachmentPart.attachFile(attachment);
                    attachmentPart.setFileName(attachment.getName());
                } catch (IOException e) {
                    return false;
                }
 
                mp.addBodyPart(attachmentPart);
			}
			
			message.setContent(mp);
			
			Transport.send(message);
 
			return true;
			
		} catch (MessagingException e) {
			return false;
		} catch (UnsupportedEncodingException e) {

			return false;
		}
	}

}

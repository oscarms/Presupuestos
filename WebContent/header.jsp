<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="budget.User" %>
<%@ page import="budget.Permission" %>
<%@ page import="budget.UserType" %>
<!DOCTYPE html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<!-- Force not caching -->
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />

<link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Arimo" />
<link rel="stylesheet" type="text/css" href="styles.css" />
<% if (session.getAttribute("user") != null) { %>
<script type="text/javascript" src="scripts.js"></script>
<% } %>
<!-- script canvasloader -->
<script src="http://heartcode-canvasloader.googlecode.com/files/heartcode-canvasloader-min-0.9.1.js" type="text/javascript"></script>

<!-- favicon, iOS icon and iOS startup image -->
<link rel="icon" type="image/png" href="icons/favicon.png" /> <!-- 32x32px -->
<link rel="apple-touch-icon" href="icons/icon060.png" />
<link rel="apple-touch-icon" sizes="76x76" href="icons/icon076.png" />
<link rel="apple-touch-icon" sizes="120x120" href="icons/icon120.png" />
<link rel="apple-touch-icon" sizes="152x152" href="icons/icon152.png" />
<link rel="apple-touch-startup-image" href="icons/startupPs.png"> <!-- 320x460px -->
<link rel="apple-touch-startup-image" media="(device-width: 320px)" href="icons/startupPs.png"> <!-- 320x460px -->
<link rel="apple-touch-startup-image" media="(device-width: 320px) and (-webkit-device-pixel-ratio: 2)" href="icons/startupPr.png"> <!-- 320x460px double density -->
<link rel="apple-touch-startup-image" media="(device-width: 320px) and (device-height: 568px) and (-webkit-device-pixel-ratio: 2)" href="icons/startupP5.png"> <!-- 320x568px double density -->
<link rel="apple-touch-startup-image" media="(device-width: 768px) and (orientation: portrait)" href="icons/startupMv.png"> <!-- 768x1004px -->
<link rel="apple-touch-startup-image" media="(device-width: 768px) and (orientation: landscape)" href="icons/startupMh.png"> <!-- 748x1024px -->
<link rel="apple-touch-startup-image" media="(device-width: 1536px) and (orientation: portrait) and (-webkit-device-pixel-ratio: 2)" href="icons/startupPv.png"> <!-- 768x1004px double density -->
<link rel="apple-touch-startup-image" media="(device-width: 1536px)  and (orientation: landscape) and (-webkit-device-pixel-ratio: 2)" href="icons/startupPh.png"> <!-- 748x1024px double density -->
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="black" />

<title>Gestión de Presupuestos</title>
</head>

<body>
<%  User userHd = (User)session.getAttribute("user");
	if (userHd != null && userHd.getUserType() == UserType.SALESPERSON ) { %>
    <div id="header">
        <a href="index.jsp"><img src="images/logo.png" alt="Gestión de Presupuestos" id="hLogo"/></a>
        <div id="hNotifications"><a href="notifications.jsp" id="mNotifications"></a></div>
    </div>
    
    <div id="menu">
    	<div id="canvasloader">
        </div>
        <div id="mMenu">
            <ul>
                <li>
                    <a href="budgets.jsp" id="mBudgets"><%= (userHd.hasPermission(Permission.VIEWOFFERS) ? "Presupuestos y Ofertas" : "Presupuestos") %></a>
                </li>
                <li>
                    <a href="products.jsp" id="mProducts">Artículos</a>
                </li>
                <li>
                    <a href="clients.jsp" id="mClients">Clientes</a>
                </li>
                <% if (userHd.hasPermission(Permission.ADMINISTRATE)) { %>
                <li>
                    <a href="salespeople.jsp" id="mSalespeople">Comerciales</a>
                </li>
                <% } %>
            </ul>
        </div>
        <div id="mUser">
        	<ul>
            	<li>
                	<a href="salesperson.jsp" id="mLogged"><%= userHd.getName() %></a>
                 </li>
                 <li>   
                    <a href="Logout" id="mLogout">Salir</a>
                 </li>
             </ul>
        </div>
    </div>
    
    <div id="info" class="info">
    </div>
<% } else { %>
    <div id="header">
        <a href="index.jsp"><img src="images/logo.png" alt="Gestión de Presupuestos" id="hLogo"/></a>
    </div>
    
    <div id="menu">
    	<div id="canvasloader">
        </div>
        <div id="mMenu">
            <ul>
                <li>
                    &nbsp;
                </li>
             </ul>
        </div>
    </div>
    
    <div id="info" class="info">
    </div>
<% } %>
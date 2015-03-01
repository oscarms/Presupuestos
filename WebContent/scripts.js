// JavaScript Document

window.onload = init;

function init() {
	// select the tab for the content
	var thisurl = window.location.toString();
	thisurl=thisurl.substring(thisurl.lastIndexOf("/")+1,thisurl.lastIndexOf(".jsp"));
	if (thisurl.toLowerCase().indexOf("budget") != -1 ) {
		document.getElementById("mBudgets").className += "selected";
	}
	if (thisurl.indexOf("client") != -1 ) {
		document.getElementById("mClients").className += "selected";
	}
	if ( (thisurl.indexOf("salesperson.jsp") != -1) && (thisurl.indexOf("salesperson.jsp?") == -1)) {
		document.getElementById("mLogged").className += "selected";
	} else {
		if (thisurl.indexOf("salespe") != -1 ) {
			if (document.getElementById("mSalespeople"))
				document.getElementById("mSalespeople").className += "selected";
		}
	}
	if (thisurl.indexOf("product") != -1 ) {
		document.getElementById("mProducts").className += "selected";
	}

	// set a counter for the spinning wheel
	window.clCount = 0;
	
	// update notifications count now and every 10 seconds
	updateNotifications();
	setInterval("updateNotifications()", 10000);
	
	disableExternalLinks();
	
	// disable enter key
	document.onkeypress = stopRKey;
	
}


function stopRKey(evt) {
	// disable enter key from http://webcheatsheet.com/javascript/disable_enter_key.php
	evt = (evt) ? evt : ((event) ? event : null); 
	var node = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null); 
	if ((evt.keyCode == 13) && (node.type=="text")) {
		return false;
	}
}

function disableExternalLinks() {
	// workaround for iOS WebApp not to open links in external Safari
	var a=document.getElementsByTagName("a");
	for(var i=0;i<a.length;i++)
	{
		a[i].onclick=function()
		{
			window.location=this.getAttribute("href");
			return false;
		};
	}
}
	
function setMessage(message,isError) {
	document.getElementById("info").innerHTML = message;
	// set in red if isError or remove if no isError
	if (isError) {
		document.getElementById("info").style.color = '#C03';
	} else {
		document.getElementById("info").style.color = '#444';
	}
}

function updateNotifications() {
	var xmlHttp;
	try {// Firefox, Opera 8.0+, Safari
		xmlHttp = new XMLHttpRequest();		
	} catch (e) {// Internet Explorer
		try {
			xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
				alert("Este navegador no está soportado");
				return false;
			}
		}
	}
	
	canvasloaderOn('canvasloader');
	
	xmlHttp.onreadystatechange = function(){
		if (xmlHttp.readyState == 4) {
			canvasloaderOff('canvasloader');
			if (xmlHttp.status==200) {
				//Get the response from the server and extract the section that comes in the body section 
				// of the second html page avoid inserting the header part of the second page in your first page's element
				notificationsCount = xmlHttp.responseText;
				notificationsCountMessage(notificationsCount);
				return ;
			}
		}
	};
	
	xmlHttp.open("GET", "notificationscount.jsp", true);
	xmlHttp.send(null);
}

function updateDiv(div,url) {
	var xmlHttp;
	try {// Firefox, Opera 8.0+, Safari
		xmlHttp = new XMLHttpRequest();		
	} catch (e) {// Internet Explorer
		try {
			xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
				alert("Este navegador no está soportado");
				return false;
			}
		}
	}
	
	canvasloaderOn(div);

	xmlHttp.onreadystatechange = function(){
		if (xmlHttp.readyState == 4) {
			canvasloaderOff(div);
			if (xmlHttp.status==200) {
				//Get the response from the server and extract the section that comes in the body section
				// of the second html page avoid inserting the header part of the second page in your first page's element
				var respText = xmlHttp.responseText.split('<body>');
				elem.innerHTML = respText[0].split('</body>')[0];
				eval(getScripts(xmlHttp.responseText));
				disableExternalLinks();
			}
		}
	};

	var elem = document.getElementById(div);
	if (!elem) {
		return;
	}
	
	elem.innerHTML = "Espere...";

	xmlHttp.open("GET", url, true);
	xmlHttp.send(null);
}

function sendForm(div,url,formId) {
	var xmlHttp;
	try {// Firefox, Opera 8.0+, Safari
		xmlHttp = new XMLHttpRequest();		
	} catch (e) {// Internet Explorer
		try {
			xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
				alert("Este navegador no está soportado");
				return false;
			}
		}
	}
	
	canvasloaderOn(div);

	xmlHttp.onreadystatechange = function(){
		if (xmlHttp.readyState == 4) {
			canvasloaderOff(div);
			if (xmlHttp.status==200) {
				//Get the response from the server and extract the section that comes in the body section of the second html page avoid inserting the header part of the second page in your first page's element
				var respText = xmlHttp.responseText.split('<body>');
				elem.innerHTML = respText[0].split('</body>')[0];
				eval(getScripts(xmlHttp.responseText));
				disableExternalLinks();
			}
		}
	};

	var elem = document.getElementById(div);
	if (!elem) {
		return;
	}
	
	xmlHttp.open("POST", url, true);
	var fd = new FormData(document.getElementById(formId));
	if (formId.indexOf("filter") < 0 && !div=="sections" ) {
		elem.innerHTML = "Espere...";
	}
	xmlHttp.send(fd);
}

/*
 * Runs the text between <script> and </script>
 */
function getScripts(content) {
	var text = content;
	while(text.indexOf("<script") >= 0) {
		text = text.substring(text.indexOf("<script"));
		eval(text.substring(text.indexOf(">")+1,text.indexOf("</script>")) );
		text = text.substring(text.indexOf("</script>")+9);
	}
}

function notificationsCountMessage(notificationsCount) {
	if (notificationsCount == 0)
		document.getElementById("mNotifications").innerHTML = "No hay notificaciones";
	else if (notificationsCount == 1)
		document.getElementById("mNotifications").innerHTML = "1 Notificación";
	else if (notificationsCount > 1)
		document.getElementById("mNotifications").innerHTML = "" + notificationsCount + " Notificaciones";
	else
		document.getElementById("mNotifications").innerHTML = "";
}

function canvasloaderOn(div) {
	window.clCount++;
	if (window.cl == null) {
		window.cl = new CanvasLoader('canvasloader');
	}
	cl.setShape('spiral'); // default is 'oval'
	cl.setDiameter(23); // default is 40
	cl.setDensity(14); // default is 40
	cl.setRange(1.2); // default is 1.3
	cl.setSpeed(1); // default is 2
	cl.setFPS(20); // default is 24
	cl.setColor('#FFFFFF');
	cl.show(); // Hidden by default
	return true;
}

function canvasloaderOff(div) {
	window.clCount--;
	if (window.clCount == 0) {
		try {
		window.cl.hide();
		} catch (e) {
			return false;
		}
	}
	return true;
}

// from http://www.matlus.com/html5-file-upload-with-progress/

function fileCheck(type,size,mime) {
	// limit file size to 50MB
    if (size > 50 * 1024 * 1024) {
		if (type=="document")
			document.getElementById('documentToUploadBtn').style.visibility = "hidden";
		if (type=="attachment")
			document.getElementById('attachmentToUploadBtn').style.visibility = "hidden";
		if (type=="cover")
			document.getElementById('coverToUploadBtn').style.visibility = "hidden";
		if (type=="productImage")
			document.getElementById('imageToUploadBtn').style.visibility = "hidden";
		if (type=="clientImage")
			document.getElementById('imageToUploadBtn').style.visibility = "hidden";
		if (type=="csv")
			document.getElementById('csvToUploadBtn').style.visibility = "hidden";
		
    	alert("Seleccione un fichero con un tamaño inferior a 50MB");
		return false;
	}
	// if attachment only accept PDF
	if (type=="attachment" && mime != "application/pdf") {
		document.getElementById('attachmentToUploadBtn').style.visibility = "hidden";
    	alert("Seleccione un anexo en formato PDF");
		return false;
	}
	
	// if cover only accept images
	if ( type=="cover" && mime.toString().indexOf("image") == -1) {
		document.getElementById('coverToUploadBtn').style.visibility = "hidden";
    	alert("Seleccione un fichero de imagen");
		return false;
	}
	
	// if productImage only accept images
	if ( type=="productImage" && mime.toString().indexOf("image") == -1) {
		document.getElementById('imageToUploadBtn').style.visibility = "hidden";
    	alert("Seleccione un fichero de imagen");
		return false;
	}
	
	// if clientImage only accept images
	if ( type=="clientImage" && mime.toString().indexOf("image") == -1) {
		document.getElementById('imageToUploadBtn').style.visibility = "hidden";
    	alert("Seleccione un fichero de imagen");
		return false;
	}
	
	// if csv only accept csv
	if ( type=="csv" && mime.toString().indexOf("text") == -1) {
		document.getElementById('csvToUploadBtn').style.visibility = "hidden";
    	alert("Seleccione un fichero CSV");
		return false;
	}
	
	// if document accept all mimes
	
	if (type=='document')
		document.getElementById('documentToUploadBtn').style.visibility = "visible";
	if (type=='attachment')
		document.getElementById('attachmentToUploadBtn').style.visibility = "visible";
	if (type=='cover')
		document.getElementById('coverToUploadBtn').style.visibility = "visible";
	if (type=='productImage')
		document.getElementById('imageToUploadBtn').style.visibility = "visible";
	if (type=='clientImage')
		document.getElementById('imageToUploadBtn').style.visibility = "visible";
	if (type=='csv')
		document.getElementById('csvToUploadBtn').style.visibility = "visible";
	return true;
		
}

function fileSelected(type) {
	
	var file = null;
	
	if (type=='document')
		file = document.getElementById('documentToUpload').files[0];
	if (type=='attachment')
		file = document.getElementById('attachmentToUpload').files[0];
	if (type=='cover')
		file = document.getElementById('coverToUpload').files[0];
	if (type=='productImage')
		file = document.getElementById('imageToUpload').files[0];
	if (type=='clientImage')
		file = document.getElementById('imageToUpload').files[0];
	if (type=='csv')
		file = document.getElementById('csvToUpload').files[0];
	
  	if (file) {
		setProgress("0%",'&nbsp');
		fileCheck(type,file.size,file.type);
	}
}

function uploadFile(type) {
	
  	var xhr = new XMLHttpRequest();
  	var fd = null;
		
	if (type=='document') {
		document.getElementById('documentToUploadBtn').style.visibility = "hidden";
		fd = new FormData(document.getElementById('uploadDocument'));
	}
	if (type=='attachment') {
		document.getElementById('attachmentToUploadBtn').style.visibility = "hidden";
		fd = new FormData(document.getElementById('uploadAttachment'));
	}
	if (type=='cover') {
		document.getElementById('coverToUploadBtn').style.visibility = "hidden";
		fd = new FormData(document.getElementById('uploadCover'));
	}
	if (type=='productImage') {
		document.getElementById('imageToUploadBtn').style.visibility = "hidden";
		fd = new FormData(document.getElementById('uploadImage'));
	}
	if (type=='clientImage') {
		document.getElementById('imageToUploadBtn').style.visibility = "hidden";
		fd = new FormData(document.getElementById('uploadImage'));
	}
	if (type=='csv') {
		document.getElementById('csvToUploadBtn').style.visibility = "hidden";
		fd = new FormData(document.getElementById('uploadCSV'));
	}
	
  	/* event listners */
  	xhr.upload.addEventListener("progress", uploadProgress, false);
  	xhr.addEventListener("load", uploadCompleted, false);
  	xhr.addEventListener("error", uploadFailed, false);
  	xhr.addEventListener("abort", uploadCanceled, false);
  	/* Be sure to change the url below to the url of your upload server side script */
	if (type=='document')
		xhr.open("POST", "UploadDocument");
	if (type=='attachment')
		xhr.open("POST", "UploadAttachment");
	if (type=='cover')
		xhr.open("POST", "UploadCover");
	if (type=="productImage")
		xhr.open("POST", "UploadProductImage");
	if (type=="clientImage")
		xhr.open("POST", "UploadClientImage");
	if (type=="csv")
		xhr.open("POST", "UploadCSV");
	
  	xhr.send(fd);
}

function uploadProgress(evt) {
  	if (evt.lengthComputable) {
   		var percentComplete = Math.round(evt.loaded * 100 / evt.total);
		setProgress(percentComplete.toString() + "%",percentComplete.toString() + "%&nbsp");
  	}
  	else
		setProgress("0%",'Incalculable&nbsp');
}

function uploadCompleted(evt) {
	setProgress("100%",evt.target.responseText + '&nbsp');

	if ( document.getElementById('documentlist') ) {
		updateDiv('documentlist','documents.jsp?budgetId='+document.getElementById("budgetid").innerHTML);
		updateDiv('noteslist','notes.jsp?budgetId='+document.getElementById("budgetid").innerHTML);
		window.location = "#closeModalWindow";
	}
	if ( document.getElementById('attachmentlist') ) {
		updateDiv('attachmentlist','attachments.jsp?budgetId='+document.getElementById("budgetid").innerHTML);
		updateDiv('noteslist','notes.jsp?budgetId='+document.getElementById("budgetid").innerHTML);
		window.location = "#closeModalWindow";
	}
	if ( document.getElementById('coverlist') ) {
		updateDiv('coverlist','covers.jsp?budgetId='+document.getElementById("budgetid").innerHTML);
		window.location = "#closeModalWindow";
	}
	if ( document.getElementById('productimage') ) {
		document.getElementById('productimage').src = 'Download?productImage=' + document.getElementById('productId').value + '&nocache=' + new Date();
		window.location = "#closeModalWindow";
	}
	if ( document.getElementById('clientimage') ) {
		document.getElementById('clientimage').src = 'Download?clientImage=' + document.getElementById('clientId').value + '&nocache=' + new Date();
		window.location = "#closeModalWindow";
	}
	if ( document.getElementById('productlist') ) {
		updateDiv('productlist','productlist.jsp');
		window.location = "#closeModalWindow";
	}
}

function uploadFailed(evt) {
	setProgress("0%",'Fallido&nbsp');
	alert("Ha habido un error durante la carga del fichero.");
}

function uploadCanceled(evt) {
	setProgress("0%",'Cancelado&nbsp');
  	alert("La carga ha sido cancelada por el usuario o el navegador ha finalizado la conexión.");
}

function setProgress(width,text) {
	var elms = document.getElementsByClassName('progress');
	for (var i=0; i<elms.length; i++) { 
  		elms.item(i).getElementsByTagName('p').item(0).style.width = width;
		elms.item(i).getElementsByTagName('p').item(0).innerHTML = text;
	}
}

// Validates that the input string is a valid number
function getNumber(n) {
	if (n.length == 0) {
		n = 0;
		return 0;
	}
		
	if (isNaN(parseFloat(n)) || !(isFinite(n)))
		return null;
	else
		return n;
}

// Validates that the input string is a valid date formatted as "dd/mm/yyyy"
function isValidDate(dateString) {
	// If the box is empty, it means no date is specified
	if (dateString.length == 0)
		return true;
	
    // First check for the pattern
    if(!/^\d{2}\/\d{2}\/\d{4}$/.test(dateString)) {
        return false;
	}

    // Parse the date parts to integers
    var parts = dateString.split("/");
    var day = parseInt(parts[0], 10);
    var month = parseInt(parts[1], 10);
    var year = parseInt(parts[2], 10);

    // Check the ranges of month and year
    if(year < 1000 || year > 3000 || month == 0 || month > 12)
        return false;

    var monthLength = [ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 ];

    // Adjust for leap years
    if(year % 400 == 0 || (year % 100 != 0 && year % 4 == 0))
        monthLength[1] = 29;

    // Check the range of the day
    return (day > 0 && day <= monthLength[month - 1]);
}

function wrongDateMessage() {
	var today = new Date();
	var dd = today.getDate();
	var mm = today.getMonth()+1; //January is 0!
	var yyyy = today.getFullYear();
	if(dd<10) {
		dd='0'+dd;
	}
	if(mm<10) {
		mm='0'+mm;
	}
	today = dd+'/'+mm+'/'+yyyy;
	setMessage("Introduzca la fecha con formato dd/mm/aaaa, por ejemplo " + today,true);
}

function isPastDate(dateString) {
	if (!isValidDate(dateString))
		return false;
	var parts = dateString.split("/");
    var day = parseInt(parts[0], 10);
    var month = parseInt(parts[1], 10);
    var year = parseInt(parts[2], 10);
	var today = new Date();
	var otherDate = new Date(year, month-1, day, 0, 0, 0, 0);
	return (otherDate<=today);
}

function validateEmail(email) {
    var re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// from http://www.emoticode.net/javascript/validar-nifcifdni.html/
function isValidCif(abc){
	par = 0;
	non = 0;
	letras = "ABCDEFGHKLMNPQS";
	let = abc.charAt(0);
	
	if (abc.length!=9) {
		return false;
	}
	
	if (letras.indexOf(let.toUpperCase())==-1) {
		return false;
	}
	
	var zz;
	for (zz=2;zz<8;zz+=2) {
		par = par+parseInt(abc.charAt(zz));
	}
	
	for (zz=1;zz<9;zz+=2) {
		nn = 2*parseInt(abc.charAt(zz));
		if (nn > 9) nn = 1+(nn-10);
		non = non+nn;
	}
	
	parcial = par + non;
	control = (10 - ( parcial % 10));
	if (control==10) control=0;
	
	if (control!=abc.charAt(8)) {
		return false;
	}
	return true;
}


function isValidNif(abc){
	dni=abc.substring(0,abc.length-1);
	let=abc.charAt(abc.length-1);
	if (!isNaN(let)) {
		//alert('Falta la letra');
		return false;
	}else{
		cadena = "TRWAGMYFPDXBNJZSQVHLCKET";
		posicion = dni % 23;
		letra = cadena.substring(posicion,posicion+1);
		if (letra!=let.toUpperCase()){
			//alert("Nif no vá¡lido");
			return false;
		}
	}
	return true;
}

// used in the form for adding notes to a budget
function addNote() {
	if (document.getElementById("addnotetext").value.length > 0) // also filters special characters
		sendForm('noteslist','AddNote', 'addnoteForm');
	else
		setMessage("Debe escribir una nota en el cuadro de texto antes de añadirla",true);
	document.getElementById("addnotetext").value = "";
}

// used in the form for filtering budgets
function filterBudgets() {
	setMessage("",false);
	if (document.getElementById("from").value != document.getElementById("from").value.replace("-","/").replace(/[^0-9\/]/g,'')) {
		document.getElementById("from").value = document.getElementById("from").value.replace("-","/").replace(/[^0-9\/]/g,'');
		wrongDateMessage();
	}
	
	if (document.getElementById("to").value != document.getElementById("to").value.replace("-","/").replace(/[^0-9\/]/g,'')) {
		document.getElementById("to").value = document.getElementById("to").value.replace("-","/").replace(/[^0-9\/]/g,'');
		wrongDateMessage();
	}
	
	if (isValidDate(document.getElementById("from").value) && isValidDate(document.getElementById("to").value) ) {
		sendForm('budgetlist','budgetlist.jsp', "filterbudget");
	} else {
		wrongDateMessage();
	}
}

// used in the form for filtering clients
function filterClients() {
	sendForm('clientlist','clientlist.jsp', "filterclient");
}

// used in the form for filtering products
function filterProducts() {
	
	setMessage("",false);
	
	if (document.getElementById("minPrice").value != document.getElementById("minPrice").value.replace(",",".").replace(/[^0-9.]/g,'')) {
		document.getElementById("minPrice").value = document.getElementById("minPrice").value.replace(",",".").replace(/[^0-9.]/g,'');
		setMessage("El precio solo puede ser un número positivo, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
	}
	
	if (document.getElementById("maxPrice").value != document.getElementById("maxPrice").value.replace(",",".").replace(/[^0-9.]/g,'')) {
		document.getElementById("maxPrice").value = document.getElementById("maxPrice").value.replace(",",".").replace(/[^0-9.]/g,'');
		setMessage("El precio solo puede ser un número positivo, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
	}
	
	if ( (getNumber(document.getElementById("minPrice").value)!=null) && (getNumber(document.getElementById("maxPrice").value)!=null) ) {
		sendForm('productlist','productlist.jsp', "filterproduct");
	} else {
		setMessage("El precio solo puede ser un número positivo, separando decimales con punto y sin símbolo de moneda, como 1234.56",true);
	}
}

// used in the form for editing salesperson data
function salespersonData() {
	if ( (document.getElementById("name").value.length > 0) && (document.getElementById("email").value.length > 0) ) {
		if ( validateEmail(document.getElementById("email").value ) ) { 
			return true;
		} else {
			setMessage("La dirección de correo electrónico debe ser válida",true);
			return false;
		}
	} else {
		setMessage("No puede dejar el nombre ni la dirección de correo electrónico en blanco",true);
		return false;
	}
}

// used in the form to change the password
function submitPassword() {
	if ( (document.getElementById("oldPassword").value.length > 0) && (document.getElementById("newPassword").value.length > 0) && (document.getElementById("confirmPassword").value.length > 0)) {
		if ( document.getElementById("newPassword").value == document.getElementById("confirmPassword").value ) {
			sendForm("changePasswordDiv", "ChangePassword", "changePassword");
		} else {
			document.getElementById("infoChangePassword").innerHTML = "<div style='color:#C03'><br />La nueva contraseña y su confirmación deben ser iguales.<br /></div>";
		}
	} else {
		document.getElementById("infoChangePassword").innerHTML = "<div style='color:#C03'><br />No puede dejar campos en blanco<br /></div>";
	}
}

// used in the form for setting the mail password
function submitMailPassword() {
	if ( (document.getElementById("password").value.length > 0)) {
		sendForm("mailPasswordDiv", "SetMailPassword", "mailPassword");
	} else {
		document.getElementById("infoMailPassword").innerHTML = "<div style='color:#C03'><br />No puede establecer la contraseña en blanco.<br /></div>";
	}
}

// used in the form for setting policies: An administrator should have all allowed
function checkPoliciesA() {
	
	if (document.getElementById("administrator").checked == true ) {
		document.getElementById("viewOffers").checked = true;
		document.getElementById("createOffers").checked = true;
		document.getElementById("allClients").checked = true;
	}
}
function checkPoliciesB() {
	if ( document.getElementById("viewOffers").checked == false ||
		 document.getElementById("createOffers").checked == false || 
		 document.getElementById("allClients").checked == false ) {
			 
			document.getElementById("administrator").checked = false;

	}
}

// used in the form for editing products
function checkProductForm() {
	setMessage("",false);
			
	if (document.getElementById("productId").value != document.getElementById("productId").value.replace(/[^0-9]/g,'')) {
		document.getElementById("productId").value = document.getElementById("productId").value.replace(/[^0-9]/g,'');
		setMessage("El número de producto solo puede ser un número natural",true);
		return false;
	}
	
	if (document.getElementById("name").value == null || document.getElementById("name").value.length < 1) {
		setMessage("El nombre de producto no puede dejarse vacío",true);
		return false;
	}
	
	if (document.getElementById("costPrice").value != document.getElementById("costPrice").value.replace(",",".").replace(/[^0-9.]/g,'')) {
		document.getElementById("costPrice").value = document.getElementById("costPrice").value.replace(",",".").replace(/[^0-9.]/g,'');
		setMessage("El precio solo puede ser un número positivo, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
		return false;
	}
	
	if ( (getNumber(document.getElementById("costPrice").value)!=null) ) {
		return true;
	} else {
		setMessage("El precio solo puede ser un número positivo, separando decimales con punto y sin símbolo de moneda, como 1234.56",true);
		return false;
	}

}

// used in the form for editing products to add a new price
function checkPriceForm() {
	setMessage("",false);
	
	if (document.getElementById("date").value != document.getElementById("date").value.replace("-","/").replace(/[^0-9\/]/g,'')) {
		document.getElementById("date").value = document.getElementById("date").value.replace("-","/").replace(/[^0-9\/]/g,'');
		wrongDateMessage();
		return false;
	}
	
	if (!isValidDate(document.getElementById("date").value) ) {
		wrongDateMessage();
		return false;
	}
	
	if( isPastDate(document.getElementById("date").value) ) {
		setMessage("La fecha introducida es la actual o ha pasado. Puede haber presupuestos u ofertas ya creados que sean afectados con el cambio de tarifa",true);
	}
	
	if (document.getElementById("newPrice").value != document.getElementById("newPrice").value.replace(",",".").replace(/[^0-9.]/g,'')) {
		document.getElementById("newPrice").value = document.getElementById("newPrice").value.replace(",",".").replace(/[^0-9.]/g,'');
		setMessage("La tarifa solo puede ser un número positivo, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
		return false;
	}
	
	if ( (getNumber(document.getElementById("newPrice").value)==null) ) {
		setMessage("La tarifa solo puede ser un número positivo, separando decimales con punto y sin símbolo de moneda, como 1234.56",true);
		return false;
	}
	
	if ( document.getElementById("discontinued").checked == true && document.getElementById("newPrice").value != "") {
		setMessage("Elija entre añadir una tarifa o descatalogar un producto",true);
		return false;
	}

	if ( document.getElementById("discontinued").checked == false && document.getElementById("newPrice").value == "") {
		setMessage("Debe añadir una tarifa o descatalogar un producto",true);
		return false;
	}
	
	return true;
	
}

function formatPriceFormA() {
	setMessage("",false);
	
	// check price format
	if (document.getElementById("newPrice").value != document.getElementById("newPrice").value.replace(",",".").replace(/[^0-9.]/g,'')) {
		document.getElementById("newPrice").value = document.getElementById("newPrice").value.replace(",",".").replace(/[^0-9.]/g,'');
		setMessage("La tarifa solo puede ser un número positivo, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
	}
	
	if ( (getNumber(document.getElementById("newPrice").value)==null) ) {
		setMessage("La tarifa solo puede ser un número positivo, separando decimales con punto y sin símbolo de moneda, como 1234.56",true);
		return false;
	}
	
	if ( document.getElementById("newPrice").value != "")
		document.getElementById("submitprice").innerHTML = "Añadir&nbsp;tarifa";
		
	// if a price is introduced, discontinued is disabled
	if ( document.getElementById("discontinued").checked == true && document.getElementById("newPrice").value != "") {
		document.getElementById("discontinued").checked = false;
		
		setMessage("Al introducir una tarifa, el producto deja de estar descatalogado",true);
		return false;
	}
	
	return true;
}

function formatPriceFormB() {
	setMessage("",false);
	// set the text according to discontinue or update price
	if ( document.getElementById("discontinued").checked )
		document.getElementById("submitprice").innerHTML = "Descatalogar";
	else
		document.getElementById("submitprice").innerHTML = "Añadir&nbsp;tarifa";
		
	// if disabled is enabled, the price is removed
	if ( document.getElementById("discontinued").checked == true && document.getElementById("newPrice").value != "" ) {
		document.getElementById("newPrice").value = "";
		setMessage("Al descatalogar un producto, no hace falta introducir su nueva tarifa",true);
		return false;
	}
	
	return true;
}

function formatPriceFormC() {
	setMessage("",false);
	
	if (document.getElementById("date").value != document.getElementById("date").value.replace("-","/").replace(/[^0-9\/]/g,'')) {
		document.getElementById("date").value = document.getElementById("date").value.replace("-","/").replace(/[^0-9\/]/g,'');
		wrongDateMessage();
		return false;
	}
	
	if (!isValidDate(document.getElementById("date").value) ) {
		wrongDateMessage();
		return false;
	}
	
	if( isPastDate(document.getElementById("date").value) ) {
		setMessage("La fecha introducida es la actual o ha pasado. Puede haber presupuestos u ofertas ya creados que sean afectados con el cambio de tarifa",true);
	}
}

// used in the form for editing clients
function checkClientForm() {
	setMessage("",false);
	
	if (document.getElementById("number").value != document.getElementById("number").value.replace(/[^0-9A-Za-z]/g,'')) {
		document.getElementById("number").value = document.getElementById("number").value.replace(/[^0-99A-Za-z]/g,'');
		setMessage("El Número de Identificación Fiscal solo puede componerse de números y letras, sin espacios o guiones",true);
		return false;
	}
	
	if ( !isValidCif(document.getElementById("number").value) && !isValidNif(document.getElementById("number").value) ) {
		setMessage("El Número de Identificación Fiscal no es válido",true);
		return false;
	}
	
	if ( document.getElementById("email").value.length > 0 && !validateEmail(document.getElementById("email").value) ) {
		setMessage("La dirección de correo electrónico debe ser válida",true);
		return false;
	}

	if (document.getElementById("number").value.length < 1) {
		setMessage("No se puede dejar el Número de Identificación Fiscal en blanco",true);
		return false;
	}
	
	if (document.getElementById("name").value.length < 1) {
		setMessage("No se puede dejar el nombre del cliente en blanco",true);
		return false;
	}
	
	return true;
}

// used in the form for creating budgets to set the expirity date
function checkExpirationDate() {
	setMessage("",false);
	
	if (document.getElementById("expiration").value != document.getElementById("expiration").value.replace("-","/").replace(/[^0-9\/]/g,'')) {
		document.getElementById("expiration").value = document.getElementById("expiration").value.replace("-","/").replace(/[^0-9\/]/g,'');
		wrongDateMessage();
	}
	
	if (!isValidDate(document.getElementById("expiration").value) ) {
		wrongDateMessage();
		return false;
	}
	
	return true;
}

// used in the form to create budgets for checking if the tax rate is a positive float
function checkTaxRate() {
	document.getElementById("tax").value;
	setMessage("",false);
	var tax = document.getElementById("tax").value;
	
	// check price format
	if (tax != tax.replace(",",".").replace(/[^0-9.]/g,'')) {
		document.getElementById("tax").value = tax.replace(",",".").replace(/[^0-9.]/g,'');
		setMessage("El impuesto solo puede ser un porcentaje positivo menor que 100, separando decimales con punto y sin símbolo de porcentaje, como 1.23",true);
	}
	
	tax = getNumber(tax);
	if ( tax==null || tax>100 || tax<0 ) {
		setMessage("El impuesto solo puede ser un porcentaje positivo menor que 100, separando decimales con punto y sin símbolo de porcentaje, como 1.23",true);
		return false;
	}
	return true;
}

// used in the form to create budgets for setting the message
function setGlobalTotalMessage() {
	if (document.getElementById("globalTotalChk").checked)
		document.getElementById("globalTotalDescription").innerHTML = "Desactívelo&nbsp;para&nbsp;que&nbsp;cada&nbsp;capítulo&nbsp;sea&nbsp;como&nbsp;un&nbsp;presupuesto&nbsp;independiente";
	else
		document.getElementById("globalTotalDescription").innerHTML = "Actívelo&nbsp;para&nbsp;que&nbsp;todos&nbsp;los&nbsp;capítulos&nbsp;se&nbsp;sumen&nbsp;al&nbsp;final";
}

// used in the form to create budgets for checking quantities and prices
function checkPositiveFloat(textboxId) {
	setMessage("",false);
	
	// check price format
	if (document.getElementById(textboxId).value != document.getElementById(textboxId).value.replace(",",".").replace(/[^0-9.]/g,'')) {
		document.getElementById(textboxId).value = document.getElementById(textboxId).value.replace(",",".").replace(/[^0-9.]/g,'');
		setMessage("Solo se permite un número positivo, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
	}
	
	if ( (getNumber(document.getElementById(textboxId).value)==null) ) {
		setMessage("Solo se permite un número positivo, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
		return false;
	}
	
	if ( (getNumber(document.getElementById(textboxId).value) != document.getElementById(textboxId).value) ) {
		setMessage("Solo se permite un número positivo, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
		return false;
	}
	
	return true;
}

// used in the form to create budgets for checking percentajes
function checkFloat(textboxId) {
	setMessage("",false);

	// check format
	if (document.getElementById(textboxId).value != document.getElementById(textboxId).value.replace(",",".").replace(/[^0-9.-]/g,'')) {
		document.getElementById(textboxId).value = document.getElementById(textboxId).value.replace(",",".").replace(/[^0-9.-]/g,'');
		setMessage("Solo se permite un número, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
	}
	
	if ( (getNumber(document.getElementById(textboxId).value) == null) ) {
		setMessage("Solo se permite un número, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
		return false;
	}
	
	if ( (getNumber(document.getElementById(textboxId).value) != document.getElementById(textboxId).value) ) {
		setMessage("Solo se permite un número, separando decimales con punto y sin símbolo de moneda, como 1.23",true);
		return false;
	}
	
	return true;
}

// used in the form to create budgets when the netPrice is introduced to calculate a discount3 that matchs the required netPrice
function setNetPriceDiscounts(section,product) {
	if (!checkPositiveFloat("section"+section+"product"+product+"netPrice") ||
			document.getElementById("section"+section+"product"+product+"netPrice").value.length < 1)
		return false;
		
	var netPrice = parseFloat(document.getElementById("section"+section+"product"+product+"netPrice").value);
	
	// calculate discount3 to match the required netPrice
	var price = parseFloat(document.getElementById("section"+section+"product"+product+"price").innerHTML.replace(".","").replace(",","."));
	// netPrice = price * (1-discount/100)
	if (document.getElementById("section"+section+"product"+product+"discount1") &&
			document.getElementById("section"+section+"product"+product+"discount1").value.length > 0)
		price = price * (1 - parseFloat(document.getElementById("section"+section+"product"+product+"discount1").value) / 100);
	if (document.getElementById("section"+section+"product"+product+"discount2") &&
			document.getElementById("section"+section+"product"+product+"discount2").value.length > 0)
		price = price * (1 - parseFloat(document.getElementById("section"+section+"product"+product+"discount2").value) / 100);
		
	var discount = 100 * (price - netPrice) / price;
	
	if (document.getElementById("section"+section+"product"+product+"discount3"))
		document.getElementById("section"+section+"product"+product+"discount3").value = discount.toFixed(2);
	else
		return false;
	
	return true;
	
}

// used in the form to create budgets when the profitMargin is introduced to calculate the required netPrice and the discount3
function setProfitMarginDiscounts(section,product) {
	if (!checkFloat("section"+section+"product"+product+"profitMargin") ||
			document.getElementById("section"+section+"product"+product+"profitMargin").value.length < 1)
		return false;
	
	// netPrice = costPrice / (1-profitMargin/100)
	var costPrice = parseFloat(document.getElementById("section"+section+"product"+product+"costPrice").innerHTML.replace(".","").replace(",","."));
	var profitMargin = parseFloat(document.getElementById("section"+section+"product"+product+"profitMargin").value);
	var netPrice = (costPrice / ( 1 - profitMargin / 100 ) );
	document.getElementById("section"+section+"product"+product+"netPrice").value = netPrice.toFixed(2);
	
	return setNetPriceDiscounts(section,product);
}

// used in the form to create budgets for calculating the numbers in the form
function calculateBudget() {
	try {

		var tax = parseInt(document.getElementById("tax").value);
		var budgetTotal = 0; // total budget partial sum
		
		// create array of sections
		// from nameSectionXForm using name="name" gives nameSectionX, convert to X
		var nameSections = document.getElementsByName("name");
		var sections = new Array();
		var section;
		var products = new Array();
		for (var i = 0; i < nameSections.length; i++) {
			section = parseInt(nameSections[i].id.replace("nameSection",""));
			if (sections.indexOf(section) == -1) {
				sections.push(section);
				products.push(new Array());
			}
		}
		
		// create array of arrays of products
		// from quantityForms using name="quantity" gives section1product3242quantity
		var nameProducts = document.getElementsByName("quantity");
		var product;
		for (var i = 0; i < nameProducts.length; i++) {
			section = parseInt(nameProducts[i].id.replace("section",""));
			product = parseInt(nameProducts[i].id.replace("section" + section + "product",""));
			if (sections.indexOf(section) == -1)
				throw("Hay una sección desconocida");
			// products[n] are the ones from sections[n]
			for (var n = 0; n < sections.length; n++) {
				if (sections[n] == section) {
					if (products[n].indexOf(product) == -1) {
						products[n].push(product);
					}
				}
			}
		}

		var sectionTotal;
		var price;
		// for each product
		for (var i = 0; i < products.length; i++) {
			// each section
			section = sections[i];
			sectionTotal = 0;
			for (var j = 0; j < products[i].length; j++) {
				product = products[i][j];
				// each product in the section
				price = parseFloat(document.getElementById("section"+section+"product"+product+"price").innerHTML.replace(".","").replace(",","."));
				// price = price * (1-discount/100)
				if (document.getElementById("section"+section+"product"+product+"discount1"))
					price = price * (1 - parseFloat(document.getElementById("section"+section+"product"+product+"discount1").value) / 100);
				if (document.getElementById("section"+section+"product"+product+"discount2"))
					price = price * (1 - parseFloat(document.getElementById("section"+section+"product"+product+"discount2").value) / 100);
				if (document.getElementById("section"+section+"product"+product+"discount3"))
					price = price * (1 - parseFloat(document.getElementById("section"+section+"product"+product+"discount3").value) / 100);
				if (document.getElementById("section"+section+"product"+product+"netPrice"))
					document.getElementById("section"+section+"product"+product+"netPrice").value = price.toFixed(2);
				// total = quantity * price
				document.getElementById("section"+section+"product"+product+"total").innerHTML =
						( (parseFloat(document.getElementById("section"+section+"product"+product+"quantity").value) * price).toFixed(2) + "€" ).replace(".",",");
				// add total to sectionTotal
				sectionTotal += parseFloat(document.getElementById("section"+section+"product"+product+"quantity").value) * price;
				// calculate profitMargin
				if (document.getElementById("section"+section+"product"+product+"profitMargin")) {
					if (price > (parseFloat(document.getElementById("section"+section+"product"+product+"costPrice").innerHTML.replace(".","").replace(",","."))))
						document.getElementById("section"+section+"product"+product+"profitMargin").value =
							( ( ( (price - (parseFloat(document.getElementById("section"+section+"product"+product+"costPrice").innerHTML.replace(".","").replace(",","."))) ) / price ) * 100 ).toFixed(2) );
					else
						document.getElementById("section"+section+"product"+product+"profitMargin").value = 0;
				}
			}
			
			// finalize section	
			document.getElementById("section"+section+"total").innerHTML = (sectionTotal.toFixed(2) + "€").replace(".",",");
			budgetTotal += 	sectionTotal;
			if (document.getElementById("section"+section+"taxes"))
				document.getElementById("section"+section+"taxes").innerHTML = (tax.toFixed(2) + "%").replace(".",",");
				
			if (document.getElementById("section"+section+"totalPlusTaxes"))
				document.getElementById("section"+section+"totalPlusTaxes").innerHTML = ((sectionTotal * (1+tax/100)).toFixed(2) + "€").replace(".",",");

		}
		
		// finalize budget
		if (document.getElementById("budgetTotal"))
			document.getElementById("budgetTotal").innerHTML = (budgetTotal.toFixed(2) + "€").replace(".",",");
		if (document.getElementById("budgetTaxes"))
			document.getElementById("budgetTaxes").innerHTML = (tax.toFixed(2) + "%").replace(".",",");
		if (document.getElementById("budgetTotalPlusTaxes"))
			document.getElementById("budgetTotalPlusTaxes").innerHTML = ((budgetTotal* (1+tax/100)).toFixed(2) + "€").replace(".",",");
		
		return true;
		
	} catch(err) {
		alert("Se ha producido un error calculando el presupuesto: " + err.message);
		return false;
	}
}

// used in the form to create budgets for checking the whole form
function checkBudgetForm() {
	if (!checkExpirationDate())
		setMessage("ATENCIÓN: La fecha no es válida y no se guardará",true);
	if (!checkTaxRate())
		setMessage("ATENCIÓN: El impuesto no es válido y no se guardará",true);
	var items;
	var names = ["quantity","netPrice"];
	for (var n = 0; n < names.length; n++) {
		items = document.getElementsByName(names[n]);
		
		for (var i = 0; i < items.length; i++) {
			if ( !checkPositiveFloat(items[i].id) ) {
				setMessage("ATENCIÓN: El valor " + items[i].value + " no es válido y no se guardará. Introduzca un número válido.",true);
				return false;
			}
			if ( items[i].value.length < 1 ) {
				setMessage("ATENCIÓN: Hay casillas vacías que se tomarán como 0.",true);
				return false;
			}
		}
	}
	
	names = ["discount1","discount2","discount3","profitMargin"];
	for (var n = 0; n < names.length; n++) {
		items = document.getElementsByName(names[n]);
		
		for (var i = 0; i < items.length; i++) {
			if ( !checkFloat(items[i].id) ) {
				setMessage("ATENCIÓN: El valor " + items[i].value + " no es válido y no se guardará. Introduzca un número válido.",true);
				return false;
			}
			if ( items[i].value.length < 1 ) {
				setMessage("ATENCIÓN: Hay casillas vacías que se tomarán como 0.",true);
				return false;
			}
		}
	}
	
	return true;
}
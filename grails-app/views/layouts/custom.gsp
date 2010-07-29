<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<!-- Include default page title -->
	<title><g:layoutTitle default="Your Warehouse App" /></title>
	<%--<link rel="stylesheet" href="http://yui.yahooapis.com/2.7.0/build/reset-fonts-grids/reset-fonts-grids.css" type="text/css"> --%>
	<link rel="stylesheet" href="${createLinkTo(dir:'js/yui/2.7.0/reset-fonts-grids',file:'reset-fonts-grids.css')}" type="text/css">
	
	<!-- Include Favicon -->
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
	
	<!-- Include Main CSS -->
	<!-- TODO Apparently there's a slight distinction between these two ... need to figure out what that distinction is -->
	<%--<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />--%>
	
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'menu.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'form.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footer.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'custom.css')}" type="text/css" media="screen, projection" />
	
	<%--
	<!-- Include Blueprint CSS --> 
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'reset.css')}" type="text/css" media="screen, projection">
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint',file:'typography.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint',file:'grid.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint',file:'forms.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'print.css')}" type="text/css" media="print">
	<!--[if IE]><link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'ie.css')}" type="text/css" media="screen, projection"><![endif]-->
	<!-- TODO Would like to use the bp:blueprintCss <bp:blueprintCss/> -->
	--%>

	
	<!-- Grails Layout : write head element for page-->
	<g:layoutHead />
	
	<!-- Include javascript files -->
	<g:javascript library="application"/>
	<g:javascript library="jquery"/>
	<script type="text/javascript">
		//$.noConflict();
		/*
		$(function () {
			$.notifyBar({
				html: "Thank you, your settings were updated!",
				delay: 100000,
				animationSpeed: "normal",
				close:true
			});  
		});
		*/


		
		// Code that uses other library's $ can follow here.
	</script>

	<!-- Manually include jquery-ui resources -->
	<link href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui-1.8.2.custom.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery-ui-1.8.2.custom.min.js')}" type="text/javascript" ></script>

	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.notifyBar',file:'jquery.notifyBar.css')}" type="text/css" media="screen"  />
	<script type="text/javascript" src="${createLinkTo(dir:'js/jquery.notifyBar',file:'jquery.notifyBar.js')}"></script>
	<script type="text/javascript" src="${createLinkTo(dir:'js/jquery',file:'jquery.ezCookie_0.7.01.js')}"></script>
	
	<!-- Dynamically include jquery-ui resources :  NOT WORKING CORRECTLY -->
	<!-- <jqui:resources components="dialog, datepicker"/> -->	
	<!-- <jqui:resources components="datepicker" mode="normal" theme="cupertino" /> -->

	<!-- Dynamically include Grails UI components -->
	<gui:resources components="richEditor, dialog, tabView, autoComplete"/>

	<!-- Custom styles to be applied to all pages -->
	<style type="text/css" media="screen"></style>
</head>
<body class="yui-skin-sam">

	<div class="notification-container"></div>
	<%-- 
	<g:if test="${flash.message}">	
		<div id="notify-container" style="display: hidden;">
			<div id="notify-message" class="message">${flash.message}</div>	
		</div>
	</g:if>
 	--%>

    <div id="doc3" class="yui-t7">
		<!-- Spinner gets displayed when AJAX is invoked -->
		<div id="spinner" class="spinner" style="display:none;">
		    <img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
		</div>
		<!-- 
			Header "hd" includes includes logo, global navigation 
		------------------------------------------------------------------->
		<div id="hd" role="banner">
		    
		    <!-- Block which includes the logo and login banner -->
		    <div class="yui-b">
				<div class="yui-gf">
				
					<div id="banner">
					    <div id="bannerLeft" class="yui-u first" >
							<div class="logo" >
							    <a class="home" href="${createLink(uri: '/dashboard/index')}" style="text-decoration: none">						    	
						    		<img src="${createLinkTo(dir:'images/icons/',file:'logo.gif')}" alt="Your Boxes. You're Welcome." 
						    			style="vertical-align: absmiddle"/>
						    			<span style="font-size: 2em; vertical-align: top;">openboxes</span>
							    </a>
							</div>
					    </div>
					    <div id="bannerRight" class="yui-u" >
					    	<div id="loggedIn">
								<ul>
								    <g:if test="${session.user}">
										<g:if test="${session?.warehouse}">
											<li>
												<%-- <g:if test="${session?.warehouse?.logoUrl}"><img src="${session?.warehouse?.logoUrl}" width="24" height="24" border="1" style="vertical-align: bottom"></img></g:if>
												&nbsp;--%>
												<span style="font-weight: bold;">
													${session?.warehouse?.name}
												</span>
												<a styte="vertical-align: middle" class="home" href='${createLink(controller: "dashboard", action:"chooseWarehouse")}'>change</a>	
											</li>
										|								
										</g:if>
										<li>
											logged in as <b>${session.user.username}</b>
										</li>
										<!-- 
										| <li><g:link class="list" controller="user" action="preferences"><g:message code="default.preferences.label"  default="Preferences"/></g:link></li>
										 -->
										| 
										<li>
											<g:link class="list" controller="auth" action="logout"><g:message code="default.logout.label"  default="logout"/></g:link>
										</li>
										<!-- 
										| <li><input type="text" value="search" name="q" style="color: #aaa; font-weight: bold;" disabled=disabled /></li>
										 -->
								    </g:if>
								    <g:else test="${!session.user}">
										<li>Not logged in</li>  | <li><g:link class="list" controller="auth" action="login"><g:message code="default.login.label" default="Login"/></g:link></li>
										<!-- 
										 | <li><g:link class="list" controller="user" action="register"><g:message code="default.register.label" default="Register"/></g:link></li>
										 | <li><g:link class="list" controller="user" action="help"><g:message code="default.help.label" default="Help"/></g:link></li>
										 -->
										 
								    </g:else>
								</ul>
							</div>					
					    </div>
					</div>
				</div>
		    </div>
		    
		</div>
    </div>
    
    <div id="doc3" class="yui-t2">	    
		<!-- 
				Body includes the divs for the main body content and left navigation menu 
			----------------------------------------------------------------------------------->
		<!-- YUI "body" block that includes the main content for the page -->
		<div id="bd" role="main">

	    	<!-- YUI main Block including page title and content -->
	      	<div id="yui-main">
		    	<div id="content" class="yui-b">
					<!-- Populated using the 'pageTitle' property defined in the GSP file -->
					<g:if test="${pageProperty(name:'page.pageTitle')}">
					    <div id="pageTitle"><h1><g:pageProperty name="page.pageTitle" /></h1></div>
					</g:if>
					<g:layoutBody />
				</div>
	      	</div>
	      		      	
	      	<!-- YUI nav block that includes the local navigation menu -->
	      	<div id="menu" role="navigation" class="yui-b">
		  		<g:if test="${session?.user}">
					<!-- Navigation Menu -->				
					<g:if test="${session?.warehouse}">
						<g:render template="/common/menu"/>
					</g:if>
					<%-- 			  		
			  		<div id="navMenu" class="homePagePanel">
			      		<div class="panelTop"><!-- used to dislay the bottom border of the navigation menu --></div>
						<div class="panelBody">							
							<h1><g:pageProperty name="page.menuTitle" /> Menu</h1>
							<ul>
								<li><span class="menuButton"><a class="dashboard" href="${createLink(uri: '/home/index')}">Dashboard</a></span></li>
								<g:pageProperty name="page.localLinks" />
							</ul>							
							<br/><!-- this is added in order to get rid of whitespace in menu -->
						</div>
						<div class="panelBtm"><!-- used to dislay the bottom border of the navigation menu --></div>
					</div>
					--%>
				</g:if>
			</div>			 
		</div>

		<!-- 
		<div>
			<button id="common">Default style bar</button>
		    <button id="error">Error style bar</button>
		    <button id="success">Success style bar</button>
		    <button id="custom">Custom styling</button>
		    <button id="close">With close button</button>	
		</div>	
		 -->
		 
		<!-- YUI "footer" block that includes footer information -->
		<div id="ft" role="contentinfo">
			<div id="footer">
				&copy; 2010 <a href="http://www.pih.org">PIH</a>&trade; Warehouse &nbsp;&nbsp; | &nbsp;&nbsp;
				Application Version: <g:meta name="app.version"/>&nbsp;&nbsp; | &nbsp;&nbsp;
				Grails Version: <g:meta name="app.grails.version"></g:meta>
			</div>
		</div>
	</div>
	
	
    <script type="text/javascript">
		$(function() {
	  		/*
			$.notifyBar({ html: "This is 'Notify bar'!" });		  
			$("#callGreen").click(function(){
				$.notifyBar({ html: "Thank you, your settings were updated!", jqObject: $("#greenDiv") });
			});		  
			$("#callError").click(function(){
				$.notifyBar({ jqObject: $("#errorDiv") });
			});
			$("#common").click(function(){
				$.notifyBar({});
			});
			$("#error").click(function(){
				$.notifyBar({ cls: "error", html: "Error occurred!" });
			});
			$("#success").click(function(){
				$.notifyBar({ cls: "success", html: "Your data has been changed!" });
			});
			$("#custom").click(function(){
				$.notifyBar({ cls: "custom", html: "This is a custom styling!" });
			});
			$("#close").click(function(){
				$.notifyBar({ html: "Click 'close' to hide notify bar", close: true, delay: 1000000 });
			});
			*/
		});    
	</script>
	
</body>
</html>

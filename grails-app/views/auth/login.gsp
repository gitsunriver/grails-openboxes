<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title>Login</title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<script src="${createLinkTo(dir:'js/', file:'detect_timezone.js')}" type="text/javascript" ></script>
</head>
<body>
	<style>
		#hd { display: none; }  
	</style>
	
	<script type="text/javascript"> 	
		jQuery(document).ready(function() {
			jQuery("#usernameField").focus(); // Focus on the first text input field in the page
			var timezone = jzTimezoneDetector.determine_timezone().timezone; // Now you have an instance of the TimeZone object.
			jQuery("#browserTimezone").val(timezone.olson_tz); // Set the user timezone offset as a hidden input
		});	
	</script>

	<div class="body">
		<g:form controller="auth" action="handleLogin" method="post">	
		
			<g:hiddenField name="targetUri" value="${params?.targetUri}" />
			<g:hiddenField id="browserTimezone" name="browserTimezone" />
			  
		    <div class="dialog">

				<div id="loginForm">
					<g:hasErrors bean="${userInstance}">
					   <div class="errors">
					       <g:renderErrors bean="${userInstance}" as="list" />
					   </div>
					</g:hasErrors>		
					
					<g:if test="${flash.message}">
					    <div class="message">${flash.message}</div>
					</g:if>				
					
					
					<fieldset> 			
						<legend>							
							<div id="logo">
								<a class="home" href="${createLink(uri: '/dashboard/index')}" style="text-decoration: none">						    	
						    		<img src="${createLinkTo(dir:'images/icons/',file:'logo.gif')}" alt="Your Boxes. You're Welcome." 
						    			style="vertical-align: absmiddle"/>
						    			<span style="font-size: 2em; vertical-align: top;">openboxes</span>
							    </a>					
							</div>	
						</legend>
						<table>
							<tbody>

								<tr class="">
									<td colspan="2">
										
									</td>	
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label for="email" class="loginField"><warehouse:message code="user.usernameOrEmail.label" default="Username" /></label>
									</td>
									<td valign="top" class="name ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
										<g:textField class="loginField" id="usernameField" name="username" value="${userInstance?.username}" size="25" />
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label for="password" class="loginField"><warehouse:message code="user.password.label" default="Password" /></label>
									</td>
									<td valign="top" class="name ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
										<g:passwordField class="loginField" name="password" value="${userInstance?.password}" size="25" />
									</td>
								</tr>
								<tr class="prop">
									<td colspan="2" valign="top" style="text-align: center;">
										<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt=""/> Login</button>												
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="" colspan="2">
										<div style="text-align: left">				
											New user? <g:link class="list" controller="auth" action="signup"><warehouse:message code="default.signup.label" default="Signup"/></g:link>
										</div>
									</td>
								</tr>
							</tbody>
						</table>
					</fieldset>
				</div>
			</div>
		</g:form>
		

	</div>
</body>
</html>

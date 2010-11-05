
<%@ page import="org.pih.warehouse.core.User" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${userInstance}">
				<div class="errors">
					<g:renderErrors bean="${userInstance}" as="list" />
				</div>
            </g:hasErrors>
            
            
            
            <g:form method="post" >
                <g:hiddenField name="id" value="${userInstance?.id}" />
                <g:hiddenField name="version" value="${userInstance?.version}" />
                
                
				<g:render template="summary"/>
				<fieldset>                
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="username"><g:message code="user.username.label" default="Username" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
	                                    <g:textField name="username" value="${userInstance?.username}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="firstName"><g:message code="user.name.label" default="First name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
	                                    <g:textField name="firstName" value="${userInstance?.firstName}" />
	                                </td>
	                            </tr>
	
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="lastName"><g:message code="user.name.label" default="Last Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
	                                    <g:textField name="lastName" value="${userInstance?.lastName}" />
	                                </td>
	                            </tr>
	
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="password"><g:message code="user.password.label" default="Password" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
	                                    <g:passwordField name="password" value="${userInstance?.password}" />
	                                </td>
	                            </tr>
	
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="password"><g:message code="user.passwordConfirm.label" default="Password Confirm" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'passwordConfirm', 'errors')}">
	                                    <g:passwordField name="passwordConfirm" value="" />
	                                </td>
	                            </tr>
	
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="email"><g:message code="user.email.label" default="Email" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
	                                    <g:textField name="email" value="${userInstance?.email}" />
	                                </td>
	                            </tr>
	                            	                            
								<tr class="prop">
									<td valign="top" class="name">
				
									</td>
									<td valign="top">
										<div class="buttons">
										    <g:actionSubmit class="positive" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
										    
										</div>
									</td>
								</tr>
	                        </tbody>
	                    </table>
	                </div>
				</fieldset>

            </g:form>
        </div>
    </body>
</html>

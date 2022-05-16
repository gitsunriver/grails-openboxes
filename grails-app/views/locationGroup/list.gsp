<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="locationGroups.label" /></title>
    </head>
    <body>        
        <div class="body">
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>

            <div class="buttonBar">
                <g:link class="button" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'locationGroup.label').toLowerCase()]"/></g:link>
                <g:isUserAdmin>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'locationGroup.label').toLowerCase()]"/></g:link>
                </g:isUserAdmin>
            </div>


			<div class="box">           	
                <table>
                    <thead>
                        <tr>      
                        	<th></th>                  
                            <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" class="bottom"/>
                            <g:sortableColumn property="locationType" title="${warehouse.message(code: 'location.locationType.label')}" class="bottom"/>
                        </tr>
                    </thead>
                    <tbody>
	                    <g:each in="${locationGroupInstanceList}" status="i" var="locationGroupInstance">
							<tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
								<td>
									<%-- 
									<g:render template="actions" model="[locationInstance:locationInstance]"/>		
									--%>
								</td>
								<td>
									<g:link action="show" id="${locationGroupInstance.id}">${fieldValue(bean: locationGroupInstance, field: "name")}</g:link>
								</td>
								<td>
									<ul>
										<g:each in="${locationGroupInstance.locations}" var="location">
											<li>${location.name }</li>
										</g:each>
									</ul>
								</td>
							</tr>
	                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${locationGroupInstanceTotal}" />
            </div>
        </div>
    </body>
</html>

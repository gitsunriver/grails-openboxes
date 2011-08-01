
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Enter request details</title>
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a href="${createLinkTo(dir:'')}">Home</a>
		</span>
	</div>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${requestInstance}">
			<div class="errors">
				<g:renderErrors bean="${requestInstance}" as="list" />
			</div>
		</g:hasErrors>
		<g:each var="requestItem" in="${requestItems}" status="i">
			<g:hasErrors bean="${requestItem}">
				<div class="errors">
					<g:renderErrors bean="${requestItem}" as="list" />
				</div>
			</g:hasErrors>
		</g:each>
		<g:form action="createRequest" method="post">
			<div class="dialog">
			
				<fieldset>
            		<g:render template="/request/summary" model="[requestInstance:requestInstance]"/>
			
					<table>
						<tbody>
						
							<tr class='prop'>
								<td valign='top' class='name'><label for='description'>Description:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:request,field:'description','errors')}'>
									<input type="text" name='description' value="${requestInstance?.description?.encodeAsHTML()}" size="30"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'>Request from:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:request,field:'origin','errors')}'>
									<g:select name="origin.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value="${requestInstance?.origin?.id}" noSelection="['':'']"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Request for:</label>
								</td>
								<td valign='top' class='value ${hasErrors(bean:request,field:'destination','errors')}'>
									${session.warehouse?.name }
									<g:hiddenField name="destination.id" value="${session.warehouse?.id}"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='requestedBy'>Requested by:</label></td>
								<td valign='top'
									class='value ${hasErrors(bean:request,field:'requestedBy','errors')}'>
									<%-- 
									<g:select class="combobox" name="requestedBy.id" from="${org.pih.warehouse.core.Person.list().sort{it.lastName}}" optionKey="id" value="${request?.requestedBy?.id}" noSelection="['':'']"/>
									--%>
									${requestInstance?.requestedBy?.name}
								</td>
							</tr>
							<%-- 
							<tr class='prop'>
								<td valign='top' class='name'><label for='dateOrdered'>Ordered on:</label></td>
								<td valign='top'
									class='value ${hasErrors(bean:request,field:'dateOrdered','errors')}'>								
									<g:jqueryDatePicker 
										id="dateOrdered" 
										name="dateOrdered" 
										value="${request?.dateOrdered }" 
										format="MM/dd/yyyy"
										size="8"
										showTrigger="false" />								
								</td>
							</tr>							
							--%>
						</tbody>
					</table>
					<div class="buttons" style="border-top: 1px solid lightgrey;">
						<g:submitButton name="next" value="Next"></g:submitButton> 
						<g:link action="createRequest" event="cancel">Cancel</g:link>
					</div>
					
					
				</fieldset>
			</div>
		</g:form>
	</div>
	<g:comboBox />	
</body>
</html>
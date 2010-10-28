  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Enter Traveler Details</title>         
    </head>
    <body>
        <div class="body">
        
        	<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if> 
			
			<g:hasErrors bean="${shipmentInstance}">
				<div class="errors"><g:renderErrors bean="${shipmentInstance}" as="list" /></div>
			</g:hasErrors> 
			
			<g:render template="flowHeader"/>
			
			
			<g:form action="suitcase" method="post">
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>
				<fieldset>
					<legend>Step 2&nbsp;Enter traveler's details</legend>					
					<div class="dialog">
						<table>
		                    <tbody>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message code="shipment.type.label" default="Type" /></label></td>
									<td valign="top"
										class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentType', 'errors')}">
										<g:if test="${shipmentInstance?.shipmentType}">
											<g:hiddenField name="shipmentType.id" value="${shipmentInstance?.shipmentType?.id}" />
											${shipmentInstance?.shipmentType?.name }																	
										</g:if>
										<g:else>
											<g:select
												name="shipmentType.id"
												from="${org.pih.warehouse.shipping.ShipmentType.list()}"
												optionKey="id" optionValue="name" value="${shipmentInstance?.shipmentType?.id}" />								
										</g:else>
									</td>
								</tr>
								<tr class='prop'>
									<td valign='top' class='name'>
										<label for='name'><label><g:message code="shipment.name.label" default="Name" /></label>
									</td>
									<td valign='top' class='value ${hasErrors(bean:shipmentInstance,field:'name','errors')}'>
										${shipmentInstance?.name?.encodeAsHTML()}
									</td>
								</tr>  
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.traveler.label" default="Traveler" /></label></td>
									<td valign="top" style="width: 30%;">
										<g:autoSuggest id="carrier" name="carrier" jsonUrl="/warehouse/json/findPersonByName" 
											width="180" size="30"
											valueId="${shipmentInstance?.carrier?.id}" 
											valueName="${shipmentInstance?.carrier?.name}"/>		
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.totalValue.label" default="Flight number" /></label></td>
									<td valign="top"
										class=" ${hasErrors(bean: shipmentInstance, field: 'flightInformation', 'errors')}"
										nowrap="nowrap">
											<g:textField name="flightInformation" size="10" value="${shipmentInstance?.flightInformation}" /> 
											<span class="fade">(e.g. AA 2292)</span>
									</td>
								</tr>										
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.totalValue.label" default="Total value (USD)" /></label></td>
									<td valign="top"
										class=" ${hasErrors(bean: shipmentInstance, field: 'totalValue', 'errors')}"
										nowrap="nowrap">
											$ <g:textField name="totalValue" value="${formatNumber(format: '##,##0.00', number: shipmentInstance.totalValue)}" size="10"/>
									</td>
								</tr>										
		                    </tbody>
	               		</table>
					</div>
					<div class="buttons">
						<g:submitButton name="back" value="Back"></g:submitButton> 
						<g:submitButton name="submit" value="Next"></g:submitButton> 
					</div>
					
				</fieldset>
			</g:form>
		</div>
	</body>
</html>

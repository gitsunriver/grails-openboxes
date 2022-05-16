  
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Enter Tracking Details</title>         
    </head>
    <body>
        <div class="body">
        
        	<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if> 
			
			<g:hasErrors bean="${shipmentInstance}">
				<div class="errors"><g:renderErrors bean="${shipmentInstance}" as="list" /></div>
			</g:hasErrors> 
			
			<g:render template="flowHeader" model="['currentState':'Traveler']"/>
			
			<g:form action="createShipment" method="post">
				<g:hiddenField name="id" value="${shipmentInstance?.id}"/>
				<fieldset>
					<legend>Step 2&nbsp;Enter tracking details</legend>				
						
					<g:render template="../shipment/summary" />	
					
								
					
					<div class="dialog">
						<table>
		                    <tbody>				  
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
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.freightForwarder.label" default="Freight Forwarder" /></label></td>
									<td valign="top" style="width: 30%;">
										&nbsp;	
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.recipient.label" default="Recipient" /></label></td>
									<td valign="top" style="width: 30%;">
										<g:autoSuggest id="recipient" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
											width="180" size="30"
											valueId="${shipmentInstance?.recipient?.id}" 
											valueName="${shipmentInstance?.recipient?.name}"/>		
									</td>
								</tr>
								
								<!-- list all the reference numbers valid for this workflow -->
								<g:each var="referenceNumberType" in="${shipmentWorkflow?.referenceNumberTypes}">
									<tr class="prop">
										<td valign="top" class="name" style="width: 10%;"><label><g:message
											code="shipment.${referenceNumberType?.name}" default="${referenceNumberType?.name}" /></label></td>
										<td valign="top" style="width: 30%;">
											<g:textField name="referenceNumbersInput.${referenceNumberType?.id}" size="10" value="${shipmentInstance?.referenceNumbers?.find({it.referenceNumberType.id == referenceNumberType.id})?.identifier}" /> 
										</td>
									</tr>
								</g:each>
																	
								<tr class="prop">
									<td valign="top" class="name"><label><g:message
										code="shipment.totalValue.label" default="Total value" /></label></td>
									<td valign="top"
										class=" ${hasErrors(bean: shipmentInstance, field: 'totalValue', 'errors')}"
										nowrap="nowrap">
											<g:textField name="totalValue" value="${formatNumber(format: '##,##0.00', number: shipmentInstance.totalValue)}" size="10"/> 
											<span class="fade">USD</span>
									</td>
								</tr>				
								<tr class="prop">
									<td valign="top" class="name" style="width: 10%;"><label><g:message
										code="shipment.comments.label" default="Comments" /></label></td>
									<td valign="top" style="width: 30%;">
										&nbsp;	
									</td>
								</tr>						
		                    </tbody>
	               		</table>
					</div>
					<div class="buttons">
						<table>
							<tr>
								<td width="45%" style="text-align: right;">
									<g:submitButton name="back" value="Back"></g:submitButton> 
									<g:submitButton name="next" value="Next"></g:submitButton> 
								</td>
								<td width="10%">&nbsp;</td>
								<td width="45%" style="text-align: left;">
									<g:submitButton name="save" value="Save and Exit"></g:submitButton>
									<g:submitButton name="cancel" value="Cancel"></g:submitButton>							
								</td>
							</tr>
						</table>
					</div>
				</fieldset>
			</g:form>
		</div>
	</body>
</html>

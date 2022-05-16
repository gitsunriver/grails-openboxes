                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Add Shipment Items</title>  
    </head>
    <body>
		<div class="body">
			<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if>
			<g:hasErrors bean="${containerInstance}">
				<div class="errors">
					<g:renderErrors bean="${containerInstance}" as="list" />
				</div>				
			</g:hasErrors>          
	
			<g:render template="flowHeader" model="['currentState':'Pack']"/>		
			 		
	
			<fieldset>
				<legend>Step 3&nbsp;Add shipment items</legend>	
					<g:render template="../shipment/summary" />	
			 		
			 		<!-- figure out what dialog box, if any, we need to render -->
			 		<g:if test="${containerToEdit || containerTypeToAdd}">
			 			<g:render template="editContainer" model="['container':containerToEdit, 'containerTypeToAdd':containerTypeToAdd]"/>
			 		</g:if>
			 		<g:if test="${boxToEdit || addBoxToContainerId}">
			 			<g:render template="editBox" model="['box':boxToEdit, 'addBoxToContainerId':addBoxToContainerId]"/>
			 		</g:if>
			 		<g:if test="${itemToEdit || addItemToContainerId}">
			 			<g:render template="editItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
			 		</g:if>
			 		
					<div class="dialog">
					<table style="border: 1px solid #CCC;" border="0">
						<thead>	
							<tr>
								<th>Item</th>
								<th>Qty</th>
								<th>Lot/Serial #</th>
								<th>Recipient</th>
								<th>Actions</th>
							</tr>
						</thead>
						<g:set var="count" value="${0 }"/>	
						<g:each var="containerInstance" in="${shipmentInstance?.containers?.findAll({!it.parentContainer})?.sort()}">
							<tbody>
								<tr class="${count++%2==0?'odd':'even' }">
									<td style="width:30%;">
										<span>
											<img src="${createLinkTo(dir:'images/icons/shipmentType',file:containerInstance.containerType.name.toLowerCase() + '.jpg')}" style="vertical-align: middle"/>
											<b><g:link action="createShipment" event="editContainer" params="[containerToEditId:containerInstance?.id]">
												${containerInstance?.name}
											</g:link></b>
										</span>
									</td>
									<td></td>
									<td></td>
									<td></td>
									<td style="width:25%; text-align: left;">
										<span nowrap>
											<g:link action="createShipment" event="addItemToContainer" params="['container.id':containerInstance.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" alt="Add an item" style="vertical-align: middle"/>
												&nbsp;add item
											</g:link> 													
											&nbsp;
											<g:link action="createShipment" event="addBoxToContainer" params="['container.id':containerInstance.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" alt="Add a box" style="vertical-align: middle"/>
												&nbsp;add box
											</g:link>
										</span>
									</td>
								</tr>
								
								<g:each var="itemInstance" in="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == containerInstance?.id})?.sort()}">		
									<tr class="${count++%2==0?'odd':'even' }">
										<td>
											<span style="padding-left: 32px;">
												<g:link action="createShipment" event="editItem" params="[itemToEditId:itemInstance?.id]">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Item" style="vertical-align: middle"/>
													&nbsp;${itemInstance?.product?.name } 	
												</g:link>
											</span>
										</td>
										<td style="text-align:left;">
											${itemInstance?.quantity}
										</td>
										<td style="text-align:left;">
											${itemInstance?.lotNumber}
										</td>
										<td style="text-align:left;">
											${itemInstance?.recipient?.name}
										</td>
										<td style="text-align: left;">		
											<g:link action="createShipment" event="deleteItem" params="['item.id':itemInstance?.id]">	<img src="${createLinkTo(dir:'images/icons/silk',file:'page_delete.png')}" alt="remove item" style="vertical-align: middle"/>
												&nbsp;remove item
											</g:link>	
										</td>
									</tr>
								</g:each>
	
								<g:each  var="boxInstance" in="${containerInstance?.containers?.sort()}">
									<tr class="${count++%2==0?'odd':'even' }">
										<td>
											<span style="padding-left: 32px;">
											<g:link action="createShipment" event="editBox" params="[boxToEditId:boxInstance?.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="Package" style="vertical-align: middle"/>
												&nbsp;${boxInstance?.name}
											</g:link>
											</span>
										</td>
										<td></td>
										<td></td>
										<td></td>
										<td style="text-align: left;">
											<g:link action="createShipment" event="addItemToContainer" params="['container.id':boxInstance.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" alt="Add an item" style="vertical-align: middle"/>
												&nbsp;add item
											</g:link> 		
											&nbsp;
											
											<g:link action="createShipment" event="deleteBox" params="['box.id':boxInstance?.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'package_delete.png')}" alt="Add an item" style="vertical-align: middle"/>
												&nbsp;remove box
											</g:link>														
										</td>
									</tr>
									
									<g:each var="itemInstance" in="${shipmentInstance?.shipmentItems?.findAll({it.container?.id == boxInstance?.id})?.sort()}">	
										<tr class="${count++%2==0?'odd':'even' }">
											<td>
												<span style="padding-left: 64px;">
													<g:link action="createShipment" event="editItem" params="[itemToEditId:itemInstance?.id]">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Item" style="vertical-align: middle"/>
														&nbsp;${itemInstance?.product?.name }
													</g:link>																	
												</span>
											</td>
											<td style="text-align:left;">
												${itemInstance?.quantity}
											</td>
											<td style="text-align:left;">
												${itemInstance?.lotNumber}
											</td>
											<td style="text-align:left;">
												${itemInstance?.recipient?.name}
											</td>
											<td style="text-align: left;">		
												<g:link action="createShipment" event="deleteItem" params="['item.id':itemInstance?.id]">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'page_delete.png')}" alt="remove item" style="vertical-align: middle"/>
													&nbsp;remove item
												</g:link>	
											</td>
										</tr>
									</g:each>												
								</g:each>
							</tbody>							
						</g:each>
						<tfoot>
							<tr>
								<td rowspan="4">
								<nobr>
									<g:each var="containerType" in="${shipmentWorkflow.containerTypes}">
										<g:link action="createShipment" event="addContainer" params="[containerTypeToAddName:containerType.name]">
											<img src="${createLinkTo(dir:'images/icons/shipmentType',file:containerType.name.toLowerCase() + '.jpg')}" style="vertical-align: middle"/>
											&nbsp;Add a ${containerType.name}
										</g:link>
									</g:each>
								</nobr>
								</td>
							</tr>
						</tfoot>
					</table>
				</div>		
				<div class="buttons">
					<g:form action="createShipment" method="post" >
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
		            </g:form>
				</div>
			</fieldset>
        </div>
    </body>
</html>

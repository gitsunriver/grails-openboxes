
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Enter shipment details</title>
<style>
	
</style>
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
		<g:hasErrors bean="${orderCommand}">
			<div class="errors">
				<g:renderErrors bean="${orderCommand}" as="list" />
			</div>
		</g:hasErrors>
		<g:form action="receiveOrder" method="post">
			<div class="dialog">
				
				<g:render template="progressBarAlt" model="['state':'enterShipmentDetails']"/>	
				<fieldset>
					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='id'>Order number</label>
								</td>
								<td valign='top'class='value'>
									${orderCommand.order.orderNumber }
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='id'>Status</label>
								</td>
								<td valign='top'class='value'>
									<g:hiddenField name="order.id" value="${orderCommand?.order?.id }"/>
									${ (orderCommand?.order?.isComplete()) ? "Complete" : "Pending" }
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='orderedBy'>Shipment type</label>
								</td>
								<td valign='top'class='value'>
									<g:select class="combobox updateable" name="shipmentType.id" from="${org.pih.warehouse.shipping.ShipmentType.list()}" 
										optionKey="id" optionValue="name" value="${orderCommand?.shipmentType?.id }" noSelection="['':'']" />
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='orderedBy'>Receipient</label>
								</td>
								<td valign='top'class='value'>
									<div class="ui-widget">
										<g:select class="combobox updateable" name="recipient.id" from="${org.pih.warehouse.core.Person.list()}" 
											optionKey="id" optionValue="name" value="${orderCommand?.recipient?.id }" noSelection="['':'']" />
									</div>									
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='shippedOn'>Shipped on</label>
								</td>
								<td valign='top'class='value'>									
									<g:jqueryDatePicker 
										id="shippedOn" 
										name="shippedOn" 
										class="updateable"
										value="${orderCommand?.shippedOn }" 
										format="MM/dd/yyyy"
										showTrigger="false" />
								</td>
							</tr>								
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='deliveredOn'>Delivered on</label>
								</td>
								<td valign='top'class='value'>
									<g:jqueryDatePicker 
										id="deliveredOn" 
										name="deliveredOn" 
										class="updateable"
										value="${orderCommand?.deliveredOn }" 
										format="MM/dd/yyyy"
										showTrigger="false" />
								</td>
							</tr>									
							<tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for='orderItems'><g:message code="order.items.label" default="Items" /></label></td>
	                            <td valign="top" class="value">

									<g:if test="${orderItems }">
										<table id="orderItemsTable">
											<thead>
												<tr class="even">
													<th class="center" colspan="4">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cart.png')}" alt="ordered" style="vertical-align: middle"/>
														Items Ordered
													</th>
													<th class="center" colspan="4" style="border-left: 1px solid lightgrey;">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}" alt="received" style="vertical-align: middle"/>
														Items Received
													</th>
												</tr>
												<tr class="even">
													<td></td>
													<td>Type</td>
													<td>Description</td>
													<td class="center">Ordered</td>										
													<%-- <td class="center">Remaining</td>--%>	
													<td style="border-left: 1px solid lightgrey;">Received</td>										
													<td>Product</td>										
													<td>Lot Number</td>		
													<%-- 								
													<td>Actions</td>										
													--%>
												</tr>
											</thead>									
											<tbody>
												<g:each var="orderItem" in="${orderItems }" status="i">
													<g:if test="${orderItem?.quantityReceived > 0}">
														<tr class="">
															<td>
																<a name="orderItems${i }"></a>
																${i }
																<g:hiddenField class="orderItemId" name="orderItems[${i }].orderItem.id" value="${orderItem?.orderItem?.id }"/>
																<g:hiddenField name="orderItems[${i }].primary" value="${orderItem?.primary }"/>
																<g:hiddenField name="orderItems[${i }].type" value="${orderItem?.type }"/>
																<g:hiddenField name="orderItems[${i }].description" value="${orderItem?.description }"/>
																<g:hiddenField name="orderItems[${i }].quantityOrdered" value="${orderItem?.quantityOrdered }"/>
															</td>
															<td>
																${orderItem?.type }
															</td>
															<td>
																${orderItem?.description }
															</td>
															<td class="center">
																${orderItem?.quantityOrdered}
															</td>
															<%--
															<td class="center">
																 ${orderItem?.quantityOrdered - orderItem?.orderItem?.quantityFulfilled()}
															</td>
															--%>
															<td class="center" style="border-left: 1px solid lightgrey;">															
																${orderItem?.quantityReceived }
															</td>
															<td>
																${orderItem?.productReceived?.name }
															</td>
															<td>
																${orderItem?.lotNumber }
															</td>
														</tr>
													</g:if>
												</g:each>
											</tbody>
										</table>
									</g:if>
									<g:else>
										<span class="fade">No items</span>
									</g:else>	
	                            </td>
	                        </tr>	
						</tbody>
					</table>
					<div class="buttons">
						<g:submitButton name="back" value="Back"></g:submitButton>
						<g:submitButton name="next" value="Next"></g:submitButton> 
					</div>
					
					
				</fieldset>
			</div>
		</g:form>
	</div>
	<g:comboBox />	
</body>
</html>
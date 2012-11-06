<script type="text/javascript">
	$(document).ready(function(){			
		$("#pick-item-dialog").dialog({ autoOpen: true, modal: true, width: 800, height: 500 });				
	});
</script>	   
<div id="pick-item-dialog" title="Pick item" style="padding: 10px; display: none;" >

	<a href="top"></a>
	
	<g:if test="${message}">
		<div class="message">
			${message}
		</div>
	</g:if>	
	<g:form name="fulfillItem" action="fulfillRequest">
		<g:hiddenField name="fulfillment.request.id" value="${requestItem?.request?.id }"/>
		<div class="dialog">
			<div class="" style="background-color: #fff; padding: 10px">
				<table>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="requisitionItem.requested.label"/></label>
						</td>
						<td class="value">			
							<format:metadata obj="${requestItem.displayName()}"/>
							(${requestItem?.quantity} <warehouse:message code="default.units.label"/>)
						</td>
					</tr>					
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="fulfillmentItem.picked.label"/></label>
						</td>
						<td class="value">							
							<format:metadata obj="${requestItem.displayName()}"/>
							<span class="quantity">(${command?.quantityFulfilledByRequestItem(requestItem) } <warehouse:message code="default.units.label"/>)</span>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="fulfillmentItem.available.label"/></label>
						</td>
						<td class="value">			
							<div style="height: 200px; overflow: auto" id="fulfillProduct">
							
								<g:if test="${product }">
									<format:product product="${product}"/>
									<span class="fade"><format:category category="${product?.category}"/></span>						
								</g:if>
								<g:else>
									<format:product product="${requestItem?.product}"/>
									<span class="fade"><format:category category="${requestItem?.product?.category}"/></span>
								</g:else>
								
								<a href="javascript:void(0);" id="changeProductBtn"><warehouse:message code="fulfillRequestWorkflow.changeProduct.label"/></a>
							
								<table border="0">
									<tr class="odd">
										<td><warehouse:message code="inventoryItem.lotNumber.label"/></td>
										<td><warehouse:message code="inventoryItem.expires.label"/></td>
										<td><warehouse:message code="inventoryItem.onHandQuantity.label"/></td>
										<td><warehouse:message code="inventoryItem.fulfillQuantity.label"/></td>
									</tr>
									<g:if test="${inventoryItems }">
										<g:each var="entry" in="${inventoryItems}" status="i">
											<g:set var="quantity" value="${entry.value }"/>
											<g:set var="inventoryItem" value="${entry.key }"/>
											
											<tr class="${i%2?'odd':'even' }">	
												<td>
													<g:hiddenField name="fulfillmentItems[${i }].requestItem.id" value="${requestItem?.id }"/>
													<g:hiddenField name="fulfillmentItems[${i }].inventoryItem.id" value="${inventoryItem?.id }"/>
													${inventoryItem?.lotNumber }
												</td>
												<td>
													${inventoryItem?.expirationDate }
												</td>
												<td>
													${quantity }
												</td>
												<td>
													<g:textField name="fulfillmentItems[${i }].quantity" size="5"/>
												</td>
											</tr>
										</g:each>
									</g:if>
									<g:else>
										<tr class="even">
											<td colspan="4" class="center">
												<warehouse:message code="inventoryItem.notAvailable.message" args="[product?.name, session?.warehouse?.name]"/>
											</td>
										</tr>
									</g:else>
								</table>
							</div>						
							<div class="buttons left" id="changeProduct" style="display: none;">
								<g:autoSuggest id="fulfillProduct" name="fulfillProduct" jsonUrl="${request.contextPath }/json/findProductByName" width="200" />
								<g:submitButton name="changeProduct" value="${warehouse.message(code:'default.button.search.label')}"></g:submitButton>					
							</div>
							
						</td>
					</tr>
				</table>
			</div>	
			<div class="buttons">
				<g:submitButton name="saveAndContinue" value="${warehouse.message(code:'default.saveAndContinue.label')}"></g:submitButton>
				<g:submitButton name="saveAndClose" value="${warehouse.message(code:'default.close.label')}"></g:submitButton>
			</div>
		</div>
	</g:form>
</div>


<script>
$(document).ready(function() {
	$("#changeProductBtn").click(function(){ 
		$("#changeProduct").toggle();
		$("#fulfillProduct").toggle();
	});
});

</script>

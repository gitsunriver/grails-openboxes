<script type="text/javascript">
	$(document).ready(function(){					
		$("#dlgMoveItem").dialog({ autoOpen: true, modal: true, width: '800px'});	
	});			
</script>

<div id="dlgMoveItem" title="Move Item" style="padding: 10px; display: none;" >
	<g:if test="${itemToMove}">
		<g:form name="moveItem" action="createShipment">
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="shipmentItem.item.label" default="Item" /></label></td>                            
						<td valign="top" class="value">
							<g:hiddenField name="item.id" value="${itemToMove.id }"/>
							<b>${itemToMove?.quantity }</b> x ${itemToMove?.product?.name }
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="shipmentItem.container.label" default="From" /></label></td>                            
						<td valign="top" class="value">
							<div>
								<g:set var="count" value="${1 }"/>
								<table>
									<tr class="${count++ % 2 ? 'odd':'even' }">
										<th>Container</th>
										<th>Quantity</th>
									</tr>
									<tr>
										<td>
											<g:if test="${!itemToMove.container}">
												<g:message code="shipmentItem.unpackedItems" default="Unpacked Items" />
											</g:if>
											<g:if test="${itemToMove?.container?.parentContainer }">
												${itemToMove?.container?.parentContainer?.name } &rsaquo;
											</g:if> 
											${itemToMove?.container?.name }
										</td>
										<td>
											${itemToMove?.quantity}
										</td>									
									</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="shipmentItem.container.label" default="To" /></label></td>                            
						<td valign="top" class="value">
							<div style="height: 350px; overflow: auto;">
								<g:set var="count" value="${1 }"/>
								<table>
									<tr class="${count++ % 2 ? 'odd':'even' }">
										<th>Container</th>
										<th>Quantity</th>
									</tr>
								
									
									<tr class="${count++ % 2 ? 'odd':'even' }">
										<td>
											<g:message code="shipmentItem.unpackedItems" default="Unpacked Items" />
										</td>
										<td>
											<g:if test="${itemToMove.container}">
												<g:textField id="newQuantity-0" class="updateQuantity" name="quantity-0" size="3" value="0"></g:textField>
											</g:if>
											<g:else>
												<g:textField id="currentQuantity" class="currentQuantity" name="quantity-0" size="3" readonly="readonly" value="${itemToMove?.quantity}"></g:textField>
											</g:else>
										</td>
									</tr>
									
									
									<g:each var="containerTo" in="${shipmentInstance?.containers.sort{it.sortOrder}}">
										<tr class="${count++ % 2 ? 'odd':'even' }">
											<td>
												<g:set var="selected" test="${itemToMove?.container?.id == containerTo?.id}"/>
												<g:if test="${containerTo?.parentContainer }">${containerTo?.parentContainer?.name } &rsaquo;</g:if> ${containerTo?.name }
											</td>
											<td>
												<g:if test="${containerTo != itemToMove.container}">
													<g:textField id="newQuantity-${containerTo?.id}" class="updateQuantity" name="quantity-${containerTo?.id}" size="3" value="0"></g:textField>
												</g:if>
												<g:else>
													<g:textField id="currentQuantity" class="currentQuantity" name="quantity-${containerTo?.id}" size="3" readonly="readonly" value="${itemToMove?.quantity}"></g:textField>
												</g:else>
											</td>
										</tr>									
									</g:each>
								</table>
								
							</div>
							<g:hiddenField id="totalQuantity" class="totalQuantity" name="totalQuantity" size="3" disabled="true" value="${itemToMove?.quantity}"/>
						</td>
					</tr>
					<tr>
						<td></td>
						<td style="text-align: left;">
							<div class="buttons">
								<g:submitButton name="moveItemToContainer" value="Move"></g:submitButton>
								<button name="cancelDialog" type="reset" onclick="$('#dlgMoveItem').dialog('close');">Cancel</button>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</g:form>														
	</g:if>
</div>		
		

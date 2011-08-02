
<script type="text/javascript">
	$(document).ready(function(){
		$("#btnAdjustStock-${itemInstance?.id}").click(function() { $("#dlgAdjustStock-${itemInstance?.id}").dialog('open'); });									
		$("#dlgAdjustStock-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '500px' });				
	});
</script>	
<div class="action-menu-item">
	<a id="btnAdjustStock-${itemInstance?.id}">
		<img src="${resource(dir: 'images/icons/silk', file: 'book_open.png')}"/>&nbsp;Adjust Stock</a>
</div>
<g:link controller="inventoryItem" action="recordInventory" params="['product.id':commandInstance?.productInstance?.id,'inventory.id':commandInstance?.inventoryInstance?.id, 'inventoryItem.id':itemInstance?.id]">
</g:link>

<div id="dlgAdjustStock-${itemInstance?.id}" title="Adjust Stock" style="padding: 10px; display: none;" >	
	<table>
		<tr>
			<td>
				<g:form controller="inventoryItem" action="adjustStock">
					<g:hiddenField name="id" value="${itemInstance?.id}"/>
					<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${commandInstance?.inventoryInstance?.id}"/>
					
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="inventoryItem.product.label" default="Product" /></label></td>                            
								<td valign="top" class="value">
									${commandInstance?.productInstance } 
									<g:if test="${itemInstance?.lotNumber }">&rsaquo; ${itemInstance?.lotNumber }</g:if>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="inventoryItem.lotNumber.label" default="Serial/Lot Number" /></label></td>                            
								<td valign="top" class="value">
									<g:if test="${itemInstance?.lotNumber }">${itemInstance?.lotNumber }</g:if>
									<g:else><span class="fade">None</span></g:else>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="inventoryItem.expirationDate.label" default="Expires" /></label></td>                            
								<td valign="top" class="value">
									<g:if test="${itemInstance?.expirationDate }">
										<format:expirationDate obj="${itemInstance?.expirationDate}"/>
									</g:if>
									<g:else>
										<span class="fade">Never</span>
									</g:else>										
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="inventoryItem.quantity.label" default="Previous Quantity" /></label></td>                            
								<td valign="top" class="value">
									<g:hiddenField id="oldQuantity" name="oldQuantity" value="${itemQuantity }"/>
									${itemQuantity }
								</td>
							</tr>  	        
							
							
							<tr class="prop">
								<td valign="top" class="name"><label><warehouse:message code="inventoryItem.quantity.label" default="New Quantity" /></label></td>                            
								<td valign="top" class="value">
									<g:textField id="newQuantity" name="newQuantity" size="3" value="${itemQuantity }" />
								</td>
							</tr>  	        
							<tr>
								<td></td>
								<td style="text-align: left;">
									<button>
										<img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}"/> Adjust stock
									</button>
								</td>
							</tr>
						</tbody>
					</table>
				</g:form>				
			</td>
			
		</tr>
	</table>													
</div>		
		     


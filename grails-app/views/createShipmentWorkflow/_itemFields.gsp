
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<format:product product="${item?.product}"/>
						<g:hiddenField id="productId" name="product.id" value="${item?.product?.id }"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="inventoryItem.lotNumber.label" /></label></td>                            
					<td valign="top" class="value">
						${item?.lotNumber }
						<g:hiddenField id="lotNumber" name="lotNumber" value="${item?.lotNumber }"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name">
						<label><warehouse:message code="inventoryItem.expirationDate.label" /></label>
					</td>                            
					<td valign="top" class="value">
						${item?.expirationDate?:warehouse.message(code:'default.never.label')}
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="inventory.onHandQuantity.label" /></label></td>                            
					<td valign="top" class="value">
						<span id="quantity-on-hand"></span>
						<img src="${resource(dir: 'images/icons/silk', file: 'reload.png')}" style="vertical-align: middle;" class="refresh"/>
					</td>
				</tr>  	        

				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="default.quantity.label" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="quantity" name="quantity" value="${item?.quantity}" size="5" class="text" /> 
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message code="shipping.recipient.label"/></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest id="recipient" name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName" 
							width="200" valueId="${item?.recipient?.id}" valueName="${item?.recipient?.name}" styleClass="text"/>							
					</td>
				</tr>
				<tr>
					<td class="name"></td>
					<td>
						<div class="left">
							<g:if test="${itemToEdit}">
								<g:submitButton name="updateItem" value="${warehouse.message(code:'shipping.saveItem.label')}"></g:submitButton>
							</g:if>
							<g:else>
								<g:submitButton name="saveItem" value="${warehouse.message(code:'shipping.saveItem.label')}"></g:submitButton>
							</g:else>
							<button name="cancelDialog" type="reset" onclick="$('#dlgEditItem').dialog('close');"><warehouse:message code="default.button.cancel.label"/></button>
						</div>
						<g:if test="${addItemToContainerId}">
							<div class="left">
								<g:submitButton name="addAnotherItem" value="${warehouse.message(code:'shipping.saveItemAndAddAnother.label')}"></g:submitButton>
							</div>
						</g:if>
					</td>
				</tr>
				
				<script>
					$(document).ready(function() {
						updateQuantityOnHand();
						$(".refresh").click(function() { 
							$("#quantity-on-hand").html("refreshing ...");
							updateQuantityOnHand();
						});

						$("#quantity").livequery(function(){
							$(this).blur(function(event) {					
								if (!validateQuantity()) { 
									alert("Please enter a valid quantity");
									$(this).val('');
									$(this).focus();
								}
							});
					    });

						$("#lotNumber-suggest").livequery(function(){
							$(this).bind( "autocompletechange", function(event, ui) {
								updateQuantityOnHand();
								$("#quantity").focus();
							});
						});
					});


					function validateQuantity() { 
						updateQuantityOnHand();
						var quantityEntered = $("#quantity").val()
						var quantityOnHand = $("#quantity-on-hand").html();
						if (quantityEntered > parseInt(quantityOnHand)) {
							return false;
						}
						return true;
					}
					
					function updateQuantityOnHand() { 
						var productId = $("#productId").val();
						var lotNumber = $("#lotNumber").val();				
						$("#quantity-on-hand").load("${request.contextPath }/json/getQuantity?productId=" + productId + "&lotNumber=" + lotNumber);
					}
				</script>

			
			
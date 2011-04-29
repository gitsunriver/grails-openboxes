
 <style>
 .actions li { padding: 10px; } 
 </style>


<div> 	
	<fieldset >
		<legend><span class="fade">Current Stock</span></legend>
		<div id="inventoryView" style="text-align: left;" class="list">	
			<br/>									
			<table border="0" style="border:1px solid #f5f5f5;">
				<thead>
					<tr class="odd">
						<th class="left" style="">Actions</th>												
						<th class="center middle" >Unit</th>
						<th>Serial/Lot Number</th>
						<th>Expires</th>
						<th class="center middle" >Qty</th>
						<g:hasErrors bean="${flash.itemInstance}">													
							<th></th>
						</g:hasErrors>
					</tr>											
				</thead>
				<tbody>
					<g:if test="${!commandInstance?.inventoryItemList}">
						<tr class="even" style="min-height: 100px;">
							<td colspan="5" style="text-align: center; vertical-align: middle">
								There are no <b>${commandInstance?.productInstance?.name }</b> items currently in stock.
							</td>
						</tr>
					</g:if>
					<g:set var="count" value="${0 }"/>
					<g:each var="itemInstance" in="${commandInstance?.inventoryItemList }" status="status">	
						<g:if test="${commandInstance.quantityByInventoryItemMap.get(itemInstance)}">		
							<!-- only show items with quantities -->	
							<g:set var="itemQuantity" value="${commandInstance.quantityByInventoryItemMap.get(itemInstance) }"/>
							<g:set var="selected" value="${params?.inventoryItem?.id && (itemInstance.id == Integer.valueOf(params?.inventoryItem?.id)) }"/>
							<g:set var="styleClass" value="${(count++%2==0)?'even':'odd' }"/>
							<g:if test="${selected }">
								<g:set var="styleClass" value="selected-row"/>
							</g:if>
							
							<style>
								.selected-row { background-color: lightyellow; } 
							</style>			
							<tr class="${styleClass} prop">
								<td class="top" style="text-align: left; width: 10%" nowrap="nowrap">
									<div class="action-menu">
										<button class="action-btn">
											<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
											&nbsp;<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
										</button>
										<div class="actions">
											<g:render template="editItemDialog" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]"/>
											<g:render template="adjustStock" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
											<g:render template="addToShipment" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
										</div>
									</div>
								</td>															
								<td class="top center">
									${commandInstance?.productInstance?.unitOfMeasure }
								</td>
								<td class="top">
									${itemInstance?.lotNumber?:'<span class="fade">None</span>' }
									<g:link action="show" controller="inventoryItem" id="${itemInstance?.id }">
									</g:link>
								</td>														
								<td class="top">
									<g:if test="${itemInstance?.expirationDate}">
										<g:formatDate date="${itemInstance?.expirationDate }" format="${org.pih.warehouse.core.Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}" />
									</g:if>
									<g:else>
										<span class="fade">Never</span>
									</g:else>
								</td>
								<td class="top center">
									<g:set var="styleClass" value=""/>
									<g:if test="${itemQuantity<0}">
										<g:set var="styleClass" value="color: red;"/>																	
									</g:if>
									<span style="${styleClass}">${itemQuantity }</span> 
								</td>	
								<g:hasErrors bean="${flash.itemInstance}">
									<td>
										<g:if test="${selected }">
											<div class="errors dialog">
												<g:eachError bean="${flash.itemInstance}">
													<g:message error="${it}"/>
												</g:eachError>																	
											</div>
										</g:if>																									
									</td>
								</g:hasErrors>	
							</tr>
						</g:if>
					</g:each>
				</tbody>
				<g:if test="${commandInstance?.inventoryItemList}">
					<tfoot>
						<tr class="prop" style="height: border: 0px;">
							<td colspan="2" style="text-align: left; border: 0px;">
							</td>
							<td style="border: 0px;">
							</td>
							<td style="border: 0px;"></td>
							<td style="text-align: center; vertical-align: middle; border: 0px;">
								<span style="font-size: 1em;"> 
									<g:set var="styleClass" value="color: black;"/>																	
									<g:if test="${commandInstance.totalQuantity < 0}">
										<g:set var="styleClass" value="color: red;"/>																	
									</g:if>														
									<span style="${styleClass }">${commandInstance.totalQuantity }</span> 
								</span>
							</td>
							<g:hasErrors bean="${flash.itemInstance}">
								<td style="border: 0px;">
								
								</td>
							</g:hasErrors>
						</tr>
					</tfoot>
				</g:if>
			</table>										
		</div>	
	</fieldset>	
</div>

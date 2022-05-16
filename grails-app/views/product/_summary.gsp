<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div id="product-summary" productid="${productInstance?.id}" class="summary">
	<table id="product-summary-table" border="0">
		<tbody>
			<tr>						
				<td class="middle" style="width: 1%;">
                    <g:if test="${productInstance?.images }">
                        <div class="nailthumb-product">
                            <g:set var="image" value="${productInstance?.images?.sort()?.first()}"/>
                            <img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" style="display:none" />
                        </div>
                    </g:if>
                    <g:else>
                    <div class="nailthumb-product">
                        <img src="${resource(dir: 'images', file: 'default-product.png')}" />
                    </div>
                    </g:else>
                </td>


				<g:if test="${productInstance?.coldChain }">
					<td style="width: 1%;" class="top">				
						<img src="${resource(dir: 'images/icons', file: 'coldchain.gif')}" 
							alt="" title="${warehouse.message(code:'product.coldChain.message') }" class="middle"/>
					</td>
				</g:if>									
				<td class="top">
								
					<div id="product-header" style="float: left;">
			            <div id="product-title" class="title">
			            	<small>${productInstance?.productCode }</small>
			            	<g:link controller="inventoryItem" action="showStockCard" params="['product.id': productInstance?.id]">
			                	${productInstance?.name }
			                </g:link>				
			            </div>
                        <div id="product-catalogs">
                            <g:each var="productCatalog" in="${productInstance?.productCatalogs }">
                                <span class="tag tag-info" title="${g.message(code: 'productCatalog.label')}">${productCatalog.name }</span>
                            </g:each>
                        </div>
                        <div id="product-tags">
                            <g:each var="tag" in="${productInstance?.tags }">
                                <g:link controller="inventory" action="browse" params="['tag':tag.tag,'max':params.max]">
                                    <span class="tag tag-success" title="${g.message(code: 'tag.label', default: 'Tag')}">${tag.tag }</span>
                                </g:link>
                            </g:each>
                        </div>

        			</div>
        		</td>
				<td class="right" width="1%">
        			<div id="product-status" class="title">
						<g:productStatus product="${productInstance.id}"/>
        			</div>
			
				</td>
			</tr>
		</tbody>
	</table>
</div>
<div class="summary-actions">
    <table>
        <tr>
            <td width="1%">
                <g:render template="../product/actions" model="[productInstance:productInstance]" />
            </td>
            <td>
                <div class="button-container">
					<div class="button-group">
						<g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'clipboard.png')}" />&nbsp;
							${warehouse.message(code: 'inventory.showStockCard.label', default: 'Show stock')}
						</g:link>
						<g:link controller='inventoryItem' action='showRecordInventory' params="['product.id':productInstance?.id]" class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'calculator.png')}" />&nbsp;
							${warehouse.message(code: 'product.button.record.label', default: 'Record stock')}
						</g:link>
						<g:link controller='product' action='edit' id='${productInstance?.id }' class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />&nbsp;
							${warehouse.message(code: 'product.button.edit.label', default: 'Edit product', args:['product'])}
						</g:link>
						<g:link controller="inventoryItem" action="showLotNumbers" params="['product.id': productInstance?.id]" class="button">
							<img src="${resource(dir: 'images/icons', file: 'barcode.png')}"/>&nbsp;
							<warehouse:message code="inventory.showLotNumbers.label"/>
						</g:link>
					</div>
                </div>
            </td>
        </tr>

    </table>
</div>
<script>
    $(function() {
        $('.nailthumb-product img').hide();
        $('.nailthumb-product img').nailthumb({width : 40, height : 40});
    });
</script>

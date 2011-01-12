
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>    
        
        <style>
        	.tabSelected { 
        		text-align: center;
	        	background-color: white; 
	        	border: 1px solid black;
	        	border-bottom: hidden; 
        	}
        	.tabDefault { 
        		text-align: center;
        		background-color: #f0f0f0; 
        		border: 1px solid black; 
        		border-bottom: 1px solid black; 
        		border-top: 1px solid black; 
        		border-right: 1px solid black; 
        		border-left: 1px solid black;
        	}
        	.tabSpacer { 
        		width: 3px;
        		border-top: 0px;
        		border-bottom: 1px solid black;
        	}
        	
        </style>
    </head>    

    <body>
        <div class="body">
        
        
			<div class="nav">
				<g:render template="nav"/>
			</div>
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${inventoryInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${inventoryInstance}" as="list" />
	            </div>
            </g:hasErrors>    
            
            <%--
            <table style="margin-top: 10px;">
	            <thead>
    				<tr>				
						<th class="tabSpacer"></th>
						<g:each var="productType" in="${productTypes}" status="i">
							<th class="${((String.valueOf(productType.id)==params?.productType?.id) || (!params?.productType && i == 0))?'tabSelected':'tabDefault'}">								
								<a href="${createLink(action:'browse',params:["productType.id":productType.id])}">${productType.name}</a>
							</th>
							<th class="tabSpacer"></th>
						</g:each>    				
    				</tr>        
            	</thead>
            </table>
             --%>
             
						<g:render template="/common/searchCriteriaHorizontal"/>					
						<br/>
						
						Showing ${productList?.totalCount } products
						<%--
						<!-- Initial implementation of the product category --> 
         				<h2>Browse by Category</h2>
           				<div>      
           					<ul>
								<g:if test="${categoryInstance.parentCategory}">
	           						<li>
	           							<g:if test="${categoryInstance?.parentCategory?.name != 'ROOT' }">
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
											<g:link controller="inventory" action="browse" params="[categoryId: categoryInstance.parentCategory?.id ]">
												${categoryInstance.parentCategory.name }
											</g:link>
										</g:if>
										<li>
											<g:render template="dynamicMenuTree" model="[category:categoryInstance?.parentCategory, level: 0, selectedCategory: categoryInstance]"/>
										</li>
									</li>
								</g:if>
								<g:else>
									<li>
										<g:render template="dynamicMenuTree" model="[category:categoryInstance, level: 0, selectedCategory: categoryInstance]"/>
									</li>
								</g:else>
							</ul>
						</div>
						--%>
						<%-- 						
         				<h2>Browse by Site</h2>
           				<div>      
							<ul>
								<g:each var="warehouse" in="${Warehouse.list() }">
									<li>
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
										<g:link controller="inventory" action="browse" params="[categoryId: categoryInstance?.id, warehouseId: warehouse.id ]">
											${warehouse?.name }
										</g:link>		
									</li>
											
								</g:each>
							</ul>
						</div>
						--%>
			            <div> 
		            		
		            		<script>
								$(function() {
									$("#dialogButton").click(function () { 
										$("#dialog").dialog({ "modal": "true", "width": 600});
									});
								});
							</script>
							
		            		<g:set var="varStatus" value="${0 }"/>
							<table border="1" style="border: 1px solid #ccc" class="dataTable">
								<thead>
									<tr class="even">
										<th>ID</th>
										<th>Code</th>
										<th>Description</th>
										<th>Quantity</th>
										<th>Alerts</th>
									</tr>
								</thead>
								
			            		<g:if test="${productList }">         		
									<tbody>
										<g:each var="productInstance" in="${productList }" status="i">
										 	<g:set var="itemInstanceList" value="${inventoryMap.get(productInstance)}"/>	
										 	<g:set var="inventoryLevel" value="${inventoryLevelMap?.get(productInstance)?.get(0)}"/>
										 	<g:set var="quantity" value="${(itemInstanceList)?itemInstanceList*.quantity.sum():0 }"/>
											<tr class="${varStatus++%2==0?'odd':'even' }">
												<%-- 
												<td style="width: 5%; text-align: center;">
													<g:if test="${itemInstanceList }">
														<g:if test="${quantity < 0}">
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'exclamation.png') }" alt="Out of Stock"/>
														</g:if>
														<g:elseif test="${inventoryLevel?.minQuantity && quantity <= inventoryLevel?.minQuantity}">
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'exclamation.png') }" alt="Low Stock"/>
														</g:elseif>
														<g:elseif test="${inventoryLevel?.reorderQuantity && quantity <= inventoryLevel?.reorderQuantity}">
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'error.png') }" alt="Reorder"/>
														</g:elseif>										
														<g:elseif test="${inventoryLevel?.maxQuantity && quantity >= inventoryLevel?.maxQuantity}" >
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'error.png') }" alt="Overstock"/>
														</g:elseif>	
														<g:else>
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png') }" alt="Ok"/>
														</g:else>
													</g:if>								
												</td>
												--%>		
												<td>${productInstance?.id }</td>						
												<td>${productInstance?.productCode }</td>						
												<td style="">
													<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">
														${productInstance?.name }
													</g:link> &nbsp; <span class="fade">${productInstance?.category?.name }</span>
														
												</td>
												<td style="width: 5%; text-align: center;">
													<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">${(itemInstanceList)?itemInstanceList*.quantity.sum():'<span class="fade">N/A</span>' }</g:link>
												</td>
												<td>
													<!-- no alerts yet -->
												</td>
											</tr>
										</g:each>
									</tbody>
								</g:if>
								<g:else>
									<tbody>
										<tr class="odd">
											<td colspan="5" style="padding: 10px; text-align: center;">
												There are no products matching the selected criteria.
												<%-- 
												<g:render template="../category/breadcrumb" model="[categoryInstance:categoryInstance]"/>
												--%>
											</td>
										</tr>
									</tbody>
								</g:else>
							</table>
						</div>
						<%-- 
       				    <div style="text-align: left; padding: 10px;">
       				    	<span class="menuButton">
	        				    <g:link class="list" controller="inventory" action="browse">Show all products</g:link>
	        				</span>
       				    </div>
       				    --%>		
		</div>
    </body>
</html>

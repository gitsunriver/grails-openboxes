		
<fieldset>
	<legend>Results</legend>
	<table>
		<tr>
			<td colspan="2">		
				<div>
			        <g:set var="productMap" value="${commandInstance?.productList.groupBy {it.category} }"/>
					<span class="fade">
						Showing ${commandInstance?.productList?.size() } product(s) in ${productMap?.keySet()?.size()} categories
							
							
							<%-- 
							<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" /></span>
							<div class="actions" style="max-height: 300px; overflow: auto;">
								<div style="padding: 10px;">
									<b>Choose a category to narrow products displayed</b>
								</div>
								<g:each var="category" in="${productMap?.keySet().sort() }">
									<g:if test="${category?.id }">
										<div class="action-menu-item">
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'folder.png' )}"/>&nbsp;
											<g:link action="narrowCategoryFilter" params="[categoryId:category?.id]">
												${category?.name }
											</g:link>
											&nbsp;
											<span class="fade">
												<g:render template="../category/breadcrumb" model="[categoryInstance:category]"/>	
											</span>
										</div>
									</g:if>
								</g:each>
							</div>
							--%>
						</span>			
					</span>	
			        <g:if test="${commandInstance?.categoryFilters || commandInstance?.searchTermFilters}">	
						&nbsp;<g:link action="clearAllFilters">Reset</g:link>							
					</g:if>
				</div>
			</td>
		</tr>				
	</table>
</fieldset>				
					
<g:if test="${commandInstance?.categoryFilters || commandInstance?.searchTermFilters}">			
	<fieldset>
		
		<div style="border: 0px dashed lightgrey; padding: 0px; margin: 0px;">
			<table>
				<g:set var="status" value="${1 }"/>
			<%-- 
			<tr class="${status++%2?'even':'odd' }" style="border-bottom: 1px dashed lightgrey;">
				<td>	
					<span class="fade">${commandInstance?.productList?.size() } search results for:</span>			
				</td>
				<td class="right">
					<g:link action="clearAllFilters">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bin.png' )}" style="vertical-align:middle"/>
					</g:link>							
				</td>				
			</tr>
			--%>
				<g:each var="filter" in="${commandInstance?.categoryFilters }">
					<tr class="${status++%2?'odd':'even' }" style="border: 0px dashed lightgrey;">
						<td>									
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'folder.png' )}"/>&nbsp;
							${filter?.name }
						</td>
						<td style="text-align: right">
							<g:link action="removeCategoryFilter" params="[categoryId:filter.id]">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png' )}" style="vertical-align:middle"/>
							</g:link>
						</td>
					</tr>
				</g:each>
				<g:each var="filter" in="${commandInstance?.searchTermFilters }">
					<tr class="${status++%2?'odd':'even' }" style="border: 0px dashed lightgrey;">
						<td>
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>&nbsp;					
							${filter }
						</td>
						<td style="text-align: right">
							<g:link action="removeSearchTerm" params="[searchTerm:filter]">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png' )}" style="vertical-align:middle"/>
							</g:link>
						</td>
					</tr>
				</g:each>
			</table>
		</div>						
	</fieldset>
</g:if>
		
			
<fieldset>
	<legend class="fade">Search</legend>

	<div id="searchCriteria" class="even" style="border: 0px solid lightgrey; margin-top: 0px;">
		<table>
			<tr >
				<td colspan="2" style="text-align: left;">
					<g:form method="GET" controller="inventory" action="browse" style="display: inline;">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>&nbsp;Search by name, lot/serial number, etc<br/>				
						<g:textField name="searchTerms" value="" size="20"/>
						<button type="submit" class="" name="submitSearch">
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png' )}" class="middle"/>
							&nbsp;Find&nbsp;</button>
					</g:form>	
				</td>
			</tr>
			<tr class="prop">
				<td colspan="2">
					<g:form action="addCategoryFilter">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'folder.png' )}" class="middle"/>&nbsp;or choose a category
						<g:link class="view" controller="category" action="tree"><warehouse:message code="default.edit.label" args="['']"/></g:link>
						<select id="categoryFilter" name="categoryId" >
							<option value="">Filter by category</option>
							<g:render template="../category/selectOptions" model="[category:commandInstance?.rootCategory, selected:null, level: 0]"/>								
						</select>
																
					</g:form>
				</td>
			
			</tr>
			<tr class="prop">
				<td colspan="2" style="text-align: left;">
					<%-- <img src="${createLinkTo(dir: 'images/icons/silk', file: 'eye.png' )}" class="middle"/>&nbsp;--%>				
					<g:if test="${session?.showHiddenProducts }">
						<g:link action="showHiddenProducts">Hide hidden products</g:link>					
					</g:if> 
					<g:else>
						<g:link action="showHiddenProducts">Show hidden products</g:link>					
					</g:else>
				</td>
			</tr>

		</table>
	</div>
</fieldset>				
				






<script>
	// 
	$(function() {
		$("#categoryFilter").change(function () { 
			$(this).closest("form").submit();
		});
	});
</script>

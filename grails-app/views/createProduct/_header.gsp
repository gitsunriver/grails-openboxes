<%-- 
<div class="box">
	<h2>${product }</h2>
	<ul>
		<li>Category = ${product?.category }</li>
		<li>Title = ${product?.title }</li>
		<li>Description = ${product?.description }</li>
		<li>GTIN = ${product?.gtin }</li>
		<li>Errors? = ${product?.hasErrors() }</li>
	</ul>
</div>
--%>
<g:if test="${message}">
	<div class="message">${message}</div>
</g:if>
<g:hasErrors bean="${product}">
	<div class="errors"><g:renderErrors bean="${product}" as="list" /></div>
</g:hasErrors>

<div class="box" style="height: 25px; background-color: white;">
	<div class="wizard-steps"> 
		<div class="${currentState.equals("search")?'active-step':''}">
			<g:link action="create" event="search">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'magnifier.png' )}" class="middle"/>&nbsp;
				<warehouse:message code="product.search.label" default="Search for a product"/>
			</g:link>
		</div>
		<div class="${currentState.equals("results")?'active-step':''}">
			<g:link action="create" event="results">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'application_view_list.png' )}" class="middle"/>&nbsp;
				<warehouse:message code="product.results.label" default="View results"/>
			</g:link>				
		</div>
		<div class="${currentState.equals("verify")?'active-step':''}">
			<%--<g:link action="create" event="verify"><warehouse:message code="product.choose.label" default="Verify product"/></g:link> --%>
			<a href="#">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png' )}" class="middle" />&nbsp;
				<warehouse:message code="product.choose.label" default="Verify product"/></a>
			
		</div>
		<div class="${currentState.equals("confirm")?'active-step':''}">
			<%--<g:link action="create" event="confirm"><warehouse:message code="product.create.label" default="Confirmation"/></g:link> --%>
			<a href="#">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png' )}" class="middle" />&nbsp;
				<warehouse:message code="product.create.label" default="Confirmation"/></a>
		</div>
	</div>
</div>


<%@ page import="org.pih.warehouse.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1>
		${fieldValue(bean: warehouseInstance, field: "country")} &gt;
		${fieldValue(bean: warehouseInstance, field: "name")} &gt;
		View Current Inventory
	    </h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">

	      <h2>Current Inventory</h2>
                <table>
		  <thead>
		    <tr>
		      <th>Product</th>
		      <th>Quantity</th>
		      <th>Reorder Level</th>
		    </tr>
		  </thead>
                  <tbody>

		    <g:each var="inventoryLineItem" in="${inventory}" status="i">
		      <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			  <td>${inventoryLineItem.key}</td>
			  <td>${inventoryLineItem.value}</td>
		      </tr>
		    </g:each>


		   <%--

			<td>${fieldValue(bean: inventoryLineItem, field: "product.name")}</td>
			<td>${fieldValue(bean: inventoryLineItem, field: "quantity")}</td>


		      <g:each in="${warehouseInstance.inventory.inventoryLineItems}" var="inventoryLineItem" status="i">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			  <td>${fieldValue(bean: inventoryLineItem, field: "id")}</td>
                          <td>${fieldValue(bean: inventoryLineItem, field: "product.name")}</td>
                          <td>${fieldValue(bean: inventoryLineItem, field: "quantity")}</td>
                          <td>${fieldValue(bean: inventoryLineItem, field: "reorderQuantity")}</td>
                        </tr>
		      </g:each>

			--%>
		  </tbody>
		</table>

	      <h2>Show all Transactions</h2>
                <table>
		  <thead>
		    <tr>
		      <th>Product</th>
		      <th>Quantity Change</th>
		    </tr>
		  </thead>
                  <tbody>


		      <g:each in="${warehouseInstance.transactions}" var="transaction" status="i">

			<tr>
			  <td colspan="2" align="right" valign="middle">
			    <g:link controller="transactionEntry" action="create" id="${transaction.id}">Add Entry on ${fieldValue(bean: transaction, field: "transactionDate")}</g:link>
			  </td>
			</tr>

			<g:each in="${transaction.transactionEntries}" var="transactionEntry" status="j">
			  <tr class="${(j % 2) == 0 ? 'odd' : 'even'}">			    
			    <td>${fieldValue(bean: transactionEntry, field: "product.name")}</td>
			    <td>${fieldValue(bean: transactionEntry, field: "quantityChange")}</td>
			  </tr>
			</g:each>
		      </g:each>


		</tbody>
		</table>


            </div>


	  <!--

	  Should allow user to reorder

            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${warehouseInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
	  -->
        </div>
    </body>
</html>

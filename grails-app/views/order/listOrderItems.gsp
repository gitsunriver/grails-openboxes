
<%@ page import="org.pih.warehouse.order.Order" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'order.label', default: 'Order')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">

					
				<g:if test="${orderItems }">
					<table>
						<thead>
							<tr>
								<th> </th>
								<g:sortableColumn property="order"
									title="${message(code: 'orderItem.order.label', default: 'Order')}" />
					
								<g:sortableColumn property="description"
									title="${message(code: 'orderItem.description.label', default: 'Description')}" />
									
								<g:sortableColumn property="status"
									title="${message(code: 'orderItem.status.label', default: 'Status')}" />
									
							</tr>
						</thead>
						<tbody>
							<g:set var="i" value="${0 }"/> 
							<g:each in="${orderItems.groupBy { it.order } }" var="entrymap">
								<g:each in="${entrymap.value }" var="orderItem" >
									<tr class="${(i++ % 2) == 0 ? 'odd' : 'even'}">
										<td>
											
										</td>
										<td>		
											<g:link controller="order" action="show" id="${orderItem?.order?.id}">	
												${fieldValue(bean: orderItem, field: "order.description")}
											</g:link>
										</td>
										<td>
											${fieldValue(bean: orderItem, field: "description")}
										</td>
										<td>
											${fieldValue(bean: orderItem, field: "quantity")}
										</td>
										<td>
											${(orderItem?.isCompletelyFulfilled())?"Complete":"Pending" }
										</td>
						
									</tr>
								</g:each>
							</g:each>
						</tbody>
					</table>
				</g:if>
				<g:else>
					There are no pending order items.
				</g:else>

			</div>
        </div>
    </body>
</html>

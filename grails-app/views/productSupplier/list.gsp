
<%@ page import="org.pih.warehouse.product.ProductSupplier" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list dialog">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['productSupplier']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['productSupplier']"/></g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                            
                                <g:sortableColumn property="id" title="${warehouse.message(code: 'productSupplier.id.label', default: 'Id')}" />
                            
                                <th><g:message code="productSupplier.product.label" default="Product" /></th>
                            
                                <g:sortableColumn property="code" title="${warehouse.message(code: 'productSupplier.code.label', default: 'Code')}" />

                                <th><g:message code="productSupplier.supplier.label" default="Supplier" /></th>

                                <th><g:message code="productSupplier.manufacturer.label" default="Manufacturer" /></th>

                                <th><g:message code="productSupplier.ratingTypeCode.label" default="Rating Type" /></th>

                                <th><g:message code="productSupplier.preferenceTypeCode.label" default="Preference Type" /></th>

                                <th><g:message code="productSupplier.unitOfMeasure.label" default="Unit of Measure" /></th>

                                <th><g:message code="productSupplier.unitPrice.label" default="Unit Price" /></th>

                                <th><g:message code="default.actions.label" default="Actions" /></th>

                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${productSupplierInstanceList}" status="i" var="productSupplierInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            
                                <td>
                                    <g:link action="edit" id="${productSupplierInstance.id}">
                                        ${fieldValue(bean: productSupplierInstance, field: "id")}</g:link>
                                </td>
                            
                                <td>
                                    <g:link action="edit" id="${productSupplierInstance.id}">
                                        ${fieldValue(bean: productSupplierInstance, field: "product")}
                                    </g:link>
                                </td>
                            
                                <td>${fieldValue(bean: productSupplierInstance, field: "code")?:g.message(code:'default.none.label')}</td>
                            
                                <td>${fieldValue(bean: productSupplierInstance, field: "supplier")}</td>

                                <td>${fieldValue(bean: productSupplierInstance, field: "manufacturer")}</td>

                                <td>${fieldValue(bean: productSupplierInstance, field: "ratingTypeCode")}</td>

                                <td>${fieldValue(bean: productSupplierInstance, field: "preferenceTypeCode")}</td>

                                <td>${fieldValue(bean: productSupplierInstance, field: "unitOfMeasure")}</td>

                                <td>${fieldValue(bean: productSupplierInstance, field: "unitPrice")}</td>

                                <td>
                                    <g:link action="edit" controller="product" id="${productSupplierInstance?.id}">
                                        <img src="${createLinkTo(dir:'images/icons/silk', file:'pencil.png')}" />
                                    </g:link>

                                    <g:link action="edit" controller="product" id="${productSupplierInstance?.product?.id}">
                                        <img src="${createLinkTo(dir:'images/icons/silk', file:'application_go.png')}"/>
                                    </g:link>

                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                    <g:unless test="${productSupplierInstanceList}">
                        <div class="center empty fade">
                            <g:message code="default.none.label"/>
                        </div>
                    </g:unless>
                </div>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${productSupplierInstanceTotal}" />
            </div>
        </div>
    </body>
</html>

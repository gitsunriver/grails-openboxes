<%@ page import="org.pih.warehouse.requisition.Requisition" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="requisition.check.label" /></title>
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>


	<g:render template="summary" model="[requisition:requisition]"/>

	<div class="yui-gf">
		<div class="yui-u first">
            <g:render template="header" model="[requisition:requisition]"/>
        </div>
        <div class="yui-u">
            <div class="box dialog">
                <h2>
                    <warehouse:message code="requisition.check.label"/>
                </h2>


                <g:form controller="requisition" action="saveDetails">
                    <g:hiddenField name="redirectAction" value="confirm"/>
                    <g:hiddenField name="id" value="${requisition?.id}"/>
                    <table style="width:auto;">
                        <tr>
                            <td class="left middle">
                                <label>
                                    ${warehouse.message(code:'requisition.checkedBy.label', default: 'Checked by')}
                                </label>
                            </td>
                            <td class="middle">
                                <g:if test="${params.edit}">
                                    <g:selectPerson id="checkedBy" name="checkedBy.id" value="${requisition?.checkedBy}"
                                                    noSelection="['null':'']" size="40"/>
                                </g:if>
                                <g:else>
                                    ${requisition?.checkedBy?.name}
                                </g:else>
                            </td>
                            <td class="left middle">
                                <label>
                                    ${warehouse.message(code:'requisition.dateChecked.label', default: 'Date checked')}
                                </label>
                            </td>
                            <td class="middle">
                                <g:if test="${params.edit}">
                                    <g:datePicker name="dateChecked" value="${requisition?.dateChecked}"/>
                                </g:if>
                                <g:else>
                                    <g:if test="${requisition.dateChecked}">
                                        <g:formatDate date="${requisition?.dateChecked}"/>
                                    </g:if>
                                    <g:else>
                                        ${warehouse.message(code:'default.none.label')}
                                    </g:else>
                                </g:else>
                            </td>                                        <td>
                            <g:if test="${params.edit}">
                                <button class="button icon approve">
                                    ${warehouse.message(code:'default.button.save.label')}
                                </button>
                                &nbsp;
                                <g:link controller="requisition" action="confirm" id="${requisition?.id}">
                                    ${warehouse.message(code:'default.button.cancel.label')}
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="requisition" action="confirm" id="${requisition?.id}"
                                        params="[edit:'on']" class="button icon edit">
                                    ${warehouse.message(code:'default.button.edit.label')}
                                </g:link>
                            </g:else>
                        </td>
                        </tr>

                    </table>
                </g:form>

                <hr/>


                <table border="0">
                    <thead>
                        <tr>
                            <th></th>
                            <th>Status</th>
                            <th>Product</th>
                            <th>Lot number</th>
                            <th class="right">Requested</th>
                            <th class="right">Picked</th>
                            <th class="right">Canceled</th>
                            <th class="right">Remaining</th>
                            <th>UOM</th>
                            <th>Reason code</th>
                        </tr>
                    </thead>
                    <tbody>

                        <g:set var="status" value="${1}"/>
                        <g:each var="requisitionItem" in="${requisition.requisitionItems }">
                            <g:set var="quantityPicked" value="${requisitionItem?.calculateQuantityPicked() }"/>
                            <g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItems() }"/>
                            <g:if test="${picklistItems}">
                                <g:each var="picklistItem" in="${picklistItems }">
                                    <g:set var="isSubstitution" value="${picklistItem?.inventoryItem?.product!=picklistItem?.requisitionItem?.product }"/>
                                    <tr class="${(isSubstitution)?'notice':'success' }">
                                        <td>
                                            ${status++ }
                                        </td>
                                        <td>
                                            ${(isSubstitution)?'Substitution':'Fulfilled' }
                                        </td>
                                        <td>
                                            <g:if test="${isSubstitution }">
                                                <span class="canceled">
                                                    ${picklistItem?.requisitionItem?.product?.productCode}
                                                    ${picklistItem?.requisitionItem?.product?.name}
                                                </span>
                                            </g:if>
                                            <p>
                                                <g:if test="${isSubstitution}">
                                                    <img src="${createLinkTo(dir:'images/icons',file:'indent.gif')}" />
                                                </g:if>
                                                ${picklistItem?.inventoryItem?.product?.productCode}
                                                ${picklistItem?.inventoryItem?.product?.name }
                                            </p>
                                        </td>
                                        <td>
                                            <span class="lotNumber">
                                                ${picklistItem.inventoryItem.lotNumber }
                                            </span>
                                        </td>
                                        <td class="right" width="1%">
                                            ${requisitionItem.quantity?:0 }

                                        </td>
                                        <td class="right" width="1%">
                                            ${picklistItem.quantity?:0 }

                                        </td>
                                        <td class="right" width="1%">
                                            ${requisitionItem.quantityCanceled?:0 }

                                        </td>
                                        <td class="right" width="1%">
                                            ${requisitionItem.calculateQuantityRemaining()?:0 }
                                        </td>
                                        <td>
                                            ${picklistItem.inventoryItem.product.unitOfMeasure?:"EA" }
                                        </td>
                                        <td>
                                            ${isSubstitution?'Substitution':''}
                                            ${requisitionItem.cancelReasonCode}
                                            <p>${requisitionItem?.cancelComments }</p>
                                            <%--
                                            <g:if test="${requisitionItem.quantity > quantityPicked}">
                                                <g:select name="reasonCode" from="['Stockout','Damaged','Expired','Reserved']"
                                                    id="reasonCode" name='reasonCode' value=""
                                                    noSelection="${['null':'Select One...']}"/>
                                            </g:if>
                                            --%>
                                        </td>
                                    </tr>
                                </g:each>
                            </g:if>
                            <g:else>
                                <tr class="error">
                                    <td>${status++ }</td>
                                    <td>
                                        Canceled
                                    </td>
                                    <td>
                                        ${requisitionItem.product?.productCode }
                                        ${requisitionItem.product?.name }
                                    </td>
                                    <td>

                                    </td>
                                    <td class="right">
                                        ${requisitionItem?.quantity?:0 }
                                    </td>
                                    <td class="right">
                                        0
                                    </td>
                                    <td class="right">
                                        ${requisitionItem.quantityCanceled?:0 }
                                    </td>
                                    <td class="right">
                                        ${requisitionItem.calculateQuantityRemaining()?:0 }
                                    </td>
                                    <td>
                                        ${requisitionItem.product.unitOfMeasure?:"EA" }
                                    </td>
                                    <td>
                                        <span title="${requisitionItem?.cancelComments }">${requisitionItem?.cancelReasonCode?:"N/A" }</span>

                                    </td>
                                </tr>
                            </g:else>
                        </g:each>
                    </tbody>
                </table>
            </div>
			<div class="clear"></div>	
			
			<div class="buttons center">
                <g:link controller="requisition" action="pick" id="${requisition.id }" class="button icon arrowleft">
                    <warehouse:message code="default.button.back.label"/>
                </g:link>
                <g:link controller="requisition" action="transfer" id="${requisition.id }" class="button icon arrowright">
                    <warehouse:message code="default.button.next.label"/>
                </g:link>
			</div>
					
		</div>
    </div>
	
	
	
</div>
</body>
</html>

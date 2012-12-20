<div id="inventoryForm">
    <g:form action="saveRecordInventory" autocomplete="off">

        <g:hiddenField name="productInstance.id" value="${commandInstance.productInstance?.id}"/>
        <g:hiddenField name="inventoryInstance.id" value="${commandInstance?.inventoryInstance?.id}"/>


        <div class="form-content box">

            <div class="middle left" style="padding: 10px 0 10px 0;">
                <label><warehouse:message code="inventory.inventoryDate.label"/></label>
                <g:jqueryDatePicker
                        id="transactionDate"
                        name="transactionDate"
                        value="${commandInstance?.transactionDate}"
                        format="MM/dd/yyyy"
                        showTrigger="false" />


                <button class="addAnother" >
                    <img src="${createLinkTo(dir:'images/icons/silk', file:'add.png') }"/>
                    <warehouse:message code="inventory.addInventoryItem.label"/>
                </button>
            </div>
            <table id="inventoryItemsTable">
                <thead>
                <tr>
                    <th><warehouse:message code="default.lotSerialNo.label"/></th>
                    <th><warehouse:message code="default.expires.label"/></th>
                    <th class="center"><warehouse:message code="inventory.oldQty.label"/></th>
                    <th class="center"><warehouse:message code="inventory.newQty.label"/></th>
                    <th class="center"><warehouse:message code="default.actions.label"/></th>
                </tr>
                </thead>
                <tbody>
                %{--<g:set var="inventoryItems" value="${commandInstance?.recordInventoryRows.findAll{it.oldQuantity != 0 || it.newQuantity != 0}}"/>	--}%
                %{--<g:if test="${inventoryItems }">											--}%
                %{--<g:each var="recordInventoryRow" in="${inventoryItems?.sort { it.expirationDate }?.sort { it.lotNumber } }" status="status">--}%
                %{--<g:set var="styleClass" value="${params?.inventoryItem?.id && recordInventoryRow?.id == params?.inventoryItem?.id ? 'selected-row' : ''}"/>--}%
                %{--<tr class="${styleClass} ${status%2==0?'odd':'even'}">--}%
                %{--<td>--}%
                %{--<g:hiddenField name="recordInventoryRows[${status}].id" value="${recordInventoryRow?.id }"/>--}%
                %{--<g:hiddenField name="recordInventoryRows[${status}].lotNumber" value="${recordInventoryRow?.lotNumber }"/>--}%
                %{--${recordInventoryRow?.lotNumber?:'<span class="fade"><warehouse:message code="default.none.label"/></span>' }--}%
                %{--</td>--}%
                %{--<td>--}%
                %{--<g:hiddenField name="recordInventoryRows[${status}].expirationDate" value="${formatDate(date: recordInventoryRow?.expirationDate, format: 'MM/dd/yyyy') }"/>--}%
                %{--<g:if test="${recordInventoryRow?.expirationDate}">--}%
                %{--<format:expirationDate obj="${recordInventoryRow?.expirationDate}"/>--}%
                %{--</g:if>--}%
                %{--<g:else>--}%
                %{--<span class="fade">${warehouse.message(code: 'default.never.label')}</span>--}%
                %{--</g:else>--}%
                %{--</td>--}%
                %{--<td class="middle center">--}%
                %{--${recordInventoryRow?.oldQuantity }	--}%
                %{--<g:hiddenField name="recordInventoryRows[${status}].oldQuantity" value="${recordInventoryRow?.oldQuantity }"/>--}%
                %{--</td>	--}%
                %{--<td class="middle center">--}%
                %{--<g:textField id="newQuantity-${status }" class="newQuantity text center" name="recordInventoryRows[${status }].newQuantity" size="8" value="${recordInventoryRow?.newQuantity }" onFocus="this.select();" onClick="this.select();"/>--}%
                %{--</td>--}%
                %{--<td class="middle left">--}%
                %{--</td>--}%
                %{--</tr>--}%
                %{--</g:each>--}%
                %{--</g:if>--}%
                %{--<g:else>--}%
                %{--<tr id="emptyRow" class="odd">--}%
                %{--<td colspan="5" style="text-align: center;">--}%
                %{--<div class="fade">--}%
                %{--<warehouse:message code="inventory.addNewInventoryItem.message"/>--}%
                %{----}%
                %{--</div>--}%
                %{--</td>--}%
                %{--</tr>--}%
                %{----}%
                %{--</g:else>--}%







                </tbody>
            </table>


        </div>
        <div class="center buttons">
            <button name="save" type="submit" class="positive" id="saveInventoryItem">
                <img src="${createLinkTo(dir:'images/icons/silk', file:'accept.png') }"/>&nbsp;<warehouse:message code="default.button.save.label"/>&nbsp;
            </button>
            &nbsp;
            <g:link controller="inventoryItem" action="showStockCard"
                    params="['product.id':commandInstance.productInstance?.id]" class="negative"><warehouse:message code="default.button.cancel.label"/></g:link>

        </div>

    </g:form>
</div>

<script>
    $(function(){
        var data = ${product};
        var viewModel = new openboxes.inventory.RecordInventoryViewModel(data.product, data.inventoryItems);
        ko.applyBindings(viewModel);
    });
</script>

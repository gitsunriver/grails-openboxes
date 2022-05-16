<div class="page" style="page-break-after: ${pageBreakAfter};">
    <table>
        <thead>
            <tr>
                <th></th>
                <th>${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
                <th>${warehouse.message(code: 'shipmentItem.quantityShipped.label')}</th>
                <th>${warehouse.message(code: 'shipmentItem.quantityReceived.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:unless test="${shipment.shipmentItems}">
                <tr>
                    <td colspan="8" class="middle center">
                        <span class="fade">
                            <warehouse:message code="default.none.label"/>
                        </span>
                    </td>
                </tr>
            </g:unless>
            <g:each in="${shipment.shipmentItems}" status="i" var="shipmentItem">

                <tr>
                    <td>
                        ${i+1}
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.product?.productCode}
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.product?.name}
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.lotNumber}
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.expirationDate}
                    </td>
                    <td>
                        ${shipmentItem?.totalQuantityShipped()}
                        ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:"default.each.label")}
                    </td>
                    <td>
                        ${shipmentItem?.totalQuantityReceived()}
                        ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:"default.each.label")}
                    </td>

                </tr>

            </g:each>
        </tbody>
    </table>
</div>


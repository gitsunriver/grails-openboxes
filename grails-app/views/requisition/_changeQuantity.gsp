<div class="box">
    <h2>${warehouse.message(code:'requisitionItem.changeQuantity.label', default:'Change quantity')}</h2>

    <g:hasErrors bean="${flash.errors}">
        <div class="errors">
            <g:renderErrors bean="${flash.errors}" as="list" />
        </div>
    </g:hasErrors>


    <g:form controller="requisitionItem" action="changeQuantity" fragment="${selectedRequisitionItem?.id}">

        <g:hiddenField name="id" value="${selectedRequisitionItem?.requisition?.id }"/>
        <g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
        <g:hiddenField name="actionType" value="${params.actionType }"/>
        <table>
            <tr>
                <td class="middle right">
                    <label><warehouse:message code="requisitionItem.product.label"/></label>
                </td>
                <td class="middle">
                    ${selectedRequisitionItem?.product?.productCode}
                    ${selectedRequisitionItem?.product}
                </td>
            </tr>
            <tr>
                <td class="middle right">
                    <label><warehouse:message code="requisitionItem.unitOfMeasure.label" default="Unit of Measure"/></label>
                </td>
                <td class="middle">
                    <g:selectUnitOfMeasure name="productPackage.id" product="${selectedRequisitionItem?.product}" class="chzn-select" style="width:300px;"/>
                </td>
            </tr>
            <tr>
                <td class="middle right">
                    <label><warehouse:message code="requisitionItem.quantity.label"/></label>
                </td>
                <td class="middle">
                    <g:textField id="quantity" name="quantity" value="${selectedRequisitionItem?.quantity}" class="text" size="5"/>
                </td>
            </tr>
            <tr>
                <td class="middle right">
                    <label><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Reason code"/></label>
                </td>
                <td class="middle">
                    <g:select name="reasonCode"
                              style="width: 350px"
                              data-placeholder="Choose a reason code ..."
                              class="chzn-select"
                              from="[
                                      'Stock out',
                                      'Substitution',
                                      'Damaged',
                                      'Expired',
                                      'Package size',
                                      'Reserved',
                                      'Cancelled by requestor',
                                      'Clinical adjustment',
                                      'Other']"
                              noSelection="['':'']"
                              value="${selectedRequisitionItem?.cancelReasonCode }"/>
                </td>
            </tr>
            <tr>
                <td class="top right">
                    <label style="display: block;"><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>
                </td>
                <td>
                    <g:textField name="comments" value="${selectedRequisitionItem.cancelComments}"
                                 size="60" class="text" placeholder="${warehouse.message(code:'requisitionItem.comments.placeholder', default:'Any additional information')}"/>

                </td>

            </tr>
            <tr>
                <td class="middle center" colspan="2">
                    <button class="button">
                        ${warehouse.message(code:'default.button.save.label') }
                    </button>

                    <g:link controller="requisition" action="review" id="${selectedRequisitionItem?.requisition?.id}">
                        ${warehouse.message(code:'default.button.cancel.label') }
                    </g:link>
                </td>
            </tr>
        </table>
    </g:form>
</div>
<script type="text/javascript">
    $(function() {
        $("#newQuantity").focus();
    });
</script>

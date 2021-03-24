<g:if test="${invoiceInstance?.id }">
	<span class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		<div class="actions" style="min-width: 200px;">
			<div class="action-menu-item">
				<g:link controller="invoice" action="show" id="${invoiceInstance?.id}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />&nbsp;
					<warehouse:message code="invoice.viewDetails.label" default="View Invoice Details"/>
				</g:link>
			</div>
			<div class="action-menu-item">
				<g:link controller="invoice" action="create" id="${invoiceInstance?.id}">
					<img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
					<warehouse:message code="invoice.editInvoice.label" default="Edit Invoice"/>
				</g:link>
			</div>
        </div>
	</span>
</g:if>

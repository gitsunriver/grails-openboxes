<g:if test="${orderInstance?.id }">
	<div class="actions" style="min-width: 200px;">
		<g:if test="${!request.request.requestURL.toString().contains('order/list')}">
			<div class="action-menu-item">
				<g:link controller="order" action="list">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View orders" style="vertical-align: middle" />
					&nbsp;${warehouse.message(code: 'order.list.label', default: 'View orders')} 
				</g:link>
			</div>
		</g:if>
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">
			<g:link controller="order" action="show" id="${orderInstance?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="Edit" style="vertical-align: middle" />
				&nbsp;${warehouse.message(code: 'order.view.label')} 
			</g:link>		
		</div>
		<div class="action-menu-item">
			<g:link target="_blank" controller="order" action="print" id="${orderInstance?.id}">
				<img src="${createLinkTo(dir: 'images/icons', file: 'pdf.png')}" class="middle"/>&nbsp;
				<warehouse:message code="order.print.label" default="Print order"/>
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="order" action="addComment" id="${orderInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />
				&nbsp;${warehouse.message(code: 'order.addComment.label')} 
			</g:link>				
		</div>		
		<div class="action-menu-item">
			<g:link controller="order" action="addDocument" id="${orderInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />
				&nbsp;${warehouse.message(code: 'order.addDocument.label')} 
			</g:link>				
		</div>
		<div class="action-menu-item">
			<hr/>
		</div>
		<div class="action-menu-item">
			<g:link controller="order" action="rollbackOrderStatus" id="${orderInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}" />
				&nbsp;${warehouse.message(code: 'order.rollbackOrderStatus.label', default: "Rollack order status" )}
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link controller="order" action="delete" id="${orderInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />
				&nbsp;${warehouse.message(code: 'order.deleteOrder.label')}
			</g:link>
		</div>
	</div>
</g:if>
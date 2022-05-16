<%@ page import="org.pih.warehouse.requisition.RequisitionStatus"%>

<g:if test="${requisition?.id }">
	<span id="shipment-action-menu" class="action-menu">
		<button class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
		</button>
		
		<%-- 
		
		<g:if test="${requisition?.isPending() }">
		</g:if>
		<g:else>
			<div class="actions" style="min-width: 300px;">
				<div class="action-menu-item center">
					<a href="#">No actions available for ${requisition?.status }</a>
				</div>
			</div>
		</g:else>
		--%>
			<div class="actions" >
				<g:if test="${!request.request.requestURL.toString().contains('requisition/list')}">
					<div class="action-menu-item">
						<g:link controller="requisition" action="list">
							<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
							&nbsp;${warehouse.message(code: 'requisition.view.label', default: 'View requisitions')}
						</g:link>
					</div>
					<div class="action-menu-item">
						<hr/>
					</div>
				</g:if>
				<div class="action-menu-item">
					<g:link controller="requisition" action="show" id="${requisition?.id}">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
						&nbsp;${warehouse.message(code: 'requisition.show.label', default: 'Preview requisition')}
					</g:link>		
				</div>
				<div class="action-menu-item">
					<g:link controller="requisition" action="edit" id="${requisition?.id}">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
						&nbsp;${warehouse.message(code: 'requisition.edit.label', default: 'Edit requisition')}
					</g:link>		
				</div>
				<%-- 
				<div class="action-menu-item">
					<g:link controller="requisition" action="printDraft" id="${requisition?.id}" target="_blank">
						<img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
						&nbsp;${warehouse.message(code: 'requisition.print.label', default: 'Print requisition')}
					</g:link>				
				</div>
				--%>
				<g:if test="${session?.warehouse?.id == requisition?.destination?.id }">
					<g:isUserManager>
						<div class="action-menu-item">
							<g:link controller="requisition" name="processRequisition" action="pick" id="${requisition?.id}">
								<img src="${resource(dir: 'images/icons/silk', file: 'cart.png')}" />
								&nbsp;${warehouse.message(code: 'requisition.process.label', default: 'Process requisition')}
							</g:link>				
						</div>
						<g:if test="${requisition.status == RequisitionStatus.CANCELED }">
							<div class="action-menu-item">
								<g:link controller="requisition" action="uncancel" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.cancel.confirm.message', default: 'Are you sure?')}');">
									<img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />
									&nbsp;${warehouse.message(code: 'requisition.uncancel.label', default: 'Un-cancel requisition')}
								</g:link>				
							</div>
						</g:if>
						<g:else>
							<div class="action-menu-item">
								<g:link controller="requisition" action="cancel" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.cancel.confirm.message', default: 'Are you sure?')}');">
									<img src="${resource(dir: 'images/icons/silk', file: 'cross.png')}" />
									&nbsp;${warehouse.message(code: 'requisition.cancel.label', default: 'Cancel requisition')}
								</g:link>				
							</div>
													
						</g:else>
					</g:isUserManager>
					<g:isUserAdmin>
			            <div class="action-menu-item">
			                <g:link controller="requisition" action="delete" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
			                    <img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
			                    &nbsp;${warehouse.message(code: 'request.delete.label', default: 'Delete requisition')}
			                </g:link>
			            </div>
		            </g:isUserAdmin>
					
					<%-- 
					<div class="action-menu-item">
						<hr/>
					</div>
					<div class="action-menu-item">
						<g:link controller="requisition" action="printDraft" id="${requisition?.id}" target="_blank">
							<img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
							&nbsp;${warehouse.message(code: 'picklist.print.label', default: 'Print picklist')}
						</g:link>				
					</div>
					<div class="action-menu-item">
						<g:link controller="requisition" action="confirm" id="${requisition?.id}">
							<img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}" />
							&nbsp;${warehouse.message(code: 'requisition.confirm.label', default: 'Confirm picklist')}
						</g:link>				
					</div>
					<div class="action-menu-item">
						<g:link controller="requisition" action="issue" id="${requisition?.id}">
							<img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />
							&nbsp;${warehouse.message(code: 'requisition.issue.label', default: 'Issue stock')}
						</g:link>				
					</div>
					--%>
				</g:if>
			</div>
	</span>
</g:if>
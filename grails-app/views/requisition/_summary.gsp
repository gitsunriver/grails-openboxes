<div>
	<g:if test="${requestInstance?.id}">
	
		<g:set var="isAddingComment" value="${request.request.requestURL.toString().contains('addComment')}"/>
		<g:set var="isAddingDocument" value="${request.request.requestURL.toString().contains('addDocument')}"/>
		<table>
			<tbody>			
				<tr>
					<td>
						<div>
							<span class="title">${requestInstance?.name}</span>							
						</div> 
						<div class="fade" style="font-size: 0.9em; line-height: 20px;">
							<!-- Hide action menu menu if the user is in the shipment workflow -->	
							<g:if test="${!params.execution  && !isAddingComment && !isAddingDocument }">
								<g:render template="/requisition/actions" model="[requestInstance:requestInstance]"/> &nbsp;|&nbsp;
							</g:if>
							<%-- 
							<span class="request-number">
								<warehouse:message code="request.requestNumber.label"/>: <b>${requestInstance?.requestNumber}</b>  
							</span>
							<span class="fade">&nbsp;|&nbsp;</span> 
							--%>
							<span class="status">
								${warehouse.message(code: 'default.status.label') }:
								<b><format:metadata obj="${requestInstance?.status}"/></b>						
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-date">
								<warehouse:message code="request.date.label"/>: <b><format:date obj="${requestInstance?.dateRequested}"/></b>
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="request-items">
								<warehouse:message code="request.requestItems.label"/>: 
								<b>${requestInstance?.requisitionItems?.size()}</b>
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-by">
								<warehouse:message code="request.requestedBy.label"/>: 
								<g:if test="${requestInstance?.requestedBy }"><b>${requestInstance?.requestedBy?.name }</b></g:if>
								<%-- 
								<g:if test="${requestInstance?.destination }">(${requestInstance?.destination?.name })</g:if>
								--%>
							</span>
						</div>
					</td>										
					<td style="text-align: right;">
						<%--
						<g:if test="${!params.execution && !isAddingComment && !isAddingDocument}">						
							<g:if test="${!requestInstance?.isComplete() && requestInstance?.status != org.pih.warehouse.request.OrderStatus.PLACED }">
								<g:form action="placeOrder">
									<g:hiddenField name="id" value="${requestInstance?.id }"/>
									<button>Place Order</button>
								</g:form>
							</g:if>
							<g:elseif test="${!requestInstance?.isComplete() && requestInstance?.status == org.pih.warehouse.request.OrderStatus.PLACED }">
								<g:link controller="receiveOrderWorkflow" action="receiveOrder" id="${requestInstance?.id}">
									<button>
										${warehouse.message(code: 'request.receive.label', default: 'Receive request')}
									</button> 
								</g:link>										
							</g:elseif>
						</g:if>
						 --%>					
					</td>
				</tr>
			</tbody>
		</table>			
	</g:if>
	<g:else>
		<table>
			<tbody>			
				<tr>
					<td>
						<div>
							<span class="title">
								<g:if test="${requestInstance?.name}">
									${requestInstance?.name }
								</g:if>
								<g:else>
									<warehouse:message code="request.new.label"/>
								</g:else>
							</span>							
						</div> 
					</td>										
					<td style="text-align: right;">
						
					</td>
				</tr>
			</tbody>
		</table>			
	
	</g:else>
</div>
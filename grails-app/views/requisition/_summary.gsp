<div id="requisition-summary">
	<g:if test="${requisition?.id}">
		<table style="width: auto;">
			<tbody>			
				<tr>

                    <td class="top" style="width: 1%;">
                        <g:render template="../requisition/actions" model="[requisition:requisition]" />
                    </td>

                    <td class="center">
						<div>
							<g:if test="${requisition?.requestNumber }">
								<img src="${createLink(controller:'product',action:'barcode',params:[data:requisition?.requestNumber,width:100,height:30,format:'CODE_128']) }"/>
							</g:if>
						</div>
						${requisition?.requestNumber }

					</td>
					<td>
						<div class="title" id="description">
							${requisition?.name }
						</div>
						<div class="clear"></div>
						<div class="fade">
							<span id="origin">
								<warehouse:message code="requisition.origin.label"/>:
								<b>${requisition?.origin?.name?.encodeAsHTML()?:warehouse.message(code: 'default.none.label')}</b>
							</span>							
							<span class="fade">&nbsp;|&nbsp;</span>
							<span id="destination">
								<warehouse:message code="requisition.destination.label"/>:
	                            <b>${requisition?.destination?.name?.encodeAsHTML()?:session?.warehouse?.name}</b>
							</span>							
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-date">
								<warehouse:message code="requisition.date.label"/>: 
								<b><format:date obj="${requisition?.dateRequested}"/></b>
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="request-items">
								<warehouse:message code="requisition.requisitionItems.label"/>:
								<b>${requisition?.requisitionItems?.size()?:0}</b>
							</span>
                            <span id="last-updated">
                                <warehouse:message code="default.lastUpdated.label"/>:
                                <b><g:formatDate date="${requisition?.lastUpdated }" format="MMM dd, yyyy"/></b>
                            </span>

							<%--
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-by">
								<warehouse:message code="requisition.processedBy.label"/>:
								<b>${requisition?.createdBy?.name?:warehouse.message(code: 'default.none.label') }</b>								
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-by">
								<warehouse:message code="requisition.requestedBy.label"/>:
								<b>${requisition?.requestedBy?.name?:warehouse.message(code: 'default.none.label') }</b>								
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span id="recipientProgram">
								<warehouse:message code="requisition.recipientProgram.label"/>:
	                            <b>${requisition?.recipientProgram?:warehouse.message(code: 'default.none.label') }</b>
							</span>							
							<span class="fade">&nbsp;|&nbsp;</span>
							<span id="recipient">
								<warehouse:message code="requisition.recipient.label"/>:
	                           <b>${requisition?.recipient?.name?:warehouse.message(code: 'default.none.label')}</b>
							</span>							
							 --%>	
						</div>
					</td>
					<td>
						<div class="left">	
							<div class="title">${requisition?.status }</div>
							<div class="clear"></div>
						</div>
					</td>
					
										
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td class="top" colspan="4">
						<%-- 
						<g:render template="actionsOther" model="[requisition:requisition]"/>
						--%>
						<g:render template="../requisition/buttons" model="[requisition:requisition]"/>
					</td>				
				</tr>
			</tfoot>			
		</table>			
	</g:if>
	<g:else>
		<div class="title" id="description">
            ${requisition?.name?:warehouse.message(code: 'requisition.new.label') }
		</div>
		<div class="clear"></div>	
	</g:else>
</div>
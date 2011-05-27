<div style="padding: 10px; border-bottom: 1px solid lightgrey;">
	<g:if test="${orderInstance?.id}">
		<table>
			<tbody>			
				<tr>
					<td>
						<div>
							<span style="font-size: 1.5em; font-weight: bold; line-height: 1em;">${orderInstance?.description}</span>							
						</div> 
						<div class="fade" style="font-size: 0.9em; line-height: 20px;">
							<!-- Hide action menu menu if the user is in the shipment workflow -->						
							<g:if test="${!params.execution }">
								<g:render template="/order/actions" model="[orderInstance:orderInstance]"/> &nbsp;|&nbsp;
							</g:if>
							<b>Order #:</b> ${orderInstance?.orderNumber}  
							&nbsp;|&nbsp; 
							<b>Date ordered:</b> <g:formatDate date="${orderInstance?.dateOrdered}"/>							
						</div>
					</td>										
					<td style="text-align: right;">
						<div class="fade" style="font-weight: bold; font-size:1.5em;">
							Pending
						</div>
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
							<span style="font-size: 1.5em; font-weight: bold; line-height: 1em;">
								<g:if test="${orderInstance?.description}">
									${orderInstance?.description }
								</g:if>
								<g:else>
									Untitled order
								</g:else>
							</span>							
						</div> 
					</td>										
					<td style="text-align: right;">
						<div class="fade" style="font-weight: bold; font-size:1.5em;">
							New
						</div>
					</td>
				</tr>
			</tbody>
		</table>			
	
	</g:else>
</div>
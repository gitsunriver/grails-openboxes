				<div class="${(userInstance?.active) ? 'active':'inactive'}">
					<table>
						<tr>
							<td>
								<g:if test="${userInstance.photo}">
		            					<img class="photo" width="25" height="25" 
		            						src="${createLink(controller:'user', action:'viewPhoto', id:userInstance.id)}" 
		            						style="vertical-align: middle" />
	            				</g:if>
	            				<g:else>
	            					<g:if test="${userInstance?.active}">
		            					<img class="photo" src="${resource(dir: 'images/icons', file: 'profile.png') }"
		            						style="vertical-align: bottom;" />
	            					</g:if>
	            					<g:else>
		            					<img class="photo" src="${resource(dir: 'images/icons', file: 'profile.png') }"
		            						style="vertical-align: bottom;" />
	            					</g:else>
	            				</g:else>
		            			<span style="font-weight: bold; font-size: 1.2em;">
		            				${fieldValue(bean: userInstance, field: "firstName")} 
		            				${fieldValue(bean: userInstance, field: "lastName")}
		            			</span>
								&nbsp;
								<g:link class="edit" action="edit" id="${userInstance?.id}" >${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}</g:link>
								&nbsp;
								
								
								<g:link class="delete" action="delete" id="${userInstance?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}</g:link>
							</td>							
							<td style="text-align: right;">
								<g:link action="toggleActivation" id="${userInstance?.id}">
									<g:if test="${userInstance?.active}">
										${warehouse.message(code: 'user.deactivate.label')}
									</g:if>
									<g:else>
										${warehouse.message(code: 'user.activate.label')}
									</g:else>
								</g:link>
								&nbsp;							
								<span style="font-size: 1.2em">
									<b>${userInstance?.active ? warehouse.message(code: 'user.active.label') : warehouse.message(code: 'user.inactive.label')}</b>
									<g:if test="${userInstance?.active}">
										<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'status_online.png') }"
		            						style="vertical-align: bottom;" />
									</g:if>
									<g:else>
		            					<img class="photo" src="${resource(dir: 'images/icons/silk', file: 'status_offline.png') }"
		            						style="vertical-align: bottom;" />
									</g:else>
								</span>
							</td>
						</tr>
					</table>
				</div>

<div class="summary">
	<table style="width:auto;">
		<tr>
			<td class="middle" width="1%">
				<g:render template="actions"/>
            </td>
            <td class="middle">
				<span class="title">
                    <g:if test="${locationInstance?.id}">
                        ${fieldValue(bean: locationInstance, field: "name")}
                    </g:if>
                    <g:else>
                        ${warehouse.message(code:'location.new.label', default: "New location")}
                    </g:else>
                    <small>(<format:metadata obj="${locationInstance?.locationType}"/>)</small>
				</span>
                <g:if test="${locationInstance?.parentLocation}">
                    <g:link controller="location" action="edit" id="${locationInstance?.parentLocation?.id}">Back to ${locationInstance?.parentLocation?.name}</g:link>
                </g:if>

			</td>
			<td class="right">
				<div class="right">
                    <span class="tag">
                        <%--
                        <g:if test="${locationInstance?.active}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'accept.png') }" class="middle" />
                        </g:if>
                        <g:else>
                            <img src="${resource(dir: 'images/icons/silk', file: 'decline.png') }" class="middle" />
                        </g:else>
                        --%>
                        ${locationInstance?.active ? warehouse.message(code:'warehouse.active.label') : warehouse.message(code:'warehouse.inactive.label')}
                    </span>
                </div>

			
			</td>
		</tr>
	</table>
</div>
<div class="button-bar">
    <g:link class="button icon search" action="list" controller="location">
        <warehouse:message code="default.list.label" args="[g.message(code: 'locations.label')]" />
    </g:link>
    <g:link class="button icon add" action="create" controller="location">
        <warehouse:message code="default.create.label" args="[g.message(code: 'location.label')]" />
    </g:link>
    <g:if test="${locationInstance}">
        <g:link class="button icon edit" action="edit" controller="location" id="${locationInstance?.id}">
            <warehouse:message code="default.edit.label" args="[g.message(code: 'location.label')]" />
        </g:link>
    </g:if>
</div>

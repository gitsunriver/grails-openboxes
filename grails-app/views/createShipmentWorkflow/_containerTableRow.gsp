<tr class="" style="border: 1px solid lightgrey;">
	<td style="vertical-align: middle;">
		<img src="${createLinkTo(dir:'images/icons/shipmentType',file:containerInstance.containerType.name.toLowerCase() + '.jpg')}" style="vertical-align: middle"/>
		<g:link action="createShipment" event="editContainer" params="[containerToEditId:containerInstance?.id]">
			&nbsp;<span class="large">${containerInstance?.name}</span>
		</g:link>
	</td>
	<td></td>
	<td></td>
	<td></td>
	<td nowrap="nowrap">
		<g:link action="createShipment" event="addItemToContainer" params="['container.id':containerInstance.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add an item" style="vertical-align: middle"/>
		</g:link> 													
	</td>
	<td nowrap="nowrap">
		<g:link action="createShipment" event="addBoxToContainer" params="['container.id':containerInstance.id]">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" alt="Add a box" style="vertical-align: middle"/>
		</g:link>
	</td>
</tr>
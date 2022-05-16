<tr class="prop">
	<td valign="top" class="name"><label>Name</label></td>                            
	<td valign="top" class="value">
		<g:textField id="name" name="name" size="50" value="${container ? container?.name : box?.name}"/> 
	</td>
</tr>

<tr class="prop">
	<td valign="top" class="name"><label>Description</label></td>                            
	<td valign="top" class="value">
		<g:textArea id="description" name="description" cols="50" rows="5" value="${container ? container?.description : box?.description}"/> 
	</td>
</tr>

<tr class="prop">
	<td valign="top" class="name"><label>Dimensions</label></td>                            
	<td valign="top" class="value">
		H: <g:textField id="height" name="height" size="5" value="${container ? container?.height : box?.height}"/>&nbsp;
		W: <g:textField id="width" name="width" size="5" value="${container ? container?.width : box?.width}"/>&nbsp;
		L: <g:textField id="length" name="length" size="5" value="${container ? container?.length : box?.length}"/>&nbsp;
		<g:select name="volumeUnits" from="${org.pih.warehouse.core.Constants.VOLUME_UNITS}" value="${container ? container?.volumeUnits : box?.volumeUnits}" />	

	</td>
</tr>

<tr class="prop">
	<td valign="top" class="name"><label>Weight</label></td>                            
	<td valign="top" class="value">
		<g:textField id="weight" name="weight" size="15" value="${container ? container?.weight : box?.weight}"/>&nbsp;
		<g:select name="weightUnits" from="${org.pih.warehouse.core.Constants.WEIGHT_UNITS}" value="${container ? container?.weightUnits : box?.weightUnits}" />	
	</td>
</tr>
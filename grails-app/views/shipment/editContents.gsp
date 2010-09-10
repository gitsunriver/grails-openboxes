<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Edit Shipment Contents</content>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>
		<table>
			<tbody>		
				<tr>
					<td style="width: 75%" valign="top">
						<fieldset>
							<g:render template="summary"/>
							<table>
								<tr>
									<td>	
										<div style="padding: 10px;">																	
											<span>Packages:</span>				
											<g:each in="${shipmentInstance?.containers}" var="container" status="i">															
												<g:if test="${container?.id == containerInstance?.id}">
													<span style="border: 1px solid black; padding: 5px;">
														&nbsp;${container.name}
													</span>													
												</g:if>
												<g:else>
													<span style="padding: 5px;">
														&nbsp;<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':container.id]">${container.name}</g:link>
													</span>
												</g:else>				
											</g:each>
										</div>
									</td>
								</tr>
							</table>
																		
							<table>
								<tr>
									<td>
										<g:if test="${containerInstance}">			
											<div id="container-${containerInstance?.id}" class="details">																									
												<script type="text/javascript">
													$(function() {
														$("#tabs").tabs();
													});
												</script>
												<div class="demo">								
													<div id="tabs">
														<ul>
															<li><a href="#tabs-1">Contents</a></li>
															<li><a href="#tabs-2">Details</a></li>
															<li><a href="#tabs-3">Clone</a></li>
														</ul>
														<div id="tabs-1">
															<g:render template="containerSummary" />																
															
															<div>
															
																		
																		
																	<h2>Add an item</h2>																	
																	<g:form action="addItemAutoComplete" id="${shipmentInstance.id}">	
																		<g:hiddenField name="container.id" value="${containerInstance?.id}"></g:hiddenField>
																	
																		<div class="list">																
																			<table>
																				<thead>
																					<tr class="prop">
																						<th></th>
																						<th>Qty</th>
																						<th>Item</th>
																						<th>Lot / Serial No</th>
																						<th>Recipient</th>
																						<th></th>
																					</tr>
																				</thead>
																			
																				<tbody>
																					<tr class="prop" style="background-color: #FFF6BF;">
																						<td width="7%" style="vertical-align: middle; text-align: center"> (new) </td>
																						<td width="10%" style="vertical-align: middle; text-align: center;">													
																							<g:textField name="quantity" value="" size="2" />
																						</td>
																						<td width="20%" style="vertical-align: middle; text-align: center;">											
																							<g:autoSuggest id="selectedItem" name="selectedItem" jsonUrl="/warehouse/shipment/findProductByName" width="200"/>	
																						</td>
																						<td width="15%" style="vertical-align: middle; text-align: center;">													
																							<g:textField name="serialNumber" value="" size="10" style="" />
																						</td>
																						<td width="20%" style="vertical-align: middle; text-align: center;">
																							<g:autoSuggest name="recipient" jsonUrl="/warehouse/shipment/findPersonByName" 
																								width="150" 
																								valueId="${shipmentInstance?.recipient?.id}" 
																								valueName="${shipmentInstance?.recipient?.email}"/>												
																						</td>
																						<td width="10%" style="vertical-align: middle; text-align: right">
																							<span class="buttons" style="padding: 0px">
																								<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add"/> Add</button>
																							</span>
																						</td>
																					</tr>	
																				</tbody>
																			</table>									
																		</div>																												
																	</g:form>

																	<br/>
																	<br/>
																	<h2>Included items</h2>																																
																	<g:form action="editContainer">
																		<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}"/>
																		<g:hiddenField name="containerId" value="${containerInstance?.id}"/>
																			<table border="0">
																				<g:if test="${containerInstance?.shipmentItems}">
																					<thead>
																						<tr class="prop">
																							<th></th>
																							<th>Qty</th>
																							<th>Item</th>
																							<th>Lot / Serial No</th>
																							<th>Recipient</th>
																							<th></th>
																						</tr>																							
																					</thead>
																					<tbody>
																					
																						<g:each var="item" in="${containerInstance.shipmentItems}" status="k">
																							<tr class="prop ${(k % 2) == 0 ? 'odd' : 'even'}">
																								<td width="7%" style="text-align: center;">${k+1}</td>
																								<td width="10%">
																									<g:hiddenField name="shipmentItems[${k}].id" value="${item.id}"></g:hiddenField>											    
																									<g:textField name="shipmentItems[${k}].quantity" value="${item.quantity}" size="2" />
																								</td>
																								<td width="20%">
																									${item?.product?.name} 
																									<g:if test="${item?.product?.unverified}">
																										<span class="fade">(unverified)</span>
																									</g:if> 
																								</td>
																								<td width="15%">
																									<g:textField name="shipmentItems[${k}].serialNumber" value="${item.serialNumber}" size="10" />																									
																								</td>
																								<td width="20%">							
																									
																									<span class="fade">${item?.recipient?.email}</span>
																									<%-- 
																									<g:autoSuggest name="shipmentItems[${k}]\\.recipient" jsonUrl="/warehouse/shipment/findPersonByName" 
																										width="150" 
																										valueId="${item?.recipient?.id}" 
																										valueName="${item?.recipient?.email}"/>												
																									--%>
																								</td>
																								<td width="10%" style="vertical-align: bottom; text-align: right">
																									<span class="buttons" style="padding: 0px">
																										<%-- <button type="submit" class="negative"><img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete"/></button>--%>
																									</span>
																								</td>
																							</tr>							
																						</g:each>																																														
																						<tr class="prop">
																							<td colspan="6">
																								<div class="buttons" style="padding: 15px; float: right;">
																									<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save"/> Save</button>
																									<%-- <g:link class="negative" controller="shipment" action="deleteContainer" id="${containerInstance.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete"/> Delete</a></g:link> &nbsp;--%>
																								</div>
																								<div class="fade" style="padding: 15px; float: right;">
																									<li>To remove an item, enter '0' in <b>Qty</b> field.</li>
																									<li>After modifying any of the values above, click <b>Save</b>.</li>  
																									
																								</div>
																							</td>
																						</tr>
																					</tbody>
																				</g:if>
																				<g:else>
																					<tbody>
																						<tr>
																							<td style="text-align: center" colspan="6" >
																								<div class="fade" style="padding: 20px;">(empty)</div>
																							</td>
																						</tr>	
																					</tbody>												
																				</g:else>	
																			</table>	
																		</g:form>	

															

															</div>
														</div><!-- tabs-1 -->	 
														
															
														<div id="tabs-2">
														
															<g:render template="containerSummary" />	
															<g:form action="editContainer">		
																<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}"></g:hiddenField>												    
																<g:hiddenField name="containerId" value="${containerInstance?.id}"></g:hiddenField>										
																			
														    	<table>
																	<tr class="prop">										
																		<td class="name"><label>${containerInstance?.containerType?.name} #</label></td>
																		<td class="value">
																			<g:textField name="name" value="${containerInstance?.name}" size="2" />
																		</td>
																	</tr>
																	<tr class="prop">
																		<td class="name"><label class="optional">Identifier #</label></td>
																		<td class="value">
																			<g:textField name="containerNumber" value="${containerInstance.containerNumber}" size="15"/> &nbsp;		
																			<span class="fade"></span>																
																		</td>													
																	</tr>
																	<tr class="prop">										
																		<td class="name"><label class="optional">Weight</label></td>
																		<td class="value">
																			<g:textField name="weight" value="${containerInstance?.weight}" size="7"/> 
																			<g:select name="weightUnits" 
																				from="${[' ', 'lb', 'kg']}"
																				value="${containerInstance?.weightUnits}">
																			</g:select>																	
																			<span class="fade">e.g. '100 lb' or '120 kg' </span>
																		</td>
																	</tr>
																	<tr class="prop">
																		<td class="name"><label class="optional">Dimensions</label></td>
																		<td class="value">
																			<g:textField name="height" value="${containerInstance?.height}" size="2"/> x
																			<g:textField name="width" value="${containerInstance?.width}" size="2"/> x
																			<g:textField name="length" value="${containerInstance?.length}" size="2"/> 																																								
																			<g:select name="volumeUnits" 
																				from="${['', 'in', 'ft', 'cm']}"
																				value="${containerInstance?.volumeUnits}">																							
																			</g:select>
																			
																			 <span class="fade">e.g. '10.1" x 4.2" x 2.8"'</span>
																		</td>		
																	</tr>
																	<tr class="prop">
																		<td class="name"><label class="optional">Description</label></td>
																		<td class="value">
																			<g:textField name="description" value="${containerInstance?.description}" size="40"/> &nbsp;
																		</td>
																	</tr>
																	<tr class="prop">																	
																		<td class=""></td>
																		<td class="value" colspan="2">
																			<div class="buttons">
																				<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button>
																				<g:link class="negative" controller="shipment" action="deleteContainer" id="${containerInstance.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete"/> Delete</a></g:link> &nbsp;
																			</div>
																		</td>
																	</tr>
																</table>					
															</g:form>		
														</div><!-- tabs-2 -->
												
														<div id="tabs-3">
														
															<g:render template="containerSummary" />	
															<g:form action="copyContainer">
																<g:hiddenField name="id" value="${containerInstance?.id}" />	
																<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />															
																<table>
																	<tbody>
																		<tr class="prop">
													                           <td valign="top" class="name"><label><g:message code="container.name.label" default="Copying unit" /></label></td>                            
													                           <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
																				${containerInstance.containerType.name}-${containerInstance.name}
																			</td>
													                       </tr>  	          
																			<tr class="prop">
													                           <td valign="top" class="name"><label><g:message code="container.copies.label" default="How Many?" /></label></td>                            
													                           <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
																				<g:textField name="copies" value="1" size="3"/>
													                              </td>
													                       </tr>  	          
													                       <tr class="prop">
																		    <td class="name"></td>
																		    <td class="value">
																				<div class="buttons">
																					<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Copy</button>
																					</div>	
																			    </td>					                        
													                        </tr>         
													                    </tbody>
													                </table>																			                	               
															    </g:form>
															</div><!-- tabs-3 -->
														</div><!-- tabs -->
													</div><!-- demo -->
												</div><!-- details -->
										</g:if>
										<g:else>												
											<div style="padding: 15px;" class="notice">
												Choose a package to edit
											</div>												
										</g:else>												
									</td>
								</tr>																										
							</table>
						</fieldset>
					</td>						
					<td valign="top" width="20%">							
						<g:render template="sidebar" />
					</td>
				</tr>
			</tbody>
		</table>							
	</div>
</body>
</html>




<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'warehouse.label', default: 'Location')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery',file:'jquery.colorpicker.css')}" type="text/css" media="screen, projection" />
		<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.colorpicker.js')}" type="text/javascript" ></script>
    </head>
    <body>
        <div class="body">
        
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationInstance}" as="list" />
	            </div>
            </g:hasErrors>
            
			<div class="buttonBar">            	
            	<span class="linkButton">
            		<g:link class="list" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'location.label').toLowerCase()]"/></g:link>
            	</span>
            	<span class="linkButton">
            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="[warehouse.message(code:'location.label').toLowerCase()]"/></g:link>
            	</span>
            	
           	</div>
            
            
            <g:form method="post" action="update">
            	<fieldset>
            		
	                <g:hiddenField name="id" value="${locationInstance?.id}" />
	                <g:hiddenField name="version" value="${locationInstance?.version}" />
	                <div class="dialog">
	                    <table>
	                    	<thead>
								<tr>
				        			<td valign="top" colspan="2">
										<g:render template="summary"/>			            			
									</td>            
		                    	</tr>	                    	
	                    	</thead>
	                        <tbody>
	                            <tr class="prop">
	                                <td valign="top" class="name">
										<label for="name"><warehouse:message code="default.name.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'name', 'errors')}">
										<g:textField name="name" value="${locationInstance?.name}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
										<label for="name"><warehouse:message code="location.locationType.label" /></label>
		
	                                </td>
	                                <td valign="top" class="value">
	                                	<g:select name="locationType.id" from="${org.pih.warehouse.core.LocationType.list()}" 
	                                		optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${locationInstance?.locationType?.id}" noSelection="['':'']" />
	                                		
	                                	
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
										<label for="name"><warehouse:message code="location.locationGroup.label" /></label>
	                                </td>
	                                <td valign="top" class="value">
	                                	<g:select name="locationGroup.id" from="${org.pih.warehouse.core.LocationGroup.list()}" optionKey="id" value="${locationInstance?.locationGroup?.id}" noSelection="['':'']" />
	                                </td>
	                            </tr>	         
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                	<label for="manager"><warehouse:message code="warehouse.manager.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'manager', 'errors')}">
	                                	<g:select name="manager.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${locationInstance?.manager?.id}"  noSelection="['null':'']" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
		                                <label for="manager"><warehouse:message code="warehouse.properties.label" /></label>
	                                </td>
	                                <td valign="top" class="value${hasErrors(bean: locationInstance, field: 'active', 'errors')}">
										<g:checkBox name="active" value="${locationInstance?.active}" />
										<warehouse:message code="warehouse.active.label" />
										
										<g:checkBox name="local" value="${locationInstance?.local}" />
										<warehouse:message code="warehouse.local.label" />
										
	                                </td>
	                            </tr>
	                            
	                            <!--                     
	                            <tr class="prop">
	                                <td valign="top" class="name">
										<label for="name"><warehouse:message code="location.supportedActivities.label" /></label>
	                                </td>
	                                <td valign="top" class="value">
	                                	<g:set var="activityList" value="${org.pih.warehouse.core.ActivityCode.list() }"/>
	                                	<g:select name="supportedActivities" multiple="true" from="${activityList }" size="${activityList.size()+1 }" style="width: 150px" 
	                                		optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${locationInstance?.supportedActivities}"
	                                		noSelection="['':'']" />
	                                </td>
	                            </tr>
	                            -->	                            
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="bgColor"><warehouse:message code="warehouse.bgColor.label"/></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'bgColor', 'errors')}">
	                                    
	                                    <g:select name="bgColor" class="colorpicker" 
	                                    	from="${org.pih.warehouse.core.Constants.COLORS}" 
	                                    	value="${locationInstance?.bgColor}" />
	                                    
                                    	<span class="fade">${locationInstance?.bgColor }</span>
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="fgColor"><warehouse:message code="warehouse.fgColor.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'fgColor', 'errors')}">
	                                    <g:select name="fgColor" class="colorpicker" 
	                                    	from="${org.pih.warehouse.core.Constants.COLORS}" 
	                                    	value="${locationInstance?.fgColor}" />
	                                    	
                                    	<span class="fade">${locationInstance?.fgColor }</span>
	                                </td>
	                            </tr>
	                            
	                            <!--  
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="parentLocation"><warehouse:message code="warehouse.parentLocation.label" default="Parent Location" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationInstance, field: 'parentLocation', 'errors')}">
										<g:select name="parentLocation.id" from="${org.pih.warehouse.core.Location.list()}" 
											optionKey="id" optionValue="name" value="" noSelection="['null': '']" />							
	                                </td>
	                            </tr>
	                            -->
	                            <tr class="prop">
	                            
	                            	<td valign="top" class="name">
	                            	
	                            	</td>
	                            	<td class="value">
										<div class="buttons left">
						                   <button type="submit">								
												<img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png')}" class="top"/>
												<warehouse:message code="default.button.save.label"/>
											</button>
											&nbsp;
											<g:link action="list">
												<img src="${createLinkTo(dir: 'images/icons/silk', file: 'decline.png')}" class="middle"/>
												${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}						
											</g:link>			
										</div>
									</td>
								</tr>
	                            
	                        </tbody>
	                    </table>
	                </div>
	                
                </fieldset>
            </g:form>
        </div>
	    <script type="text/javascript">

	        function selectCombo(comboBoxElem, value) {
		        alert(comboBoxElem + " " + value)
				if (comboBoxElem != null) {
					if (comboBoxElem.options) { 
						for (var i = 0; i < comboBoxElem.options.length; i++) {
				        	if (comboBoxElem.options[i].value == value &&
				                comboBoxElem.options[i].value != "") { //empty string is for "noSelection handling as "" == 0 in js
				                comboBoxElem.options[i].selected = true;
				                break
				        	}
						}
					}
				}
			}						
	    
	        $(document).ready(function() {
	            $('#bgColor').colorpicker({
	                size: 20,
	                label: '',
	                hide: true
	            });

	            $('#fgColor').colorpicker({
	                size: 20,
	                label: '',
	                hide: true
	            });
			
	        });
	    </script>        
    </body>
</html>

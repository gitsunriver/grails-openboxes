
<%@ page import="org.pih.warehouse.core.Localization" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'localization.label', default: 'Localization')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div>            	
	            	<span class="linkButton">
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="['localization']"/></g:link>
	            	</span>
            	</div>
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'localization.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="code" title="${warehouse.message(code: 'localization.code.label', default: 'Code')}" />
                        
                            <g:sortableColumn property="locale" title="${warehouse.message(code: 'localization.locale.label', default: 'Locale')}" />
                        
                            <g:sortableColumn property="text" title="${warehouse.message(code: 'localization.text.label', default: 'Text')}" />
                        
                            <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'localization.dateCreated.label', default: 'Date Created')}" />
                        
                            <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'localization.lastUpdated.label', default: 'Last Updated')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${localizationInstanceList}" status="i" var="localizationInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${localizationInstance.id}">${fieldValue(bean: localizationInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: localizationInstance, field: "code")}</td>
                        
                            <td>${fieldValue(bean: localizationInstance, field: "locale")}</td>
                        
                            <td>${fieldValue(bean: localizationInstance, field: "text")}</td>
                        
                            <td><format:date obj="${localizationInstance.dateCreated}" /></td>
                        
                            <td><format:date obj="${localizationInstance.lastUpdated}" /></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${localizationInstanceTotal}" />
            </div>
        </div>
    </body>
</html>

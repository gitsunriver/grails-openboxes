<%@ page import="java.util.Locale" %>
<?xml version="1.0" encoding="UTF-8"?>
<html>
<head>
	<!-- Include default page title -->
	<title><g:layoutTitle default="OpenBoxes" /></title>
	
	<!-- YUI -->
	<yui:stylesheet dir="reset-fonts-grids" file="reset-fonts-grids.css" />
	
	<!-- Include Favicon -->
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
	
	<!-- Include Main CSS -->
	<!-- TODO Apparently there is a slight distinction between these two ... need to figure out what that distinction is -->
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css" media="all" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'menu.css')}" type="text/css" media="all" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'form.css')}" type="text/css" media="all" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footer.css')}" type="text/css" media="all" />	
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'wizard.css')}" type="text/css" media="all" />	
	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.megaMenu/',file:'jquery.megamenu.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.nailthumb',file:'jquery.nailthumb.1.1.css')}" type="text/css" media="all" />
	<!--  
	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.mcdropdown/css',file:'jquery.mcdropdown.css')}" type="text/css" media="all" />
	--> 
	
	<!-- Include javascript files -->
	<g:javascript library="application"/>

	<!-- Include jQuery UI files -->
	<g:javascript library="jquery" plugin="jquery" />
	<jqui:resources />
	<link href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui.css')}" type="text/css" rel="stylesheet" media="screen, projection" />

	
  		
 	<!-- Include Jquery Validation and Jquery Validation UI plugins --> 
 	<jqval:resources />       
    <jqvalui:resources />


	<%--
	<link href="${createLinkTo(dir:'js/jquery.jqGrid/css', file:'ui.jqgrid.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<script src="${createLinkTo(dir:'js/jquery.jqGrid/js', file:'jquery.jqGrid.min.js')}" type="text/javascript" ></script>
	 --%>
	<%--
    <script type="text/javascript" src="${createLinkTo(dir:'js/jquery/', file:'fg.menu.js')}"></script>
    <link type="text/css" href="${createLinkTo(dir:'js/jquery/', file:'fg.menu.css')}" media="screen" rel="stylesheet" />	
	--%>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'custom.css')}" type="text/css" media="all" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'buttons.css')}" type="text/css" media="all" />
	
  <!-- jquery validation messages -->
  <g:if test="${ session?.user?.locale && session?.user?.locale != 'en'}">
    <script src="${createLinkTo(dir:'js/jquery.validation/', file:'messages_'+ session?.user?.locale + '.js')}"  type="text/javascript" ></script>
  </g:if>


	<!-- Custom styles to be applied to all pages -->
	<style type="text/css" media="screen"></style>
	
	<!-- Grails Layout : write head element for page-->
	<g:layoutHead />

	<g:render template="/common/customCss"/>
	
	<ga:trackPageview />
</head>
<body class="yui-skin-sam">

	<g:render template="/common/customVariables"/>
	
	<%-- 
	
	<g:if test="${flash.message}">	
		<div id="notify-container" style="display: hidden;">
			<div id="notify-message" class="message">${flash.message}</div>	
		</div>
	</g:if>
 	--%>
 	
 	<g:if test="${session.useDebugLocale}">
	<div id="debug-header" class="notice" style="margin-bottom: 0px;">
	 	You are in DEBUG mode.
	 	
	 	<div class="right">
		 	<g:link controller="localization" action="list">Show all localizations</g:link> |
		 	<g:link controller="user" action="disableDebugMode">Disable debug mode</g:link>
	 	</div>
	 	<g:each var="localization" in="${flash.localizations }">
	 		<div>
	 			${localization.code } = ${localization.text }
	 		</div>
	 	</g:each>
	 	
	 	
	</div>
</g:if>
 	
	<!-- Header "hd" includes includes logo, global navigation -->
	<div id="hd" role="banner">
	    <g:render template="/common/header"/>		    
	</div>
	<g:if test="${session?.user && session?.warehouse}">
		<div id="megamenu">    
			<g:include controller="dashboard" action="megamenu"/>		    
		</div>
	</g:if>	    
  	<g:if test="${session.user}">
 		<div>
   			<ul class="breadcrumb">
   				<li>
					<g:link controller="dashboard" action="index">
						<img src="${createLinkTo(dir:'images/icons/silk',file:'house.png')}" class="home"/>
						<span><warehouse:message code="default.home.label" default="Home"/></span>
					</g:link>
				</li>
				<g:if test="${session?.user && session?.warehouse}">
					<li>
						<a href="javascript:void(0);" class="warehouse-switch">
							<img src="${createLinkTo(dir:'images/icons/silk',file:'map.png')}" class="map"/>
							${session?.warehouse?.name }
						</a>
					</li>
		    	</g:if>
				<g:if test="${controllerName }">
					<li>
						<g:link controller="${controllerName }" action="index">
							<warehouse:message code="${controllerName + '.label'}" />
						</g:link>
					</li>
				</g:if>
				<%-- 
				<g:if test="${actionName }">
					<li>
						<a href="">
							${actionName.capitalize() }
						</a>
		    		</li>
	    		</g:if>
	    		--%>
	    		<g:if test="${g.layoutTitle() && !actionName.equals('index') && !actionName.contains('list') }">
		    		<li>
		    			<a href="#">${g.layoutTitle()}</a>
		    		</li>
	    		</g:if>

    		</ul>
    		
    		
    		
    		<%-- 				    
		    	<g:link controller="dashboard" action="index">
			    	<img src="${createLinkTo(dir: 'images/icons/silk', file: 'house.png')}" style="vertical-align: bottom;"/>
		    	</g:link>
			    &nbsp;&rsaquo;&nbsp;								
				<g:if test="${session?.warehouse}">									
					<g:if test="${session.warehouse.logo }">
						<img class="photo" width="25" height="25" 
							src="${createLink(controller:'location', action:'viewLogo', id:session.warehouse.id)}" style="vertical-align: middle" />
					</g:if>
					${session?.warehouse?.name} &nbsp;&rsaquo;&nbsp;
				</g:if> 
				<!--  note that both breadcrumbs are overrideable by using the content tag is the view to set the value of label1 or label2 -->
			    <g:set var="label1">${g.pageProperty(name: 'page.label1') ?: warehouse.message(code: "breadcrumbs." + params.controller + ".label")}</g:set>
			    <g:set var="label2">${g.pageProperty(name: 'page.label2') ?: g.layoutTitle()}</g:set>
			   		${label1 ?: params.controller}
			    <g:if test="${label1 != label2}">
					&nbsp;&rsaquo;&nbsp;								
	    			${label2}
	    		</g:if>
    		--%>
  	
   		</div>
   		<%-- 
		<g:if test="${session?.warehouse}">
			<div style="width:100%;">
				<div class="box" style="width: 100%; text-align: center;">
					<g:globalSearch cssClass="globalSearch" width="300" name="searchTerms" jsonUrl="${request.contextPath }/json/globalSearch"></g:globalSearch>
				</div>
			</div>
		</g:if>   		
   		--%>
  	</g:if>
	

  	<%-- 
  	<div class="page-actions">
  		${g.pageProperty(name: 'page.actions')}
  	</div>
  	--%>
  	<%--
  	<div class="page-header">
	  	<div class="page-actions">
	  		${g.pageProperty(name: 'page.actions')}
	  	</div>
	  	<div class="page-title">
			<g:if test="${session?.user && session?.warehouse}">
				<h1>${g.pageProperty(name: 'page.label2') ?: g.layoutTitle()}</h1>	
		   	</g:if>
	   	</div>
	</div>  	
  	 --%>
	
	<!-- Body includes the divs for the main body content and left navigation menu -->
		
	<div id="bd" role="main">
	    <div id="doc3"><!--class="yui-t3"-->		    	
	      	<div id="yui-main">
		    	<div id="content" class="yui-b">
					<g:layoutBody />
				</div>
	      	</div>
		</div>
	</div>
	
	<div id="localization-dialog" class="dialog" style="display: none;">
	
		<g:form controller="localization" action="save">
			<style>
				#localization-dialog label { display: block; }
				#localization-dialog div { padding; 20px; margin: 10px; }
			</style>
			<div style="float: left;">
				<div>
					<label>ID</label>
					<g:hiddenField id="id" name="id" value=""/>					
					<div id="show-id"></div>
			
				</div>
				<div>			
					<label>Locale</label>
					<g:hiddenField id="locale" name="locale" value="${session?.user?.locale }"/>
					<div id="show-locale">${session?.user?.locale }</div>
				</div>
				<div>
					<label>Code</label>
					<g:hiddenField id="code" name="code" value=""/>
					<div id="show-code"></div>
				</div>
				<div>
					<label>Args</label>
					
					<div id="show-args"></div>		
				</div>
				<div>
					<label>Text</label>
					<g:textArea id="message" name="text" value="" cols="60" rows="4"/>		
				</div>
				<div>
					<label>Resolved text</label>
					<div id="resolved-message"></div>		
				</div>
			</div>			
			<div class="clear"></div>		
			<div class="buttons">			
				<button id="save-localization" class="button">Save</button>
				<g:link controller="localization" action="list">Show all localizations</g:link>
			</div>
		</g:form>
		
		<%-- 
		<g:form controller="localization" action="save">
			<g:each var="localized" in="${request.localized }">
				<table id="localization-table-${localized?.key?.replace(".", "-") }" class="localization-table" style="display: none;">
					<tr class="${localized.key }">
						<td>						
							<label>${localized.key }</label>
						</td>
					<tr>
					<g:each var="value" in="${localized.value }">
						<tr class="prop">
							<td class="name">						
								${value.key }
							</td>
							<td class="value">	
								<g:textArea name="${localized.key }_${value.key }" 
									value="${value.value }" class="text" cols="60" rows="3"/> 
							</td>		
						</tr>						
					</g:each>
					<tr>
						<td colspan="2">
							<div class="buttons">
								<button class="button">
									<warehouse:message code="default.button.save.label"></warehouse:message>
								</button>
							</div>
						</td>
					</tr>
				</table>
			</g:each>
		</g:form>
		--%>	
	</div>

	<!-- YUI "footer" block that includes footer information -->
	<div id="ft" role="contentinfo">
		<g:render template="/common/footer" />
	</div>
	
	<!-- Include other plugins -->
	<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery.ui.autocomplete.selectFirst.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.cookies/', file:'jquery.cookies.2.2.0.min.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.cookie/', file:'jquery.cookie.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.tmpl/', file:'jquery.tmpl.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.tmplPlus/', file:'jquery.tmplPlus.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.livequery/', file:'jquery.livequery.min.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.livesearch/', file:'jquery.livesearch.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.hoverIntent/', file:'jquery.hoverIntent.minified.js')}" type="text/javascript" ></script>	
	
    <g:if test="${System.getenv().get('headless') != 'false'}" env="test"> 
    	<!--headless driver throw error when using watermark-->
	</g:if>
    <g:else>
        <script src="${createLinkTo(dir:'js/jquery.watermark/', file:'jquery.watermark.min.js')}" type="text/javascript" ></script>
    </g:else>
	<script src="${createLinkTo(dir:'js/', file:'global.js')}" type="text/javascript" ></script>	
	<script src="${createLinkTo(dir:'js/jquery.megaMenu/', file:'jquery.megamenu.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/', file:'underscore-min.js')}" type="text/javascript" ></script>
	
	<script type="text/javascript">
		$(function() { 		
						
			$(".megamenu").megamenu({'show_method':'simple', 'hide_method': 'simple'});
			$("#localization-dialog").dialog({ autoOpen: false, modal: true, width: '1000px' });	

			
			/*
			$(".open-dialog").click(function() { 
				var id = $(this).attr("id");
				$("#dialog-" + id).dialog('open');
			});
			$(".close-dialog").click(function() { 
				var id = $(this).attr("id");
				$("#dialog-" + id).dialog('close');
			});
			*/
			
			<g:if test="${session.useDebugLocale}">			
				$(".show-localization-dialog").click(function(event) { 
					var id = $(this).attr("data-id");
					var message = $(this).attr("data-message");				
					var resolvedMessage = $(this).attr("data-resolved-message");				
					var code = $(this).attr("data-code");				
					var localized = $(this).attr("data-localized");				
					var args = $(this).attr("data-args");		
					var locale = $(this).attr("data-locale");				
					var argsArray = args.split(",");
					$("#id").val(id);
					$("#show-id").text(id);
					$("#code").val(code);
					$("#message").val(message);
					$("#resolved-message").text(resolvedMessage);
					$("#localized").text(localized);
					$("#locale").val(locale);
					$("#show-code").text(code);					
					$("#show-args").text(args);
					$("#show-locale").val(locale);
					$("#localization-dialog").dialog('open');
					event.preventDefault();
				});
			</g:if>

			<%-- Automatic status message updater because it's not an ideal solution and isn't currently used 
			<g:if test="${new Boolean(grailsApplication.config.grails.statusUpdate.enabled?:'true') }">
				<g:if test="${session.user && session.warehouse}">
				
					// Creates an AJAX update thread that calls back to the server to see if there are any alerts 
					// or status updates that need to be broadcast to all users
					var handler = $.PeriodicalUpdater('${request.contextPath}/dashboard/status', 
						{ 
							method: 'get', // method; get or post 
							data: '', // array of values to be passed to the page - e.g. {name: "John", greeting: "hello"} 
							minTimeout: 5000, // starting value for the timeout in milliseconds 
							maxTimeout: 60000, // maximum length of time between requests 
							multiplier: 2, // the amount to expand the timeout by if the response hasn't changed (up to maxTimeout) 
							type: 'json', // response type - text, xml, json, etc. See $.ajax config options 
							maxCalls: 10, // maximum number of calls. 0 = no limit. 
							autoStop: 0 // automatically stop requests after this many returns of the same data. 0 = disabled. 
						}, 
						function(remoteData, success, xhr, handle) { 
							if (remoteData != '') {
								for (var i = 0; i < remoteData.length; i++) {
									$('#status').text(remoteData[i].comment);
								}	
								$('#status').addClass("notice");						
							}
						}
					);
				</g:if>
			</g:if>
			--%> 
			
			$(".warehouse-switch").click(function() {
				//$("#warehouse-menu").toggle();
				$("#warehouseMenu").dialog({ 
					autoOpen: true, 
					modal: true, 
					width: 600,
					height: 400
				});
			});
			
			
			function showActions() {
				//$(this).children(".actions").show();
			}
			
			function hideActions() { 
				$(this).children(".actions").hide();
			}

			/* This is used to remove the action menu when the */
			$(".action-menu").hoverIntent({
				sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
				interval: 5,   // number = milliseconds for onMouseOver polling interval
				over: showActions,     // function = onMouseOver callback (required)
				timeout: 100,   // number = milliseconds delay before onMouseOut
				out: hideActions       // function = onMouseOut callback (required)
			});  
			
			// Create an action button that toggles the action menu on click
			//button({ text: false, icons: {primary:'ui-icon-gear',secondary:'ui-icon-triangle-1-s'} }).
			/*
			$(".action-btn").click(function(event) {
				$(this).parent().children(".actions").toggle();
				event.preventDefault();
			});
			*/
			/*			
			$(".action-btn").button({ text: false, icons: {primary:'ui-icon-gear',secondary:'ui-icon-triangle-1-s'} });
			*/			
			$(".action-btn").click(function(event) {
				//show the menu directly over the placeholder
				var actions = $(this).parent().children(".actions");

				// Need to toggle before setting the position 
				actions.toggle();

				// Set the position for the actions menu
			    actions.position({
					my: "left top",
					at: "left bottom",				  
					of: $(this).closest(".action-btn"),
					//offset: "0 0"
					collision: "flip"
				});
				
				// To prevent the action button from POST'ing to the server
				event.preventDefault();
			});
		});
	</script>
    <script type="text/javascript">
		var monthNamesShort = [];
		<g:each in="${1..12}" var="monthNum">
			monthNamesShort[${monthNum-1}] = '<warehouse:message code="month.short.${monthNum}.label"/>';
		</g:each>
    </script>    
    
    <%-- Disable feedback widget if grails.feedback.enabled is set to false --%>   
    
	<%-- 
    <g:if test="${new Boolean(grailsApplication.config.grails.feedback.enabled?:'true') }">
		<script type="text/javascript">
		  var uvOptions = {};
		  (function() {
            $.fn.watermark = $.fn.watermark || function(text,options){} //for headless driver stub the problematic watermark
		    var uv = document.createElement('script'); uv.type = 'text/javascript'; uv.async = true;
		    uv.src = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'widget.uservoice.com/gMxKSy5iKCBPkbBzs8Q.js';
		    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(uv, s);
		  })();
		</script>    
	</g:if>
	--%>
</body>
</html>

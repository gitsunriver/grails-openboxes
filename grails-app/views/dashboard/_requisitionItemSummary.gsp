<style>
    #dataTable_filter { margin: 5px;}
    #dataTable_length { margin: 5px; }
    #dataTable_info { margin: 5px; }
    #dataTable_paginate { margin: 0px; }
</style>

<div class="box">
    <h2>

        <div class="action-menu" style="position:absolute;top:5px;right:5px">
            <button class="action-btn">
                <img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
            </button>
            <div class="actions">
                <div class="action-menu-item">
                    <g:link controller="dashboard" action="downloadFastMoversAsCsv">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="Download Fast Movers as CSV" style="vertical-align: middle" />
                        <warehouse:message code="dashboard.downloadFastMoversAsCsv.label" default="Download Fast Movers as CSV"/>
                    </g:link>

                </div>
            </div>
        </div>

        <warehouse:message code="requisitionItems.fastMovers.label" default="Fast Movers (last 30 days)"/>
        <span class="beta">Beta</span>
        <%--
        <span class="action-menu">
            <button class="action-btn">
                <img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
            </button>
            <div class="actions">
                <div class="action-menu-item">
                    <g:link controller="dashboard" action="index" class="${!params.onlyShowMine?'selected':''}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
                        Show all requisitions
                    </g:link>
                </div>
            </div>
        </span>
        --%>
    </h2>


	<div class="widget-content" style="padding:0; margin:0">
        <table id="fastMoversDataTable">
            <thead>
                <%--<th>Rank</th>--%>
                <th>Code</th>
                <th>Product</th>
                <th>Requests</th>
                <th>Demand</th>
                <th>QoH</th>
            </thead>
            <tbody>

            </tbody>
        </table>
	</div>

</div>
<script>
    $(window).load(function(){

        var dataTable = $('#fastMoversDataTable').dataTable( {
            "bProcessing": true,
            "sServerMethod": "GET",
            "iDisplayLength": 5,
            "bSearch": false,
            "bScrollCollapse": true,
            "bJQueryUI": true,
            "bAutoWidth": true,
            "sPaginationType": "two_button",
            "sAjaxSource": "${request.contextPath}/json/getFastMovers",
            "fnServerParams": function ( data ) {
                //console.log("server data " + data);
                //var locationId = $("#locationId").val();
                //var date = $("#date").val();
                //data.push({ name: "location.id", value: locationId });
                //data.push({ name: "date", value: date })
            },
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                $.ajax( {
                    "dataType": 'json',
                    "type": "GET",
                    "url": sSource,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 120000,   // optional if you want to handle timeouts (which you should)
                    "error": handleAjaxError // this sets up jQuery to give me errors
                } );
            },
//            "fnServerData": function ( sSource, aoData, fnCallback ) {
//                $.getJSON( sSource, aoData, function (json) {
//                    console.log(json);
//                    fnCallback(json);
//                });
//            },
            "oLanguage": {
                "sZeroRecords": "No records found",
                "sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner.gif' /> Loading... "
            },
            //"fnInitComplete": fnInitComplete,
            //"iDisplayLength" : -1,
            "aLengthMenu": [
                [5, 10, 25, 100, 1000, -1],
                [5, 10, 25, 100, 1000, "All"]
            ],
            "aoColumns": [

                //{ "mData": "id", "bVisible":false }, // 0
                //{ "mData": "rank", "sWidth": "1%" }, // 1
                { "mData": "productCode", "sWidth": "1%" }, // 2
                { "mData": "name" }, // 3
                { "mData": "requisitionCount", "sWidth": "5%", "sType": 'numeric' }, // 4
                { "mData": "quantityRequested", "sWidth": "5%", "sType": 'numeric' }, // 5
                { "mData": "quantityOnHand", "sWidth": "5%", "sType": 'numeric' } // 5
                //

            ],
            "bUseRendered": false,
            "aaSorting": [[ 2, "desc"],[3, "desc"]],
            "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
                //console.log(nRow);
                //console.log(aData);
                //console.log(iDisplayIndex);

                $('td:eq(1)', nRow).html('<a href="${request.contextPath}/inventoryItem/showStockCard/' + aData["id"] + '">' +
                        aData["name"] + '</a>');
                return nRow;
            }

        });


    });

    function handleAjaxError( xhr, status, error ) {
        if ( status === 'timeout' ) {
            alert( 'The server took too long to send the data.' );
        }
        else {
            // User probably refreshed page or clicked on a link, so this isn't really an error
            if(xhr.readyState == 0 || xhr.status == 0) {
                return;
            }

            if (xhr.responseText) {
                var error = eval("(" + xhr.responseText + ")");
                alert("An error occurred on the server.  Please contact your system administrator.\n\n" + error.errorMessage);
            } else {
                alert('An unknown error occurred on the server.  Please contact your system administrator.');
            }
        }
        console.log(dataTable);
        dataTable.fnProcessingDisplay( false );
    }

</script>
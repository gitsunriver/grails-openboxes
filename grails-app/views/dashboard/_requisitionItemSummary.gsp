<head>
    <link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">
    <style>
        #dataTable_filter { margin: 5px;}
        #dataTable_length { margin: 5px; }
        #dataTable_info { margin: 5px; }
        #dataTable_paginate { margin: 0px; }

    </style>
</head>

<div class="box">
    <h2>
        <warehouse:message code="requisitionItems.mostRequested.label" default="Most requested items over the past month (fast-movers)"/>
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

	<div class="widget-content" style="padding:0px;; margin:0">
        <table id="dataTable">
            <thead>
                <th>Rank</th>
                <th>Code</th>
                <th>Product</th>
                <th>Count</th>
                <th>Quantity</th>
            </thead>
            <tbody>

            </tbody>
        </table>
	</div>
    <div class="clearfix"></div>
    <br/><br/>
</div>
<script type="text/javascript" charset="utf8" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.js"></script>
<script>
    $(window).load(function(){

        var dataTable = $('#dataTable').dataTable( {
            "bProcessing": true,
            "sServerMethod": "GET",
            "iDisplayLength": 5,
            "bSearch": false,
            "bScrollCollapse": true,
            "bJQueryUI": false,
            "bAutoWidth": true,
            "sPaginationType": "two_button",
            "sAjaxSource": "${request.contextPath}/json/getMostRequestedItems",
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
                    "timeout": 30000,   // optional if you want to handle timeouts (which you should)
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
                "sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner-large.gif' /><br/>Loading..."
            },
            //"fnInitComplete": fnInitComplete,
            //"iDisplayLength" : -1,
            "aLengthMenu": [
                [5, 10, 25, 100, 1000, -1],
                [5, 10, 25, 100, 1000, "All"]
            ],
            "aoColumns": [

                //{ "mData": "id", "bVisible":false }, // 0
                { "mData": "rank", "sWidth": "1%" }, // 1
                { "mData": "productCode", "sWidth": "1%" }, // 2
                { "mData": "name" }, // 3
                { "mData": "count", "sWidth": "5%"  }, // 4
                { "mData": "quantity", "sWidth": "5%"  } // 5
                //

            ],
            "bUseRendered": false,
            "aaSorting": [[ 3, "desc" ], [4, "desc"]],
            "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
                //console.log(nRow);
                //console.log(aData);
                //console.log(iDisplayIndex);

                $('td:eq(2)', nRow).html('<a href="${request.contextPath}/inventoryItem/showStockCard/' + aData["id"] + '">' +
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
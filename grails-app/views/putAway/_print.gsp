<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <style>

        table {border-collapse: collapse; page-break-inside: auto;}
        thead {display: table-header-group;}
        tr {page-break-inside: avoid; page-break-after: auto;}
        td {vertical-align: top; }
        th { background-color: lightgrey; font-weight: bold;}
        body { font-size: 11px; }
        * {
            padding: 0;
            margin: 0;
        }
        div.header {
            display: block;
            text-align: center;
            position: running(header);
        }
        div.footer {
            display: block;
            text-align: center;
            position: running(footer);
        }

        @page {
            size: letter; /*letter landscape*/
            background: white;
            @top-center { content: element(header) }
            @bottom-center { content: element(footer) }
        }
        .small {font-size: xx-small;}
        .line{border-bottom: 1px solid black}
        .logo { width: 20px; height: 20px; }
        .canceled { text-decoration: line-through; }
        .page-start {
            -fs-page-sequence: start;
            page-break-before: avoid;
        }
        table {
            -fs-table-paginate: paginate;
            page-break-inside: avoid;
            border-collapse: collapse;
            border-spacing: 0;
            margin: 5px;

        }
        .page-content {
            page-break-before: always;
            page-break-after: avoid;

        }

        .page-header {
            page-break-before: avoid;
        }

        /* forces a page break */
        .break {page-break-after:always}

        span.page:before { content: counter(page); }
        span.pagecount:before { content: counter(pages); }
        body {
            font: 11px "lucida grande", verdana, arial, helvetica, sans-serif;
        }
        table td, table th {
            padding: 5px;
            border: 1px solid black;
        }
    </style>

</head>

<body>

    <div class="header">
        <div id="page-header" class="small">
            Putaway
        </div>
    </div>

    <div class="footer">
        <div id="page-footer" class="small">
            <div>
                Page <span class="page" /> of <span class="pagecount" />
            </div>
        </div>
    </div>

    <div class="content">
        <h1><warehouse:message code="putawayOrder.label"/></h1>
        <table>
            <tr>
                <td>
                    <img class="logo" src="${createLinkTo(dir: 'images/', file: 'hands.jpg', absolute: true)}"/>
                </td>
                <td>
                    ${jsonObject?.putawayNumber}
                </td>
            </tr>
        </table>

        <table>
            <thead>
                <tr>
                    <th>Code</th>
                    <th>Name</th>
                    <th>Lot Number</th>
                    <th>Expiration Date</th>
                    <th>Putaway Bin</th>
                    <th>Putaway Quantity</th>
                    <th>Total Quantity</th>
                </tr>
            </thead>
            <g:each var="putawayItem" in="${jsonObject.putawayItems}">
                <g:if test="${putawayItem.splitItems.empty}">
                    <tr>
                        <td>${putawayItem["product.productCode"]}</td>
                        <td>${putawayItem["product.name"]}</td>
                        <td>${putawayItem["inventoryItem.lotNumber"]}</td>
                        <td>${putawayItem["inventoryItem.expirationDate"]}</td>
                        <td>${putawayItem["putawayLocation.name"]}</td>
                        <td>${putawayItem?.quantity}</td>
                        <td>${putawayItem?.quantity}</td>

                    </tr>
                </g:if>
                <g:else>
                    <g:each var="splitItem" in="${putawayItem.splitItems}" status="status">
                        <tr>
                            <td>${status==0 ? putawayItem["product.productCode"] : ""}</td>
                            <td>${status==0 ? putawayItem["product.name"]: ""}</td>
                            <td>${status==0 ? putawayItem["inventoryItem.lotNumber"]: ""}</td>
                            <td>${status==0 ? putawayItem["inventoryItem.expirationDate"]: ""}</td>
                            <td>${splitItem["putawayLocation.name"]}</td>
                            <td>${splitItem["quantity"]?:""}</td>
                            <td>${status==0 ? putawayItem?.quantity: ""}</td>
                        </tr>
                    </g:each>

                </g:else>
            </g:each>
        </table>

        <table>
            <tr>
                <th><g:message code="putawayOrder.createdBy.label"/></th>
                <td width="50%"></td>
            </tr>
            <tr>
                <th><g:message code="putawayOrder.completedBy.label"/></th>
                <td></td>
            </tr>
            <tr>
                <th><g:message code="putawayOrder.putawayDate.label"/></th>
                <td></td>
            </tr>
        </table>


</div>

</body>
</html>

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

        @media print {
            table td, table th {

            }


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

<div style="-fs-page-sequence: start; page-break-before: avoid;">
    <div style="position: running(header);" class="header">
        <div id="page-header" class="small">
            ${requisition.requestNumber} | ${requisition?.name }
        </div>
    </div>

    <div style="position: running(footer);" class="footer">
        <div id="page-footer" class="small">
            <%--<div class="line">&#160;</div>--%>
            Page <span class="page" /> of <span class="pagecount" />
        </div>
    </div>

</div>
<div class="content">
    <h1><warehouse:message code="picklist.label"/></h1>
    <table>
        <tr>
            <td width="1%">
                <img class="logo" src="${createLinkTo(dir: 'images/', file: 'hands.jpg', absolute: true)}"/>
            </td>
            <td>
                <h2>${requisition.requestNumber} | ${requisition?.name }</h2>
                <div class="header">
                    <%--
                    <g:if test="${requisition.requestNumber}">
                        <img src="${createLink(controller: 'product', action: 'barcode', params: [data: requisition?.requestNumber, width: 100, height: 30, format: 'CODE_128'])}"/>
                    </g:if>
                    --%>
                </div>
            </td>
        </tr>
    </table>
</div>
<div>
    <h2>${warehouse.message(code:"default.details.label", default: 'Details')}</h2>

    <table class="border">
        <tr>
            <td>
                <label><warehouse:message code="requisition.depot.label"/>:</label>
            </td>
            <td>
                ${requisition.destination?.name}
            </td>
        </tr>
        <tr>
            <td>
                <label><warehouse:message code="requisition.ward.label"/>:</label>
            </td>
            <td>
                ${requisition.origin?.name}
            </td>
        </tr>

        <tr>
            <td>
                <label><warehouse:message code="requisition.date.label"/>:</label>
            </td>
            <td>
                <g:formatDate
                        date="${requisition?.dateRequested}" format="MMM d, yyyy  hh:mma"/>
            </td>
        </tr>
        <tr>
            <td>
                <label><warehouse:message code="picklist.datePrinted.label" default="Date printed"/>:</label>
            </td>
            <td>
                <g:formatDate
                        date="${new Date()}" format="MMM d, yyyy hh:mma"/>
            </td>
        </tr>

    </table>
</div>

<div>
    <h2>${warehouse.message(code:"default.signatures.label", default: 'Signatures')}</h2>
    <table>
        <tr>
            <td width="15%"></td>
            <td width="20%"><warehouse:message code="default.name.label"/></td>
            <td width="40%"><warehouse:message code="default.signature.label"/></td>
            <td width="15%" class="center"><warehouse:message code="default.date.label"/></td>
            <td width="10%" class="center"><warehouse:message code="default.time.label"/></td>
        </tr>
        <tr>
            <td>
                <label><warehouse:message code="requisition.requestedBy.label"/></label>
            </td>
            <td>
                ${requisition?.requestedBy?.name}
            </td>
            <td>

            </td>
            <td>
                <g:formatDate date="${requisition?.dateRequested}" format="MMM d, yyyy"/>
            </td>
            <td>
                <g:formatDate date="${requisition?.dateRequested}" format="hh:mma"/>
            </td>
        </tr>
        <tr>
            <td>
                <label><warehouse:message code="requisition.createdBy.label"/></label>
            </td>
            <td>
                ${requisition?.createdBy?.name}
            </td>
            <td>

            </td>
            <td>
                <g:formatDate date="${requisition?.dateCreated}" format="MMM d, yyyy"/>
            </td>
            <td>
                <g:formatDate date="${requisition?.dateCreated}" format="hh:mma"/>
            </td>
        </tr>
        <tr>
            <td>
                <label><warehouse:message code="requisition.verifiedBy.label"/></label>
            </td>
            <td>
                ${requisition?.verifiedBy?.name}
            </td>
            <td>

            </td>
            <td>
                <g:formatDate date="${requisition?.dateVerified}" format="MMM d, yyyy"/>
            </td>
            <td>
                <g:formatDate date="${requisition?.dateVerified}" format="hh:mma"/>
            </td>
        </tr>
        <tr>
            <td>
                <label><warehouse:message code="requisition.pickedBy.label"/></label>
            </td>
            <td>
                ${picklist?.picker?.name}
            </td>
            <td>

            </td>
            <td>
                <g:formatDate date="${picklist?.datePicked}" format="MMM d, yyyy"/>
            </td>
            <td>
                <g:formatDate date="${picklist?.datePicked}" format="hh:mma"/>
            </td>
        </tr>
        <tr>
            <td>
                <label><warehouse:message code="requisition.reviewedBy.label" default="Checked by"/></label>
            </td>
            <td>
                ${requisition?.checkedBy?.name}
            </td>
            <td>

            </td>
            <td>
                <g:formatDate date="${requisition?.dateChecked}" format="MMM d, yyyy"/>
            </td>
            <td>
                <g:formatDate date="${requisition?.dateChecked}" format="hh:mma"/>
            </td>
        </tr>
    </table>
</div>

<g:set var="requisitionItems" value='${requisition.requisitionItems.sort { it.product.name }}'/>
<g:set var="requisitionItemsCanceled" value='${requisitionItems.findAll { it.isCanceled()||it.isSubstituted() }}'/>
<g:set var="requisitionItems" value='${requisitionItems.findAll { !it.isCanceled()&&!it.isChanged() }}'/>
<g:set var="requisitionItemsColdChain" value='${requisitionItems.findAll { it?.product?.coldChain }}'/>
<g:set var="requisitionItemsControlled" value='${requisitionItems.findAll {it?.product?.controlledSubstance}}'/>
<g:set var="requisitionItemsHazmat" value='${requisitionItems.findAll {it?.product?.hazardousMaterial}}'/>
<g:set var="requisitionItemsOther" value='${requisitionItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

<div>
    <g:if test="${requisitionItemsColdChain}">
        <g:set var="pageTitle">
            <%--<img src="${resource(dir: 'images/icons/', file: 'coldchain.gif', absolute: true)}" title="Cold chain"/>--%>
            ${warehouse.message(code:'product.coldChain.label', default:'Cold chain')}
        </g:set>
        <g:render template="page" model="[pageTitle: pageTitle, requisitionItems:requisitionItemsColdChain, location: location, picklist:picklist, pageBreakAfter: (requisitionItemsControlled||requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsControlled}">
        <g:set var="pageTitle">
            <%--<img src="${resource(dir: 'images/icons/silk', file: 'error.png', absolute: true)}" title="Controlled substance"/>--%>
            ${warehouse.message(code:'product.controlledSubstance.label', default:'Controlled substance')}
        </g:set>
        <g:render template="page" model="[pageTitle: pageTitle,requisitionItems:requisitionItemsControlled, location: location, picklist:picklist, pageBreakAfter: (requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsHazmat}">
        <g:set var="pageTitle">
            <%--<img src="${resource(dir: 'images/icons/silk', file: 'exclamation.png', absolute: true)}" title="Hazardous material"/>--%>
            ${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous material')}
        </g:set>
        <g:render template="page" model="[pageTitle: pageTitle,requisitionItems:requisitionItemsHazmat, location: location, picklist:picklist, pageBreakAfter: (requisitionItemsOther)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsOther}">
        <g:set var="pageTitle">
            <%--<img src="${resource(dir: 'images/icons/silk', file: 'package.png', absolute: true)}" title="Other items"/>--%>
            ${warehouse.message(code:'default.otherItems.label', default:'Other items')}
        </g:set>
        <g:render template="page" model="[pageTitle: pageTitle,requisitionItems:requisitionItemsOther, location: location, picklist:picklist, pageBreakAfter: 'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsCanceled}">
        <g:set var="pageTitle">
        <%--<img src="${resource(dir: 'images/icons/silk', file: 'package.png', absolute: true)}" title="Other items"/>--%>
            ${warehouse.message(code:'default.canceled.label', default:'Canceled items')}
        </g:set>
        <g:render template="page" model="[pageTitle: pageTitle,requisitionItems:requisitionItemsCanceled, location: location, picklist:picklist, pageBreakAfter: 'avoid']"/>
    </g:if>
</div>
</body>
</html>

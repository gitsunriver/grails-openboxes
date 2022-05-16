<div class="box">
    <h2>
        <warehouse:message code="product.sources.label" default="Product Sources"/>
    </h2>

        <div class="dialog">
            <table>
                <thead>
                <%--
                    <g:sortableColumn property="id" title="${warehouse.message(code: 'productSupplier.id.label', default: 'Id')}" />

                    <th><g:message code="productSupplier.product.label" default="Product" /></th>
                --%>
                <g:sortableColumn property="code" title="${warehouse.message(code: 'productSupplier.code.label', default: 'Code')}" />

                <th><g:message code="default.name.label" default="Name" /></th>

                <th><g:message code="productSupplier.supplier.label" default="Supplier" /></th>

                <th><g:message code="productSupplier.supplierCode.label" default="Supplier Code" /></th>

                <th><g:message code="productSupplier.manufacturer.label" default="Manufacturer" /></th>

                <th><g:message code="productSupplier.manufacturerCode.label" default="Manufacturer Code" /></th>

                <th><g:message code="productSupplier.preferenceTypeCode.label" default="Preference Type" /></th>

                <th><g:message code="productSupplier.ratingTypeCode.label" default="Rating Type" /></th>

                <th><g:message code="productSupplier.unitOfMeasure.label" default="Unit of Measure" /></th>

                <th><g:message code="productSupplier.unitPrice.label" default="Unit Price" /></th>

                <th><g:message code="default.actions.label" default="Actions" /></th>


                </thead>
                <tbody>
                    <g:if test="${productInstance?.productSuppliers}">

                        <g:each var="productSupplier" in="${productInstance?.productSuppliers.sort()}" status="status">
                            <tr class="prop ${status%2==0?'odd':'even'}">

                                <%--
                                <td><g:link controller="productSupplier" action="edit" id="${productSupplier.id}">${fieldValue(bean: productSupplier, field: "id")}</g:link></td>

                                <td>
                                    <a href="javascript:void(0);" class="btn-show-dialog"
                                       data-target="#product-supplier-dialog"
                                       data-title="${g.message(code:'productSupplier.label')}"
                                       data-url="${request.contextPath}/productSupplier/dialog?id=${productSupplier?.id}&product.id=${productInstance?.id}">
                                        ${fieldValue(bean: productSupplier, field: "product")}
                                    </a>
                                </td>
                                --%>
                                <td>${fieldValue(bean: productSupplier, field: "code")?:g.message(code:'default.none.label')}</td>

                                <td>${fieldValue(bean: productSupplier, field: "name")?:g.message(code:'default.none.label')}</td>

                                <td>${fieldValue(bean: productSupplier, field: "supplier")}</td>

                                <td>${fieldValue(bean: productSupplier, field: "supplierCode")}</td>

                                <td>${fieldValue(bean: productSupplier, field: "manufacturer")}</td>

                                <td>${fieldValue(bean: productSupplier, field: "manufacturerCode")}</td>

                                <td>${fieldValue(bean: productSupplier, field: "preferenceTypeCode")}</td>

                                <td>${fieldValue(bean: productSupplier, field: "ratingTypeCode")}</td>

                                <td>${fieldValue(bean: productSupplier, field: "unitOfMeasure")}</td>

                                <td>${fieldValue(bean: productSupplier, field: "unitPrice")}</td>

                                <td>
                                    <a href="javascript:void(0);" class="btn-show-dialog button"
                                       data-target="#product-supplier-dialog"
                                       data-title="${g.message(code:'productSupplier.label')}"
                                       data-url="${request.contextPath}/productSupplier/dialog?id=${productSupplier?.id}&product.id=${productInstance?.id}">
                                        <g:message code="default.button.edit.label"/>
                                    </a>
                                    <g:link controller="productSupplier" action="edit" id="${productSupplier.id}" class="button" method="DELETE">
                                        <g:message code="default.advanced.label"/>
                                    </g:link>

                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    <g:unless test="${productInstance?.productSuppliers}">
                        <tr class="prop">
                            <td class="empty center" colspan="11">
                                <g:message code="productSuppliers.empty.label" default="There are no product suppliers"/>
                            </td>
                        </tr>
                    </g:unless>
                </tbody>
                <tfoot>
                <tr>
                    <td colspan="11">
                        <div class="center">
                            <button class="button btn-show-dialog"
                                    data-target="#product-supplier-dialog"
                                    data-title="${g.message(code: 'default.create.label', args: [g.message(code:'productSupplier.label')])}"
                                    data-url="${request.contextPath}/productSupplier/dialog?product.id=${productInstance?.id}">
                                ${g.message(code: 'default.button.create.label', default: 'Create')}
                            </button>
                        </div>
                    </td>
                </tr>
                </tfoot>
            </table>
        </div>
</div>

<div id="product-supplier-dialog" class="dialog hidden" title="Product Supplier">
    <!-- Dynamic content -->
</div>


<g:javascript>
    $(document).ready(function() {
        $(".btn-show-dialog").click(function(event) {
            var target = $(this).data("target")
            var url = $(this).data("url");
            var title = $(this).data("title");
            $(target).attr("title", title);
            $(target).dialog({
                autoOpen: true,
                modal: true,
                width: 800,
                height: 600,
                open: function(event, ui) {
                    $(this).html("Loading...")
                    $(this).load(url, function(response, status, xhr) {

                        if (status == "error") {

                            // Clear error
                            $(this).text("")
                            $("<p/>").addClass("error").text("An unexpected error has occurred: " + xhr.status + " " + xhr.statusText).appendTo($(this));

                            // If in debug mode (which we always are, at the moment) we can display the error response
                            // from the server (or javascript error in case error response is not in JSON)
                            try {
                                var error = JSON.parse(response);
                                var stack = $("<div/>").addClass("stack empty").appendTo($(this));
                                $("<pre/>").text(error.errorMessage).appendTo(stack)
                            } catch(e) {
                                console.log("exception: ", e);
                                //$("<pre/>").text(e.stack).appendTo($(this));
                                $(this).append(response);
                            }

                        }

                        //$(this).dialog('open');

                    });
                }

            });


        });

    });
</g:javascript>
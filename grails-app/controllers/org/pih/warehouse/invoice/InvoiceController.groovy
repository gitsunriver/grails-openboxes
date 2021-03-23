/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

package org.pih.warehouse.invoice

import org.pih.warehouse.core.Location

import java.text.SimpleDateFormat

class InvoiceController {
    def invoiceService

    // This template is generated by webpack during application start
    def index = {
        redirect(action: "create", params: params)
    }

    def create = {
        render(template: "/common/react", params: params)
    }

    def list = {
        params.max = params.max?:10
        params.offset = params.offset?:0
        params.createdBy = params.createdBy ?: null
        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        params.dateInvoiced = params.dateInvoiced ? dateFormat.parse(params.dateInvoiced) : null
        params.invoiceNumber = params.invoiceNumber ?: ""
        def location = Location.get(session.warehouse.id)
        def invoices = invoiceService.listInvoices(location, params)

        [
                invoices         : invoices,
        ]
    }
}

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

class InvoiceList {
    String id
    Invoice invoice

    String invoiceNumber
    String vendorInvoiceNumber

    String partyFromId
    String partyId
    String vendor // = party.code + " " + part.name

    InvoiceTypeCode invoiceTypeCode
    InvoiceStatus status

    String currency
    String itemCount

    Date   dateInvoiced
    Date   dateCreated
    String createdById
    String createdByName

    static mapping = {
        version false
        cache usage: "read-only"
    }
}

/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

// import java.text.SimpleDateFormat;
// import java.util.Date;

class QuantityTagLib {

    def showQuantity = { attrs, body ->
        def requisitionItem = attrs.requisitionItem
        if (requisitionItem) {
            if (requisitionItem?.productPackage) {
                out << "${requisitionItem?.quantity} ${requisitionItem?.productPackage?.uom?.code}/${requisitionItem?.productPackage?.quantity}"
            }
            else {
                out << "${requisitionItem?.quantity} EA/1"
            }


        }

    }

}


/*
${requisitionItem.quantity} x
<g:if test="${requisitionItem?.productPackage}">
${requisitionItem?.productPackage?.uom?.code}/${requisitionItem?.productPackage?.quantity}
</g:if>
                                                    <g:else>
                                                        EA/1
</g:else>
*/


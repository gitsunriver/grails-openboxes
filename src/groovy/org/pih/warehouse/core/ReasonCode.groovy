/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core;

/**
 * Reasons for changing request:
 * Stock-out
 * Low stock
 * Expired product
 * Clinical override
 * Replaced with formulary/stock item
 * Package size
 * Cancelled by requester
 * Other
 *
 * Reason for changing what was supposed to be picked:
 * Stock-out
 * Expired product
 * Damaged product
 * Could not locate
 * Other
 *
 */
public enum ReasonCode {

    STOCKOUT(1),
    LOW_STOCK(2),
    EXPIRED(3),
    DAMAGED(4),
    SUBSTITUTION(5),
    PACKAGE_SIZE(6),
    CLINICAL_OVERRIDE(7),
    INSUFFICIENT_CONSUMPTION(8),
    REPLACED_BY_FORMULARY_ITEM(9),
    CANCELED_BY_REQUESTER(10),
    NON_FORMULARY_NO_SUBSTITUTION(11),
    NOT_STOCKED(12),
    AVAILABLE_STOCK_RESERVED(13),
    COULD_NOT_LOCATE(14),
    DIFFERENT_LOCATION(15),
    DATA_ENTRY_ERROR(16),
    OTHER(17)


    final Integer sortOrder

    ReasonCode(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    static list() {
        [STOCKOUT, LOW_STOCK, DAMAGED, EXPIRED, INSUFFICIENT_CONSUMPTION, SUBSTITUTION,
                CLINICAL_OVERRIDE, REPLACED_BY_FORMULARY_ITEM, NOT_STOCKED, COULD_NOT_LOCATE, DIFFERENT_LOCATION,
                PACKAGE_SIZE, CANCELED_BY_REQUESTER, NON_FORMULARY_NO_SUBSTITUTION, AVAILABLE_STOCK_RESERVED, DATA_ENTRY_ERROR, OTHER]
    }

	static listRequisitionQuantityChangeReasonCodes() {
		[STOCKOUT, LOW_STOCK, DAMAGED, EXPIRED, INSUFFICIENT_CONSUMPTION,
                PACKAGE_SIZE, CANCELED_BY_REQUESTER, NON_FORMULARY_NO_SUBSTITUTION, NOT_STOCKED,AVAILABLE_STOCK_RESERVED, DATA_ENTRY_ERROR, OTHER]
	}
    static listRequisitionSubstitutionReasonCodes() {
        [SUBSTITUTION, REPLACED_BY_FORMULARY_ITEM, STOCKOUT, LOW_STOCK, DAMAGED, EXPIRED, AVAILABLE_STOCK_RESERVED, DATA_ENTRY_ERROR, OTHER]
    }

    static listPicklistQuantityChangeReasonCodes() {
        [STOCKOUT,EXPIRED,DAMAGED,COULD_NOT_LOCATE,DATA_ENTRY_ERROR,OTHER]
    }
}

/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.fulfillment

import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.requisition.RequisitionItem;
import org.pih.warehouse.shipping.ShipmentItem;

class FulfillmentItem implements Serializable {

	String id
	
	// Attributes
	Integer quantity
	InventoryItem inventoryItem
	RequisitionItem requestItem
	
	// Audit fields
	Date dateCreated
	Date lastUpdated
	
	static mapping = {
		id generator: 'uuid'
	}
	
	// Bi-directional associations
	static belongsTo = [ fulfillment : Fulfillment ]
	
	// One-to-many associations
	static hasMany = [ shipmentItems : ShipmentItem ]
		
    static constraints = {
		requestItem(nullable:true)
		inventoryItem(nullable:true)
		quantity(nullable:true)
    }
	
	Integer quantityPacked() { 
		return shipmentItems?.sum { it.quantity }
	}
}

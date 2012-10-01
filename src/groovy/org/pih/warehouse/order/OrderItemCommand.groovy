package org.pih.warehouse.order

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.codehaus.groovy.grails.validation.Validateable

@Validateable
class OrderItemCommand  implements Serializable {

	Boolean primary
	OrderItem orderItem
	Shipment shipment
	ShipmentItem shipmentItem
	
	// from order item
	String type
	String description
	Integer quantityOrdered
	
	// for shipment item
	String lotNumber
	Date expirationDate
	Product productReceived
	Integer quantityReceived	
		
	static constraints = {
		productReceived(nullable:false)
	}
	
}


package org.pih.warehouse.shipping

import java.io.Serializable;
import java.util.Date;

class ShipmentWorkflow implements Serializable {

	String name					// user-defined name of the workflow
	ShipmentType shipmentType  	// the shipment type this workflow is associated with
	
	// Audit fields
	Date dateCreated
	Date lastUpdated
	
	// one-to-many associations
	List referenceNumberTypes
	List containerTypes
	
	// Core association mappings
	static hasMany = [ referenceNumberTypes : ReferenceNumberType,
	                   containerTypes : ContainerType ]
	                   
    static constraints = {
		name(nullable:false, blank: false)
		shipmentType(nullable:false, unique:true)  // for now, we are only allowing one workflow per shipment type, though we may want to change this
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
		
		// a shipment workflow can't have two identical reference number types
		referenceNumberTypes ( validator: { referenceNumberTypes ->
        	referenceNumberTypes?.unique( { a, b -> a.id <=> b.id } )?.size() == referenceNumberTypes?.size()       
		} )
		
		// a shipment workflow can't have two identical container types
		containerTypes ( validator: { containerTypes ->
        	containerTypes?.unique( { a, b -> a.id <=> b.id } )?.size() == containerTypes?.size()       
		} )
		
    }
	
	String toString() { name }
}

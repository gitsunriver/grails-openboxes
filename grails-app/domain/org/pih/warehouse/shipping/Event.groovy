package org.pih.warehouse.shipping

import java.util.Date;
import org.pih.warehouse.core.Location;

class Event implements Comparable {
	
	Date eventDate				// The date and time on which the Event occurred
	EventType eventType			// The type of the Event
	Location eventLocation		// The Location at which the Event occurred	
	Location targetLocation		// Optionally, the Location at which the Event is targeted
	String description			// an optional description of the event
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	
	static belongsTo = [ shipment : Shipment ]
	                     
	static constraints = {
		targetLocation(nullable:true)
		description(nullable:true)		
	}

	String toString() { return "$eventType $eventLocation on $eventDate"; }
	int compareTo(obj) { eventDate.compareTo(obj.eventDate) }
	
	
}

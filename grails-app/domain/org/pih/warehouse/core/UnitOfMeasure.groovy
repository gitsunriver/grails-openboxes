package org.pih.warehouse.core

import java.util.Date;

import org.pih.warehouse.auth.AuthService;

class UnitOfMeasure {

	def beforeInsert = {
		createdBy = AuthService.currentUser.get()
	}
	def beforeUpdate ={
		updatedBy = AuthService.currentUser.get()
	}
	
	String id
	String name					// unit of measure name (cubic millimeters)
	String code					// abbreviation (e.g. mm3)
	String description			// description of unit of measure (optional)
	UnitOfMeasureClass uomClass
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	User createdBy
	User updatedBy
	
	static mapping = {
		id generator: 'uuid'
	}
	
	static constraints = { 
		name(nullable:false, maxSize:255)
		code(nullable:false, maxSize:255)
		description(nullable:true, maxSize:255)
		uomClass(nullable:true)
		
		createdBy(nullable:true)
		updatedBy(nullable:true)
	}
	
	String toString() { return name } 
}

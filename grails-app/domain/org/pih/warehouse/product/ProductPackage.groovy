/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.product

import java.util.Date;

import org.pih.warehouse.auth.AuthService;
import org.pih.warehouse.core.UnitOfMeasure;
import org.pih.warehouse.core.User;

class ProductPackage implements Serializable {

	def beforeInsert = {
		createdBy = AuthService.currentUser.get()
	}
	def beforeUpdate ={
		updatedBy = AuthService.currentUser.get()
	}

	String id
	String name				// Name of product as it appears on the package
	String description		// Description of the package
	String gtin				// Global trade identification number
	Integer quantity		// Number of units (each) in the box
	UnitOfMeasure uom		// Unit of measure of the package (e.g. box, case, etc)
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	User createdBy
	User updatedBy
	
	static belongsTo = [product : Product]

	static mapping = {
		id generator: 'uuid'
	}
	
    static constraints = {
		//name(nullable:false,unique:true)
		name(nullable:true)
		description(nullable:true)
		gtin(nullable:false)
		uom(nullable:true)
		quantity(nullable:false)
		createdBy(nullable:true)
		updatedBy(nullable:true)
    }
	
	String toString() { return name }
	
}

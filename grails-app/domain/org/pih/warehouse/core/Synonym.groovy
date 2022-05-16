/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.product.Product;

// import java.util.Date

class Synonym implements Serializable {

    def beforeInsert = {
        User.withNewSession {
            def currentUser = AuthService.currentUser.get()
            if (currentUser) {
                createdBy = currentUser
                updatedBy = currentUser
            }
        }
    }
    def beforeUpdate = {
        User.withNewSession {
            def currentUser = AuthService.currentUser.get()
            if (currentUser) {
                updatedBy = currentUser
            }
        }
    }
	
	String id
	String name
    Locale locale
	Date dateCreated;
	Date lastUpdated;
	User createdBy
	User updatedBy
	
	static belongsTo = [product:Product]
	
	static mapping = {
		id generator: 'uuid'
        // Using a join tale because we want multiple one-to-many associations, not many-to-many
        // product-synonyms, shipment-synonyms, etc
		//products joinTable: [name:'product_synonym', column: 'product_id', key: 'synonym_id']
	}
	
	//static hasMany = [products : Product]
	
	static constraints = {
		name(nullable:false, maxSize: 255)
        locale(nullable:true)
		updatedBy(nullable:true)
		createdBy(nullable:true)
	}

}

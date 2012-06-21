package org.pih.warehouse.product

import java.util.Date;

/**
 *
 */
class ProductGroup implements Comparable, Serializable {

	// Base product information
	String id
	String description	
	Category category 
	
	List products
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;

	static hasMany = [ products : Product ]
	
	static mapping = {
		id generator: 'uuid'
		products joinTable: [name:'product_group_product', column: 'product_group_id', key: 'product_id']
	}
		
	
	static constraints = {
		description(nullable:false, blank: false, maxSize: 255)
	}

	String toString() { return "$description"; }

	int compareTo(obj) {
		this.description <=> obj.description
	}

	int hashcode() {
		if (this.id != null) {
			return this.id.hashCode();
		}
		return super.hashCode();
	}

	boolean equals(Object o) {
		if (o instanceof ProductGroup) {
			ProductGroup that = (ProductGroup)o;
			return this.id == that.id;
		}
		return false;
	}
}


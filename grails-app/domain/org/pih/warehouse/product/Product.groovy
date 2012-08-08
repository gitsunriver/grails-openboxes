package org.pih.warehouse.product

import java.util.Date;
import java.util.Collection;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.auth.AuthService;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.UnitOfMeasure;
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.shipping.ShipmentItem;

/**
 * An product is an instance of a generic.  For instance,
 * the product might be Ibuprofen, but the product is Advil 200mg
 *
 * We only track products and lots in the warehouse.  Generics help us
 * report on product availability across a generic product like Ibuprofen
 * no matter what size or shape it is.
 * 
 * We will just support "1 unit" for now.  Variations of products will 
 * eventually be stored as product variants (e.g. a 200 count bottle of 
 * 20 mg tablets vs a 50 count bottle of 20 mg tablets will both be stored 
 * as 20 mg tablets).  
 */
class Product implements Comparable, Serializable {
	
	def beforeInsert = {
		createdBy = AuthService.currentUser.get()
	}
	def beforeUpdate ={
		updatedBy = AuthService.currentUser.get()
	}

	// Base product information 
	String id	
	String name;							// Specific description for the product
	String description;						// Not used at the moment
	String productCode 						// Internal product code identifier
	Boolean coldChain = Boolean.FALSE;
	
	// New fields that need to be reviewed
	String upc				// Universal product code
	String ndc				// National drug code
	String manufacturer		// Manufacturer
	String manufacturerCode // Manufacturer product (e.g. catalog code)
	String unitOfMeasure	// each, pill, bottle, box

	UnitOfMeasure defaultUom
	//Integer uomQuantity 
	
	// Associations 
	Category category;						// primary category
	List attributes = new ArrayList();		// custom attributes
	List categories = new ArrayList();		// secondary categories
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	User createdBy
	User updatedBy
	
		
	static transients = ["rootCategory", "images"];
	
	static hasMany = [ 
		categories : Category, 
		attributes : ProductAttribute, 
		tags : String, 
		documents : Document, 
		productGroups: ProductGroup, 
		packages : ProductPackage ]	
	
	static mapping = {
		id generator: 'uuid'
		categories joinTable: [name:'product_category', column: 'category_id', key: 'product_id']
		attributes joinTable: [name:'product_attribute', column: 'attribute_id', key: 'product_id']
		documents joinTable: [name:'product_document', column: 'document_id', key: 'product_id']
		productGroups joinTable: [name:'product_group_product', column: 'product_group_id', key: 'product_group_id']		
	}
		
    static constraints = {
		name(nullable:false, blank: false, maxSize: 255)
		description(nullable:true, maxSize: 255)
		productCode(nullable:true, maxSize: 255)
		unitOfMeasure(nullable:true, maxSize: 255)
		category(nullable:false)
		coldChain(nullable:true)
		
		defaultUom(nullable:true)
		upc(nullable:true, maxSize: 255)
		ndc(nullable:true, maxSize: 255)
		manufacturer(nullable:true, maxSize: 255)
		manufacturerCode(nullable:true, maxSize: 255)
		
		createdBy(nullable:true)
		updatedBy(nullable:true)

    }
	
	def getCategoriesList() {
		return LazyList.decorate(categories,
			  FactoryUtils.instantiateFactory(Category.class))
	}
	
	Category getRootCategory() { 
		Category rootCategory = new Category();
		rootCategory.categories = this.categories;
		return rootCategory;
	}
	
	Collection getImages() { 
		return documents?.findAll { it.contentType.startsWith("image") }
	}
	
	String toString() { return "$name"; }
	
	/**
	* Sort by name
	*/
	int compareTo(obj) {
		this.name <=> obj.name
	}
	
	/**
	 * Some utility methods
	 */
	
	/**
	 * Returns true if there are any transaction entries or shipment items in the system associated with this product, false otherwise
	 */
	Boolean hasAssociatedTransactionEntriesOrShipmentItems() {
		def items = InventoryItem.findAllByProduct(this)
		if (items && items.find { TransactionEntry.findByInventoryItem(it) } ) { return true }
		if (ShipmentItem.findByProduct(this)) { return true }
		return false
	}
	
	int hashcode() {
		if (this.id != null) {
			return this.id.hashCode();
		}
		return super.hashCode();
	}
	
	boolean equals(Object o) {
		if (o instanceof Product) {
			Product that = (Product)o;
			return this.id == that.id;
		}
		return false;
	}
}


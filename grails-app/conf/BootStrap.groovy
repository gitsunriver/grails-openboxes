

import grails.util.GrailsUtil;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import java.util.Date;
import javax.sql.DataSource;
import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.DataType;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.DocumentType;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Role;
import org.pih.warehouse.core.RoleType;
import org.pih.warehouse.core.User;
import org.pih.warehouse.donation.Donor;
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.inventory.TransactionType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Attribute;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductAttribute;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.ContainerType;
import org.pih.warehouse.shipping.ReferenceNumberType;
import org.pih.warehouse.shipping.ReferenceNumber;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.ShipmentType;
import org.pih.warehouse.shipping.ShipmentMethod;
import org.pih.warehouse.shipping.Shipper;
import org.pih.warehouse.shipping.ShipperService;

class BootStrap {
	
	
	DataSource dataSource;
	
	def init = { servletContext ->
		
		// ================================    Static Data    ============================================
		//
		// Use the 'demo' environment to create a database with 'static' and 'demo' data.  Then  
		// run the following: 
		//
		// 		$ grails -Dgrails.env=demo run-app	
		//
		// In another terminal, run through these commands to generate the appropriate 
		// changelog files for a new version of the data model 
		// 
		// 		$ grails db-diff-schema > grails-app/migrations/x.x.x/changelog-initial-schema.xml
		// 		$ grails db-diff-index > grails-app/migrations/x.x.x/changelog-initial-indexes.xml
		// 		$ grails db-diff-data > grails-app/migrations/x.x.x/changelog-initial-data.xml
		// 
		// Migrating existing data to the new data model is still a work in progress, but you can 
		// use the previous versions changelogs.  
		//
		if (GrailsUtil.environment == 'demo') {
			
			Category CATEGORY_MEDICINES = new Category(parentCategory: null, name: "Medicines").save();
			Category CATEGORY_SUPPLIES = new Category(parentCategory: null, name: "Supplies").save();
			Category CATEGORY_EQUIPMENT = new Category(parentCategory: null, name: "Equipment").save();
			Category CATEGORY_PERISHABLES = new Category(parentCategory: null, name: "Perishables").save();
			Category CATEGORY_OTHER = new Category(parentCategory: null, name: "Other").save();
			Category CATEGORY_MEDICAL_SUPPLIES = new Category(parentCategory: CATEGORY_SUPPLIES, name: "Medical Supplies").save();
			Category CATEGORY_HOSPITAL_SUPPLIES = new Category(parentCategory: CATEGORY_SUPPLIES, name: "Hospital and Clinic Supplies").save();
			Category CATEGORY_OFFICE_SUPPLIES = new Category(parentCategory: CATEGORY_SUPPLIES, name: "Office Supplies").save();		
			Category CATEGORY_MEDICAL_EQUIPMENT = new Category(parentCategory: CATEGORY_EQUIPMENT, name: "Medical Equipment").save();
			Category CATEGORY_SURGICAL_EQUIPMENT = new Category(parentCategory: CATEGORY_EQUIPMENT, name: "Surgical Equipment").save();
			Category CATEGORY_TECH_EQUIPMENT = new Category(parentCategory: CATEGORY_EQUIPMENT, name: "IT Equipment").save();
			Category CATEGORY_FURNITURE = new Category(parentCategory: CATEGORY_EQUIPMENT, name: "Furniture and Equipment").save();		
			Category CATEGORY_FOOD = new Category(parentCategory: CATEGORY_PERISHABLES, name: "Food").save();
			Category CATEGORY_MEDICINES_ARV = new Category(parentCategory: CATEGORY_MEDICINES, name: "ARVS").save();
			Category CATEGORY_MEDICINES_ANESTHESIA = new Category(parentCategory: CATEGORY_MEDICINES, name: "Anesteshia").save();
			Category CATEGORY_MEDICINES_CANCER = new Category(parentCategory: CATEGORY_MEDICINES, name: "Cancer").save();
			Category CATEGORY_MEDICINES_CHRONIC_CARE = new Category(parentCategory: CATEGORY_MEDICINES, name: "Chronic Care").save();
			Category CATEGORY_MEDICINES_PAIN = new Category(parentCategory: CATEGORY_MEDICINES, name: "Pain").save();
			Category CATEGORY_MEDICINES_TB = new Category(parentCategory: CATEGORY_MEDICINES, name: "TB").save();
			Category CATEGORY_MEDICINES_OTHER = new Category(parentCategory: CATEGORY_MEDICINES, name: "Other").save();
			Category CATEGORY_MED_SUPPLIES_LAB = new Category(parentCategory: CATEGORY_MEDICAL_SUPPLIES, name: "Lab").save();
			Category CATEGORY_MED_SUPPLIES_SURGICAL = new Category(parentCategory: CATEGORY_MEDICAL_SUPPLIES, name: "Surgical").save();
			Category CATEGORY_MED_SUPPLIES_XRAY = new Category(parentCategory: CATEGORY_MEDICAL_SUPPLIES, name: "X-Ray").save();
			Category CATEGORY_MED_SUPPLIES_DENTAL = new Category(parentCategory: CATEGORY_MEDICAL_SUPPLIES, name: "Dental").save();
			Category CATEGORY_MED_SUPPLIES_OTHER = new Category(parentCategory: CATEGORY_MEDICAL_SUPPLIES, name: "Other").save();		
		
			//ConditionType CONDITION_AIDS_HIV = new ConditionType(name: "HIV/AIDS").save();
			//ConditionType CONDITION_CANCER = new ConditionType(name: "Cancer").save();
			//ConditionType CONDITION_DIARRHEA = new ConditionType(name: "Diarrhea").save();
			//ConditionType CONDITION_PAIN = new ConditionType(name: "Pain").save();
			//ConditionType CONDITION_TUBERCULOSIS = new ConditionType(name: "Tuberculosis").save();

			ContainerType CONTAINER_CONTAINER = new ContainerType(name:"Container").save();
			ContainerType CONTAINER_PALLET = new ContainerType(name:"Pallet").save();
			ContainerType CONTAINER_SUITCASE = new ContainerType(name:"Suitcase").save();
			ContainerType CONTAINER_BOX = new ContainerType(name:"Box").save();
			ContainerType CONTAINER_TRUNK = new ContainerType(name:"Trunk").save();
			ContainerType CONTAINER_ITEM = new ContainerType(name:"Item").save();
			ContainerType CONTAINER_OTHER = new ContainerType(name:"Other").save();
		
			DocumentType DOCUMENT_AIRWAY_BILL = new DocumentType(name: "Airway Bill").save();
			DocumentType DOCUMENT_BILL_OF_LADING = new DocumentType(name: "Bill of Lading").save();
			DocumentType DOCUMENT_PACKING_LIST = new DocumentType(name: "Packing List").save();
			DocumentType DOCUMENT_CERTIFICATE_OF_DONATION = new DocumentType(name: "Certificate of Donation").save();
			DocumentType DOCUMENT_COMMERCIAL_INVOICE = new DocumentType(name: "Commercial Invoice").save();
			DocumentType DOCUMENT_MATERIAL_SAFETY_DATA_SHEET = new DocumentType(name: "Material Safety Data Sheet").save();
			DocumentType DOCUMENT_CERTICATE_OF_ANALYSIS = new DocumentType(name: "Certificate of Analysis").save();
			DocumentType DOCUMENT_MANIFEST = new DocumentType(name: "Manifest").save();
			DocumentType DOCUMENT_OTHER = new DocumentType(name: "Other").save();

			Donor DONOR_ABC = new Donor(name: "Donor Organization ABC", description: "").save();
			Donor DONOR_XYZ = new Donor(name: "Donor Organization XYZ", description: "").save();
			Donor DONOR_123 = new Donor(name: "Donor Organization 123", description: "").save();
				
			//ShipmentStatus SHIPMENT_STATUS_NEW = new ShipmentStatus(name:"New", color: "red", description: "Order is being processed", finalStatus:false, sortOrder: 1).save();
			//ShipmentStatus SHIPMENT_STATUS_PICKED = new ShipmentStatus(name:"Picked", description: "Items have been picked from warehouse.  Items have not not shipped yet", finalStatus:false, sortOrder: 2).save();
			//ShipmentStatus SHIPMENT_STATUS_PACKED = new ShipmentStatus(name:"Packed", description: "Items have been packed and staged.  Items have not shipped yet", finalStatus:false, sortOrder: 3).save();
			//ShipmentStatus SHIPMENT_STATUS_LOADED = new ShipmentStatus(name:"Loaded", description: "Items have been loaded onto truck.  Items have not shipped yet.", finalStatus:false, sortOrder: 4).save();
			//ShipmentStatus SHIPMENT_STATUS_READY = new ShipmentStatus(name:"Ready", color: "green", description: "Items are ready to be shipped.", finalStatus:false, sortOrder: 5).save();
			//ShipmentStatus SHIPMENT_STATUS_SHIPPED = new ShipmentStatus(name:"Shipped", color: "green", description: "Items have been shipped from the warehouse.", finalStatus:false, sortOrder: 6).save();
			//ShipmentStatus SHIPMENT_STATUS_IN_TRANSIT = new ShipmentStatus(name:"In transit", color: "green", description: "In transit to destination.", finalStatus:false, sortOrder: 7).save();
			//ShipmentStatus SHIPMENT_STATUS_IN_S = new ShipmentStatus(name:"In s", description: "Going through s inspection", finalStatus:false, sortOrder: 8).save();
			//ShipmentStatus SHIPMENT_STATUS_RETURNED = new ShipmentStatus(name:"Returned", color: "red", description: "Items returned by recipient.", finalStatus:false, sortOrder: 9).save();
			//ShipmentStatus SHIPMENT_STATUS_ARRIVED = new ShipmentStatus(name:"Arrived", color: "green", description: "Awaiting confirmation from recipient.", finalStatus:true, sortOrder: 10).save();	
			//ShipmentStatus SHIPMENT_STATUS_DELIVERED = new ShipmentStatus(name:"Delivered", color: "#AAA", description: "Received confirmation from recipient.", finalStatus:true, sortOrder: 11).save();	
			//ShipmentStatus SHIPMENT_STATUS_COMPLETED = new ShipmentStatus(name:"Completed", description: "Received confirmation from recipient.  Items safely delivered to destination", finalStatus:true, sortOrder: 12).save();
				
			//InventoryStatus IN_STOCK = new InventoryStatus()
			//InventoryStatus LOW_STOCK = new InventoryStatus();
			//InventoryStatus OUT_OF_STOCK = new InventoryStatus()
			//InventoryStatus ON_BACKORDER = new InventoryStatus()
			//InventoryStatus STOCK_AVAILABLE = new InventoryStatus();
			//InventoryStatus UNAVAILABLE = new InventoryStatus();
		
			//EventType EVENT_ORDER_RECEIVED = new EventType(name:"Order has been received", initial: true).save();
			//EventType EVENT_ORDER_PICKED = new EventType(name:"Order is being picked", pending: true).save();
			//EventType EVENT_ORDER_FULFILLED = new EventType(name:"Order has been fulfilled", completed: true).save();
		
			EventType EVENT_SHIPMENT_REQUESTED = new EventType(name: "Requested", description: "Shipment has been requested", initial: true).save();
			EventType EVENT_SHIPMENT_PACKED = new EventType(name:"Packed", description:"Shipment has been packed", pending: true).save();     	
			EventType EVENT_SHIPMENT_LOADED = new EventType(name:"Loaded", description:"Shipment has been loaded onto truck", pending: true).save();
			EventType EVENT_SHIPMENT_DEPARTED = new EventType(name:"Departed", description:"Shipment has departed origin", pending: true).save();
			EventType EVENT_SHIPMENT_ARRIVED = new EventType(name:"Arrived", description:"Shipment has arrived and is awaiting signature", pending: true).save();
			EventType EVENT_SHIPMENT_DELIVERED = new EventType(name:"Received", description:"Shipment has been received", complete: true).save();
		
			//EventType EVENT_GOODS_UNLOADED = new EventType(name:"Shipment has been unloaded", description:"Shipment has arrived", ).save();
			//EventType EVENT_GOODS_STAGED = new EventType(name:"Shipment has been staged", description:"Shipment has arrived").save();
			//EventType EVENT_GOODS_UNPACKED = new EventType(name:"Shipment has been unpacked", description:"Shipment has arrived").save();
			//EventType EVENT_GOODS_STORED = new EventType(name:"Shipment has been stored", description:"Shipment has been stored in warehouse").save();

			// Unique internal identifier, PO Number, Bill of Lading Number, or er name,      	
			ReferenceNumberType REFERENCE_PO_NUMBER = new ReferenceNumberType(name: "Purchase Order Number", description: "Purchase Order Number").save();
			ReferenceNumberType REFERENCE_ER_NAME = new ReferenceNumberType(name: "er Name", description: "er name").save();
			ReferenceNumberType REFERENCE_INTERNAL_IDENTIFIER = new ReferenceNumberType(name: "Internal Identifier", description: "Internal Identifier").save();
			ReferenceNumberType REFERENCE_BILL_OF_LADING_NUMBER = new ReferenceNumberType(name: "Bill of Lading Number", description: "Bill of Lading Number").save();
		
	//		SupplierType SUPPLIER_LOCAL = new SupplierType(name: "Local", description: "Local supplier").save();
	//		SupplierType SUPPLIER_INTERNATIONAL = new SupplierType(name: "International", description: "International supplier").save();
	//		SupplierType SUPPLIER_NATIONAL = new SupplierType(name: "National", description: "National supplier").save();
	//		SupplierType SUPPLIER_OEM = new SupplierType(name: "OEM", description: "Original equipment manufacturer").save();
	//		SupplierType SUPPLIER_OTHER = new SupplierType(name: "Other", description: "Other").save();
		
			Shipper SHIPPER_FEDEX = new Shipper(name: "FedEx", description:"", trackingFormat:"999999999999", trackingUrl:"http://www.fedex.com/Tracking?ascend_header=1&clienttype=dotcom&cntry_code=us&language=english&tracknumbers=%s", parameterName:"").save(flush:true);
			Shipper SHIPPER_UPS = new Shipper(name: "UPS", description:"", trackingFormat:"1Z9999W99999999999", trackingUrl:"http://wwwapps.ups.com/WebTracking/processInputRequest?sort_by=status&tracknums_displayed=1&TypeOfInquiryNumber=T&loc=en_US&InquiryNumber1=%s&track.x=0&track.y=0", parameterName:"").save(flush:true);
			Shipper SHIPPER_DHL = new Shipper(name: "DHL", description:"", trackingFormat:"", trackingUrl:"http://www.google.com/search?hl=en&site=&q=", parameterName:"q").save(flush:true);
			Shipper SHIPPER_USPS = new Shipper(name: "USPS", description:"", trackingFormat:"", trackingUrl:"http://www.google.com/search?hl=en&site=&q=", parameterName:"q").save(flush:true);
			Shipper SHIPPER_COURIER = new Shipper(name: "Courier", description:"",  trackingFormat:"", trackingUrl:"http://www.google.com/search?hl=en&site=&q=", parameterName:"q").save(flush:true);
		
			ShipperService SHIPPER_SERVICE_FEDEX_AIR = new ShipperService(name: "Same Day Air", description: "Same Day Delivery").save(flush:true);
			ShipperService SHIPPER_SERVICE_FEDEX_FREIGHT = new ShipperService(name: "Express Freight", description: "Next Day Delivery").save(flush:true);
			ShipperService SHIPPER_SERVICE_FEDEX_GROUND = new ShipperService(name: "Ground", description: "3-5 Business Days").save(flush:true);
			ShipperService SHIPPER_SERVICE_UPS_AIR = new ShipperService(name: "Same Day Air", description: "Same Day Delivery").save(flush:true);
			ShipperService SHIPPER_SERVICE_UPS_FREIGHT = new ShipperService(name: "Express Freight", description: "Next Day Delivery").save(flush:true);
			ShipperService SHIPPER_SERVICE_UPS_GROUND = new ShipperService(name: "Ground", description: "3-5 Business Days").save(flush:true);
			ShipperService SHIPPER_SERVICE_DHL_AIR = new ShipperService(name: "Same Day Air", description: "Same Day Delivery").save(flush:true);
			ShipperService SHIPPER_SERVICE_DHL_FREIGHT = new ShipperService(name: "Express Freight", description: "Next Day Delivery").save(flush:true);
			ShipperService SHIPPER_SERVICE_DHL_GROUND = new ShipperService(name: "Ground", description: "3-5 Business Days").save(flush:true);
			ShipperService SHIPPER_SERVICE_USPS_GROUND = new ShipperService(name: "Ground", description: "3-5 Business Days").save(flush:true);
			ShipperService SHIPPER_SERVICE_COURIER_AIR = new ShipperService(name: "International Flight", description: "").save(flush:true);

			SHIPPER_FEDEX.addToShipperServices(SHIPPER_SERVICE_FEDEX_AIR);
			SHIPPER_FEDEX.addToShipperServices(SHIPPER_SERVICE_FEDEX_FREIGHT);
			SHIPPER_FEDEX.addToShipperServices(SHIPPER_SERVICE_FEDEX_GROUND);
			SHIPPER_UPS.addToShipperServices(SHIPPER_SERVICE_UPS_AIR);
			SHIPPER_UPS.addToShipperServices(SHIPPER_SERVICE_UPS_FREIGHT);
			SHIPPER_UPS.addToShipperServices(SHIPPER_SERVICE_UPS_GROUND);
			SHIPPER_DHL.addToShipperServices(SHIPPER_SERVICE_DHL_AIR);
			SHIPPER_DHL.addToShipperServices(SHIPPER_SERVICE_DHL_FREIGHT);
			SHIPPER_DHL.addToShipperServices(SHIPPER_SERVICE_DHL_GROUND);
			SHIPPER_USPS.addToShipperServices(SHIPPER_SERVICE_USPS_GROUND);
			SHIPPER_COURIER.addToShipperServices(SHIPPER_SERVICE_COURIER_AIR);
		
			ShipmentType SHIPMENT_TYPE_AIR = new ShipmentType(name: "Air", sortOrder: 1).save(flush:true);
			ShipmentType SHIPMENT_TYPE_SEA = new ShipmentType(name: "Sea", sortOrder: 2).save(flush:true);
			ShipmentType SHIPMENT_TYPE_DOMESTIC = new ShipmentType(name: "Domestic", sortOrder: 3).save(flush:true);
			ShipmentType SHIPMENT_TYPE_SUITCASE = new ShipmentType(name: "Suitcase", sortOrder: 4).save(flush:true);
			ShipmentType SHIPMENT_TYPE_OTHER = new ShipmentType(name: "Other", sortOrder: 5).save(flush:true);		
			SHIPMENT_TYPE_AIR.addToContainerTypes(CONTAINER_BOX).save();
			SHIPMENT_TYPE_AIR.addToContainerTypes(CONTAINER_PALLET).save();
			SHIPMENT_TYPE_AIR.addToContainerTypes(CONTAINER_SUITCASE).save();
			SHIPMENT_TYPE_AIR.addToContainerTypes(CONTAINER_OTHER).save();		
			SHIPMENT_TYPE_DOMESTIC.addToContainerTypes(CONTAINER_BOX).save();
			SHIPMENT_TYPE_DOMESTIC.addToContainerTypes(CONTAINER_PALLET).save();
			SHIPMENT_TYPE_DOMESTIC.addToContainerTypes(CONTAINER_SUITCASE).save();
			SHIPMENT_TYPE_DOMESTIC.addToContainerTypes(CONTAINER_TRUNK).save();
			SHIPMENT_TYPE_DOMESTIC.addToContainerTypes(CONTAINER_OTHER).save();
			SHIPMENT_TYPE_SEA.addToContainerTypes(CONTAINER_BOX).save();
			SHIPMENT_TYPE_SEA.addToContainerTypes(CONTAINER_CONTAINER).save();		
			SHIPMENT_TYPE_SUITCASE.addToContainerTypes(CONTAINER_ITEM).save();
		
			TransactionType TRANSACTION_INCOMING = new TransactionType(name:"Incoming").save(flush:true, validate:true);		
			TransactionType TRANSACTION_OUTGOING = new TransactionType(name:"Outgoing").save(flush:true, validate:true);		
			TransactionType TRANSACTION_DONATION = new TransactionType(name:"Donation").save(flush:true, validate:true);
		

		

		
			// ================================    Demo data    ============================================
		
			User admin = new User(username:"admin", password: "password", email:"admin@pih.org", firstName:"Miss", lastName:"Administrator", active: true).save();
			User manager = new User(username:"manager", password: "password", email:"manager@pih.org", firstName:"Mister", lastName:"Manager", manager: admin, active: true).save();
			User jmiranda = new User(username:"jmiranda", password: "password", email:"jmiranda@pih.org", firstName:"Justin", lastName:"Miranda", manager: manager, active: true).save();
			User inactive = new User(username:"inactive", password: "password", email:"inactive@pih.org", firstName:"In", lastName:"Active", manager: manager, active: false).save();
		
			Role ROLE_ADMIN = new Role(roleType: RoleType.ROLE_ADMIN);	
			Role ROLE_MANAGER = new Role(roleType: RoleType.ROLE_MANAGER);	
			Role ROLE_USER = new Role(roleType: RoleType.ROLE_USER);	

			jmiranda.addToRoles(ROLE_USER);
			manager.addToRoles(ROLE_MANAGER);
			admin.addToRoles(ROLE_ADMIN);
			inactive.addToRoles(ROLE_USER);
			
			Address address1 = new Address(address: "888 Commonwealth Avenue",address2: "Third Floor",city:"Boston",stateOrProvince:"MA",postalCode: "02215",country: "United States").save(flush:true)
			Address address2 = new Address(address: "1000 State Street",address2: "Building A",city: "Miami",stateOrProvince: "FL",postalCode: "33126",country: "United States").save(flush:true);
			Address address3 = new Address(address: "12345 Main Street", address2: "Suite 401", city: "Tabarre", stateOrProvince: "", postalCode: "", country: "Haiti").save(flush:true);
			Address address4 = new Address(address: "2482 Massachusetts Ave", address2: "", city: "Boston", stateOrProvince: "MA", postalCode: "02215", country: "United Status").save(flush:true);

			Location boston = new Location(name: "Boston Headquarters", address: address1, manager: manager, logoUrl: "http://a3.twimg.com/profile_images/134665083/BOS_Red_Sox_normal.PNG").save(flush:true);
			Location miami = new Location(name: "Miami Location", address: address2, manager: manager, logoUrl: "http://pihemr.files.wordpress.com/2008/01/pih-hands.jpg").save(flush:true);
			Location tabarre = new Location(name: "Tabarre Depot", address: address3, manager: manager, logoUrl: "http://pihemr.files.wordpress.com/2008/01/pih-hands.jpg").save(flush:true);
			Location acme = new Location(name: "ACME Supply Company", address: address4, manager: manager, logoUrl: "http://pihemr.files.wordpress.com/2008/01/pih-hands.jpg").save(flush:true);
		
			//Inventory tabarreInventory = new Inventory(warehouse:tabarre, lastInventoryDate: new Date()).save(flush:true);		
			//InventoryItem inventoryItem1 = new InventoryItem(product: advil, quantity: 100, reorderQuantity: 50, idealQuantity: 100, binLocation: "Location Bin A1").save(flush:true);
			//InventoryItem inventoryItem2 = new InventoryItem(product: tylenol, quantity: 200, reorderQuantity: 50, idealQuantity: 100, binLocation: "Location Bin A1").save(flush:true);
			//tabarreInventory.addToInventoryItems(inventoryItem1).save(flush:true, validate:false);
			//tabarreInventory.addToInventoryItems(inventoryItem2).save(flush:true, validate:false);
		
			//Transaction transaction1 = new Transaction(transactionDate:new Date(), targetLocation:tabarre, transactionType:TRANSACTION_INCOMING); // removed .save(flush:true);
			//TransactionEntry transactionEntry1 = new TransactionEntry(product: advil, quantityChange:50, confirmDate:new Date());
			//tabarre.addToTransactions(transaction1).save();
			//transaction1.addToTransactionEntries(transactionEntry1).save(flush:true, validate:false);
		
			/*
			ShipmentMethod SHIPMENT_METHOD_UPS_GROUND = new ShipmentMethod(shipperService: SHIPPER_SERVICE_UPS_GROUND, trackingNumber: "").save(flush:true);
			ShipmentMethod SHIPMENT_METHOD_UPS_AIR = new ShipmentMethod(shipperService: SHIPPER_SERVICE_UPS_AIR, trackingNumber: "").save(flush:true);
			ShipmentMethod SHIPMENT_METHOD_FEDEX_AIR = new ShipmentMethod(shipperService: SHIPPER_SERVICE_DHL_AIR, trackingNumber: "").save(flush:true);

			Shipment shipment1 = new Shipment(name: "Sample Shipment 1", 		
				origin : boston,
				destination : tabarre,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_UPS_AIR, 
				trackingNumber: "1Z9999W99999999999",
				expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-01"), 
				expectedDeliveryDate : Date.parse("yyyy-MM-dd", "2010-06-05")).save(flush:true);	

			Shipment shipment2 = new Shipment(name: "Sample Shipment 2",
				origin : boston,
				destination : miami,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_UPS_AIR, 
				trackingNumber: "1Z9999W99999999999",
				expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-02"), 
				expectedDeliveryDate : Date.parse("yyyy-MM-dd", "2010-06-03")).save(flush:true);

			Shipment shipment3 = new Shipment(name: "Sample Shipment 3",
				origin : boston,
				destination : tabarre,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_UPS_AIR, 
				trackingNumber: "1Z9999W99999999999",
				expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-01"), 
				expectedDeliveryDate : Date.parse("yyyy-MM-dd", "2010-06-05")).save(flush:true);

			Shipment shipment4 = new Shipment(name: "Sample Shipment 4",
				origin : miami,
				destination : tabarre,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_UPS_AIR, 
				trackingNumber: "1Z9999W99999999999",
				expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-01"), 
				expectedDeliveryDate : Date.parse("yyyy-MM-dd", "2010-06-05")).save(flush:true);

			Shipment shipment5 = new Shipment(name: "Sample Shipment 5", 				
				origin : miami,
				destination : tabarre,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_UPS_AIR, 
				trackingNumber: "1Z9999W99999999999",
				expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-05"), 
				expectedDeliveryDate : Date.parse("yyyy-MM-dd", "2010-06-07")).save(flush:true);	
		
			Shipment shipment6 = new Shipment(name: "Sample Shipment 6", 				
				origin : miami, 
				destination : boston,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_UPS_AIR, 
				trackingNumber: "1Z9999W99999999999", 
				expectedShippingDate : new Date(), 
				expectedDeliveryDate : null).save(flush:true);	

			Shipment shipment7 = new Shipment(name: "Sample Shipment 7",
				origin : boston,
				destination : tabarre,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_UPS_AIR, 
				trackingNumber: "1Z9999W99999999999",
				expectedShippingDate : new Date(), 
				expectedDeliveryDate : null).save(flush:true);

			Shipment shipment8 = new Shipment(name: "Sample Shipment 8", 
				origin : miami,
				destination : boston,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_UPS_AIR, 
				trackingNumber: "1Z9999W99999999999",
				expectedShippingDate : new Date(), 
				expectedDeliveryDate : null).save(flush:true);

			Shipment shipment9 = new Shipment(name: "Sample Shipment 9",
				origin : miami,
				destination : boston,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_FEDEX_AIR, 
				trackingNumber: "1Z9999W99999999999",
				expectedShippingDate : new Date(), 
				expectedDeliveryDate : null).save(flush:true);

			Shipment shipment10 = new Shipment(name: "Sample Shipment 10",
				origin : miami,
				destination : boston,
				shipmentType: SHIPMENT_TYPE_AIR,
				shipmentMethod: SHIPMENT_METHOD_UPS_AIR, 
				trackingNumber: "1Z9999W99999999999",
				expectedShippingDate : new Date(), 
				expectedDeliveryDate : null).save(flush:true);
		
			Comment comment1 = new Comment(comment: "We need to ship this as soon as possible!", sender: jmiranda, recipient: jmiranda)
			Comment comment2 = new Comment(comment: "Did you ship this yet?!?!?!?", sender: manager, recipient: jmiranda)
			Comment comment3 = new Comment(comment: "What is taking so long?", sender: admin, recipient: jmiranda)

			Event event1 = new Event(eventType:EVENT_SHIPMENT_REQUESTED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-25 15:30:00"), eventLocation: boston)
			Event event2 = new Event(eventType:EVENT_SHIPMENT_PACKED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-25 17:45:00"), eventLocation: boston)
			Event event3 = new Event(eventType:EVENT_SHIPMENT_LOADED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-26 09:00:00"), eventLocation: boston)
			Event event4 = new Event(eventType:EVENT_SHIPMENT_DEPARTED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-26 11:00:00"), eventLocation: boston)
			Event event5 = new Event(eventType:EVENT_SHIPMENT_ARRIVED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-27 12:30:00"), eventLocation: miami)
			Event event6 = new Event(eventType:EVENT_SHIPMENT_DEPARTED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-27 13:30:00"), eventLocation: miami)
			Event event7 = new Event(eventType:EVENT_SHIPMENT_ARRIVED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-28 09:30:00"), eventLocation: tabarre)
			Event event8 = new Event(eventType:EVENT_SHIPMENT_DELIVERED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-28 10:30:00"), eventLocation: tabarre)

			Document document1 = new Document(filename: "packing-list.pdf", documentType: DOCUMENT_PACKING_LIST, fileContents: "This page intentionally left blank.", contentType: "text/plain")		
			Document document2 = new Document(filename: "invoice.pdf", documentType: DOCUMENT_COMMERCIAL_INVOICE, fileContents: "This page intentionally left blank.", contentType: "text/plain") 

			Container pallet1 = new Container(name: "1", containerType: CONTAINER_PALLET, weight: 1000, units: "kg");
			Container pallet2 = new Container(name: "2", containerType: CONTAINER_PALLET, weight: 2000, units: "kg");
			Container box1 = new Container(name: "3", containerType: CONTAINER_BOX, weight: 100, units: "kg");
			Container box2 = new Container(name: "4", containerType: CONTAINER_BOX, weight: 200, units: "kg");

			ShipmentItem shipmentItem1 = new ShipmentItem(product : advil, quantity : 100);
			ShipmentItem shipmentItem2 = new ShipmentItem(product : tylenol, quantity : 200);
			ShipmentItem shipmentItem3 = new ShipmentItem(product : aspirin, quantity : 300);

			shipment1.addToComments(comment1).save(flush:true);
			shipment1.addToComments(comment2).save(flush:true);
			shipment1.addToComments(comment3).save(flush:true);		
			shipment1.addToContainers(pallet1).save(flush:true);
			shipment1.addToContainers(pallet2).save(flush:true);
			shipment1.addToContainers(box1).save(flush:true);
			shipment1.addToContainers(box2).save(flush:true);
			shipment1.addToDocuments(document1).save(flush:true);
			shipment1.addToDocuments(document2).save(flush:true);
			shipment1.addToEvents(event1).save(flush:true);
			shipment1.addToEvents(event2).save(flush:true);
			shipment1.addToEvents(event3).save(flush:true);
			shipment1.addToEvents(event4).save(flush:true);
			shipment1.addToEvents(event5).save(flush:true);		
			shipment1.addToEvents(event6).save(flush:true);		
			shipment1.addToEvents(event7).save(flush:true);		
			shipment1.addToEvents(event8).save(flush:true);		
			//shipment1.addToReferenceNumbers(new ReferenceNumber(identifier:"0000000001", referenceNumberType:REFERENCE_INTERNAL_IDENTIFIER)).save(flush:true)
			//shipment1.addToReferenceNumbers(new ReferenceNumber(identifier:"0002492910", referenceNumberType:REFERENCE_PO_NUMBER)).save(flush:true)
			pallet1.addToShipmentItems(shipmentItem1).save(flush:true);
			pallet1.addToShipmentItems(shipmentItem2).save(flush:true);
			pallet1.addToShipmentItems(shipmentItem3).save(flush:true);
			*/
		}
		
		if (GrailsUtil.environment == 'test') {
			log.info("\t\tRunning liquibase changelog(s) ...")
			Liquibase liquibase = null
			try {
				def c = dataSource.getConnection()
				if (c == null) {
					throw new RuntimeException("Connection could not be created.");
				}
				//LiquibaseUtil.class.getClassLoader();
				def classLoader = getClass().classLoader;
				def fileOpener = classLoader.loadClass("org.liquibase.grails.GrailsFileOpener").getConstructor().newInstance()

				//def fileOpener = new ClassLoaderFileOpener()
				def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(c)
				log.info("\t\tSetting default schema to " + c.catalog)
				database.setDefaultSchemaName(c.catalog)
				liquibase = new Liquibase("changelog.xml", fileOpener, database);
				liquibase.update(null)
			}
			finally {
				if (liquibase && liquibase.database) {
					liquibase.database.close()
				}
			}
			log.info("\t\tFinished running liquibase changelog(s)!")
		}
		
				
		def destroy = {
			
		}
		
	}
	
}

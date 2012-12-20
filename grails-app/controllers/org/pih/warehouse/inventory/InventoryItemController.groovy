/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.inventory;


import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;


import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.ShipmentItemException;


import grails.converters.*
import grails.validation.ValidationException;

class InventoryItemController {

	def inventoryService;
	def shipmentService;
	def requisitionService;
	def orderService;
	
	/**
	 * 
	 */
	def show = {
		def itemInstance = InventoryItem.get(params.id)
		def transactionEntryList = TransactionEntry.findAllByInventoryItem(itemInstance)
		[
			itemInstance : itemInstance,
			transactionEntryList : transactionEntryList
		]
	}
	
	/**
	 * Ajax method for the Record Inventory page.
	 */
	def getInventoryItems = { 
		log.info params
		def productInstance = Product.get(params?.product?.id);
		def inventoryItemList = inventoryService.getInventoryItemsByProduct(productInstance)
		render inventoryItemList as JSON;
	}
	
	/**
	 * Displays the stock card for a product
	 */
	def showStockCard = { StockCardCommand cmd ->
		// add the current warehouse to the command object
		cmd.warehouseInstance = Location.get(session?.warehouse?.id)
		
		// now populate the rest of the commmand object
		def commandInstance = inventoryService.getStockCardCommand(cmd, params)
		
		// populate the pending shipments
		// TODO: move this into the service layer after we find a way to add shipping service to inventory service
		// (that is, find a workaround to GRAILS-5080)
		//	shipmentService.getPendingShipmentsWithProduct(commandInstance.warehouseInstance, commandInstance?.productInstance)
		commandInstance.pendingShipmentList = 
			shipmentService.getPendingShipments(commandInstance.warehouseInstance);

		def shipmentItems = 
			shipmentService.getPendingShipmentItemsWithProduct(commandInstance.warehouseInstance, commandInstance?.productInstance)
		
		def shipmentMap = shipmentItems.groupBy { it.shipment } 
		if (shipmentMap) { 
			shipmentMap.keySet().each { 
				def quantity = shipmentMap[it].sum() { it.quantity } 
				shipmentMap.put(it, quantity)
			}
		}	
		
		def orderItems = 
			orderService.getPendingOrderItemsWithProduct(commandInstance.warehouseInstance, commandInstance?.productInstance);
		def orderMap = orderItems.groupBy { it.order }
		if (orderMap) {
			orderMap.keySet().each {
				def quantity = orderMap[it].sum() { it.quantity }
				orderMap.put(it, quantity)
			}
		}
		
		//def requestItems =
		//	requisitionService.getPendingRequisitionItemsWithProduct(commandInstance.warehouseInstance, commandInstance?.productInstance)
		def requestMap = [:]//requestItems.groupBy { it.requisition }
//		if (requestMap) {
//			requestMap.keySet().each {
//				def quantity = requestMap[it].sum() { it.quantity }
//				requestMap.put(it, quantity)
//			}
//		}
	
		session.lastProduct = commandInstance.productInstance
			
			
		[ commandInstance: commandInstance, orderMap: orderMap, shipmentMap: shipmentMap, requestMap: requestMap ]
	}

	/**
	 * Displays the stock card for a product
	 */
	def showLotNumbers = { StockCardCommand cmd ->
		// add the current warehouse to the command object
		cmd.warehouseInstance = Location.get(session?.warehouse?.id)

		// now populate the rest of the commmand object
		def commandInstance = inventoryService.getStockCardCommand(cmd, params)

		def inventoryItem = InventoryItem.get(params?.inventoryItem?.id)
		def transactionEntries = inventoryItem ? TransactionEntry.findAllByInventoryItem(inventoryItem) : []

		[ commandInstance: commandInstance, inventoryItem: inventoryItem, transactionEntries : transactionEntries  ]
	}

	/**
	 * Displays the stock card for a product
	 */
	def showTransactionLog = { StockCardCommand cmd ->
		// add the current warehouse to the command object
		cmd.warehouseInstance = Location.get(session?.warehouse?.id)

		// now populate the rest of the commmand object
		def commandInstance = inventoryService.getStockCardCommand(cmd, params)

		[ commandInstance: commandInstance ]
	}

		
	/**
	* Displays the stock card for a product
	*/
   def showGraph = { StockCardCommand cmd ->
	   // add the current warehouse to the command object
	   cmd.warehouseInstance = Location.get(session?.warehouse?.id)

	   // now populate the rest of the commmand object
	   def commandInstance = inventoryService.getStockCardCommand(cmd, params)

	   log.info ("Inventory item list: " + commandInstance?.inventoryItemList)
	   [ commandInstance: commandInstance  ]
   }
	
	/**
	 * Display the Record Inventory form for the product 
	 */
	def showRecordInventory = { RecordInventoryCommand commandInstance -> 
		
		// We need to set the inventory instance in order to save an 'inventory' transaction
		if (!commandInstance.inventoryInstance) { 
			def warehouseInstance = Location.get(session?.warehouse?.id)				
			commandInstance.inventoryInstance = warehouseInstance?.inventory;		
		}
		inventoryService.populateRecordInventoryCommand(commandInstance, params)
		
		Product productInstance = commandInstance.productInstance;
		def transactionEntryList = inventoryService.getTransactionEntriesByInventoryAndProduct(commandInstance?.inventoryInstance, [productInstance]);
		
		// Get the inventory warning level for the given product and inventory 
		commandInstance.inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, commandInstance?.inventoryInstance);
		
		// Compute the total quantity for the given product
		commandInstance.totalQuantity = inventoryService.getQuantityByProductMap(transactionEntryList)[productInstance] ?: 0

        Map<Product, List<InventoryItem>> inventoryItems = inventoryService.getInventoryItemsWithQuantity([productInstance], commandInstance.inventoryInstance)
        def result = []
        inventoryItems.keySet().each { product ->
            result = inventoryItems[product].collect { ((InventoryItem)it).toJson() }
        }
        String jsonString = [product: productInstance.toJson(), inventoryItems: result] as JSON
        println jsonString

		[ commandInstance : commandInstance, product : jsonString]
	}
	
	def saveRecordInventory = { RecordInventoryCommand cmd ->
		log.info ("Before saving record inventory " + params)
		inventoryService.saveRecordInventoryCommand(cmd, params)
		if (!cmd.hasErrors()) { 
			log.info ("No errors, show stock card")				
			redirect(action: "showStockCard", params: ['product.id':cmd.productInstance.id])
			return;
		}
			
		log.info ("User chose to validate or there are errors")
		
		//chain(action: "recordInventory", model: [commandInstance:cmd])
		def warehouseInstance = Location.get(session?.warehouse?.id)

		cmd.inventoryInstance = warehouseInstance?.inventory;
		cmd.inventoryLevelInstance = InventoryLevel.findByProductAndInventory(cmd?.productInstance, cmd?.inventoryInstance);

		// Get the inventory warning level for the given product and inventory
		cmd.inventoryLevelInstance = InventoryLevel.findByProductAndInventory(cmd?.productInstance, cmd?.inventoryInstance);
		def transactionEntryList = inventoryService.getTransactionEntriesByInventoryAndProduct(cmd?.inventoryInstance, [cmd?.productInstance]);
		
		def totalQuantity = inventoryService.getQuantityByProductMap(transactionEntryList)[cmd?.productInstance] ?: 0

		render(view: "showRecordInventory", model: [ commandInstance : cmd ])
	}

	def showTransactions = {
		
		def warehouseInstance = Location.get(session?.warehouse?.id)
		def productInstance = Product.get(params?.product?.id)
		def inventoryInstance = warehouseInstance.inventory
		def inventoryItemList = inventoryService.getInventoryItemsByProductAndInventory(productInstance, inventoryInstance)
		def transactionEntryList = TransactionEntry.findAllByProductAndInventory(productInstance, inventoryInstance)
		def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, inventoryInstance);
		
		[ 	inventoryInstance: inventoryInstance,
			inventoryLevelInstance: inventoryLevelInstance,
			productInstance: productInstance,
			inventoryItemList: inventoryItemList,
			transactionEntryList: transactionEntryList,
			transactionEntryMap: transactionEntryList.groupBy { it.transaction } ]
	}
	
			
	def createInventoryItem = {
		
		flash.message = "${warehouse.message(code: 'inventoryItem.temporaryCreateInventoryItem.message')}"
		
		def productInstance = Product.get(params?.product?.id)
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		def itemInstance = new InventoryItem(product: productInstance)
		def inventoryLevelInstance = inventoryService.getInventoryLevelByProductAndInventory(productInstance, inventoryInstance)
		def inventoryItems = inventoryService.getInventoryItemsByProduct(productInstance);
		[itemInstance: itemInstance, inventoryInstance: inventoryInstance, inventoryItems: inventoryItems, inventoryLevelInstance: inventoryLevelInstance, totalQuantity: totalQuantity]
	}

	def saveInventoryItem = {
		log.info "save inventory item " + params
		def productInstance = Product.get(params.product.id)
		def inventoryInstance = Inventory.get(params.inventory.id)
		def inventoryItem = new InventoryItem(params)
		def inventoryItems = inventoryService.getInventoryItemsByProduct(inventoryItem.product);
		inventoryInstance.properties = params;		

		def transactionInstance = new Transaction(params);
		def transactionEntry = new TransactionEntry(params);
		if (!transactionEntry.quantity) {
			transactionEntry.errors.rejectValue("quantity", 'transactionEntry.quantity.invalid')
		}
		
		if (transactionEntry.hasErrors()) { 
			inventoryItem.errors = transactionEntry.errors
		}
		if (transactionInstance.hasErrors()) {
			inventoryItem.errors = transactionInstance.errors
		}
		
		
				
		// TODO Move all of this logic into the service layer in order to take advantage of Hibernate/Spring transactions
		if (!inventoryItem.hasErrors() && inventoryItem.save()) { 
			//flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), inventoryItem.id])}"
			//redirect(controller: "inventoryItem", action: "showStockCard", id: inventoryItem.product.id);

			// Need to create a transaction if we want the inventory item 
			// to show up in the stock card			
			transactionInstance.transactionDate = new Date();
			transactionInstance.transactionDate.clearTime(); // we only want to store the date component
			transactionInstance.transactionType = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID);
			def warehouseInstance = Location.get(session.warehouse.id);
			transactionInstance.source = warehouseInstance;
			transactionInstance.inventory = warehouseInstance.inventory;
			
			transactionEntry.inventoryItem = inventoryItem;
			//transactionEntry.quantity = params.quantity;
			transactionInstance.addToTransactionEntries(transactionEntry);
			
			transactionInstance.save()
			flash.message = "${warehouse.message(code: 'inventoryItem.savedItemWithinNewTransaction.message', args: [inventoryItem.id ,  transactionInstance.id])}"
	
		} else { 	
			render(view: "createInventoryItem", model: [itemInstance: inventoryItem, inventoryInstance: inventoryInstance, inventoryItems: inventoryItems])
			return;
		}
		
		 
		// If all else fails, return to the show stock card page
		redirect(action: 'showStockCard', id: productInstance?.id)
	}
	


	
	
	def edit = {
		def itemInstance = InventoryItem.get(params.id)
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		if (!itemInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
			redirect(action: "show", id: itemInstance.id)
		}
		else {
			return [itemInstance: itemInstance]
		}
	}
	
	def editInventoryLevel = {
		
		def productInstance = Product.get(params?.product?.id)
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		if (!inventoryInstance) { 
			def warehouse = Location.get(session?.warehouse?.id);
			inventoryInstance = warehouse.inventory
		}
		
		def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, inventoryInstance)
		if (!inventoryLevelInstance) { 
			inventoryLevelInstance = new InventoryLevel();
		}
		
		[productInstance:productInstance, inventoryInstance:inventoryInstance, inventoryLevelInstance:inventoryLevelInstance]
	}

	def updateInventoryLevel = { 
		
		log.info ("update inventory level " + params)
		
		def productInstance = Product.get(params?.product?.id)
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		def inventoryLevelInstance = InventoryLevel.get(params.id)

		if (inventoryLevelInstance) { 
			inventoryLevelInstance.properties = params
		}
		else { 
			inventoryLevelInstance = new InventoryLevel(params)
		}
		
		if (!inventoryLevelInstance.hasErrors() && inventoryLevelInstance.save()) { 
			log.info ("save inventory level ")
			flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'Inventory level')])}"
		}
		else { 
			log.info ("render with errors")
			render(view: "updateInventoryLevel", model: 
				[productInstance:productInstance, inventoryInstance:inventoryInstance, inventoryLevelInstance:inventoryLevelInstance]);
			return;
		}
		
		redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance?.id)
	}
	
	
	/**
	 * Handles form submission from Show Stock Card > Adjust Stock dialog.	
	 */
	def adjustStock = {
		log.info "Params " + params;
		def itemInstance = InventoryItem.get(params.id)
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		if (itemInstance) {
			boolean hasErrors = inventoryService.adjustStock(itemInstance, params);
			if (!itemInstance.hasErrors() && !hasErrors) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), itemInstance.id])}"
			}
			else {
				// There were errors, so we want to display the itemInstance.errors to the user
				flash.itemInstance = itemInstance;
			}
		}
		redirect(controller: "inventoryItem", action: "showStockCard", id: itemInstance?.product?.id, params: ['inventoryItem.id':itemInstance?.id])
	}
	
	def update = {		
		
		log.info "Params " + params;
		def itemInstance = InventoryItem.get(params.id)
		def productInstance = Product.get(params?.product?.id)
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		if (itemInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (itemInstance.version > version) {
					itemInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'inventoryItem.label', default: 'Inventory Item')] as Object[], "Another user has updated this inventory item while you were editing")
					//render(view: "show", model: [itemInstance: itemInstance])
					redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance?.id)
					return
				}
			}
			itemInstance.properties = params
			
			// FIXME Temporary hack to handle a changed values for these two fields
			itemInstance.lotNumber = params?.lotNumber
			
			if (!itemInstance.hasErrors() && itemInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), itemInstance.id])}"
			}
			else {
				flash.message = "${warehouse.message(code: 'default.not.updated.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), itemInstance.id])}"
				log.info "There were errors trying to save inventory item " + itemInstance?.errors
				//flash.message = "There were errors"
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
		}
		redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance?.id)
	}
	
	
	
	
	def deleteTransactionEntry = { 
		def transactionEntry = TransactionEntry.get(params.id)
		def productInstance 
		if (transactionEntry) {
			productInstance = transactionEntry.inventoryItem.product 
			transactionEntry.delete();
		}
		redirect(action: 'showStockCard', params: ['product.id':productInstance?.id])
	}
	
	def addToInventory = {
		def product = Product.get( params.id )
		render warehouse.message(code: 'inventoryItem.productAddedToInventory.message', args: [product.name])
		//return product as XML		
	}	
	/**
	 * shipment.name:1, 
	 * shipment:[name:1, id:1], 
	 * inventory.id:1, 
	 * inventory:[id:1], 
	 * recipient.name:3, 
	 * recipient:[name:3, id:3], 
	 * product.id:8, product:[id:8], 
	 * quantity:1, 
	 * recipient.id:3, addItem:, 
	 * shipment.id:1, 
	 * inventoryItem.id:1, 
	 * inventoryItem:[id:1], 
	 * action:addToShipment, 
	 * controller:inventoryItem]
	*/
	def addToShipment = {
		log.info "params" + params
		def shipmentInstance = null;
		def containerInstance = null;
		def productInstance = Product.get(params?.product?.id);
		def personInstance = Person.get(params?.recipient?.id);
		def inventoryItem = InventoryItem.get(params?.inventoryItem?.id);
		
		def shipmentContainer = params.shipmentContainer?.split(":")
		
		shipmentInstance = Shipment.get(shipmentContainer[0]);
		containerInstance = Container.get(shipmentContainer[1]);
		
		log.info("shipment "  + shipmentInstance);
		log.info("container "  + containerInstance);
		
		
		
		def shipmentItem = new ShipmentItem(
			product: productInstance,
			inventoryItem: inventoryItem,
			quantity: params.quantity,
			recipient: personInstance,
			lotNumber: inventoryItem.lotNumber?:'',
			shipment: shipmentInstance,
			container: containerInstance);
		
		try {
			shipmentService.validateShipmentItem(shipmentItem)
					
			if(shipmentItem.hasErrors() || !shipmentItem.validate()) {
				flash.message = "${warehouse.message(code: 'inventoryItem.errorValidatingItem.message')}\n"
				shipmentItem.errors.each { flash.message += it }
				
			}
	
			if (!shipmentItem.hasErrors()) { 
				if (!shipmentInstance.addToShipmentItems(shipmentItem).save()) {
					log.error("Sorry, unable to add new item to shipment.  Please try again.");
					flash.message = "${warehouse.message(code: 'inventoryItem.unableToAddItemToShipment.message')}"
				}
				else { 
					def productDescription = format.product(product:productInstance) + (inventoryItem?.lotNumber) ? " #" + inventoryItem?.lotNumber : "";	
					flash.message = "${warehouse.message(code: 'inventoryItem.addedItemToShipment.message', args: [productDescription,shipmentInstance?.name])}"
				}
			}

		} catch (ShipmentItemException e) {
			//e.errors.reject()
			flash['errors'] = e.shipmentItem.errors		
		} catch (ValidationException e) { 
			flash['errors'] = e.errors
		}

		redirect(action: "showStockCard", params: ['product.id':productInstance?.id]);
	}
	

	
		
	def saveInventoryLevel = {
		// Get existing inventory level
		def inventoryLevelInstance = InventoryLevel.get(params.id)		
		def productInstance = Product.get(params?.product?.id)
		//def inventoryInstance = Inventory.get(params?.inventory?.id);

		if (inventoryLevelInstance) { 
			inventoryLevelInstance.properties = params;
		}
		else { 
			inventoryLevelInstance = new InventoryLevel(params);
		}
		
		if (!inventoryLevelInstance.hasErrors() && inventoryLevelInstance.save()) { 
			
		}
		else { 
			flash.message = "${warehouse.message(code: 'inventoryItem.errorSavingInventoryLevels.message')}<br/>" 
			inventoryLevelInstance.errors.allErrors.each { 
				flash.message += it + "<br/>";
			}
		}
		redirect(action: 'showStockCard', params: ['product.id':productInstance?.id])
	}
	
	/*
	def saveInventoryItem = {
		def inventory = Inventory.get(params.id)
		def inventoryItem = new InventoryItem(params)
		inventory.addToInventoryItem(inventoryItem)
		if(! inventory.hasErrors() && inventory.save()) {
			render template:'inventoryItemRow', bean:inventoryItem, var:'inventoryItem'
		}
	}
	*/
	
	def create = { 
		def inventoryItem = new InventoryItem(params)
		if (InventoryItem && inventoryItem.save() ) { 
			flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventoryItem.label'), params.id])}"
		}
		else { 
			flash.message = "${warehouse.message(code: 'default.not.created.message', args: [warehouse.message(code: 'inventoryItem.label')])}"
		}
		redirect(action: 'showLotNumbers', params: ['product.id':inventoryItem?.product?.id])
	}
	
	
	def delete = {
		def inventoryItem = InventoryItem.get(params.id)
		if (inventoryItem) {
			try {
				inventoryItem.delete(flush: true)
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Attribute'), params.id])}"
				//redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Attribute'), params.id])}"
				//redirect(action: "list", id: params.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Attribute'), params.id])}"
			//redirect(action: "list")
		}

		redirect(action: 'showLotNumbers', params: ['product.id':inventoryItem?.product?.id])
	}

	def deleteInventoryItem = {
		def inventoryItem = InventoryItem.get(params.id);
		def productInstance = inventoryItem?.product;
		def inventoryInstance = Inventory.get(inventoryItem?.inventory?.id);
		
		if (inventoryItem && inventoryInstance) {
			inventoryInstance.removeFromInventoryItems(inventoryItem).save();
			inventoryItem.delete();
		}		
		else {
			inventoryItem.errors.reject("inventoryItem.error", "Could not delete inventory item")
			params.put("product.id", productInstance?.id);
			params.put("inventory.id", inventoryInstance?.id);
			log.info "Params " + params;
			chain(action: "createInventoryItem", model: [inventoryItem: inventoryItem], params: params)
			return;
		}
		redirect(action: 'showStockCard', params: ['product.id':productInstance?.id])
		
	}
	
	
	def saveTransactionEntry = {			
		def productInstance = Product.get(params?.product?.id)				
		if (!productInstance) {
			flash.message = "${warehouse.message(code: 'default.notfound.message', args: [warehouse.message(code: 'product.label', default: 'Product'), productInstance.id])}"
			redirect(action: "showStockCard", id: productInstance?.id)
		}
		else { 
			def inventoryItem = inventoryService.findByProductAndLotNumber(productInstance, params.lotNumber?:null)
			if (!inventoryItem) { 
				flash.message = "${warehouse.message(code: 'default.notfound.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.lotNumber])}"
			} 
			else {  
				def transactionInstance = new Transaction(params)
				def transactionEntry = new TransactionEntry(params)
				
				// If we're transferring stock to another location OR consuming stock, 
				// then we need to make sure the quantity is negative
				if (transactionInstance?.destination?.id != session?.warehouse?.id 
					|| transactionInstance?.transactionType?.name == 'Consumption') { 
					if (transactionEntry.quantity > 0) { 
						transactionEntry.quantity = -transactionEntry.quantity;
					}
				}
				
				transactionEntry.inventoryItem = inventoryItem;
				if (!transactionEntry.hasErrors() &&
					transactionInstance.addToTransactionEntries(transactionEntry).save(flush:true)) {
					flash.message = "${warehouse.message(code: 'inventoryItem.savedTransactionEntry.label')}"
				} 
				else {
					transactionInstance.errors.each { println it }
					transactionEntry.errors.each { println it }
						flash.message = "${warehouse.message(code: 'inventoryItem.inventoryItem.unableToSaveTransactionEntry.message.label')}"
				}
				
				/*
				if (!transactionInstance.hasErrors() && transactionInstance.save(flush:true) {
					def transactionEntry = new TransactionEntry(params)
					transactionEntry.inventoryItem = inventoryItem;					
					if (!transactionEntry.hasErrors() && 
						transactionInstance.addToTransactionEntries(transactionEntry).save(flush:true)) {
						transactionEntry.errors.each { println it }
						flash.message = "Unable to save transaction entry"
					} else { 
						flash.message = "${warehouse.message(code: 'default.saved.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), itemInstance.id])}"					
					}
				}
				else { 
					transactionInstance.errors.each { println it }
					flash.message = "Unable to save transaction"
				}	
				*/
			}
		}
		redirect(action: "showStockCard",  params: ['product.id':productInstance?.id])		
	}
	
	
	def editItemDialog = {
		def itemInstance = InventoryItem.get(params.id);
		render(view:'editItemDialog', model: [itemInstance: itemInstance]);
	}

}


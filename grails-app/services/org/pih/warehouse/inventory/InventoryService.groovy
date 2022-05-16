/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import grails.plugin.springcache.annotations.Cacheable
import grails.validation.ValidationException
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.StringUtils
import org.grails.plugins.csv.CSVWriter
import org.hibernate.annotations.Cache
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.id.UUIDHexGenerator
import org.joda.time.LocalDate
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.ImporterUtil
import org.pih.warehouse.importer.InventoryExcelImporter
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductException
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.reporting.Consumption
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.validation.Errors
import util.InventoryUtil

import java.sql.BatchUpdateException
import java.text.ParseException;
import java.util.Random


import java.text.SimpleDateFormat

class InventoryService implements ApplicationContextAware {

	def dataSource
    def sessionFactory
    def dataService
	def productService
	def identifierService
	//def authService

	ApplicationContext applicationContext

	boolean transactional = true

	/**
	 * @return shipment service
	 */
	def getShipmentService() {
		return applicationContext.getBean("shipmentService")
	}

	/**
	 * @return order service
	 */
	def getOrderService() {
		return applicationContext.getBean("orderService")
	}


	/**
	 * Saves the specified warehouse
	 *
	 * @param warehouse
	 */
	void saveLocation(Location warehouse) {
		log.debug("saving warehouse " + warehouse)
		log.debug("location type " + warehouse.locationType)
		// make sure a warehouse has an inventory
		if (!warehouse.inventory) {
			addInventory(warehouse)
		}
		log.debug warehouse.locationType

		warehouse.save(failOnError: true)
	}

	/**
	 * Returns the Location specified by the passed id parameter;
	 * if no parameter is specified, returns a new location instance
	 *
	 * @param locationId
	 * @return
	 */
	Location getLocation(String locationId) {
		if (locationId) {
			Location location = Location.get(locationId)
			if (!location) {
				throw new Exception("No location found with locationId ${locationId}")
			}
			else {
				return location
			}
		}
		// otherwise, we need to create a new, empty location
		else {
			Location location = new Location()
			return location
		}
	}

	/**
	 * Adds an inventory to the specified warehouse
	 *
	 * @param warehouse
	 * @return
	 */
	Inventory addInventory(Location warehouse) {
		if (!warehouse) {
			throw new RuntimeException("No warehouse specified.")
		}
		if (warehouse.inventory) {
			throw new RuntimeException("An inventory is already associated with this warehouse.")
		}

		warehouse.inventory = new Inventory(['warehouse': warehouse])
		saveLocation(warehouse)

		return warehouse.inventory
	}

	/**
	 * @return a Sorted Map from product primary category to List of products
	 */
	Map getProductMap(Collection inventoryItems) {

		Map map = new TreeMap();
		if (inventoryItems) {
			inventoryItems.each {
				Category category = it.category ? it.category : new Category(name: "Unclassified")
				List list = map.get(category)
				if (list == null) {
					list = new ArrayList();
					map.put(category, list);
				}
				list.add(it);
			}
			for (entry in map) {
				entry.value.sort { item1, item2 ->
					//item1?.product?.name <=> item2?.product?.name
					item1?.description <=> item2?.description
				}
			}
		}
		return map
	}

	/**
	 * Search inventory items by term or product Id
	 *
	 * @param searchTerm
	 * @param productId
	 * @return
	 */
	List findInventoryItems(String searchTerm) {

        def results = InventoryItem.withCriteria {
            createAlias('product', 'p', CriteriaSpecification.LEFT_JOIN)
            or {
                ilike("lotNumber", "%" + searchTerm + "%")
                ilike("p.name", "%" + searchTerm + "%")
                ilike("p.productCode", "%" + searchTerm + "%")
            }
        }
        return results
	}

	List findInventoryItemsByProducts(List<Product> products) {
		def inventoryItems = []
		if (products) {
			inventoryItems = InventoryItem.withCriteria {
				'in'("product", products)
				order("expirationDate", "asc")
			}
		}
		return inventoryItems;
	}

	/**
	 *
	 * @param commandInstance
	 * @return
	 */
	InventoryCommand browseInventory(InventoryCommand commandInstance) {

		// add an inventory to this warehouse if it doesn't exist
		if (!commandInstance?.warehouseInstance?.inventory) {
			addInventory(commandInstance.warehouseInstance)
		}

		// Get the selected category or use the root category
		def rootCategory = productService?.getRootCategory();
		commandInstance.categoryInstance = commandInstance?.categoryInstance ?: productService.getRootCategory();

		// Get current inventory for the given products
		//if (commandInstance?.categoryInstance != rootCategory || commandInstance?.searchPerformed || commandInstance.tag) {
		getCurrentInventory(commandInstance);
		//}
		//else {
		//   commandInstance?.categoryToProductMap = [:]
		//}

		return commandInstance;
	}

	/**
	 *
	 * @param commandInstance
	 * @return
	 */
	Map getCurrentInventory(InventoryCommand commandInstance) {

        long initialStartTime = System.currentTimeMillis()
		long startTime = System.currentTimeMillis()
		log.debug "getCurrentInventory()"
		def inventoryItemCommands = [];
		List categories = new ArrayList();
		if (commandInstance?.subcategoryInstance) {
			categories.add(commandInstance?.subcategoryInstance);
		}
		else {
			categories.add(commandInstance?.categoryInstance);
		}

		List searchTerms = (commandInstance?.searchTerms ? Arrays.asList(commandInstance?.searchTerms?.split(" ")) : null);
		log.info "searchTerms = " + searchTerms
		log.debug("get products: " + commandInstance?.warehouseInstance)
		log.info "command.tag  = " + commandInstance.tag

		def products = []

        // User wants to view all products that match the given tag
		if (commandInstance.tag) {
			commandInstance.numResults = countProductsByTags(commandInstance.tag)
			products = getProductsByTags(commandInstance.tag, commandInstance?.maxResults as int, commandInstance?.offset as int)
		}

        // User wants to view all products in the given shipment
        else if (commandInstance.shipment) {
            commandInstance.numResults = countProductsByShipment(commandInstance.shipment)
            products = getProductsByShipment(commandInstance.shipment, commandInstance?.maxResults as int, commandInstance?.offset as int)

        }
        else {
			// Get all products, including hidden ones
			def matchCategories = getExplodedCategories(categories)
            log.info " * Get all categories: " + (System.currentTimeMillis() - startTime) + " ms"
			startTime = System.currentTimeMillis()
			
			products = getProductsByTermsAndCategories(searchTerms, matchCategories, commandInstance?.showHiddenProducts, commandInstance?.warehouseInstance.inventory, commandInstance?.maxResults, commandInstance?.offset)
            log.info " * Get products by terms and categories: " + (System.currentTimeMillis() - startTime) + " ms"
			startTime = System.currentTimeMillis()

			commandInstance.numResults = products.totalCount
			
			if (!commandInstance?.showHiddenProducts) {
				products.removeAll(getHiddenProducts(commandInstance?.warehouseInstance))
			}
            log.info " * After removing all hidden products: " + (System.currentTimeMillis() - startTime) + " ms"
			startTime = System.currentTimeMillis()
			
			// now localize to only match products for the current locale
			// TODO: this would also have to handle the category filtering
			//  products = products.findAll { product ->
			//  def localizedProductName = getLocalizationService().getLocalizedString(product.name);
			// TODO: obviously, this would have to use the actual locale
			// return productFilters.any {
			//   localizedProductName.contains(it)  // TODO: this would also have to be case insensitive
			// }
			// }
		}
		products = products?.sort() { map1, map2 -> map1.category <=> map2.category ?: map1.name <=> map2.name };
        log.info " * Sort products " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()
		
		// Get quantity for each item in inventory TODO: Should only be doing this for the selected products for speed
		def quantityOnHandMap = [:]//getQuantityByProductMap(commandInstance?.warehouseInstance?.inventory, products);
        log.info " * Get quantity on hand map " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()
		
		def quantityOutgoingMap = [:] 
		// getOutgoingQuantityByProduct(commandInstance?.warehouseInstance, products);
        log.info " * Get outgoing map " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()
		
		def quantityIncomingMap = [:] 
		// getIncomingQuantityByProduct(commandInstance?.warehouseInstance, products);
        log.info " * Get incoming map: " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()
		
		
		def quantityOnHand = 0;
		def quantityToReceive = 0;
		def quantityToShip = 0;
		
		startTime = System.currentTimeMillis()
		def inventoryLevelMap = InventoryLevel.findAllByInventory(commandInstance?.warehouseInstance?.inventory)?.groupBy { it.product }
        log.debug " * Get inventory level map: " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()
		def inventoryLevel
		products.each { product ->
			def innerStartTime = System.currentTimeMillis()
			inventoryLevel = (inventoryLevelMap[product])?inventoryLevelMap[product][0]:null
			//log.debug "product " + product
			//log.debug "inventoryLevel.class.name = " + inventoryLevel?.class?.name
			//log.debug "inventoryLevels " + inventoryLevel
			
						
			if (inventoryLevel && inventoryLevel instanceof ArrayList) { 
				throw new Exception("Cannot have multiple inventory levels for a single product [" + product.productCode + ":" + product.name + "]: " + inventoryLevel)
			
			}
			quantityOnHand = quantityOnHandMap[product] ?: 0
			quantityToReceive = quantityIncomingMap[product] ?: 0
			quantityToShip = quantityOutgoingMap[product] ?: 0
			if (commandInstance?.showOutOfStockProducts || (quantityOnHand + quantityToReceive + quantityToShip > 0)) {
				inventoryItemCommands << getInventoryItemCommand(product,						
						commandInstance?.warehouseInstance?.inventory,
						inventoryLevel,
						quantityOnHand, quantityToReceive, quantityToShip,
						commandInstance?.showOutOfStockProducts)
			}
            log.info " * process product : " + (System.currentTimeMillis() - innerStartTime) + " ms"
		}
        log.info " * process on hand quantity: " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()
		
		/*
		def productGroups = getProductGroups(commandInstance);
		productGroups.each { productGroup ->
			def inventoryItemCommand = getInventoryItemCommand(productGroup, commandInstance?.warehouseInstance.inventory, commandInstance.showOutOfStockProducts)
			inventoryItemCommand.inventoryItems = new ArrayList();
			productGroup.products.each { product ->
				inventoryItemCommand.quantityOnHand += quantityOnHandMap[product] ?: 0
				inventoryItemCommand.quantityToReceive += quantityIncomingMap[product] ?: 0
				inventoryItemCommand.quantityToShip += quantityOutgoingMap[product] ?: 0
				inventoryItemCommand.inventoryItems << getInventoryItemCommand(product, commandInstance?.warehouseInstance.inventory,
						quantityOnHandMap[product] ?: 0,
						quantityIncomingMap[product] ?: 0,
						quantityOutgoingMap[product] ?: 0,
						commandInstance.showOutOfStockProducts)

				// Remove all products in the product group from the main productd list
				inventoryItemCommands.removeAll { it.product == product }
			}
			inventoryItemCommands << inventoryItemCommand
		}
		log.debug " * after creating inventory items: " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()
		*/
		
		commandInstance?.categoryToProductMap = getProductMap(inventoryItemCommands);

        log.info " * Get category to product map: " + (System.currentTimeMillis() - startTime) + " ms"

        log.info "Total time - Get current inventory: " + (System.currentTimeMillis() - initialStartTime) + " ms"
		return commandInstance?.categoryToProductMap
	}


	InventoryItemCommand getInventoryItemCommand(Product product, Inventory inventory, InventoryLevel inventoryLevel, Integer quantityOnHand, Integer quantityToReceive, Integer quantityToShip, Boolean showOutOfStockProducts) {
		InventoryItemCommand inventoryItemCommand = new InventoryItemCommand();
		
		//def startTime = System.currentTimeMillis()
		//def inventoryLevel = InventoryLevel.findByProductAndInventory(product, inventory)
		//log.debug " * find inventory level by product and inventory: " + (System.currentTimeMillis() - startTime) + " ms"
		inventoryItemCommand.description = product.name		
		inventoryItemCommand.category = product.category
		inventoryItemCommand.product = product
		inventoryItemCommand.inventoryLevel = inventoryLevel
		inventoryItemCommand.quantityOnHand = quantityOnHand
		inventoryItemCommand.quantityToReceive = quantityToReceive
		inventoryItemCommand.quantityToShip = quantityToShip
		return inventoryItemCommand
	}

	InventoryItemCommand getInventoryItemCommand(ProductGroup productGroup, Inventory inventory, Boolean showOutOfStockProducts) {
		InventoryItemCommand inventoryItemCommand = new InventoryItemCommand();
		inventoryItemCommand.description = productGroup.description
		inventoryItemCommand.productGroup = productGroup
		inventoryItemCommand.category = productGroup.category
		//inventoryItemCommand.quantityOnHand = 1
		//inventoryItemCommand.quantityToReceive = 0
		//inventoryItemCommand.quantityToShip = 0

		return inventoryItemCommand
	}

	/**
	 *
	 * @param inventoryCommand
	 * @return
	 */
	Set<ProductGroup> getProductGroups(InventoryCommand inventoryCommand) {
		List categoryFilters = new ArrayList();
		if (inventoryCommand?.subcategoryInstance) {
			categoryFilters.add(inventoryCommand?.subcategoryInstance);
		}
		else {
			categoryFilters.add(inventoryCommand?.categoryInstance);
		}

		List searchTerms = (inventoryCommand?.searchTerms ? Arrays.asList(inventoryCommand?.searchTerms.split(" ")) : null);

		def productGroups = getProductGroups(inventoryCommand?.warehouseInstance, searchTerms, categoryFilters,
				inventoryCommand?.showHiddenProducts);

		productGroups = productGroups?.sort() { it?.description };
		return productGroups;
	}

	/**
	 * Get all product groups for the given location, searchTerms, categories.
	 * @param location
	 * @param searchTerms
	 * @param categoryFilters
	 * @param showHiddenProducts
	 * @return
	 */
	Set<ProductGroup> getProductGroups(Location location, List searchTerms, List categoryFilters, Boolean showHiddenProducts) {
		def productGroups = ProductGroup.list()
		productGroups = productGroups.intersect(getProductGroups(searchTerms, categoryFilters))
		/*
		 // Get products that match the search terms by name and category
		 def categories = getCategoriesMatchingSearchTerms(searchTerms)
		 // Categories
		 def matchCategories = getExplodedCategories(categoryFilters);
		 log.debug "matchCategories " + matchCategories
		 // Get all products, including hidden ones
		 if (!showHiddenProducts) {
		 def statuses = []
		 statuses << InventoryStatus.NOT_SUPPORTED
		 statuses << InventoryStatus.SUPPORTED_NON_INVENTORY
		 def removeProducts = getProductsByLocationAndStatuses(location, statuses)
		 log.debug "remove " + removeProducts.size() + " hidden products"
		 products.removeAll(removeProducts)
		 }
		 log.debug "base products " + products.size();
		 if (matchCategories && searchTerms) {
		 def searchProducts = ProductGroup.createCriteria().list() {
		 and {
		 or {
		 searchTerms.each {
		 ilike("description", "%" + it + "%")
		 }
		 }
		 'in'("category", matchCategories)
		 }
		 }
		 products = products.intersect(searchProducts);
		 }
		 else {
		 def searchProducts = ProductGroup.createCriteria().list() {
		 or {
		 if (searchTerms) {
		 searchTerms.each {
		 String[] filterTerms = it.split("\\s+");
		 or {
		 and {
		 filterTerms.each {
		 ilike("description", "%" + it + "%")
		 }
		 }
		 and {
		 filterTerms.each {
		 ilike("manufacturer", "%" + it + "%")
		 }
		 }
		 and {
		 filterTerms.each {
		 ilike("productCode", "%" + it + "%")
		 }
		 }
		 }
		 }
		 }
		 if (matchCategories) {
		 'in'("category", matchCategories)
		 }
		 if (categories) {
		 'in'("category", categories)
		 }
		 }
		 }
		 products = products.intersect(searchProducts);
		 }
		 // now localize to only match products for the current locale
		 // TODO: this would also have to handle the category filtering
		 //  products = products.findAll { product ->
		 //  def localizedProductName = getLocalizationService().getLocalizedString(product.name);  // TODO: obviously, this would have to use the actual locale
		 // return productFilters.any {
		 //   localizedProductName.contains(it)  // TODO: this would also have to be case insensitive
		 // }
		 // }
		 */
		return productGroups;
	}


	List getProductGroups(List searchTerms, List categories) {
		def productGroups = ProductGroup.createCriteria().list() {
			or {
				if (searchTerms) {
					and {
						searchTerms.each { searchTerm ->
							ilike("description", "%" + searchTerm + "%")
						}
					}
				}
				if (categories) {
					'in'("category", categories)
				}
			}
		}
		return productGroups
	}

	/**
	 * Get the outgoing quantity for all products at the given location.
	 *
	 * @param location
	 * @return
	 */
	Map getOutgoingQuantityByProduct(Location location, List<Product> products) {
		Map quantityByProduct = [:]
		Map quantityShippedByProduct = getShipmentService().getOutgoingQuantityByProduct(location, products);
		Map quantityOrderedByProduct = getOrderService().getOutgoingQuantityByProduct(location, products)
		//Map quantityRequestedByProduct = getRequisitionService().getOutgoingQuantityBgetInventoryItemsJsonByProductyProduct(location)
		quantityShippedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;
			productQuantity += quantity ?: 0;
			quantityByProduct[product] = productQuantity;
		}
		quantityOrderedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;
			productQuantity += quantity ?: 0;
			quantityByProduct[product] = productQuantity;
		}
		//		quantityRequestedByProduct.each { product, quantity ->
		//			def productQuantity = quantityByProduct[product];
		//			if (!productQuantity) productQuantity = 0;
		//			productQuantity += quantity?:0;
		//			quantityByProduct[product] = productQuantity;
		//		}
		return quantityByProduct;
	}

	/**
	 * Get the incoming quantity for all products at the given location.
	 * @param location
	 * @return
	 */
	Map getIncomingQuantityByProduct(Location location, List<Product> products) {
		Map quantityByProduct = [:]
		Map quantityShippedByProduct = getShipmentService().getIncomingQuantityByProduct(location, products);
		Map quantityOrderedByProduct = getOrderService().getIncomingQuantityByProduct(location, products)
		//Map quantityRequestedByProduct = getRequisitionService().getIncomingQuantityByProduct(location)
		quantityShippedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;
			productQuantity += quantity ?: 0;
			quantityByProduct[product] = productQuantity;
		}
		quantityOrderedByProduct.each { product, quantity ->
			def productQuantity = quantityByProduct[product];
			if (!productQuantity) productQuantity = 0;
			productQuantity += quantity ?: 0;
			quantityByProduct[product] = productQuantity;
		}
		//		quantityRequestedByProduct.each { product, quantity ->
		//			def productQuantity = quantityByProduct[product];
		//			if (!productQuantity) productQuantity = 0;
		//			productQuantity += quantity?:0;
		//			quantityByProduct[product] = productQuantity;
		//		}
		return quantityByProduct;
	}

	/**
	 * Get a map of quantity for each inventory item for the given inventory and product.
	 *
	 * @param inventory
	 * @param product
	 * @return
	 */
	Map getQuantityByInventoryAndProduct(Inventory inventory, Product product) {
		Map inventoryItemQuantity = [:]
		Set inventoryItems = getInventoryItemsByProductAndInventory(product, inventory);
		Map<InventoryItem, Integer> quantityMap = getQuantityForInventory(inventory)
		
		//log.debug "getQuantityByInventoryAndProduct: " + quantityMap
		
		inventoryItems.each {
			def quantity = quantityMap[it]
			if (quantity) { 
				inventoryItemQuantity[it] = quantity
			}
		}
		return inventoryItemQuantity;
	}


    def getExpirationSummary(location) {
        def expirationSummary = [:]
        def expirationAlerts = getExpirationAlerts(location)
        expirationAlerts.groupBy { it?.inventoryItem?.expires }.each { key, value ->
            expirationSummary[key] = value.size()

        }
        return expirationSummary
    }

    def getExpirationAlerts(location) {
        def startTime = System.currentTimeMillis()

        def expirationAlerts = []
        def today = new Date()
        def quantityMap = getQuantityForInventory(location.inventory)

        quantityMap.each { key, value ->
            if (value > 0) {
                def daysToExpiry = key.expirationDate ? (key.expirationDate - today) : null
                expirationAlerts << [ id:  key.id, lotNumber: key.lotNumber, quantity: value,
                    expirationDate: key.expirationDate, daysToExpiry: daysToExpiry,
                    product: key.product.toJson(), inventoryItem: key.toJson()
                ]
            }
        }

        log.info "Expiration alerts: " + (System.currentTimeMillis() - startTime) + " ms"

        return expirationAlerts

    }


	def getInventoryItemSnapshot(Location location, Integer daysToExpiry) {
        def results = InventoryItemSnapshot.executeQuery("""
            SELECT a.inventoryItem, a.quantityOnHand, DATEDIFF(a.inventoryItem.expirationDate, current_date) as daysToExpiry
            FROM InventoryItemSnapshot a
            WHERE a.date = (select max(b.date) from InventoryItemSnapshot b)
            AND a.location = :location
            AND a.quantityOnHand > 0
            AND DATEDIFF(a.inventoryItem.expirationDate, current_date) <= :daysToExpiry
            ORDER BY a.inventoryItem.expirationDate ASC
            """, [location:location, daysToExpiry:daysToExpiry])
        return results
	}


	/**
	 * Get all expired inventory items for the given category and location.
	 * 
	 * @param category
	 * @param location
	 * @return
	 */
	List getExpiredStock(Category category, Location location) {
		
		long startTime = System.currentTimeMillis()
		
		// Stock that has already expired
		def expiredStock = InventoryItem.findAllByExpirationDateLessThan(new Date(), [sort: 'expirationDate', order: 'desc']);

		log.debug expiredStock

		Map<InventoryItem, Integer> quantityMap = getQuantityByLocation(location)
		expiredStock = expiredStock.findAll { quantityMap[it] > 0 }

		// FIXME poor man's filter
		if (category) {
			expiredStock = expiredStock.findAll { item -> item?.product?.category == category }
		}

        log.debug "Get expired stock: " + (System.currentTimeMillis() - startTime) + " ms"
		return expiredStock

	}

	/**
	 * Get all inventory items that are expiring within the given threshold.
	 *
	 * @param category the category filter
	 * @param threshold the threshold filter
	 * @return a list of inventory items
	 */
	List getExpiringStock(Category category, Location location, Integer threshold) {
		long startTime = System.currentTimeMillis()
		
		def today = new Date();

		// Get all stock expiring ever (we'll filter later)
		def expiringStock = InventoryItem.findAllByExpirationDateGreaterThan(today + 1, [sort: 'expirationDate', order: 'asc']);
		def quantityMap = getQuantityByLocation(location)
		expiringStock = expiringStock.findAll { quantityMap[it] > 0 }
		if (category) {
			expiringStock = expiringStock.findAll { item -> item?.product?.category == category }
		}

		if (threshold) {
			expiringStock = expiringStock.findAll { item -> (item?.expirationDate && (item?.expirationDate - today) <= threshold) }
		}
        log.debug "Get expiring stock: " + (System.currentTimeMillis() - startTime) + " ms"
		return expiringStock
	}


    def getReconditionedStock(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getQuantityByProductMap(location.inventory)
        def reconditionedStock = quantityMap.findAll { it.key.reconditioned }
        log.debug "Get reconditioned stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return reconditionedStock
    }


    def getTotalStock(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getQuantityByProductMap(location.inventory)
        log.debug "Get total stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return quantityMap
    }

    def getInStock(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getQuantityByProductMap(location.inventory)
        def inStock = quantityMap.findAll { it.value > 0 }
        log.debug "Get in stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return inStock
    }


    def getTotalStockValue(Location location) {
        def hitCount = 0;
        def missCount = 0;
        def totalCount = 0;
        def totalStockValue = 0.0

        if (location.inventory) {
            def quantityMap = getQuantityByProductMap(location.inventory)
            quantityMap.each { product, quantity ->
                if (product.pricePerUnit) {
                    def stockValueForProduct = product.pricePerUnit * quantity
                    totalStockValue += stockValueForProduct
                    hitCount++
                }
                else {
                    missCount++
                }
            }
            totalCount = quantityMap?.keySet()?.size()
        }
        return [totalStockValue:totalStockValue, hitCount: hitCount, missCount: missCount, totalCount:totalCount]

    }


    def getDashboardAlerts(Location location) {
		log.info "Dashboard alerts for ${location}"

        long startTime = System.currentTimeMillis()
        def quantityMap = getQuantityByProductMap(location.inventory)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        log.info inventoryLevelMap.keySet().size()

        def totalStock = quantityMap
        def reconditionedStock = quantityMap.findAll { it.key.reconditioned }
        def onHandQuantityZero = quantityMap.findAll { it.value <= 0 }
        def inStock = quantityMap.findAll { it.value > 0 }


        //def lowStock = quantityMap.findAll { it.value <= it?.key?.getInventoryLevel(location?.id)?.minQuantity }
        def outOfStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0
        }

        def lowStock = quantityMap.findAll { product,quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def minQuantity = inventoryLevelMap[product]?.first()?.minQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && minQuantity && quantity <= minQuantity
        }

        def reorderStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def reorderQuantity = inventoryLevelMap[product]?.first()?.reorderQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && reorderQuantity && quantity <= reorderQuantity
        }

        def overStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def maxQuantity = inventoryLevelMap[product]?.first()?.maxQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && maxQuantity && quantity > maxQuantity
        }

        def outOfStockClassA = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && inventoryLevel?.abcClass == "A"
        }

        def outOfStockClassB = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && inventoryLevel?.abcClass == "B"
        }

        def outOfStockClassC = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && inventoryLevel?.abcClass == "C"
        }

        def outOfStockClassNone = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && inventoryLevel?.abcClass == null
        }



        //println lowStock.keySet().size()
        log.debug "Get low stock: " + (System.currentTimeMillis() - startTime) + " ms"
        //return lowStock

        [   lowStock: lowStock.keySet().size(),
                lowStockCost: getTotalCost(lowStock),
            reorderStock: reorderStock.keySet().size(),
                reorderStockCost: getTotalCost(reorderStock),
            overStock: overStock.keySet().size(),
                overStockCost: getTotalCost(overStock),
            totalStock: totalStock.keySet().size(),
                totalStockCost: getTotalCost(totalStock),
            reconditionedStock: reconditionedStock.keySet().size(),
                reconditionedStockCost: getTotalCost(reconditionedStock),
            outOfStock:outOfStock.keySet().size(),
                outOfStockCost: getTotalCost(outOfStock),
            outOfStockClassA:outOfStockClassA.keySet().size(),
                outOfStockCostClassA: getTotalCost(outOfStockClassA),
            outOfStockClassB:outOfStockClassB.keySet().size(),
                outOfStockCostClassB: getTotalCost(outOfStockClassB),
            outOfStockClassC:outOfStockClassC.keySet().size(),
                outOfStockCostClassC: getTotalCost(outOfStockClassC),
            outOfStockClassNone:outOfStockClassNone.keySet().size(),
                outOfStockCostClassNone: getTotalCost(outOfStockClassNone),

            onHandQuantityZero:onHandQuantityZero.keySet().size(),
                onHandQuantityZeroCost: getTotalCost(onHandQuantityZero),
            inStock:inStock.keySet().size(),
                inStockCost: getTotalCost(inStock),
        ]
    }


    def getTotalCost(quantityMap) {
        def totalCost = 0;
        quantityMap.each { k,v ->
            totalCost += k.pricePerUnit?:0 * v?:0
        }
        return totalCost;
    }

    def getInventoryStatus(Location location) {
        def quantityMap = getQuantityByProductMap(location.inventory)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def inventoryStatusMap = [:]
        quantityMap.each { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            inventoryStatusMap[product] = inventoryLevel?.statusMessage(quantity)?:"${inventoryLevel?.id}"

        }
        return inventoryStatusMap
    }

    /**
     * Get inventory status, inventory level and quantity on hand for all products.
     *
     * @param location
     * @return
     */
    def getInventoryStatusAndLevel(Location location) {
        def quantityMap = getQuantityByProductMap(location.inventory)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def inventoryStatusMap = [:]
        quantityMap.each { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def status = inventoryLevel?.statusMessage(quantity)?:"NONE"
            inventoryStatusMap[product] = [inventoryLevel:inventoryLevel,status:status,onHandQuantity:quantity]
        }
        return inventoryStatusMap
    }

    /**
     * Get inventory status for a single inventory level
     *
     * @param inventoryLevel
     * @param quantity
     * @return
     */
    /*
    def getInventoryStatus(inventoryLevel, quantity) {
        def status = ""
        if (inventoryLevel?.status >= InventoryStatus.SUPPORTED  || !inventoryLevel?.status) {
            if (quantity <= 0) {
                status = "STOCK_OUT"
            }
            else if (inventoryLevel?.minQuantity && quantity <= inventoryLevel?.minQuantity) {
                status = "LOW_STOCK"
            }
            else if (inventoryLevel?.reorderQuantity && quantity <= inventoryLevel?.reorderQuantity ) {
                status = "REORDER"
            }
            else if (inventoryLevel?.maxQuantity && quantity > inventoryLevel?.maxQuantity && inventoryLevel?.maxQuantity > 0) {
                status = "OVERSTOCK"
            }
            else {
                status = "IN_STOCK"
            }
        }
        else if (inventoryLevel?.status == InventoryStatus.NOT_SUPPORTED) {
            status = "NOT_SUPPORTED"
        }
        else if (inventoryLevel?.status == InventoryStatus.SUPPORTED_NON_INVENTORY) {
            status = "SUPPORTED_NON_INVENTORY"
        }
        else {
            status = "UNAVAILABLE"
        }
        return status
    }*/


    def getQuantityOnHandZero(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getQuantityByProductMap(location.inventory)
        //def stockOut = quantityMap.findAll { it.value <= 0 }

        //def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def stockOut = quantityMap.findAll { product, quantity ->
            //def inventoryLevel = inventoryLevelMap[product]?.first()
            quantity <= 0
        }

        log.info "Get quantity on hand zero: " + (System.currentTimeMillis() - startTime) + " ms"
        return stockOut

    }

    def getOutOfStock(Location location, String abcClass) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getQuantityByProductMap(location.inventory)
        //def stockOut = quantityMap.findAll { it.value <= 0 }

        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def stockOut = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            if (abcClass)
                inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0 && (abcClass == inventoryLevel.abcClass)
            else
                inventoryLevel?.status >= InventoryStatus.SUPPORTED && quantity <= 0
        }

        log.info "Get stock out: " + (System.currentTimeMillis() - startTime) + " ms"
        return stockOut
    }


    def getLowStock(Location location) {
		long startTime = System.currentTimeMillis()
		def quantityMap = getQuantityByProductMap(location.inventory)
        log.info ("getQuantityByProductMap: " + (System.currentTimeMillis() - startTime) + " ms")
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        log.info ("getInventoryLevelMap: " + (System.currentTimeMillis() - startTime) + " ms")
        log.info inventoryLevelMap.keySet().size()
		//def lowStock = quantityMap.findAll { it.value <= it?.key?.getInventoryLevel(location?.id)?.minQuantity }
        def lowStock = quantityMap.findAll { product,quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def minQuantity = inventoryLevelMap[product]?.first()?.minQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && minQuantity && quantity <= minQuantity
        }
        log.info "Get low stock: " + (System.currentTimeMillis() - startTime) + " ms"
		return lowStock
	}

	def getReorderStock(Location location) {
		long startTime = System.currentTimeMillis()
		def quantityMap = getQuantityByProductMap(location.inventory)
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
		def reorderStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def reorderQuantity = inventoryLevelMap[product]?.first()?.reorderQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && reorderQuantity && quantity <= reorderQuantity
        }
        log.info "Get reorder stock: " + (System.currentTimeMillis() - startTime) + " ms"
		return reorderStock
	}

    def getOverStock(Location location) {
        long startTime = System.currentTimeMillis()
        def quantityMap = getQuantityByProductMap(location.inventory)
        //def overStock = quantityMap.findAll { it.value > it?.key?.getInventoryLevel(location?.id)?.maxQuantity }
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory).groupBy { it.product }
        def overStock = quantityMap.findAll { product, quantity ->
            def inventoryLevel = inventoryLevelMap[product]?.first()
            def maxQuantity = inventoryLevelMap[product]?.first()?.maxQuantity
            inventoryLevel?.status >= InventoryStatus.SUPPORTED && maxQuantity && quantity > maxQuantity
        }
        log.info "Get over stock: " + (System.currentTimeMillis() - startTime) + " ms"
        return overStock
    }

	/**
	 * Get all products matching the given terms and categories.
	 * 
	 * @param terms
	 * @param categories
	 * @return
	 */
	List<Product> getProductsByTermsAndCategories(terms, categories, showHidden, currentInventory, maxResult, offset) {
		def startTime = System.currentTimeMillis()
		//def products = Product.executeQuery("select
		//"select inventory_item.lot_number from product left join inventory_item on inventory_item.product_id = product.id where product.name like '%lactomer%'"
		def unsupportedProducts = []
		if(!showHidden) {
			currentInventory?.configuredProducts?.each { 
				if(it.status != InventoryStatus.SUPPORTED) { 
					unsupportedProducts.add(it.product)					
				}
			}
		}

		def products = Product.createCriteria().list() {
			if (terms) {
				createAlias("inventoryItems", "inventoryItems", CriteriaSpecification.LEFT_JOIN)
                createAlias("inventoryLevels", "inventoryLevels", CriteriaSpecification.LEFT_JOIN)
			}
			if(categories) {
				inList("category", categories)
            }
			if (!showHidden) {
				not {
					inList("id", (unsupportedProducts.collect { it.id })?: [""])
				}
			}
			if (terms) {
				and {
                    terms.each { term ->
                        or {
                            ilike('inventoryItems.lotNumber', "%" + term + "%")
                            ilike('inventoryLevels.binLocation', "%" + term + "%")
                            ilike("name", "%" + term + "%")
                            ilike("description", "%" + term + "%")
                            ilike("brandName", "%" +term + "%")
                            ilike("manufacturer", "%" +term + "%")
                            ilike("manufacturerCode", "%" +term + "%")
                            ilike("manufacturerName", "%" + term + "%")
                            ilike("vendor", "%" + term + "%")
                            ilike("vendorCode", "%" + term + "%")
                            ilike("vendorName", "%" + term + "%")
                            ilike("upc", "%" + term + "%")
                            ilike("ndc", "%" + term + "%")
                            ilike("unitOfMeasure", "%" + term + "%")
                            ilike("productCode", "%" + term + "%")
                        }
                    }
				}
			}

		}
        log.info " * Query for products: " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()

		//products = products.collect { it }.unique { it.id }
        log.info " * Get unique IDs: " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()


        log.info "Max " + maxResult
        log.info "Offset " + offset
		products = Product.createCriteria().list(max: maxResult, offset: offset) {
			inList("id", (products.collect { it.id })?: [""])
			//order("category", "asc")
			order("name", "asc")
		}

        log.info " * Re-query for products by ID: " + (System.currentTimeMillis() - startTime) + " ms"
		startTime = System.currentTimeMillis()
	
		return products;	
	}

    /**
     * Get all products with the given tags.
     *
     * @param inputTags
     * @return
     */
    def getProductsByTags(List<String> inputTags) {
        return getProductsByTags(inputTags, 10, 0)
    }


	/**
	 * Get all products that have the given tags.
	 * 
	 * @param inputTags
	 * @return
	 */
	def getProductsByTags(List<String> inputTags, int max, int offset) {
        log.info "Get products by tags=${inputTags} max=${max} offset=${offset}"
		def products = Product.withCriteria {
			tags { 'in'('tag', inputTags) }
			if (max > 0) maxResults(max)
			firstResult(offset)
			//maxResults(params.max)
			//firstResult(params.offset)
		}
		return products
	}

	def countProductsByTags(List inputTags) {
        log.debug "Get products by tags: " + inputTags
		def results = Product.withCriteria {
			projections { count('id') }
			tags { 'in'('tag', inputTags) }
		}
		//log.debug "Results " + results[0]
		return results[0]
	}
	
	
	/**
	 * Get all products that have the given tag.
	 * 
	 * @param tag
	 * @return
	 */
	def getProductsByTag(String tag) {
		def products = Product.withCriteria {
			tags { eq('tag', tag) }
		}
		return products
	}

    def countProductsByShipment(shipment) {
        return getProductsByShipment(shipment, 0, 0)?.size()?:0
    }

    def getProductsByShipment(shipment, max, offset) {
        def products = []
        if (shipment) {
            shipment.shipmentItems.each { shipmentItem ->
                products << shipmentItem.inventoryItem.product
            }
        }
        return products;

    }

	/**
	 * Return a list of products that are marked as NON_SUPPORTED or NON_INVENTORIED
	 * at the given location.
	 *
	 * @param location
	 * @return
	 */
	List getHiddenProducts(Location location) {
		return getProductsByLocationAndStatuses(location, [
			InventoryStatus.NOT_SUPPORTED,
			InventoryStatus.SUPPORTED_NON_INVENTORY
		])
	}

	List getProductsWithoutInventoryLevel(Location location) {
		def session = sessionFactory.getCurrentSession()
		def query = session.createSQLQuery(
				"select * from product left outer join\
               (select * from inventory_level where inventory_level.inventory_id = :inventoryId) as i\
               on product.id = i.product_id").addEntity(Product.class);
		def products = query.setString("inventoryId", location.inventory.id).list();

		return products
	}

	List getProductsByLocationAndStatuses(Location location, List statuses) {
		log.debug("get products by statuses: " + location)
		def session = sessionFactory.getCurrentSession()
		def products = session.createQuery("select product from InventoryLevel as inventoryLevel \
		   right outer join inventoryLevel.product as product \
           where inventoryLevel.status IN (:statuses) \
		   and inventoryLevel.inventory.id = :inventoryId")
				.setParameterList("statuses", statuses)
				.setParameter("inventoryId", location?.inventory?.id)
				.list()
		return products
	}


	List getProductsByLocationAndStatus(Location location, InventoryStatus status) {
		log.debug("get products by status: " + location)
		def session = sessionFactory.getCurrentSession()
		def products = session.createQuery("select product from InventoryLevel as inventoryLevel \
	   		right outer join inventoryLevel.product as product \
	   		where inventoryLevel.status = :status \
	   		and inventoryLevel.inventory.id = :inventoryId")
				.setParameter("status", status)
				.setParameter("inventoryId", location?.inventory?.id)
				.list()
		return products
	}

	/**
	 * Get quantity for the given product and lot number at the given warehouse.
	 *
	 * @param warehouse
	 * @param product
	 * @param lotNumber
	 * @return
	 */
	Integer getQuantity(Location location, Product product, String lotNumber) {

		log.debug("Get quantity for product " + product?.name + " lotNumber " + lotNumber + " at location " + location?.name)
		if (!location) {
			throw new RuntimeException("Must specify location in order to calculate quantity on hand");
		}
		else {
			location = Location.get(location?.id)
		}
		def inventoryItem = findInventoryItemByProductAndLotNumber(product, lotNumber)
		if (!inventoryItem) {
			throw new RuntimeException("There's no inventory item for product " + product?.name + " lot number " + lotNumber)
		}

		return getQuantity(location.inventory, inventoryItem)
	}

	/**
	 * Converts a list of passed transaction entries into a quantity
	 * map indexed by product and then by inventory item
	 *
	 * Note that the transaction entries should all be from the same inventory,
	 * or the quantity results would be somewhat nonsensical
     *
     * Also note that this method is calculating backwards to get the current quantity on hand gor the given product
     * and its inventory items.
	 *
	 * TODO: add a parameter here to optionally take in a product, which means that we are only
	 * calculation for a single product, which means that we can stop after we hit a product inventory transaction?
	 *
	 * @param entries
	 * @return
	 */
	Map<Product, Map<InventoryItem, Integer>> getQuantityByProductAndInventoryItemMap(List<TransactionEntry> entries) {
		def startTime = System.currentTimeMillis()
		def quantityMap = [:]
        def reachedInventoryTransaction = [:]   // used to keep track of which items we've found an inventory transaction for
        def reachedProductInventoryTransaction = [:]  // used to keep track of which items we've found a product inventory transaction for

        if (entries) {

            // first make sure the transaction entries are sorted, with most recent first
            entries = entries.sort().reverse()

            entries.each { transactionEntry ->

                // There are cases where the transaction entry might be null, so we need to check for this edge case
                if (transactionEntry) {

                    def inventoryItem = transactionEntry.inventoryItem
                    def product = inventoryItem.product
                    def transaction = transactionEntry.transaction

                    //boolean hasReachedInventoryTransaction = false
                    //boolean hasReachedProductInventoryTransaction = false

                    // first see if this is an entry we can skip (because we've already reached a product inventory transaction
                    // for this product, or a inventory transaction for this inventory item)

                    //hasReachedInventoryTransaction = reachedInventoryTransaction[product][inventoryItem]

                    if (!(reachedProductInventoryTransaction[product] && reachedProductInventoryTransaction[product] != transaction) &&
                            !(reachedInventoryTransaction[product] && reachedInventoryTransaction[product][inventoryItem]
                                    && reachedInventoryTransaction[product][inventoryItem] != transaction)) {

                        //println "PROCESS ${product.name?.padRight(20)} LOT #${inventoryItem?.lotNumber?.padRight(10)} ${transaction?.transactionDate?.format("dd-MMM-yyyy hh:mma")?.padRight(20)} ${transaction?.transactionType?.transactionCode?.toString()?.padRight(10)} ${transactionEntry?.quantity?.toString()?.padRight(5)}"
                        // check to see if there's an entry in the map for this product and create if needed
                        if (!quantityMap[product]) {
                            quantityMap[product] = [:]
                        }

                        // check to see if there's an entry for this inventory item in the map and create if needed
                        if (!quantityMap[product][inventoryItem]) {
                            quantityMap[product][inventoryItem] = 0
                        }

                        //println "Transaction entry " + transactionEntry.id
                        //println "Transaction " + transactionEntry.transaction
                        // now update quantity as necessary
                        def code = transactionEntry.transaction.transactionType.transactionCode

                        if (code == TransactionCode.CREDIT) {
                            quantityMap[product][inventoryItem] += transactionEntry.quantity
                        }
                        else if (code == TransactionCode.DEBIT) {
                            quantityMap[product][inventoryItem] -= transactionEntry.quantity
                        }
                        else if (code == TransactionCode.INVENTORY) {
                            quantityMap[product][inventoryItem] += transactionEntry.quantity

                            // mark that we are done with this inventory item (after this transaction)
                            if (!reachedInventoryTransaction[product]) {
                                reachedInventoryTransaction[product] = [:]
                            }
                            reachedInventoryTransaction[product][inventoryItem] = transaction
                        }
                        else if (code == TransactionCode.PRODUCT_INVENTORY) {
                            quantityMap[product][inventoryItem] += transactionEntry.quantity

                            // mark that we are done with this product (after this transaction)
                            reachedProductInventoryTransaction[inventoryItem.product] = transaction
                        }
                        else {
                            throw new RuntimeException("Transaction code ${code} is not valid")
                        }
                    }
                    else {
                        //println "IGNORE  ${product.name?.padRight(20)} LOT #${inventoryItem?.lotNumber?.padRight(10)} ${transaction?.transactionDate?.format("dd-MMM-yyyy hh:mma")?.padRight(20)} ${transaction?.transactionType?.transactionCode?.toString()?.padRight(10)} ${transactionEntry?.quantity?.toString()?.padRight(5)}"

                    }
                }
            }
        }
        log.info " * getQuantityByProductAndInventoryItemMap(): " + (System.currentTimeMillis() - startTime) + " ms"

		return quantityMap
	}

	/**
	 * Converts a list of passed transaction entries into a quantity
	 * map indexed by product
	 *
	 * Note that the transaction entries should all be from the same inventory,
	 * or the quantity results would be somewhat nonsensical
	 *
	 * @param entries
	 * @return
	 */
	Map<Product, Integer> getQuantityByProductMap(List<TransactionEntry> entries) {
		def startTime = System.currentTimeMillis()
		def quantityMap = [:]

		// first get the quantity and inventory item map
		def quantityMapByProductAndInventoryItem = getQuantityByProductAndInventoryItemMap(entries)

		// now collapse this down to be by product
		quantityMapByProductAndInventoryItem.keySet().each {
			def product = it
			quantityMap[product] = 0
			quantityMapByProductAndInventoryItem[product].values().each { quantityMap[product] += it }
		}

        log.info "getQuantityByProductMap(transactionEntries): " + (System.currentTimeMillis() - startTime) + " ms"

		return quantityMap
	}


    Map getQuantityByInventoryItemMap(location, products) {
        def transactionEntries = getTransactionEntriesByInventoryAndProduct(location.inventory, products)
        return getQuantityByInventoryItemMap(transactionEntries)
    }

	/**
	 * Converts list of passed transactions entries into a quantity
	 * map indexed by inventory item
	 *
	 * @param entries
	 * @return
	 */
	Map getQuantityByInventoryItemMap(List<TransactionEntry> entries) {
		def startTime = System.currentTimeMillis()
		def quantityMap = [:]

		// first get the quantity and inventory item map
		def quantityByProductAndInventoryItemMap =
				getQuantityByProductAndInventoryItemMap(entries)

        //log.info "quantityByProductAndInventoryItemMap: " + quantityByProductAndInventoryItemMap

		// now collapse this down to be by product
		quantityByProductAndInventoryItemMap.keySet().each { product ->
			quantityByProductAndInventoryItemMap[product].keySet().each { inventoryItem ->
				if (!quantityMap[inventoryItem]) {
					quantityMap[inventoryItem] = 0;
				}
				quantityMap[inventoryItem] += quantityByProductAndInventoryItemMap[product][inventoryItem]
			}
		}
        log.info " * getQuantityByInventoryItemMap(): " + (System.currentTimeMillis() - startTime) + " ms"
        //log.info "quantityMap: " + quantityMap
		return quantityMap
	}

    def getQuantityByProductGroup(Location location) {
        def quantityMap = getQuantityByProductMap(location.inventory)

        return quantityMap
    }


	/**
	 * Get a map of quantities (indexed by product) for a particular inventory.
	 *
	 * TODO This might perform poorly as we add more and more transaction entries
	 * into an inventory.
	 *
	 * @param inventoryInstance
	 * @return
	 */
    Map<Product, Integer> getQuantityByProductMap(String locationId) {
        def location = Location.get(locationId)
        return getQuantityByProductMap(location.inventory)
    }


	Map<Product, Integer> getQuantityByProductMap(Inventory inventory) {
        def startTime = System.currentTimeMillis()
		def transactionEntries = getTransactionEntriesByInventory(inventory);
		def quantityMap = getQuantityByProductMap(transactionEntries)

        log.info "getQuantityByProductMap(inventory): " + (System.currentTimeMillis() - startTime) + " ms"
		
		return quantityMap
	}


    List<Product> getProductsByInventory(Inventory inventory) {
        InventoryLevel.executeQuery("select il.product from InventoryLevel as il where il.inventory = :inventory", [inventory:inventory])
    }

    /**
     * Get a map of quantities (indexed by product) for the given location
     * @param location
     * @param products
     * @return
     */
    Map<Product, Integer> getQuantityByProductMap(Location location, List<Product> products) {
        return getQuantityByProductMap(location.inventory, products)
    }

	/**
	 * Get a map of quantities (indexed by product) for a particular inventory.
	 *
	 * @param inventoryInstance
	 * @return
	 */
	Map<Product, Integer> getQuantityByProductMap(Inventory inventory, List<Product> products) {
		long startTime = System.currentTimeMillis()
        log.debug "get quantity by product map "

        log.debug "inventory: " + inventory
        log.debug "products: " + products.size()
		def transactionEntries = getTransactionEntriesByInventoryAndProduct(inventory, products);
		def quantityMap = getQuantityByProductMap(transactionEntries)
        log.info "quantityMap: " + quantityMap.keySet()

        // FIXME Hacky way to make sure all products that have been passed in have an entry in the quantity map
        // FIXME Requires a proper implementation for hashCode/equals methods which was disabled due to a different bug
        products.each {
            def product = Product.get(it.id)
            log.debug "contains key for product ${product.productCode} ${product.name}: " + quantityMap.containsKey(product)
            if (!quantityMap.containsKey(product)) {
                quantityMap[product] = 0
            }
        }

        log.info "getQuantityByProductMap(): " + (System.currentTimeMillis() - startTime) + " ms"

		return quantityMap
	}

	/**
	 * Gets a product-to-quantity maps for all products in the selected inventory
	 * whose quantity falls below a the minimum or reorder level
	 * (Note that items that are below the minimum level are excluded from
	 * the list of items below the reorder level)
	 *
	 * Set the includeUnsupported boolean to include unsupported items when compiling the product-to-quantity maps
	 */
	Map<String, Map<Product, Integer>> getProductsBelowMinimumAndReorderQuantities(Inventory inventoryInstance, Boolean includeUnsupported) {
		long startTime = System.currentTimeMillis()
		def inventoryLevels = getInventoryLevelsByInventory(inventoryInstance)

		Map<Product, Integer> reorderProductsQuantityMap = new HashMap<Product, Integer>()
		Map<Product, Integer> minimumProductsQuantityMap = new HashMap<Product, Integer>()

		for (level in inventoryLevels) {
			
			def quantityMap = getQuantityByInventoryAndProduct(inventoryInstance, level.product)
			
			// getQuantityByInventory returns an Inventory Item to Quantity map, so we want to sum all the values in this map to get the total quantity
			def quantity = quantityMap?.values().sum { it } ?: 0

			// The product is supported or the user asks for unsupported products to be included
			if (level.status == InventoryStatus.SUPPORTED || includeUnsupported) {
				if (quantity <= level.minQuantity) {
					minimumProductsQuantityMap[level.product] = quantity
				}
				else if (quantity <= 0) {
					minimumProductsQuantityMap[level.product] = quantity
				}
				else if (quantity <= level.reorderQuantity) {
					reorderProductsQuantityMap[level.product] = quantity
				}
			}
		}
        log.info "getProductsBelowMinimumAndReorderQuantities(): " + (System.currentTimeMillis() - startTime) + " ms"
		return [minimumProductsQuantityMap: minimumProductsQuantityMap, reorderProductsQuantityMap: reorderProductsQuantityMap]
	}
	/**
	 * @return current location from thread local
	 */
	Location getCurrentLocation() {
		def currentLocation = AuthService?.currentLocation?.get()
		if (!currentLocation?.inventory)
			throw new Exception("Inventory not found")
		return currentLocation
	}

	/**
	 * @param location
	 * @param product
	 * @return	get quantity by location and product
	 */
	Integer getQuantityAvailableToPromise(Location location, Product product) { 
		def quantityMap = getQuantityForProducts(location.inventory, [product.id])
        log.debug "quantity map " + quantityMap;
		def quantityOnHand = getQuantityOnHand(location, product)?:0
		def quantityOutgoing = getQuantityToShip(location, product)?:0
		def quantityAvailableToPromise = quantityOnHand - quantityOutgoing
		
		return quantityAvailableToPromise?:0
	}
	
	/**
	 * @param location
	 * @param product
	 * @return	get quantity by location and product
	 */
	Integer getQuantityOnHand(Location location, Product product) {
		log.info "quantity on hand for location " + location + " product " + product
		def quantityMap = getQuantityForProducts(location.inventory, [product.id])
        log.debug "quantity map " + quantityMap;
		def quantity = quantityMap[product.id]
		
		return quantity?:0
	}
	
	Integer getQuantityToReceive(Location location, Product product) { 		
		Map quantityMap = getIncomingQuantityByProduct(location, [product]);
		def quantity = quantityMap[product]
		return quantity?:0		
	}

	Integer getQuantityToShip(Location location, Product product) {
		Map quantityMap = getOutgoingQuantityByProduct(location, [product]);
		def quantity = quantityMap[product]
		return quantity?:0
	}

	
	/**
	 * @param inventoryItem
	 * @return current quantity of the given inventory item.
	 */
	Integer getQuantity(InventoryItem inventoryItem) {
		def startTime = System.currentTimeMillis()
		def currentLocation = getCurrentLocation()
		def quantity = getQuantity(currentLocation.inventory, inventoryItem)
        log.info "getQuantity(): " + (System.currentTimeMillis() - startTime) + " ms"
		return quantity
	}

	/**
	 * Gets the quantity of a specific inventory item at a specific inventory
	 *
	 * @param item
	 * @param inventory
	 * @return
	 */
	Integer getQuantity(Inventory inventory, InventoryItem inventoryItem) {

		if (!inventory) {
            log.debug AuthService.currentLocation
			def currentLocation = AuthService?.currentLocation?.get()
            log.debug currentLocation
			if (!currentLocation?.inventory)
				throw new Exception("Inventory not found")

			inventory = currentLocation.inventory
		}
		def transactionEntries = getTransactionEntriesByInventoryAndInventoryItem(inventory, inventoryItem)
		def quantityMap = getQuantityByInventoryItemMap(transactionEntries)

		// FIXME was running into an issue where a proxy object was being used to represent the inventory item
		// so the map.get() method was returning null.  So we needed to fully load the inventory item using
		// the GORM get method.
        // inventoryItem -> org.pih.warehouse.inventory.InventoryItem_$$_javassist_10
        //log.debug "inventoryItem -> " + inventoryItem.class
		inventoryItem = InventoryItem.get(inventoryItem.id)
		Integer quantity = quantityMap[inventoryItem]
		return quantity ?: 0;
	}


	Integer getQuantityAvailableToPromise(InventoryItem inventoryItem) {
		def currentLocation = getCurrentLocation()
		return getQuantityAvailableToPromise(currentLocation.inventory, inventoryItem)
	}

	Integer getQuantityAvailableToPromise(Inventory inventory, InventoryItem inventoryItem) {
		def quantityOnHand = getQuantity(inventory, inventoryItem)
		/*
		 def shipmentItems = ShipmentItem.findAllByInventoryItem(inventoryItem)
		 shipmentItems.findAll { (it?.shipment?.origin == inventory?.warehouse) && it?.shipment?.isPending() }
		 def quantityShipping = shipmentItems.sum { it.quantity }
		 def picklistItems = PicklistItem.findAllByInventoryItem(inventoryItem)
		 picklistItems.findAll { it?.picklist?.request?.origin == inventory?.warehouse }
		 def quantityPicked = picklistItems.sum { it.quantity }
		 */


		return quantityOnHand;
	}

    /**
     * Get the most recent quantity on hand from inventory item snapshot table. If there are no
     * records in the inventory item snapshot table then we calculate the QoH from transactions.
     *
     * @param location
     * @return
     */
    Map<InventoryItem, Integer> getQuantityByLocation(Location location) {
        Map<InventoryItem, Integer> quantityMap = getMostRecentInventoryItemSnapshot(location)
        if (!quantityMap) {
            quantityMap = getQuantityForInventory(location?.inventory)
        }
        return quantityMap;
    }

	/**
	 * Calculate quantity on hand values for all available inventory items at the given inventory.
	 *
     * FIXME Use sparingly - this is very expensive because it calculates the QoH over an entire inventory.
     *
	 * @param inventory
	 * @return
	 */
	Map<InventoryItem, Integer> getQuantityForInventory(Inventory inventory) {
		def transactionEntries = getTransactionEntriesByInventory(inventory);
		return getQuantityByInventoryItemMap(transactionEntries);
	}

    /**
     * Calculate quantity on hand values for all available inventory items in the given inventory.
     *
     * @param inventory
     * @param products
     * @return
     */
    Map<InventoryItem, Integer> getQuantityForInventory(Inventory inventory, List<Product> products) {
        def transactionEntries = getTransactionEntriesByInventoryAndProduct(inventory, products)
        return getQuantityByInventoryItemMap(transactionEntries);
    }


    Map<InventoryItem, Integer> getMostRecentInventoryItemSnapshot(Location location) {
        Map quantityMap = [:]
        def results = InventoryItemSnapshot.executeQuery("""
            SELECT a.inventoryItem, a.quantityOnHand, DATEDIFF(a.inventoryItem.expirationDate, current_date) as daysToExpiry
            FROM InventoryItemSnapshot a
            WHERE a.date = (select max(b.date) from InventoryItemSnapshot b)
            AND a.location = :location
            AND a.quantityOnHand > 0
            ORDER BY a.inventoryItem.expirationDate ASC
            """, [location:location])


        results.each {
            quantityMap[it[0]] = it[1]
        }

        return quantityMap
    }

	/**
	 * Fetches and populates a StockCard Command object
	 *
	 * @param cmd
	 * @param params
	 * @return
	 */
    //@Cacheable("stockCardCommandCache")
	StockCardCommand getStockCardCommand(StockCardCommand cmd, Map params) {
		// Get basic details required for the whole page
		cmd.productInstance = Product.get(params?.product?.id ?: params.id);  // check product.id and id
        if (!cmd.productInstance) {
            throw new ProductException("Product with identifier '${params?.product?.id?:params.id}' could not be found")
        }

		cmd.inventoryInstance = cmd.warehouseInstance?.inventory
		cmd.inventoryLevelInstance = getInventoryLevelByProductAndInventory(cmd.productInstance, cmd.inventoryInstance)

		// Get current stock of a particular product within an inventory
		// Using set to make sure we only return one object per inventory items
		Set inventoryItems = getInventoryItemsByProductAndInventory(cmd.productInstance, cmd.inventoryInstance);
		cmd.inventoryItemList = inventoryItems as List
		cmd.inventoryItemList?.sort { it.expirationDate }?.sort { it.lotNumber }

        cmd.totalQuantity = getQuantityOnHand(cmd.warehouseInstance, cmd.productInstance)

		// Get all lot numbers for a given product
		cmd.lotNumberList = getInventoryItemsByProduct(cmd?.productInstance) as List

		// Get transaction log for a particular product within an inventory
		cmd.transactionEntryList = getTransactionEntriesByInventoryAndProduct(cmd.inventoryInstance, [cmd.productInstance]);
		cmd.transactionEntriesByInventoryItemMap = cmd.transactionEntryList.groupBy { it.inventoryItem }
		cmd.transactionEntriesByTransactionMap = cmd.transactionEntryList.groupBy { it.transaction }

		// create the quantity map for this product
		cmd.quantityByInventoryItemMap = getQuantityByInventoryItemMap(cmd.transactionEntryList)

		return cmd
	}


    def getStockHistory(Inventory inventory, Product product) {
        return getTransactionEntriesByInventoryAndProduct(inventory, [product]);
    }

	/**
	 * Fetches and populates a RecordInventory Command object
	 *
	 * @param commandInstance
	 * @param params
	 * @return
	 */
	void populateRecordInventoryCommand(RecordInventoryCommand commandInstance, Map params) {
		log.debug "Params " + params;

		// set the default transaction date to today
		commandInstance.transactionDate = new Date()
		//commandInstance.transactionDate.clearTime()

		if (!commandInstance?.productInstance) {
			commandInstance.errors.reject("error.product.invalid", "Product does not exist");
		}
		else {
			commandInstance.recordInventoryRow = new RecordInventoryRowCommand();

			// get all transaction entries for this product at this inventory
			def transactionEntryList = getTransactionEntriesByInventoryAndProduct(commandInstance?.inventoryInstance, [
				commandInstance?.productInstance
			])
			// create a map of inventory item quantities from this
			def quantityByInventoryItemMap = getQuantityByInventoryItemMap(transactionEntryList)

			quantityByInventoryItemMap.keySet().each {
				def quantity = quantityByInventoryItemMap[it]

				def inventoryItemRow = new RecordInventoryRowCommand()
				inventoryItemRow.id = it.id
				inventoryItemRow.lotNumber = it.lotNumber
				inventoryItemRow.expirationDate = it.expirationDate
				//inventoryItemRow.description = it.description;
				inventoryItemRow.oldQuantity = quantity
				inventoryItemRow.newQuantity = quantity
				commandInstance.recordInventoryRows.add(inventoryItemRow)
			}

		}
		//return commandInstance
	}

	/**
	 * Processes a RecordInventory Command object and perform updates
	 *
	 * @param cmd
	 * @param params
	 * @return
	 */
	RecordInventoryCommand saveRecordInventoryCommand(RecordInventoryCommand cmd, Map params) {
		log.debug "Saving record inventory command params: " + params

		try {
			// Validation was done during bind, but let's do this just in case
			//if (cmd.validate()) {
				def inventoryItems = getInventoryItemsByProductAndInventory(cmd.productInstance, cmd.inventoryInstance)
				// Create a new transaction
				def transaction = new Transaction(cmd.properties)
				transaction.inventory = cmd.inventoryInstance
				transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)

				// Process each row added to the record inventory page
				cmd.recordInventoryRows.each { row ->

					if (row) {
						// 1. Find an existing inventory item for the given lot number and product and description
						def inventoryItem =
								findInventoryItemByProductAndLotNumber(cmd.productInstance, row.lotNumber)

						// 2. If the inventory item doesn't exist, we create a new one
						if (!inventoryItem) {
							inventoryItem = new InventoryItem();
							inventoryItem.properties = row.properties
							inventoryItem.product = cmd.productInstance;
							if (!inventoryItem.hasErrors() && inventoryItem.save()) {

							}
							else {
								// TODO Old error message = "Property [${error.getField()}] of [${inventoryItem.class.name}] with value [${error.getRejectedValue()}] is invalid"
								inventoryItem.errors.allErrors.each { error ->
									cmd.errors.reject("inventoryItem.invalid",
											[
												inventoryItem,
												error.getField(),
												error.getRejectedValue()] as Object[],
											"[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");

								}
								// We need to fix these errors before we can move on
								return cmd;
							}
						}
						// 3. Create a new transaction entry (even if quantity didn't change)
						def transactionEntry = new TransactionEntry();
						transactionEntry.properties = row.properties;
						transactionEntry.quantity = row.newQuantity
						transactionEntry.inventoryItem = inventoryItem;
						transaction.addToTransactionEntries(transactionEntry);
					}
				}

                // 4. Make sure that the inventory item has been saved before we process the transactions
				if (!cmd.hasErrors()) {
					// Check if there are any changes recorded ... no reason to
					if (!transaction.transactionEntries) {
						// We could do this, but it keeps us from changing the lot number and description
						cmd.errors.reject("transaction.noChanges", "There are no quantity changes in the current transaction");
					}
					else {
						if (!transaction.hasErrors() && transaction.save()) {
							// We saved the transaction successfully
						}
						else {
							transaction.errors.allErrors.each { error ->
								cmd.errors.reject("transaction.invalid",
										[
											transaction,
											error.getField(),
											error.getRejectedValue()] as Object[],
										"Property [${error.getField()}] of [${transaction.class.name}] with value [${error.getRejectedValue()}] is invalid");
							}
						}
					}
				}
			//}
		} catch (Exception e) {
			log.error("Error saving an inventory record to the database ", e);
			throw e;
		}
		return cmd;
	}

	/**
	 * Returns a list of categories and their children, given an initial set of
	 * categories.  This function should probably be recursive so that we traverse
	 * the entire category tree.
	 *
	 * @param categories
	 * @return
	 */
	List getExplodedCategories(List categories) {
		def matchCategories = []
		if (categories) {
			categories.each { category ->
				if (category) {
					matchCategories << category;
					matchCategories.addAll((category?.children) ? getExplodedCategories(category?.children) : []);
				}
			}
		}
		return matchCategories;
	}

	/**
	 * Return a list of categories that match the given search terms.
	 *
	 * @param searchTerms
	 * @return
	 */
	List getCategoriesMatchingSearchTerms(List searchTerms) {
		def categories = []
		if (searchTerms) {

			categories = Category.createCriteria().list() {
				or {
					searchTerms.each { searchTerm ->
						ilike("name", "%" + searchTerm + "%")
					}
				}
			}
		}
		return getExplodedCategories(categories)
		//return categories;
	}

	/**
	 * Get all products for the given category.
	 *
	 * @param category
	 * @return
	 */
	List getProductsByCategory(Category category) {
		def products = Product.createCriteria().list() { eq("category", category) }
		return products;
	}

	/**
	 *
	 * @param category
	 * @return
	 */
	List getProductsByNestedCategory(Category category) {
		def products = [];
		if (category) {
			def categories = (category?.children) ?: [];
			categories << category;
			if (categories) {
				log.debug("get products by nested category: " + category + " -> " + categories)

				products = Product.createCriteria().list() { 'in'("category", categories) }
			}
		}
		return products;
	}

	/**
	 * @param productId
	 * @return a list of inventory items
	 */
	List getInventoryItemsByProduct(Product productInstance) {
		if (!productInstance)
			throw new Exception("ProductNotFoundException")
		return InventoryItem.findAllByProduct(productInstance)

	}

	/**
	 * @param productIds
	 * @return a map of inventory items by product
	 */
	Map getInventoryItemsByProducts(Location warehouse, List<Integer> productIds) {
		def inventoryItemMap = [:]
		if (productIds) {
			//def inventory = Inventory.get(warehouse?.inventory?.id);
			productIds.each {
				def product = Product.get(it);
				//def inventoryItems = getInventoryItemsByProductAndInventory(product, inventory);
				inventoryItemMap[product] = getInventoryItemsByProduct(product)
			}
		}
		return inventoryItemMap;
	}

	/**
	 * @return a map of inventory items indexed by product
	 */
	Map getInventoryItems() {
		def inventoryItemMap = [:]
		Product.list().each { inventoryItemMap.put(it, []) }
		InventoryItem.list().each {
			inventoryItemMap[it.product] << it
		}
		return inventoryItemMap;
	}


	List<InventoryItem> findInventoryItemWithEmptyLotNumber() {
		return InventoryItem.createCriteria().list() {
			or {
				isNull("lotNumber")
				eq("lotNumber", "")
			}
		}
	}

	List<Product> findProductsWithoutEmptyLotNumber() {
		def products = Product.list();
		def productsWithEmptyInventoryItem = findInventoryItemWithEmptyLotNumber()?.collect { it.product };
		def productsWithoutEmptyInventoryItem = products - productsWithEmptyInventoryItem;
		return productsWithoutEmptyInventoryItem;
	}

	/**
	 * Finds the inventory item for the given product and lot number.
	 *
	 * @param product the product of the desired inventory item
	 * @param lotNumber the lot number of the desired inventory item
	 * @return a single inventory item
	 */
	InventoryItem findInventoryItemByProductAndLotNumber(Product product, String lotNumber) {
		log.debug("Find inventory item by product " + product?.id + " and lot number '" + lotNumber + "'")
		def inventoryItems = InventoryItem.createCriteria().list() {
			and {
				eq("product.id", product?.id)
				if (lotNumber) {
					log.debug "lot number is not null"
					eq("lotNumber", lotNumber)
				}
				else {
					or {
						log.debug "lot number is null"
						isNull("lotNumber")
						eq("lotNumber", "")
					}
				}
			}
		}
		log.debug("Returned inventory items " + inventoryItems);
		// If the list is non-empty, return the first item
		if (inventoryItems) {
			return inventoryItems.get(0);
		}
		return null;
	}

	/**
	 * Uses an inventory item that is bound at the controller.
	 *
	 * @param inventoryItem
	 * @return
	 */
	InventoryItem findOrCreateInventoryItem(InventoryItem inventoryItem) {
		return findOrCreateInventoryItem(inventoryItem.product, inventoryItem.lotNumber, inventoryItem.expirationDate)
	}

	/**
	 * TODO Need to finish this method.
	 *
	 * @param product
	 * @param lotNumber
	 * @param expirationDate
	 * @return
	 */
	InventoryItem findOrCreateInventoryItem(Product product, String lotNumber, Date expirationDate) {
		def inventoryItem =
				findInventoryItemByProductAndLotNumber(product, lotNumber);

		// If the inventory item doesn't exist, we create a new one
		if (!inventoryItem) {
			inventoryItem = new InventoryItem();
			inventoryItem.lotNumber = lotNumber
			inventoryItem.expirationDate = expirationDate;
			inventoryItem.product = product
			inventoryItem.save()
		}
		return inventoryItem

	}

	/**
	 * Get all inventory items for a given product within the given inventory.
	 *
	 * @param productInstance
	 * @param inventoryInstance
	 * @return a list of inventory items.
	 */
	Set getInventoryItemsByProductAndInventory(Product productInstance, Inventory inventoryInstance) {
		def inventoryItems = [] as Set
		def transactionEntries = getTransactionEntriesByInventoryAndProduct(inventoryInstance, [productInstance]);
		transactionEntries.each { inventoryItems << it.inventoryItem; }
		inventoryItems = inventoryItems.sort { it.expirationDate }.sort { it.lotNumber }
		return inventoryItems;
	}

	/**
	 * Get all the inventory levels associated with the given inventory
	 */
	List<InventoryLevel> getInventoryLevelsByInventory(Inventory inventoryInstance) {
		return InventoryLevel.findAllByInventory(inventoryInstance)
	}

	/**
	 * Get a single inventory level instance for the given product and inventory.
	 *
	 * @param productInstance
	 * @param inventoryInstance
	 * @return
	 */
	InventoryLevel getInventoryLevelByProductAndInventory(Product productInstance, Inventory inventoryInstance) {
		def inventoryLevel = InventoryLevel.findByProductAndInventory(productInstance, inventoryInstance)

		return (inventoryLevel) ?: new InventoryLevel();

	}


	/**
	 * Get all transaction entries over all products/inventory items.
	 *
	 * @param inventoryInstance
	 * @return
	 */
	List getTransactionEntriesByInventory(Inventory inventory) {
        def startTime = System.currentTimeMillis()
		def criteria = TransactionEntry.createCriteria();
		def transactionEntries = criteria.list {
			transaction {
				eq("inventory", inventory)
				order("transactionDate", "asc")
				order("dateCreated", "asc")
			}
		}
        log.info "getTransactionEntriesByInventory(): " + (System.currentTimeMillis() - startTime)


		return transactionEntries;
	}

	/**
	 * Get all transaction entries over list of products/inventory items.
	 *
	 * @param inventoryInstance
	 * @return
	 */
	List getTransactionEntriesByInventoryAndProduct(Inventory inventory, List<Product> products) {
		def criteria = TransactionEntry.createCriteria();
		def transactionEntries = criteria.list {
			transaction {
				eq("inventory", inventory)
				order("transactionDate", "asc")
				order("dateCreated", "asc")
			}
			if (products) {
				inventoryItem { inList("product", products) }
			}
		}
		return transactionEntries;
	}

	/**
	 * Gets all transaction entries for a inventory item within an inventory
	 *
	 * @param inventoryItem
	 * @param inventory
	 */
	List getTransactionEntriesByInventoryAndInventoryItem(Inventory inventory, InventoryItem item) {
		return TransactionEntry.createCriteria().list() {
            inventoryItem { eq("product", item?.product) }
            //eq("inventoryItem", inventoryItem)
            transaction { eq("inventory", inventory) }
		}
	}

	/**
	 * Adjusts the stock level by adding a new transaction entry with a
	 * quantity change.
	 *
	 * Passing in an instance of inventory item so that we can attach errors.
	 * @param inventoryItem
	 * @param params
	 */
	boolean adjustStock(InventoryItem itemInstance, Map params) {

		def inventoryInstance = Inventory.get(params?.inventory?.id)

		if (itemInstance && inventoryInstance) {
			def transactionEntry = new TransactionEntry(params);
			def quantity = 0;
			try {
				quantity = Integer.valueOf(params?.newQuantity);
			} catch (Exception e) {
				itemInstance.errors.reject("inventoryItem.quantity.invalid")
			}

			def transactionInstance = new Transaction(params);
			if (transactionEntry.hasErrors() || transactionInstance.hasErrors()) {
				itemInstance.errors = transactionEntry.errors
				itemInstance.errors = transactionInstance.errors
			}

			if (!itemInstance.hasErrors() && itemInstance.save()) {
				// Need to create a transaction if we want the inventory item
				// to show up in the stock card
				transactionInstance.transactionDate = new Date();
				//transactionInstance.transactionDate.clearTime();
				transactionInstance.transactionType = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID);
				transactionInstance.inventory = inventoryInstance;

				// Add transaction entry to transaction
				transactionEntry.inventoryItem = itemInstance;
				transactionEntry.quantity = quantity
				transactionInstance.addToTransactionEntries(transactionEntry);
				if (!transactionInstance.hasErrors() && transactionInstance.save()) {

				}
				else {
					transactionInstance?.errors.allErrors.each { itemInstance.errors << it; }
				}
			}
		}
	}
	
	
	/**
	 * Adjusts the stock level by adding a new transaction entry with a
	 * quantity change.
	 *
	 * Passing in an instance of inventory item so that we can attach errors.
	 * @param inventoryItem
	 * @param params
	 */
	def transferStock(InventoryItem inventoryItem, Inventory inventory, Location destination, Location source, Integer quantity) {
		def transaction = new Transaction();
		//def inventoryInstance = Inventory.get(params?.inventory?.id)
		Integer quantityOnHand = getQuantity(inventoryItem);
        log.debug "quantityOnHand: " + quantityOnHand
		
		if (inventoryItem && inventory) {
			def transactionEntry = new TransactionEntry();
			transactionEntry.quantity = quantity;
			transactionEntry.inventoryItem = inventoryItem

            if (destination && source) {
                transaction.errors.reject("Cannot specify both destination and source in a transaction")
            }
            if (destination) {
                if (!destination?.inventory) {
                    //throw new RuntimeException("Destination does not have an inventory")
                    transaction.errors.reject("Destination does not have an inventory")
                }
                if (inventory == destination?.inventory) {
                    //throw new RuntimeException("Destination must be different from source")
                    transaction.errors.reject("Cannot transfer to the same location")
                }

                // Only check this if the transfer is going out (we don't want to restrict if the stock is being transferred in)
                if (quantityOnHand < quantity) {
                    //inventoryItem.errors.reject("inventoryItem.quantity.invalid")
                    //throw new RuntimeException("Cannot exceed quantity on hand")
                    transaction.errors.reject("Cannot exceed quantity on hand")
                }

            }
            if (source) {
                if (!source?.inventory) {
                    //throw new RuntimeException("Destination does not have an inventory")
                    transaction.errors.reject("Source does not have an inventory")
                }
                if (inventory == source?.inventory) {
                    //throw new RuntimeException("Destination must be different from source")
                    transaction.errors.reject("Cannot transfer from the same location")
                }
            }

			if (quantity <= 0) { 
				//throw new RuntimeException("Quantity must be greater than 0")
				transaction.errors.reject("Quantity must be greater than 0")
			}

			
			// Need to create a transaction if we want the inventory item
			// to show up in the stock card
			transaction.transactionDate = new Date();		
			transaction.destination = destination;
            transaction.source = source
			transaction.transactionType = (destination) ? TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) : TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)
            transaction.inventory = inventory;
			transaction.transactionNumber = generateTransactionNumber()

			// Add transaction entry to transaction
			transactionEntry.inventoryItem = inventoryItem;
			transactionEntry.quantity = quantity
			transaction.addToTransactionEntries(transactionEntry);
			
			if (!transaction.hasErrors() && transaction.save()) {
				// saved successfully
				log.info("Saved transaction " + transaction)
				if (!saveLocalTransfer(transaction)) {
					throw new ValidationException("Unable to save local transfer", transaction.errors)
				}
			}
		}
		return transaction
	}

	/**
	 * Create a transaction for the Send Shipment event.
	 *
	 * @param shipmentInstance
	 */
	void createSendShipmentTransaction(Shipment shipmentInstance) {
		log.debug "create send shipment transaction"

		if (!shipmentInstance.origin.isWarehouse()) {
			throw new RuntimeException("Can't create send shipment transaction for origin that is not a depot")
		}

		try {
			// Create a new transaction for outgoing items
			Transaction debitTransaction = new Transaction();
			debitTransaction.transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
			debitTransaction.source = null
			//debitTransaction.destination = shipmentInstance?.destination.isWarehouse() ? shipmentInstance?.destination : null
			debitTransaction.destination = shipmentInstance?.destination
			debitTransaction.inventory = shipmentInstance?.origin?.inventory ?: addInventory(shipmentInstance.origin)
			debitTransaction.transactionDate = shipmentInstance.getActualShippingDate()

			shipmentInstance.shipmentItems.each {
				def inventoryItem =
						findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)

				// If the inventory item doesn't exist, we create a new one
				if (!inventoryItem) {
					inventoryItem = new InventoryItem();
					inventoryItem.lotNumber = it.lotNumber
					inventoryItem.product = it.product
					if (!inventoryItem.hasErrors() && inventoryItem.save()) {
						// at this point we've saved the inventory item successfully
					}
					else {
						//
						inventoryItem.errors.allErrors.each { error ->
							def errorObj = [
								inventoryItem,
								error.getField(),
								error.getRejectedValue()] as Object[]
							shipmentInstance.errors.reject("inventoryItem.invalid",
									errorObj, "[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");
						}
						return;
					}
				}

				// Create a new transaction entry for each shipment item
				def transactionEntry = new TransactionEntry();
				transactionEntry.quantity = it.quantity;
				transactionEntry.inventoryItem = inventoryItem;
				debitTransaction.addToTransactionEntries(transactionEntry);
			}

			if (!debitTransaction.save()) {
				log.debug "debit transaction errors " + debitTransaction.errors
				throw new TransactionException(message: "An error occurred while saving ${debitTransaction?.transactionType?.transactionCode} transaction", transaction: debitTransaction);
			}

			// Associate the incoming transaction with the shipment
			shipmentInstance.addToOutgoingTransactions(debitTransaction)
			shipmentInstance.save();

		} catch (Exception e) {
			log.error("An error occrred while creating transaction ", e);
			throw e
			//shipmentInstance.errors.reject("shipment.invalid", e.message);  // this doesn't seem to working properly
		}
	}

	/**
	 * Finds the local transfer (if any) associated with the given transaction
	 *
	 * @param transaction
	 * @return
	 */
	LocalTransfer getLocalTransfer(Transaction transaction) {
		LocalTransfer transfer = null
		transfer = LocalTransfer.findBySourceTransaction(transaction)
		if (transfer == null) { transfer = LocalTransfer.findByDestinationTransaction(transaction) }
		return transfer
	}

	/**
	 * Returns true/false if the given transaction is associated with a local transfer
	 *
	 * @param transaction
	 * @return
	 */
	Boolean isLocalTransfer(Transaction transaction) {
		getLocalTransfer(transaction) ? true : false
	}

	/**
	 * Returns true/false if the passed transaction is a valid candidate for a local transfer
	 *
	 * @param transaction
	 * @return
	 */
	Boolean isValidForLocalTransfer(Transaction transaction) {
		// make sure that the transaction is of a valid type
		if (transaction?.transactionType?.id != Constants.TRANSFER_IN_TRANSACTION_TYPE_ID &&
		transaction?.transactionType?.id != Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
			return false
		}

		// make sure we are operating only on locally managed warehouses
		if (transaction?.source) {
			if (!(transaction?.source instanceof Location)) {   //todo: should use source.isWarehouse()? hibernate always set source to a location
				return false
			}
			else if (!transaction?.source.local) {
				return false
			}
		}
		if (transaction?.destination) {
			if (!(transaction?.destination instanceof Location)) { //todo: should use destination.isWarehouse()? hibernate always set destination to a location
				return false
			}
			else if (!transaction?.destination.local) {
				return false
			}
		}

		return true
	}

	/**
	 * Deletes a local transfer (and underlying transactions) associated with the passed transaction
	 *
	 * @param transaction
	 */
	void deleteLocalTransfer(Transaction transaction) {
		LocalTransfer transfer = getLocalTransfer(transaction)
		if (transfer) {
			transfer.delete(flush: true)
		}
	}

	/**
	 * Creates or updates the local transfer associated with the given transaction
	 * Returns true if the save/update was successful
	 *
	 * @param baseTransaction
	 * @return
	 */
	Boolean saveLocalTransfer(Transaction baseTransaction) {
		// note than we are using exceptions here to take advantage of Grails built-in transactional capabilities on service methods
		// if there is an error, we want to throw an exception so the whole transaction is rolled back
		// (we can trap these exceptions if we want in the calling controller)

		if (!isValidForLocalTransfer(baseTransaction)) {
			throw new RuntimeException("Invalid transaction for creating a local transaction")
		}

		// first save the base transaction
		if (!baseTransaction.save(flush: true)) {
			throw new RuntimeException("Unable to save base transaction " + baseTransaction?.id)
		}

		// try to fetch any existing local transfer
		LocalTransfer transfer = getLocalTransfer(baseTransaction)

		// if there is no existing local transfer, we need to create a new one and set the source or destination transaction as appropriate
		if (!transfer) {
			transfer = new LocalTransfer()
			if (baseTransaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
				transfer.sourceTransaction = baseTransaction
			}
			else {
				transfer.destinationTransaction = baseTransaction
			}
		}

		// create and save the new mirrored transaction
		Transaction mirroredTransaction = createMirroredTransaction(baseTransaction)
		mirroredTransaction.transactionNumber = generateTransactionNumber()
		if (!mirroredTransaction.save(flush: true)) {
			throw new RuntimeException("Unable to save mirrored transaction " + mirroredTransaction?.id)
		}

		// now assign this mirrored transaction to the local transfer
		Transaction oldTransaction
		if (baseTransaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
			oldTransaction = transfer.destinationTransaction
			transfer.destinationTransaction = mirroredTransaction
		}
		else {
			oldTransaction = transfer.sourceTransaction
			transfer.sourceTransaction = mirroredTransaction
		}

		// save the local transfer
		if (!transfer.save(flush: true)) {
			throw new RuntimeException("Unable to save local transfer " + transfer?.id)
		}

		// delete the old transaction
		if (oldTransaction) {
			oldTransaction.delete(flush: true)
		}

		return true
	}

	/**
	 * Private utility method to create a "mirror" of the given transaction
	 * (If given a Transfer Out transaction, creates the appropriate Transfer In transacion, and vice versa)
	 *
	 * @param baseTransaction
	 * @return
	 */
	private Transaction createMirroredTransaction(Transaction baseTransaction) {
		Transaction mirroredTransaction = new Transaction()

		if (baseTransaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
			mirroredTransaction.transactionType = TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)
			mirroredTransaction.source = baseTransaction.inventory.warehouse
			mirroredTransaction.destination = null
			mirroredTransaction.inventory = baseTransaction.destination.inventory ?: addInventory(baseTransaction.destination)
		}
		else if (baseTransaction.transactionType.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) {
			mirroredTransaction.transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
			mirroredTransaction.source = null
			mirroredTransaction.destination = baseTransaction.inventory.warehouse
			mirroredTransaction.inventory = baseTransaction.source.inventory ?: addInventory(baseTransaction.source)
		}
		else {
			throw new RuntimeException("Invalid transaction type for mirrored transaction")
		}

        mirroredTransaction.requisition = baseTransaction.requisition
		mirroredTransaction.transactionDate = baseTransaction.transactionDate

		// create the transaction entries based on the base transaction
		baseTransaction.transactionEntries.each {
			/*
			 def inventoryItem =
			 findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)
			 // If the inventory item doesn't exist, we create a new one
			 if (!inventoryItem) {
			 inventoryItem = new InventoryItem(lotNumber: it.lotNumber, product:it.product)
			 if (inventoryItem.hasErrors() && !inventoryItem.save()) {
			 throw new RuntimeException("Unable to create inventory item $inventoryItem while creating local transfer")
			 }
			 }*/

			// Create a new transaction entry
			def transactionEntry = new TransactionEntry();
			transactionEntry.quantity = it.quantity;
			transactionEntry.inventoryItem = it.inventoryItem;
			mirroredTransaction.addToTransactionEntries(transactionEntry);
		}

		return mirroredTransaction
	}

	/**
	 *
	 * @return
	 */
	def getConsumptionTransactionsBetween(Date startDate, Date endDate) {
		log.debug("startDate = " + startDate + " endDate = " + endDate)
		def criteria = Consumption.createCriteria()
		def results = criteria.list {
			if (startDate && endDate) {
				between('transactionDate', startDate, endDate)
			}
		}

		return results
	}

	/**
	 *
	 * @return
	 */
	def getConsumptions(Date startDate, Date endDate, String groupBy) {
		log.debug("startDate = " + startDate + " endDate = " + endDate)
		def criteria = Consumption.createCriteria()
		def results = criteria.list {
			if (startDate && endDate) {
				between('transactionDate', startDate, endDate)
			}
			projections {
				sum('quantity')
				groupProperty('product')
				groupProperty('transactionDate')
			}
		}

		return results
	}

	/**
	 *
	 */
	def getQuantity(Product product, Location location, Date beforeDate) {
		def quantity = 0;
		def transactionEntries = getTransactionEntriesBeforeDate(product, location, beforeDate)
		quantity = adjustQuantity(quantity, transactionEntries)
		return quantity;
	}


	def getQuantity(InventoryItem inventoryItem, Location location, Date beforeDate) {
		def quantity = 0;
		def transactionEntries = getTransactionEntriesBeforeDate(inventoryItem, location, beforeDate)
		quantity = adjustQuantity(quantity, transactionEntries)
		return quantity
	}

	/**
	 * Get the initial quantity of a product for the given location and date.
	 * If the date is null, then we assume that the answer is 0.
	 *
	 * @param product
	 * @param location
	 * @param date
	 * @return
	 */
	def getInitialQuantity(Product product, Location location, Date date) {
		return getQuantity(product, location, date ?: new Date());
	}

	/**
	 * Get the current quantity (as of the given date) or today's date if the
	 * given date is null.
	 *
	 * @param product
	 * @param location
	 * @param date
	 * @return
	 */
	def getCurrentQuantity(Product product, Location location, Date date) {
		return getQuantity(product, location, date ?: new Date());
	}

	/**
	 *
	 * @param initialQuantity
	 * @param transactionEntries
	 * @return
	 */
	def adjustQuantity(Integer initialQuantity, List<Transaction> transactionEntries) {
		def quantity = initialQuantity;
		transactionEntries.each { transactionEntry ->
			quantity = adjustQuantity(quantity, transactionEntry)
		}
		return quantity;
	}

	/**
	 *
	 * @param initialQuantity
	 * @param transactionEntry
	 * @return
	 */
	def adjustQuantity(Integer initialQuantity, TransactionEntry transactionEntry) {
		def quantity = initialQuantity;

		def code = transactionEntry?.transaction?.transactionType?.transactionCode;
		if (code == TransactionCode.INVENTORY || code == TransactionCode.PRODUCT_INVENTORY) {
			quantity = transactionEntry.quantity;
		}
		else if (code == TransactionCode.DEBIT) {
			quantity -= transactionEntry.quantity;
		}
		else if (code == TransactionCode.CREDIT) {
			quantity += transactionEntry.quantity;
		}
		return quantity;
	}

	/**
	 *
	 * @param product
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	def getTransactionEntries(Product product, Location location, Date startDate, Date endDate) {
		def criteria = TransactionEntry.createCriteria();
		def transactionEntries = criteria.list {
			inventoryItem { eq("product", product) }
			transaction {
				// All transactions between start date and end date
				if (startDate && endDate) {
					between("transactionDate", startDate, endDate)
				}
				// All transactions after start date
				else if (startDate) {
					ge("transactionDate", startDate)
				}
				// All transactions before end date
				else if (endDate) {
					le("transactionDate", endDate)
				}
				eq("inventory", location?.inventory)
				order("transactionDate", "asc")
				order("dateCreated", "asc")
			}
		}
		return transactionEntries;
	}

	/**
	 *
	 * @param product
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	def getTransactionEntriesBeforeDate(InventoryItem inventoryItem, Location location, Date beforeDate) {
		def transactionEntries = []
		if (beforeDate) {
			def criteria = TransactionEntry.createCriteria();
			transactionEntries = criteria.list {
				and {
					eq("inventoryItem", inventoryItem)
					transaction {
						// All transactions before given date
						lt("transactionDate", beforeDate)
						eq("inventory", location?.inventory)
						order("transactionDate", "asc")
						order("dateCreated", "asc")
					}
				}
			}
		}
		return transactionEntries;
	}

	/**
	 *
	 * @param product
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	def getTransactionEntriesBeforeDate(Product product, Location location, Date beforeDate) {
		def criteria = TransactionEntry.createCriteria();
		def transactionEntries = []

		if (beforeDate) {
			transactionEntries = criteria.list {
				inventoryItem { eq("product", product) }
				transaction {
					// All transactions before given date
					lt("transactionDate", beforeDate)
					eq("inventory", location?.inventory)
					order("transactionDate", "asc")
					order("dateCreated", "asc")

				}
			}
		}
		return transactionEntries;
	}

	/**
	 *
	 * @param location
	 * @param category
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	def getTransactionEntries(Location location, Category category, Date startDate, Date endDate) {
		def categories = []
		categories << category
		def matchCategories = getExplodedCategories(categories)
		return getTransactionEntries(location, matchCategories, startDate, endDate)

	}

	def getTransactionEntries(Location location, List categories, Date startDate, Date endDate) {
		def criteria = TransactionEntry.createCriteria();
		def transactionEntries = criteria.list {
			if (categories) {
				inventoryItem {
					product { 'in'("category", categories) }
				}
			}
			transaction {
				if (startDate && endDate) {
					between("transactionDate", startDate, endDate)
				}
				else if (startDate) {
					ge("transactionDate", startDate)
				}
				else if (endDate) {
					le("transactionDate", endDate)
				}
				eq("inventory", location?.inventory)
				order("transactionDate", "asc")
				order("dateCreated", "asc")
			}
		}
		return transactionEntries;


	}

	public void validateData(ImportDataCommand command) {
		processData(command);
	}


	public void processData(ImportDataCommand command) {
		Date today = new Date()
		//today.clearTime()
		def transactionInstance = new Transaction(transactionDate: today,
		transactionType: TransactionType.findById(Constants.INVENTORY_TRANSACTION_TYPE_ID),
		inventory: command?.location?.inventory)

		command.transaction = transactionInstance

		// Iterate over each row and validate values
		command?.data?.each { Map params ->
			//log.debug "Inventory item " + importParams

			validateInventoryData(params, command.errors)

			def expirationDate = ImporterUtil.parseDate(params.expirationDate, command.errors)
			def category = ImporterUtil.findOrCreateCategory(params.category, command.errors);

			// Create product if not exists
			Product product = Product.findByName(params.productDescription);
			if (!product) {
				product = new Product(
						name: params.productDescription,
						upc: params.upc,
						ndc: params.ndc,
						category: category,
						manufacturer: params.manufacturer,
						manufacturerCode: params.manufacturerCode,
						unitOfMeasure: params.unitOfMeasure,
						coldChain: Boolean.valueOf(params.coldChain));

				if (!product.validate()) {
					product.errors.allErrors.each {
						command.errors.addError(it);
					}
				}
				else {
					command.products << product
				}
				log.debug "Created new product " + product.name;
			}

			// Find the inventory item by product and lotNumber and description
			InventoryItem inventoryItem =
					findInventoryItemByProductAndLotNumber(product, params.lotNumber);

			log.debug("Inventory item " + inventoryItem)
			// Create inventory item if not exists
			if (!inventoryItem) {
				inventoryItem = new InventoryItem()
				inventoryItem.product = product
				inventoryItem.lotNumber = params.lotNumber;
				inventoryItem.expirationDate = expirationDate;
				if (!inventoryItem.validate()) {
					inventoryItem.errors.allErrors.each {
						command.errors.addError(it);
					}
				}
				else {
					command.inventoryItems << inventoryItem
				}
			}

			// Create a transaction entry if there's a quantity specified
			if (params?.quantity) {
				TransactionEntry transactionEntry = new TransactionEntry();
				transactionEntry.quantity = params.quantity;
				transactionEntry.inventoryItem = inventoryItem;
				transactionInstance.addToTransactionEntries(transactionEntry);
				if (!transactionEntry.validate()) {
					transactionEntry.errors.allErrors.each {
						command.errors.addError(it);
					}
				}
			}
		}

		if (transactionInstance.validate()) {
			transactionInstance.errors.allErrors.each {
				command.errors.addError(it);
			}
		}
	}

	/**
	 *
	 * @param importParams
	 * @param errors
	 */
	def validateInventoryData(Map params, Errors errors) {
		def lotNumber = (params.lotNumber) ? String.valueOf(params.lotNumber) : null;
		if (params?.lotNumber instanceof Double) {
			errors.reject("Property 'Serial Number / Lot Number' with value '${lotNumber}' should be not formatted as a Double value");
		}
		else if (!params?.lotNumber instanceof String) {
			errors.reject("Property 'Serial Number / Lot Number' with value '${lotNumber}' should be formatted as a Text value");
		}

		def quantity = params.quantity ?: 0;
		if (!params?.quantity instanceof Double) {
			errors.reject("Property [quantity] with value '${quantity} for '${lotNumber}' should be formatted as a Double value");
		}
		else if (params?.quantity instanceof String) {
			errors.reject("Property [quantity] with value '${quantity} for '${lotNumber}' should not be formatted as a Text value");
		}

		def manufacturerCode = (params.manufacturerCode) ? String.valueOf(params.manufacturerCode) : null;
		if (!params?.manufacturerCode instanceof String) {
			errors.reject("Property 'Manufacturer Code' with value '${manufacturerCode}' should be formatted as a Text value");
		}
		else if (params?.manufacturerCode instanceof Double) {
			errors.reject("Property 'Manufacturer Code' with value '${manufacturerCode}' should not be formatted as a Double value");
		}

		def upc = (params.upc) ? String.valueOf(params.upc) : null;
		if (!params?.upc instanceof String) {
			errors.reject("Property 'UPC' with value '${upc}' should be formatted as a Text value");
		}
		else if (params?.upc instanceof Double) {
			errors.reject("Property 'UPC' with value '${upc}' should be not formatted as a Double value");
		}

		def ndc = (params.ndc) ? String.valueOf(params.ndc) : null;
		if (!params?.ndc instanceof String) {
			errors.reject("Property 'GTIN' with value '${ndc}' should be formatted as a Text value");
		}
		else if (params?.ndc instanceof Double) {
			errors.reject("Property 'GTIN' with value '${ndc}' should be not formatted as a Double value");
		}
	}

	/**
	 * Import data from given inventoryMapList into database.
	 *
	 * @param location
	 * @param inventoryMapList
	 * @param errors
	 */
	public void importData(ImportDataCommand command) {


		try {
			def dateFormat = new SimpleDateFormat("yyyy-MM-dd");


			Date today = new Date()

			def transactionInstance = new Transaction(transactionDate: today,
			transactionType: TransactionType.findById(Constants.INVENTORY_TRANSACTION_TYPE_ID),
			inventory: command?.location.inventory)

			// Iterate over each row
			command?.data?.each { Map params ->

                log.debug params

				def lotNumber = (params.lotNumber) ? String.valueOf(params.lotNumber) : null;
				def quantity = (params.quantity) ?: 0;

				def unitOfMeasure = params.unitOfMeasure;
				def manufacturer = (params.manufacturer) ? String.valueOf(params.manufacturer) : null;
				def manufacturerCode = (params.manufacturerCode) ? String.valueOf(params.manufacturerCode) : null;
				def upc = (params.upc) ? String.valueOf(params.upc) : null;
				def ndc = (params.ndc) ? String.valueOf(params.ndc) : null;

				def expirationDate = ImporterUtil.parseDate(params.expirationDate, command.errors);

				def category = ImporterUtil.findOrCreateCategory(params.category, command.errors)
				category.save();
				if (!category) {
					throw new ValidationException("error finding/creating category")
				}
				log.debug "Creating product " + params.productDescription + " under category " + category
				// Create product if not exists
				Product product = Product.findByName(params.productDescription);
				if (!product) {
					product = new Product(
							name: params.productDescription,
							upc: upc,
							ndc: ndc,
							category: category,
							manufacturer: manufacturer,
							manufacturerCode: manufacturerCode,
							unitOfMeasure: unitOfMeasure,
							coldChain: Boolean.valueOf(params.coldChain));

					if (product.hasErrors() || !product.save()) {
						command.errors.reject("Error saving product " + product?.name)

					}
					log.debug "Created new product " + product.name;
				}

				// Find the inventory item by product and lotNumber and description
				InventoryItem inventoryItem =
						findInventoryItemByProductAndLotNumber(product, lotNumber);

				log.debug("Inventory item " + inventoryItem)
				// Create inventory item if not exists
				if (!inventoryItem) {
					inventoryItem = new InventoryItem()
					inventoryItem.product = product
					inventoryItem.lotNumber = lotNumber;
					inventoryItem.expirationDate = expirationDate;
					if (inventoryItem.hasErrors() || !inventoryItem.save()) {
						inventoryItem.errors.allErrors.each {
							log.error "ERROR " + it;
							command.errors.addError(it);
						}
					}
				}

				// Create a transaction entry if there's a quantity specified
				if (params?.quantity) {
					TransactionEntry transactionEntry = new TransactionEntry();
					transactionEntry.quantity = quantity;
					transactionEntry.inventoryItem = inventoryItem;
					transactionInstance.addToTransactionEntries(transactionEntry);
				}
			}

			// Only save the transaction if there are transaction entries and there are no errors
			if (transactionInstance?.transactionEntries && !command.hasErrors()) {
				if (!transactionInstance.save()) {
					transactionInstance.errors.allErrors.each {
						command.errors.addError(it);
					}
				}
			}
		} catch (Exception e) {
			// Bad practice but need this for testing
			log.error("Error importing inventory", e);
			throw e;
		}

	}

	public Map<String, Integer> getQuantityForProducts(Inventory inventory, ArrayList<String> productIds) {
        log.debug "inventory " + inventory + " " + ", productIds: " + productIds
		def ids = productIds.collect{ "'${it}'"}.join(",")
        log.debug "ids: " + ids
		def result =[:]
		if (ids) {
            //
			//def sql = "select te from TransactionEntry as te where te.transaction.inventory.id='${inventory.id}' and te.inventoryItem.product.id in (${ids}) " +
            //        "order by te.transaction.transactionDate asc, te.transaction.dateCreated asc"

            def sql = "select te from TransactionEntry as te where te.transaction.inventory.id='${inventory.id}' and te.inventoryItem.product.id in (${ids})"
            log.debug "SQL: " + sql
			def transactionEntries = TransactionEntry.executeQuery(sql)
			log.debug "transactionEntries " + transactionEntries
            //transactionEntries.each{ println(it)}
			//transactionEntries.each{
            //    println it.transaction.transactionType.transactionCode.toString() + "," +
            //            it.transaction.transactionDate.toString() + "," +
            //            it.transaction.dateCreated.toString() + "," +
            //            it.inventoryItem.lotNumber + "," +
            //            it.quantity
            //}
			def map = getQuantityByProductMap(transactionEntries)
			map.keySet().each{ result[it.id] = map[it] }
		}
		log.debug "getQuantityForProducts " + result
		result
	}

	public Map<Product, List<InventoryItem>> getInventoryItemsWithQuantity(List<Product> products, Inventory inventory) {

		def transactionEntries = TransactionEntry.createCriteria().list() {
			transaction { eq("inventory", inventory) }
			if (products) {
				inventoryItem { 'in'("product", products) }
			}
		}
		def map = getQuantityByProductAndInventoryItemMap(transactionEntries)
		def result = [:]
		map.keySet().each{ product ->
			def valueMap = map[product]

			def inventoryItems = valueMap.keySet().collect { item ->
				item.quantity = valueMap[item]
				item
			}
			inventoryItems.sort{ it.expirationDate}
			result[product]  = inventoryItems
		}
		result
	}


	/**
	 * @param product
	 * @return
	 */
	//public Integer getQuantityAvailableToPromise(Product product) {}
	//public Integer getQuantityOnHand(Product product) {}
	//public Integer getQuantityAvailableToPromise(InventoryItem inventoryItem) {}
	//public Integer getQuantityOnHand(InventoryItem inventoryItem) {}


	/**
	 * @return	a unique identifier to be assigned to a transaction
	 */
	public String generateTransactionNumber() {
		return identifierService.generateTransactionIdentifier()
	}

    public List<Transaction> getCreditsBetweenDates(List<Location> fromLocations, List<Location> toLocations, Date fromDate, Date toDate) {
        def transactions = Transaction.createCriteria().list() {
            transactionType {
                eq("transactionCode", TransactionCode.CREDIT)
            }
            if (fromLocations) {
                'in'("source", fromLocations)
            }
            if (toLocations) {
                'in'("inventory", toLocations.collect { it.inventory })
            }
            between('transactionDate', fromDate, toDate)
        }
        return transactions
    }

    public List<Transaction> getDebitsBetweenDates(List<Location> fromLocations, List<Location> toLocations, Date fromDate, Date toDate) {
        def transactions = Transaction.createCriteria().list() {
            transactionType {
                eq("transactionCode", TransactionCode.DEBIT)
            }
            if (toLocations) {
                'in'("destination", toLocations)
            }
            //eq("inventory", fromLocation.inventory)
            if (fromLocations) {
                'in'("inventory", fromLocations.collect { it.inventory })
            }
            between('transactionDate', fromDate, toDate)
        }
        return transactions
    }


    def getInventorySampling(Location location, Integer n) {
        def inventoryItems = []
        Map<InventoryItem, Integer> inventoryItemMap = getQuantityForInventory(location.inventory);

        log.info inventoryItemMap

        List inventoryItemKeys = inventoryItemMap.keySet().asList()
        Integer maxSize = inventoryItemKeys.size()

        if (n > maxSize) {
            throw new RuntimeException("You cannot request more items than are available at this location [requested=${n},available=${maxSize}].")
        }

        Random random = new Random()
        def randomIntegerList = []
        (1..n).each {
            println "n: " + n
            println "maxSize: " + maxSize
            def randomIndex = random.nextInt(maxSize)
            println "randomIndex: " + randomIndex
            def inventoryItem = inventoryItemKeys.get(randomIndex)
            println "lotNumber: " + inventoryItem.lotNumber
            inventoryItems << inventoryItem
        }
        return inventoryItems;
    }


    /**
     *  Returns the quantity on hand of each product at the given location as of the given date.
     */
    def getQuantityOnHandAsOfDate(location, date) {
        //def transactionEntries = getTransactionEntriesBeforeDate(location, date)
        //return getQuantityByProductMap(transactionEntries)
        return getQuantityOnHandAsOfDate(location, date, null)
    }

    /**
     *  Returns the quantity on hand of each product for the given tag at the given location as of the given date.
     */
    def getQuantityOnHandAsOfDate(location, date, tag) {
        def transactionEntries = getTransactionEntriesBeforeDate(location, date, tag)
        def quantityMap = getQuantityByProductMap(transactionEntries)

        // Make sure that ALL products in the tag are represented
        if (tag) {
            tag.products.each { p ->
                def product = Product.get(p.id)
                def quantity = quantityMap[product]
                if (!quantity) {
                    quantityMap[product] = 0
                }
            }
        }
        return quantityMap



    }


    def getTransactionEntriesBeforeDate(location, date) {
        return getTransactionEntriesBeforeDate(location, date, null)
    }


    def getTransactionEntriesBeforeDate(location, date, tag) {
        def criteria = TransactionEntry.createCriteria();
        def transactionEntries = []
        if (date) {
            def products = Tag.get(tag?.id)?.products
            log.info "Get products by tag ${tag?.id}: " + products
            transactionEntries = criteria.list {
                if (products) {
                    inventoryItem {
                        'in'("product", products)
                    }
                }
                transaction {
                    // All transactions before given date
                    lt("transactionDate", date)
                    eq("inventory", location?.inventory)
                    order("transactionDate", "asc")
                    order("dateCreated", "asc")

                }
            }
        }
        return transactionEntries;
    }

    /**
     * Export given products.
     * @param products
     * @return
     */
    String exportBaselineQoH(products, quantityMapByDate) {
        def csvrows = []
        products.each { product ->
            //def inventoryLevel = inventoryLevelMap[product]
            def csvrow = [
                'Product code': product.productCode?:'',
                'Product': product.name,
                'UOM': product.unitOfMeasure,
                'Generic product': product?.genericProduct?.description?:"",
                'Category': product?.category?.name,
                'Manufacturer': product?.manufacturer?:"",
                'Manufacturer code': product?.manufacturerCode?:"",
                'Vendor': product?.vendor?:"",
                'Vendor code': product?.vendorCode?:""
                //'Bin Location': inventoryLevel?.binLocation?:"",
                //'Min': inventoryLevel?.minQuantity?:"",
                //'Reorder': inventoryLevel?.reorderQuantity?:"",
                //'Max': inventoryLevel?.maxQuantity?:""
            ]

            if (quantityMapByDate) {
                quantityMapByDate.each { key, value ->
                    csvrow[key.format("dd-MMM-yyyy")] = quantityMapByDate[key][product]
                }
            }

            csvrows << csvrow
        }
        return dataService.generateCsv(csvrows)
    }


    def getQuantityOnHand(product) {
        log.info ("Get getQuantityOnHand() for product ${product?.name} at all locations")
        def quantityMap = [:]
        def locations = Location.list()
        locations.each { location ->
            if (location.inventory && location.isWarehouse()) {
                def quantity = getQuantityOnHand(location, product)
                if (quantity) {
                    quantityMap[location] = quantity
                }
            }
        }
        return quantityMap
    }


    String exportLatestInventoryDate(location) {
        def formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        def sw = new StringWriter()


        def quantityMap = getTotalStock(location);
        def statusMap = getInventoryStatus(location)
        def products = quantityMap.keySet()

        def latestInventoryDates = TransactionEntry.executeQuery("""
                select ii.product.id, max(t.transactionDate)
                from TransactionEntry as te
                left join te.inventoryItem as ii
                left join te.transaction as t
                where t.inventory = :inventory
                and t.transactionType.transactionCode in (:transactionCodes)
                group by ii.product
                """,
                [inventory: location.inventory, transactionCodes: [TransactionCode.PRODUCT_INVENTORY, TransactionCode.INVENTORY]])


        // Convert to map
        def latestInventoryDateMap = [:]
        latestInventoryDates.each {
            latestInventoryDateMap[it[0]] = it[1]
        }

        def inventoryLevelMap = [:]
        def inventoryLevels = InventoryLevel.findAllByInventory(location.inventory)
        inventoryLevels.each { inventoryLevel ->
            inventoryLevelMap[inventoryLevel.product] = inventoryLevel
        }


        def csvWriter = new CSVWriter(sw, {
            "Product Code" { it.productCode }
            "Name" { it.name }
            "Bin Location" { it.binLocation }
            "ABC" { it.abcClass }
            "Most Recent Stock Count" { it.latestInventoryDate }
            "QoH" { it.quantityOnHand }
            "Unit of Measure" { it.unitOfMeasure }
            "Date Created" { it.dateCreated }
            "Date Updated" { it.lastUpdated }
        })

        products.each { product ->
            def latestInventoryDate = latestInventoryDateMap[product.id]
            def row =  [
                    productCode: product.productCode?:"",
                    name: product.name,
                    unitOfMeasure: product.unitOfMeasure?:"",
                    abcClass: inventoryLevelMap[product]?.abcClass?:"",
                    binLocation: inventoryLevelMap[product]?.binLocation?:"",
                    latestInventoryDate: latestInventoryDate?"${formatDate.format(latestInventoryDate)}":"",
                    quantityOnHand: quantityMap[product]?:"",
                    dateCreated: product.dateCreated?"${formatDate.format(product.dateCreated)}":"",
                    lastUpdated: product.lastUpdated?"${formatDate.format(product.lastUpdated)}":"",
            ]
            csvWriter << row
        }
        return sw.toString()
    }

    /**
     *
     * @param command
     */
    def validateInventoryData(ImportDataCommand command) {

        def dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
        def calendar = Calendar.getInstance()
        command.data.eachWithIndex { row, index ->
            def rowIndex = index + 2

            if (!command.warnings[index]) {
                command.warnings[index] = []
            }

            def product = Product.findByProductCode(row.productCode)
            if (!product) {
                command.errors.reject("error.product.notExists", "Row ${rowIndex}: Product '${row.productCode}' does not exist");
                command.warnings[index] << "Product '${row.productCode}' does not exist"
            }
            else {
                def manufacturerCode = row.manufacturerCode
                if (manufacturerCode instanceof Double) {
                    //command.errors.reject("error.manufacturerCode.invalid", "Row ${rowIndex}: Manufacturer code '${manufacturerCode}' must be a string")
                    command.warnings[index] << "Manufacturer code '${manufacturerCode}' must be a string"
                    manufacturerCode = manufacturerCode.toInteger().toString()
                }
                if (row.manufacturer && product.manufacturer && row.manufacturer != product.manufacturer) {
                    command.warnings[index] << "Manufacturer [${row.manufacturer}] is not the same as in the database [${product.manufacturer}]"
                }

                if (row.manufacturerCode && product.manufacturerCode && row.manufacturerCode != product.manufacturerCode) {
                    command.warnings[index] << "Manufacturer code [${row.manufacturerCode}] is not the same as in the database [${product.manufacturerCode}]"
                }
                def lotNumber = row.lotNumber
                if (lotNumber instanceof Double) {
                    //command.errors.reject("error.lotNumber.invalid", "Row ${rowIndex}: Lot number '${lotNumber}' must be a string")
                    command.warnings[index] << "Lot number '${lotNumber}' must be a string"
                    lotNumber = lotNumber.toInteger().toString()
                }
                def inventoryItem = InventoryItem.findByProductAndLotNumber(product, lotNumber)
                if (!inventoryItem) {
                    command.warnings[index] << "Inventory item for lot number '${lotNumber}' does not exist and will be created"
                }

                def expirationDate = null
                try {
                    if (row.expirationDate) {
                        if (row.expirationDate instanceof String) {
                            expirationDate = dateFormatter.parse(row.expirationDate)
                            calendar.setTime(expirationDate)
                            expirationDate = calendar.getTime()
                        }
                        else if (row.expirationDate instanceof Date) {
                            expirationDate = row.expirationDate
                        }
                        else if (row.expirationDate instanceof LocalDate) {
                            expirationDate = row.expirationDate.toDate()
                        }
                        else {
                            expirationDate = row.expirationDate
                            command.warnings[index] << "Expiration date '${row.expirationDate}' has unknown format ${row?.expirationDate?.class}"
                        }

                        if (expirationDate <= new Date()) {
                            command.warnings[index] << "Expiration date '${row.expirationDate}' is not valid"
                        }

                    }
                } catch (ParseException e) {
                    command.errors.reject("error.expirationDate.invalid", "Row ${rowIndex}: Product '${row.productCode}' must have a valid date (or no date)")
                }


                //def minLength = Math.min(product.name.length(),row.product.length())
                def levenshteinDistance = StringUtils.getLevenshteinDistance(product.name, row.product)
                if (row.product && levenshteinDistance > 0) {
                    command.warnings[index] << "Product name [${row.product}] does not appear to be the same as in the database [${product.name}] (Levenshtein distance: ${levenshteinDistance})"
                }

            }

            if ((row.quantity as int) < 0) {
                command.errors.reject("error.quantity.negative", "Row ${rowIndex}: Product '${row.productCode}' must have positive quantity");
            }


        }
    }

    /**
     *
     * @param command
     * @return
     */
    def importInventoryData(ImportDataCommand command) {
        def dateFormatter = new SimpleDateFormat("yy-mm")
        def importer = new InventoryExcelImporter(command.importFile.absolutePath)
        def data = importer.data
        assert data != null
        log.info "Data to be imported: " + data

        def transaction = new Transaction()
        transaction.transactionDate = command.date
        transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
        transaction.transactionNumber = generateTransactionNumber()
        transaction.comment = "Imported from ${command.importFile.name} on ${new Date()}"
        transaction.inventory = command.location.inventory

        def calendar = Calendar.getInstance()
        command.data.eachWithIndex { row, index ->
            println "${index}: ${row}"
            def transactionEntry = new TransactionEntry()
            transactionEntry.quantity = row.quantity.toInteger()
            transactionEntry.comments = row.comments

            // Find an existing product, should fail if not found
            def product = Product.findByProductCode(row.productCode)
            assert product

            // Check the Levenshtein distance between the given name and stored product name (make sure they're close)
            println "Levenshtein distance: " + StringUtils.getLevenshteinDistance(product.name, row.product)
            //assert product.name == row.product

            // Handler for the lot number
            def lotNumber = row.lotNumber
            if (lotNumber instanceof Double) {
                lotNumber = lotNumber.toInteger().toString()
            }
            println "Lot Number: " + lotNumber

            // Expiration date should be the last day of the month
            def expirationDate = null
            if (row.expirationDate instanceof String) {
                expirationDate = dateFormatter.parse(row.expirationDate)
                calendar.setTime(expirationDate)
                expirationDate = calendar.getTime()
            }
            else if (row.expirationDate instanceof Date) {
                expirationDate = row.expirationDate
            }
            else if (row.expirationDate instanceof LocalDate) {
                expirationDate = row.expirationDate.toDate()
            }
            else {
                expirationDate = row.expirationDate
            }

            // Find or create an inventory item
            def inventoryItem = findOrCreateInventoryItem(product, lotNumber, expirationDate)
            println "Inventory item: " + inventoryItem.id + " " + inventoryItem.dateCreated + " " + inventoryItem.lastUpdated
            transactionEntry.inventoryItem = inventoryItem

            transaction.addToTransactionEntries(transactionEntry)
        }
        transaction.save(flush:true, failOnError: true);
        println "Transaction ${transaction?.transactionNumber} saved successfully! "
        println "Added ${transaction?.transactionEntries?.size()} transaction entries"
        return data
    }

    def getTransactionDates() {
        //return Transaction.executeQuery('select distinct transactionDate from Transaction')
        def transactionDates = []
        //def results = Transaction.executeQuery("select distinct year(transactionDate), month(transactionDate), day(transactionDate) from Transaction where inventory = :inventory",
        //        [inventory:location.inventory])

        def results = Transaction.executeQuery("select transactionDate from Transaction")

        results.each { date ->
            //def date = new Date().updated([year: it[0], month: it[1], day: it[2]])
            date.clearTime()
            transactionDates << date
        }
        return transactionDates.unique().sort().reverse()
    }



    def getTransactionDates(location, product) {
        def transactionDates = []
        def startDate = new Date() - 365 * 5
        def endDate = new Date()
        (endDate..startDate).each {
            it.clearTime()
            transactionDates.add(it)
        }

        /*
        Calendar.with {
            (2010..2014).each { year ->
                (JANUARY..DECEMBER).each { month ->
                    def calendar = instance
                    calendar[YEAR] = year
                    calendar[MONTH] = month
                    calendar[DAY_OF_MONTH] = 1
                    def date = calendar.getTime()
                    date.clearTime()
                    transactionDates.add(date)
                }
            }
        }
        */

        println "transactionDates: " + transactionDates
        /*
        def criteria = TransactionEntry.createCriteria()
        def results = criteria.list {
            projections {
                transaction {
                    distinct ("transactionDate")
                }
            }

            transaction {
                eq("inventory", location.inventory)
            }
            inventoryItem {
                eq("product", product)
            }
        }
        results.each { date ->
            date.clearTime()
            transactionDates << date
        }
        */

        return transactionDates
    }

    def getDepotLocations() {
        def locations = Location.findAll("from Location as l where l.inventory is not null")
        return locations.findAll { it.isWarehouse() }

    }

    //@Cacheable("inventorySnapshotCache")
    /*
	def findInventorySnapshotByDateAndLocation(Date date, Location location) {
        def data = []
        if (location && date) {
            //def quantityMap = inventoryService.getQuantityByProductMap(location.inventory)
            def inventorySnapshots = InventorySnapshot.findAllByLocationAndDate(location, date)
            inventorySnapshots.each {
                data << [   date           : it.date,
                            location       : it.location.name,
                            category       : it.product?.category?.name,
                            productCode    : it.product.productCode,
                            product        : it.product.name,
                            productGroup   : null, //it?.product?.genericProduct?.name,
                            quantityOnHand : it.quantityOnHand,
                            tags           : it.product.tagsToString(),
                            unitOfMeasure  : it?.product?.unitOfMeasure?:"EA"
                ]
            }
        }
        return data;
	}
	*/

    def findInventorySnapshotByDateAndLocation(Date date, Location location) {
        def data = []
        if (location && date) {

            long startTime = System.currentTimeMillis()

            //, productGroups, tags
            //left outer join fetch product.productGroups as productGroups
            //left outer join fetch product.tags as tags
            //
            def results = InventorySnapshot.executeQuery("""
                    select i.date, i.location.name as location, product, category.name, i.quantityOnHand
                    from InventorySnapshot i, Product product, Category category
                    where i.location = :location
                    and i.date = :date
                    and i.product = product
                    and i.product.category = category
                    group by i.date, i.location.name, product
                    """, [location:location, date: date])

            log.info "Results: " + results.size()
            log.info "Query response time: " + (System.currentTimeMillis() - startTime)
            startTime = System.currentTimeMillis()

            results.each {
                Product product = it[2]
                data << [
                        date                : it[0],
                        location            : it[1],
                        category            : it[3],
                        productCode         : product.productCode,
                        product             : product.name,
                        productGroup        : product.genericProduct?.name,
                        tags                : product.tagsToString(),
                        //productGroup        : it[5]*.description?.join(":")?:"", //product?.genericProduct?.name,
                        //tags                : it[6]*.tag?.join(","),
                        quantityOnHand      : it[4],
                        unitOfMeasure       : product?.unitOfMeasure?:"EA"
                ]
            }
            log.info "Post-processing response time: " + (System.currentTimeMillis() - startTime)
        }
        return data
    }


    def createOrUpdateInventorySnapshot(Date date) {
        def startTime = System.currentTimeMillis()
        date.clearTime()
        def locations = getDepotLocations()
        locations.each { location ->
			log.info "Creating or updating inventory snapshot for date ${date}, location ${location.name} ..."
			createOrUpdateInventorySnapshot(date, location)
        }

        println "Created inventory snapshot for ${date} in " + (System.currentTimeMillis() - startTime) + " ms"
    }


    def createOrUpdateInventorySnapshot(Date date, Location location) {
        try {
			log.info "Create or update inventory snapshot for location ${location.name} on date ${date}"
			// Only process locations with inventory
			if (location?.inventory) {
				String dateString = date.format("yyyy-MM-dd HH:mm:ss")
				//def productQuantityMap = getQuantityByProductMap(location.inventory)
				def quantityMap = getQuantityOnHandAsOfDate(location, date)
				def products = quantityMap.keySet();

				log.info "Saving inventory snapshots for ${products?.size()} products "
				def startTime = System.currentTimeMillis()

				def sql = new Sql(dataSource)
				if (sql) {
					try {
						sql.withBatch(1000) { stmt ->
							products.eachWithIndex { product, index ->
								//log.info "Saving inventory snapshot for product[${index}]: " + product
								def onHandQuantity = quantityMap[product]
								def insertStmt = "insert into inventory_snapshot(version,date,location_id,product_id,inventory_item_id,quantity_on_hand) " +
										"values (0,'${dateString}','${location?.id}','${product?.id}',NULL,${onHandQuantity}) " +
										"ON DUPLICATE KEY UPDATE quantity_on_hand=${onHandQuantity}"
								stmt.addBatch(insertStmt)
							}
							stmt.executeBatch()
						}
					} catch (BatchUpdateException e) {
						log.error("Error executing batch update for location ${location.name} " + e.message, e)
					}
				}
				log.info ("Time to execute batch statements " + (System.currentTimeMillis() - startTime) + " ms")
			}
            log.info "Saved inventory snapshot for products=ALL, location=${location}, date=${date}"
        } catch (Exception e) {
            log.error("Unable to complete inventory snapshot process", e)
        }
    }

    def createOrUpdateInventorySnapshot(Location location, Product product) {
        try {
            def dates = getTransactionDates(location, product)
            dates.each { date ->
                def quantity = getQuantity(product, location, date)
                log.info "Create or update inventory snapshot for product ${product} at location ${location.name} on date ${date} = ${quantity} ${product.unitOfMeasure}"
				createOrUpdateInventorySnapshot(date, product, location, quantity)
            }
			log.info "Saved inventory snapshot for product=${product.productCode}, location=${location}, dates=ALL"
        } catch (Exception e) {
            log.error("Unable to complete inventory snapshot process", e)
        }
    }



    def createOrUpdateInventorySnapshot(Date date, Location location, Product product) {
        try {
            def inventorySnapshots = InventorySnapshot.countByDateAndLocation(date, location)
            println "Date ${date}, location ${location}: " + inventorySnapshots
            if (inventorySnapshots == 0) {
                log.info "Create or update inventory snapshot for location ${location.name} on date ${date}"
                def quantity = getQuantity(product, location, date)
				createOrUpdateInventorySnapshot(date, product, location, quantity)
            }
			log.info "Saved inventory snapshot for product=${product.productCode}, location=${location}, date=${date}"
        } catch (Exception e) {
            log.error("Unable to complete inventory snapshot process", e)
        }
    }


    def createOrUpdateInventorySnapshot(Date date, Product product, Location location, Integer onHandQuantity) {
        log.info "Updating inventory snapshot for product " + product.name + " @ " + location.name
        try {
			def inventorySnapshot = InventorySnapshot.findWhere(date: date, location: location, product:product)
            if (!inventorySnapshot) {
                inventorySnapshot = new InventorySnapshot(date: date, location: location, product: product)
            }
            //def pendingQuantity = calculatePendingQuantity(product, location)
            inventorySnapshot.quantityOnHand = onHandQuantity?:0
            //inventorySnapshot.quantityInbound = pendingQuantity[0]?:0
            //inventorySnapshot.quantityOutbound = pendingQuantity[1]?:0
            //inventorySnapshot.lastUpdated = new Date()
            inventorySnapshot.save(flush:true)
        }
        catch (Exception e) {
            log.error("Error saving inventory snapshot for product " + product.name + " and location " + location.name, e)
            throw e;
        }
    }




	def createOrUpdateInventoryItemSnapshot(Date date) {
		def startTime = System.currentTimeMillis()
		date.clearTime()
		def locations = getDepotLocations()
		locations.each { location ->
			log.info "Creating or updating inventory item snapshot for date ${date} and location ${location.name} ..."
			createOrUpdateInventoryItemSnapshot(date, location)
		}
		println "Created inventory snapshot for ${date} in " + (System.currentTimeMillis() - startTime) + " ms"
	}



    def createOrUpdateInventoryItemSnapshot(Date date, Location location) {

        try {
            def inventoryItemSnapshots = InventoryItemSnapshot.countByDateAndLocation(date, location)
            log.info "Date ${date}, location ${location}: " + inventoryItemSnapshots
            log.info "Create or update inventory snapshot for location ${location.name} on date ${date}"
            // Only process locations with inventory
            if (location.inventory) {

				// Get quantity on hand for all products at a given location
				def onHandQuantityMap = getQuantityForInventory(location.inventory)
				def inventoryItems = onHandQuantityMap.keySet();
				log.info "Saving inventory item snapshots for ${inventoryItems?.size()} inventory items"

				def startTime = System.currentTimeMillis()
				def sql = new Sql(dataSource)
				if (sql) {
					String dateString = date.format("yyyy-MM-dd HH:mm:ss")
					sql.withBatch(1000) { stmt ->
						inventoryItems.eachWithIndex { inventoryItem, index ->
							//log.info "Saving inventory snapshot for product[${index}]: " + product
							def onHandQuantity = onHandQuantityMap[inventoryItem]

							//stmt.addBatch(date:date.format("yyyy-MM-dd hh:mm:ss"), locationId:location.id, productId:product.id, inventoryItemId:null, quantityOnHand:onHandQuantity)
							def insertStmt = "insert into inventory_snapshot(id,version,date,location_id,product_id,inventory_item_id,quantity_on_hand,date_created,last_updated) " +
									"values ('${UUID.randomUUID().toString()}', 0,'${dateString}','${location?.id}','${inventoryItem?.product?.id}',NULL,${onHandQuantity},now(),now()) " +
									"ON DUPLICATE KEY UPDATE quantity_on_hand=${onHandQuantity},last_updated=now()"
							stmt.addBatch(insertStmt)
						}
						stmt.executeBatch()
					}
				}
				log.info ("Time to execute batch statements " + (System.currentTimeMillis() - startTime) + " ms")

            }
            log.info "Saved inventory snapshot for all products at location=${location.name} on date=${date}"
        } catch (Exception e) {
            log.error("Unable to complete inventory snapshot process", e)
        }
    }

	def createOrUpdateInventoryItemSnapshot(Date date, InventoryItem inventoryItem, Location location, Integer quantityOnHand) {
		log.info "Updating inventory snapshot for product " + inventoryItem?.product?.name + " at location " + location.name
		try {
			def inventoryItemSnapshot = InventoryItemSnapshot.findWhere(date: date, location: location, product:inventoryItem?.product, inventoryItem: inventoryItem)
			if (!inventoryItemSnapshot) {
				inventoryItemSnapshot = new InventoryItemSnapshot(date: date, location: location, product: inventoryItem?.product, inventoryItem: inventoryItem)
			}
			//def pendingQuantity = calculatePendingQuantity(product, location)
			inventoryItemSnapshot.quantityOnHand = quantityOnHand?:0
			//inventorySnapshot.quantityInbound = pendingQuantity[0]?:0
			//inventorySnapshot.quantityOutbound = pendingQuantity[1]?:0
			//inventorySnapshot.lastUpdated = new Date()
			inventoryItemSnapshot.save()
		}
		catch (Exception e) {
			log.error("Error saving inventory snapshot for inventory item " + inventoryItem + " and location " + location.name, e)
			throw e;
		}
	}

	/**
	 * Calculate pending quantity for a given product and location.
	 *
	 * @param product
	 * @param location
     * @return
     */
	def calculatePendingQuantity(product, location) {
        def inboundQuantity = 0;
        def outboundQuantity = 0;
        try {
            def shipmentItems = ShipmentItem.withCriteria {
                shipment {
                    eq("destination", location)
                }
                or {
                    inventoryItem {
                        eq("product", product)
                    }
                    eq("product", product)
                }
            }
            inboundQuantity = shipmentItems.sum { it.quantity }
            shipmentItems = ShipmentItem.withCriteria {
                shipment {
                    eq("origin", location)
                }
                or {
                    inventoryItem {
                        eq("product", product)
                    }
                    eq("product", product)
                }
            }
            outboundQuantity = shipmentItems.sum { it.quantity }

        } catch (Exception e) {
            log.info ("Error " + e.message)
        }

        [inboundQuantity, outboundQuantity]
    }

	/**
	 * Build quantity map for all products at a given location.
	 *
	 * @param location
	 * @return
     */
    def getQuantityMap(Location location) {
        def quantityMap = [:]
        def products = Product.list()
        products.each { product ->
            calculateQuantityOnHand(product, location)
        }
        return quantityMap
    }

	/**
	 * Calculate the quantity on hand for a given inventory item and location.
	 *
	 * @param inventoryItem
	 * @param location
     */
    def calculateQuantityOnHand(InventoryItem inventoryItem, Location location) {
        throw new UnsupportedOperationException("Method has not been implemented yet")
    }

	/**
	 * Calculate the quantity on hand for a given product and location.
	 *
	 * @param product
	 * @param location
     * @return
     */
    def calculateQuantityOnHand(Product product, Location location) {
		long startTime = System.currentTimeMillis()

        def quantityOnHand = 0
        def stockCountDate = null
        def mostRecentStockCount = getMostRecentQuantityOnHand(product, location)
        if (mostRecentStockCount) {
            stockCountDate = mostRecentStockCount[0][0]
            quantityOnHand = mostRecentStockCount[0][1]
        }

        println "stockCountDate: " + stockCountDate
        println "quantityOnHand: " + quantityOnHand

        def quantityDebit = getQuantityChangeSince(product, location, TransactionCode.DEBIT, stockCountDate)[0]?:0
        def quantityCredit = getQuantityChangeSince(product, location, TransactionCode.CREDIT, stockCountDate)[0]?:0

        println "quantityDebit: " + quantityDebit
        println "quantityCredit: " + quantityCredit

		log.info ("Time to calculate quantity on hand: " + (System.currentTimeMillis() - startTime) + " ms")
        return quantityOnHand - quantityDebit + quantityCredit
    }

	/**
	 * Get most recent product inventory transaction.
	 *
	 * @param product
	 * @param location
     * @return
     */
    def getMostRecentQuantityOnHand(Product product, Location location) {
        def results = Transaction.executeQuery( """
                                    SELECT max(te.transaction.transactionDate), sum(te.quantity)
                                    FROM TransactionEntry te
                                    JOIN te.transaction
                                    JOIN te.inventoryItem
                                    WHERE te.inventoryItem.product = :product
                                    AND te.transaction.inventory = :inventory
                                    AND te.transaction.transactionType.transactionCode = :transactionCode
                                    group by te.transaction.transactionDate
                                    order by te.transaction.transactionDate desc
                                    """, [max: 1, product:product, inventory:location.inventory, transactionCode:TransactionCode.PRODUCT_INVENTORY] );

        //return results ? results[0] : null
        return results
    }

    def getQuantityChangeSince(Product product, Location location, TransactionCode transactionCode, Date transactionDate) {
        def results
        if (transactionDate) {
            results = Transaction.executeQuery( """
                                        SELECT sum(te.quantity)
                                        FROM TransactionEntry te
                                        JOIN te.transaction
                                        JOIN te.inventoryItem
                                        WHERE te.inventoryItem.product = :product
                                        AND te.transaction.inventory = :inventory
                                        AND te.transaction.transactionType.transactionCode = :transactionCode
                                        AND te.transaction.transactionDate >= :transactionDate
                                        """, [product:product, inventory:location.inventory, transactionCode:transactionCode, transactionDate: transactionDate] );
        }
        else {
            results = Transaction.executeQuery( """
                                        SELECT sum(te.quantity)
                                        FROM TransactionEntry te
                                        JOIN te.transaction
                                        JOIN te.inventoryItem
                                        WHERE te.inventoryItem.product = :product
                                        AND te.transaction.inventory = :inventory
                                        AND te.transaction.transactionType.transactionCode = :transactionCode
                                        """, [product:product, inventory:location.inventory, transactionCode:transactionCode] );

        }
        return results
    }

    /**
     * Generic product summary widget on the dashboard.
     *
     * @param location
     * @return
     */
    def getGenericProductSummary(location) {

        def genericProductMap = [:]


        // Create list of entries with quantity on hand for each product
        def entries = []
        def quantityMap = getQuantityByProductGroup(location)
        quantityMap.each { key, value ->
            entries << [product: key, genericProduct: key.genericProduct, currentQuantity: (value>0)?value:0]    // make sure currentQuantity >= 0
        }
        entries.sort { it.product.name }


        // Get the inventory levels for all products at the given location
        def inventoryLevelMap = InventoryLevel.findAllByInventory(location.inventory)?.groupBy { it.product }
        log.info inventoryLevelMap

        // Group entries by generic product
        genericProductMap = entries.inject([:].withDefault { [
                status:null,
                name:null,
                minQuantity:0,
                reorderQuantity:0,
                maxQuantity:0,
                currentQuantity:0,
                hasPreferred:false,
                lastUpdated:null,
                // Debugging information
                productCount:0,
                product:null,
                genericProduct:null,
                type:null,
                inventoryLevel:null,
                message:"",
                products:[]]
        } ) { map, entry ->
            def product = entry?.product
            def inventoryLevel = (inventoryLevelMap[product])?inventoryLevelMap[product][0]:null
            def nameKey = entry?.genericProduct?.description?:entry?.product?.name
            map[nameKey].name = nameKey

            if (entry?.genericProduct) {
                map[nameKey].genericProduct = entry?.genericProduct?.id
                map[nameKey].type = "Generic Product"
            }
            else {
                map[nameKey].product = entry?.product?.id
                map[nameKey].type = "Product"
            }
            map[nameKey].currentQuantity += entry.currentQuantity?:0
            map[nameKey].productCount++

            // If there's no preferred already
            if (!map[nameKey].hasPreferred) {
                // If this inventory level is preferred
                if (inventoryLevel?.preferred) {
                    map[nameKey].hasPreferred = true
                    map[nameKey].minQuantity = inventoryLevel?.minQuantity?:0
                    map[nameKey].reorderQuantity = inventoryLevel?.reorderQuantity?:0
                    map[nameKey].maxQuantity = inventoryLevel?.maxQuantity?:0
                    map[nameKey].inventoryLevel = inventoryLevel
                    map[nameKey].lastUpdated = inventoryLevel?.lastUpdated
                    map[nameKey].message = "INFO: Using preferred inventory level (${product?.productCode})."
                }
                // Otherwise, use the inventory level that was last updated
                else {
                    // jgreenspan says:
                    // I think that if a flag is not set then it should default to the most recently updated inventory level.
                    // If this is difficult to do then the report can show an error when there is no flag. Then this will force us to select a flag.
                    def lastUpdated1 = inventoryLevel?.lastUpdated
                    def lastUpdated2 = map[nameKey]?.lastUpdated

                    // If there's no existing inventory level OR current inventory level was updated after the existing
                    // inventory level then we should use the current inventory level
                    if (!lastUpdated2 || (lastUpdated1 && lastUpdated2 && lastUpdated1.after(lastUpdated2))) {
                        map[nameKey].minQuantity = inventoryLevel?.minQuantity?:0
                        map[nameKey].reorderQuantity = inventoryLevel?.reorderQuantity?:0
                        map[nameKey].maxQuantity = inventoryLevel?.maxQuantity?:0
                        map[nameKey].inventoryLevel = inventoryLevel
                        map[nameKey].lastUpdated = lastUpdated1
                    }
                    map[nameKey].message = "WARN: No preferred items, using last updated inventory level (${inventoryLevel?.product?.productCode})."
                }
            }
            // If there are more than one (preferred) inventory levels then we should use the latest
            else {
                if (inventoryLevel?.preferred) {
                    map[nameKey].minQuantity = 0
                    map[nameKey].reorderQuantity = 0
                    map[nameKey].maxQuantity = 0
                    map[nameKey].message = "ERROR: More than one preferred item, cannot determine inventory level to use."
                }
            }

            // FIXME Remove this once we're confident that the report looks ok.
            // DEBUG Keep track of each of the products for debugging purposes
            map[nameKey].products << [
                    product:entry.product?.name,
                    productCode: entry?.product?.productCode,
                    genericProduct: entry?.genericProduct?.description,
                    status: inventoryLevel?.statusMessage(entry.currentQuantity?:0),
                    minQuantity:inventoryLevel?.minQuantity?:0,
                    reorderQuantity:inventoryLevel?.reorderQuantity?:0,
                    maxQuantity:inventoryLevel?.maxQuantity?:0,
                    currentQuantity:entry.currentQuantity?:0,
                    preferred:inventoryLevel?.preferred?:false
            ]
            map
        }


        // Assign status based on inventory level values
        genericProductMap.each { k, v ->
            genericProductMap[k].status = getStatusMessage(null, v.minQuantity, v.reorderQuantity, v.maxQuantity, v.currentQuantity)
            //InventoryUtil.getStatusMessage(null, v.minQuantity, v.reorderQuantity, v.maxQuantity, v.currentQuantity)
        }
        return genericProductMap.values().groupBy { it.status }

    }

    /**
     * The logic for this method is located in several locations because the logic is specific to
     * a single product / inventoryLevel, whereas this method is for the entire product group.
     *
     * FIXME Move to InteventoryUtil when complete
     * FIXME Add unit tests for this logic
     *
     * @param inventoryStatus
     * @param minQuantity
     * @param reorderQuantity
     * @param maxQuantity
     * @param currentQuantity
     * @return
     */
    def String getStatusMessage(inventoryStatus, minQuantity, reorderQuantity, maxQuantity, currentQuantity) {
        def statusMessage = ""
        if (inventoryStatus == InventoryStatus.SUPPORTED  || !inventoryStatus) {
            if (currentQuantity <= 0) {
                if (maxQuantity == 0 && minQuantity == 0 && reorderQuantity == 0) {
                    statusMessage = "STOCK_OUT_OBSOLETE"
                }
                else {
                    statusMessage = "STOCK_OUT"
                }
            }
            else {
                if (minQuantity && minQuantity > 0 && currentQuantity <= minQuantity ) {
                    statusMessage = "LOW_STOCK"
                }
                else if (reorderQuantity && reorderQuantity > 0 && currentQuantity <= reorderQuantity ) {
                    statusMessage = "REORDER"
                }
                else if (maxQuantity && maxQuantity > 0 && currentQuantity > maxQuantity ) {
                    statusMessage = "OVERSTOCK"
                }
                else if (currentQuantity > 0) {
                    if (maxQuantity == 0 && minQuantity == 0 && reorderQuantity == 0) {
                        statusMessage = "IN_STOCK_OBSOLETE"
                    }
                    else {
                        statusMessage = "IN_STOCK"
                    }
                }
                else {
                    statusMessage = "OBSOLETE"
                }
            }
        }
        else if (inventoryStatus == InventoryStatus.NOT_SUPPORTED) {
            statusMessage = "NOT_SUPPORTED"
        }
        else if (inventoryStatus == InventoryStatus.SUPPORTED_NON_INVENTORY) {
            statusMessage = "SUPPORTED_NON_INVENTORY"
        }
        else {
            statusMessage = "UNAVAILABLE"
        }
        //log.info "getStatusMessage(${inventoryStatus}, ${minQuantity}, ${reorderQuantity}, ${maxQuantity}, ${currentQuantity}) = ${statusMessage}"
        return statusMessage
    }

    /**
     * Get fast moving items based on requisition data.
     *
     * @param location
     * @param date
     * @param max
     * @return
     */
    def getFastMovers(location, date, max) {
        def startTime = System.currentTimeMillis()
        def data = [:]
        try {
            data.location = location.id
            data.startDate = date-30
            data.endDate = date

            def criteria = RequisitionItem.createCriteria()
            def results = criteria.list {
                requisition {
                    eq("destination", location)
                    between("dateRequested", date-30, date)
                }
                projections {
                    //product {
                    //    groupProperty('id')
                    //    groupProperty('name')
                    //    groupProperty('productCode')
                    //}
                    groupProperty("product")
                    countDistinct('id', "occurrences")
                    sum("quantity", "quantity")
                }
                order('occurrences','desc')
                order('quantity','desc')
                if (max) { maxResults(max) }
            }

            def quantityMap = getQuantityByProductMap(location.inventory)
            //println "quantityMap: " + quantityMap

            def count = 1;
            data.results = results.collect {
                [
                        rank: count++,
                        id: it[0].id,
                        productCode: it[0].productCode,
                        name: it[0].name,
                        //genericProduct: it[0]?.genericProduct?.name?:"",
                        category: it[0]?.category?.name?:"",
                        requisitionCount: it[1],
                        quantityRequested: it[2],
                        quantityOnHand: (quantityMap[Product.get(it[0].id)]?:0),
                ]
            }
            data.responseTime = (System.currentTimeMillis() - startTime) + " ms"


        } catch (Exception e) {
            log.error("Error occurred while getting requisition items " + e.message, e)
            data = e.message
        }
        return data
    }

}


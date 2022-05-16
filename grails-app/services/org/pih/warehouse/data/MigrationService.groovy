/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.data

import grails.validation.ValidationException
import groovyx.gpars.GParsPool
import org.hibernate.FetchMode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.PreferenceTypeCode
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier

class MigrationService {

    def dataService
    def inventoryService
    def persistenceInterceptor

    boolean transactional = true

    def getInventoryTransactionEntries(Location location, Integer max) {
        def inventoryTransactions = TransactionEntry.createCriteria().list {
            fetchMode 'transaction', FetchMode.JOIN
            fetchMode 'transaction.inventory', FetchMode.JOIN
            fetchMode 'transaction.inventory.warehouse', FetchMode.JOIN
            fetchMode 'inventoryItem', FetchMode.JOIN
            fetchMode 'inventoryItem.product', FetchMode.JOIN
            transaction {
                transactionType {
                    eq("transactionCode", TransactionCode.INVENTORY)
                }
                eq("inventory", location.inventory)
                order("transactionDate", "asc")
                order("dateCreated", "asc")
            }
            maxResults(max)
        }
        return inventoryTransactions
    }

    def getTransactionEntries(Location location, Product product) {
        TransactionEntry.createCriteria().list {
            inventoryItem {
                eq("product", product)
            }
            transaction {
                eq("inventory", location.inventory)
                order("transactionDate", "asc")
                order("dateCreated", "asc")
            }
        }
    }


    def migrateInventoryTransactions(Location location, Product product, boolean performMigration) {

        def runningBalance
        def previousTransaction
        def runningBalanceList = []
        def transactionEntries = getTransactionEntries(location, product)
        def runningBalanceMap = [:]


        log.info "DATE".padRight(25) + "CODE".padRight(20) + "ITEMKEY".padRight(25) + "QTY".padRight(5) + "PRE".padRight(5) + "BAL".padRight(5) + "ADJ".padRight(5)

        for (transactionEntry in  transactionEntries) {


            boolean sameTransaction = (previousTransaction == transactionEntry.transaction)
            if (!sameTransaction && transactionEntry.transaction.transactionType.transactionCode == TransactionCode.PRODUCT_INVENTORY) {
                runningBalanceMap = [:]
            }
            def itemKey = "${transactionEntry?.inventoryItem?.product?.productCode}:${transactionEntry?.inventoryItem?.lotNumber}:${transactionEntry?.binLocation?.name}"
            Integer runningBalanceBefore = runningBalanceMap[itemKey]?:0

            runningBalance = applyTransactionEntry(runningBalanceBefore, transactionEntry, sameTransaction)
            runningBalanceMap[itemKey] = runningBalance
            Integer adjustmentQuantity = 0
            Integer oldQuantity = transactionEntry.quantity
            Integer productBalance = runningBalanceMap.values().sum()

            if (transactionEntry?.transaction?.transactionType?.transactionCode == TransactionCode.INVENTORY) {

                adjustmentQuantity = runningBalance - runningBalanceBefore

                // Convert inventory transaction to adjustment
                if (performMigration) {
                    transactionEntry.comments = "Automatically converted transaction from INVENTORY ${oldQuantity} " +
                            "to ADJUSTMENT ${adjustmentQuantity} with expected BALANCE ${productBalance} on ${new Date()}"
                    transactionEntry.transaction.transactionType = TransactionType.load(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
                    transactionEntry.quantity = adjustmentQuantity
                    transactionEntry.save()
                }
            }

            log.info "${transactionEntry?.transaction?.transactionDate.toString().padRight(25)}" +
                    "${transactionEntry?.transaction?.transactionType?.transactionCode.name().padRight(20)}" +
                    "${itemKey.padRight(25)}" +
                    "${transactionEntry?.quantity?.toString().padRight(5)}" +
                    "${runningBalanceBefore.toString()?.padRight(5)}" +
                    "${runningBalance.toString()?.padRight(5)}" +
                    "${adjustmentQuantity.toString()?.padRight(5)}"

            previousTransaction = transactionEntry.transaction

            runningBalanceList << [transactionEntry:
                                           [
                                                   productCode: transactionEntry?.inventoryItem?.product?.productCode,
                                                   lotNumber: transactionEntry?.inventoryItem.lotNumber,
                                                   binLocation: transactionEntry?.binLocation?.name,
                                                   transactionDate: transactionEntry?.transaction?.transactionDate,
                                                   transactionCode: transactionEntry?.transaction?.transactionType?.transactionCode.name(),
                                                   quantity: transactionEntry?.quantity
                                           ], runningBalance: runningBalanceMap.clone()]
        }

        return runningBalanceList
    }

    /**
     *
     * @param initialQuantity
     * @param transactionEntry
     * @return
     */
    def applyTransactionEntry(BigDecimal runningBalance, TransactionEntry transactionEntry, boolean sameTransaction) {
        BigDecimal balance = runningBalance;

        def transactionCode = transactionEntry?.transaction?.transactionType?.transactionCode;
        if (transactionCode == TransactionCode.PRODUCT_INVENTORY) {
            if (sameTransaction) {
                balance += transactionEntry.quantity;
            }
            else {
                balance = transactionEntry.quantity;
            }
        }
        else if (transactionCode == TransactionCode.INVENTORY) {
            balance = transactionEntry.quantity;
        }
        else if (transactionCode == TransactionCode.DEBIT) {
            balance -= transactionEntry.quantity;
        }
        else if (transactionCode == TransactionCode.CREDIT) {
            balance += transactionEntry.quantity;
        }
        return balance;
    }

    def getQuantityBeforeTransactionEntry(TransactionEntry transactionEntry) {
        def quantity = 0;
        InventoryItem inventoryItem = transactionEntry.inventoryItem
        Location location = transactionEntry.transaction.inventory.warehouse
        Date date = transactionEntry.transaction.transactionDate
        def transactionEntries = inventoryService.getTransactionEntriesOnOrBeforeDate(inventoryItem, location, date)
        log.info ("Transaction entries: " + transactionEntries)
        transactionEntries.remove(transactionEntry)
        quantity = inventoryService.adjustQuantity(quantity, transactionEntries)
        return quantity

    }

    def getQuantityByBinLocationsBeforeTransactionEntry(TransactionEntry transactionEntry) {
        InventoryItem inventoryItem = transactionEntry.inventoryItem
        Location location = transactionEntry.transaction.inventory.warehouse
        Date date = transactionEntry.transaction.transactionDate
        def transactionEntries = inventoryService.getTransactionEntriesOnOrBeforeDate(inventoryItem, location, date)
        transactionEntries.removeAll(transactionEntry.transaction?.transactionEntries)
        def binLocations = inventoryService.getQuantityByBinLocation(transactionEntries, true)
        binLocations = binLocations.collect { it.value }
        return binLocations

    }


    def migrateInventoryTransaction(TransactionEntry transactionEntry) {

        InventoryItem inventoryItem = transactionEntry?.inventoryItem
        Location transactionLocation = transactionEntry?.transaction?.inventory?.warehouse
        Location binLocation = transactionEntry?.binLocation
        Date transactionDate = transactionEntry.transaction?.transactionDate
        Product product = transactionEntry.inventoryItem?.product
        log.info "Process transaction entry ${transactionEntry.id} for product ${product.productCode}"

        BigDecimal quantityOnHandThen = getQuantityBeforeTransactionEntry(transactionEntry)
        //BigDecimal quantityOnHandThen = inventoryService.getQuantity(inventoryItem, transactionLocation, transactionDate)
        //BigDecimal binQuantityOnHandNow = inventoryService.getQuantityFromBinLocation(transactionLocation, binLocation, inventoryItem)
        def adjustmentDebit = TransactionType.load(Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID)
        def adjustmentCredit = TransactionType.load(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
        BigDecimal newQuantity = transactionEntry.quantity - quantityOnHandThen
        def oldQuantity = transactionEntry?.quantity
        def adjustmentType
        def newTransactionType
        if (newQuantity >= 0) {
            newTransactionType = adjustmentCredit
            adjustmentType = "CREDIT"
        }
        else {
            adjustmentType = "DEBIT"
            newQuantity = -newQuantity
            newTransactionType = adjustmentDebit
        }
//        transactionEntry?.quantity = newQuantity
//        transactionEntry?.transaction?.transactionType = newTransactionType
//        transactionEntry.comments = "Migrated from INVENTORY ${oldQuantity} to ${adjustmentType} ${newQuantity} (QoH should be ${quantityOnHandThen})"
//        transactionEntry.save(flush:true)

        def binLocations = getQuantityByBinLocationsBeforeTransactionEntry(transactionEntry)
        BigDecimal productQuantityOnHandNow = inventoryService.getQuantityOnHand(transactionLocation, product)

        return [
                locationId: transactionLocation.id,
                locationName: transactionLocation.name,
                productId: inventoryItem?.product?.id,
                code: inventoryItem.product?.productCode,
                lotNumber: inventoryItem.lotNumber,
                binLocation: binLocation?.name,
                transactionEntry: transactionEntry?.id,
                transactionDate: transactionEntry?.transaction?.transactionDate,
                transactionCode: transactionEntry?.transaction?.transactionType?.transactionCode,
                adjustmentType: adjustmentType,
                oldQuantity: oldQuantity,
                quantityOnHandThen:quantityOnHandThen,
                newQuantity: newQuantity,
                //itemQohThen: quantityOnHandThen,
                productQohNow: productQuantityOnHandNow,
                //binQohNow: binQuantityOnHandNow
                binLocations: binLocations
        ]


    }




    def migrateOrganizations() {
        def migratedList = []
        List suppliers = getSuppliersForMigration()
        PartyType partyType = PartyType.findByCode("ORG")
        suppliers.each { Location supplier ->
            if (!supplier.organization) {
                def organization = migrateOrganization(supplier, partyType)
                migratedList.add(organization)
            }
        }
        return migratedList
    }

    def migrationOrganizationsInParallel() {

        def migratedList = []
        List suppliers = getSuppliersForMigration()
        PartyType partyType = PartyType.findByCode("ORG")

        GParsPool.withPool {
            migratedList = suppliers.collectParallel { supplier ->
                persistenceInterceptor.init()
                supplier = Location.load(supplier.id)
                if (!supplier.organization) {
                    def organization = migrateOrganization(supplier, partyType)
                    persistenceInterceptor.flush()
                    return organization
                }
                persistenceInterceptor.destroy()
            }
        }

        log.info ("migrated: ${migratedList.size()}")

        return migratedList

    }

    def migrateOrganization(Location supplier, PartyType partyType) {
        def organization = findOrCreateOrganization(supplier.name, supplier.description, partyType, [RoleType.ROLE_SUPPLIER])
        if (!organization.save(flush: true)) {
            log.info("errors: " + organization.errors)
            throw new ValidationException("Cannot create organization ${organization?.name}: ", organization.errors)
        }
        supplier.organization = organization
        if (supplier.hasErrors() || !supplier.save(flush: true)) {
            log.info("errors: " + supplier.errors)
            throw new ValidationException("Cannot migrate supplier ${supplier?.name}: ", supplier.errors)
        }
        return organization
    }



    def getSuppliersForMigration() {
        LocationType supplierType = LocationType.findByLocationTypeCode(LocationTypeCode.SUPPLIER)
        def suppliers = Location.createCriteria().list {
            eq("active", true)
            eq("locationType", supplierType)
            isNull("organization")
        }
        return suppliers
    }


    def migrateProductSuppliersInParallel() {
        def migratedList = []
        //def ids = Product.executeQuery("select distinct(p.id) from Product p where p.active = true")
        def ids = getProductsForMigration()
        GParsPool.withPool {
            migratedList = ids.collectParallel { id ->
                persistenceInterceptor.init()
                try {
                    def productSupplier = migrateProductSupplier(id)
                    persistenceInterceptor.flush()
                    return productSupplier
                } catch (Exception e) {
                    log.error("Error migrating product supplier " + e.message, e)

                } finally {
                    persistenceInterceptor.destroy()
                }
            }
        }

        log.info ("migrated: ${migratedList.size()}")

        return migratedList

    }




    def migrateProductSuppliers() {
        def migratedList = []
        def ids = getProductsForMigration()
        ids.eachWithIndex { id, index ->
            def productSupplier = migrateProductSupplier(id)
            if (index % 50 == 0) {
                persistenceInterceptor.flush()
            }
            migratedList.add(productSupplier)
        }
        return migratedList
    }

    def getProductsForMigration() {

        def products = Product.createCriteria().list {
            projections {
                property("id")
            }
            or {
                isNotNull("manufacturer")
                isNotNull("manufacturerName")
                isNotNull("manufacturerCode")
                isNotNull("vendor")
                isNotNull("vendorName")
                isNotNull("vendorCode")
                isNotNull("brandName")
                isNotNull("modelNumber")
                isNotNull("upc")
                isNotNull("ndc")
            }
            //maxResults(5000)
        }
        return products;
    }


    def migrateProductSupplier(String id) {

        def now = new Date()
        Product product = Product.load(id)
        PartyType orgType = PartyType.findByCode("ORG")
        def productSupplier = new ProductSupplier()
        productSupplier.productCode = product.productCode
        productSupplier.name = product.name
        productSupplier.product = product
        productSupplier.description = product.description

        if (product.manufacturer) {
            def manufacturer = findOrCreateOrganization(product.manufacturer, null, orgType, [RoleType.ROLE_MANUFACTURER])
            productSupplier.manufacturer = manufacturer
            if (!productSupplier.manufacturer) {
                productSupplier.errors.rejectValue("manufacturer", "productSupplier.invalid.manufacturer","Manufacturer ${product?.manufacturer} does not exist.")
            }
        }

        if (product.vendor) {
            def supplier = findOrCreateOrganization(product.vendor, null, orgType, [RoleType.ROLE_SUPPLIER])
            productSupplier.supplier = supplier
            if (!productSupplier.supplier) {
                productSupplier.errors.rejectValue("supplier", "productSupplier.invalid.supplier", "Supplier ${product?.vendor} does not exist.")
            }
        }

        productSupplier.manufacturerCode = product.manufacturerCode
        productSupplier.manufacturerName = product.manufacturerName
        productSupplier.supplierCode = product.vendorCode
        productSupplier.supplierName = product.vendorName
        productSupplier.upc = product.upc
        productSupplier.ndc = product.ndc
        productSupplier.brandName = product.brandName
        productSupplier.modelNumber = product.modelNumber
        productSupplier.unitPrice = product.pricePerUnit
        productSupplier.ratingTypeCode = RatingTypeCode.NOT_RATED
        productSupplier.preferenceTypeCode = PreferenceTypeCode.NOT_PREFERRED

        if (!ProductSupplier.find(productSupplier)) {
            // To ensure that we can find the product supplier using the finder above we needed to
            // postponse setting of these two fields
            def index = (product.productSuppliers?.size() ?: 0) + 1
            productSupplier.code = product.productCode + "-${index}"
            productSupplier.comments = "Migrated ${now}"

            if (productSupplier.hasErrors() || !productSupplier.save()) {
                log.info("Product supplier " + productSupplier.errors)
                throw new ValidationException("Cannot migrate supplier ${productSupplier?.name}: ", productSupplier?.errors)
            }
        }
    }


    def findOrCreateOrganization(String name, String description, PartyType partyType, List roleTypes) {

        Organization organization = Organization.findByName(name)
        if (!organization) {
            organization = new Organization()
            organization.name = name
            organization.description = description
            organization.partyType = partyType
        }

        if (roleTypes) {
            roleTypes.each { roleType ->
                if (!organization.roles.find { it.roleType == roleType }) {
                    organization.addToRoles(new PartyRole(roleType: roleType))
                }
            }
        }

        if (organization.hasErrors() || !organization.save()) {
            throw new ValidationException("Validation error", organization.errors)
        }


        return organization
    }

    def deleteOrganizations() {

        int deletedCount

        LocationType supplierType = LocationType.findByLocationTypeCode(LocationTypeCode.SUPPLIER)
        def suppliers = Location.createCriteria().list {
            eq("active", true)
            eq("locationType", supplierType)
            isNotNull("organization")
        }

        if (suppliers) {
            suppliers.each { supplier ->
                supplier.organization.delete();
                supplier.organization = null
            }
            deletedCount = suppliers.size()

        } else {
            // FIXME Need to remove
            PartyRole.executeUpdate("delete from PartyRole")
            deletedCount = Organization.executeUpdate("delete from Organization")
        }

        return deletedCount

    }


    def deleteProductSuppliers() {
        def productSuppliers = ProductSupplier.createCriteria().list {
            ilike("comments", "Migrated%")
        }
        productSuppliers.each { productSupplier ->
            productSupplier.product?.removeFromProductSuppliers(productSupplier)
            productSupplier.delete();
        }

        return productSuppliers?.size()
    }
}

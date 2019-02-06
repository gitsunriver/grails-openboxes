/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.importer


import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExcelImportUtils
import org.pih.warehouse.core.Constants
import org.pih.warehouse.data.UserDataService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product


class UserExcelImporter extends AbstractExcelImporter {

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'username',
                'B':'firstName',
                'C':'lastName',
                'D':'email',
                'E':'defaultRoles'
		]
	]

	static Map propertyMap = [
			username:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            firstName:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            lastName:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            email:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            defaultRoles:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
	]


	UserExcelImporter(String fileName) {
		super(fileName)
	}

	def getDataService() {
		return ApplicationHolder.getApplication().getMainContext().getBean("userDataService")
	}

	List<Map> getData() {
		return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }


    void validateData(ImportDataCommand command) {
		dataService.validateData(command)
    }


    /**
     * Import data from given map into database.
     *
     * @param location
     * @param inventoryMapList
     * @param errors
     */
    void importData(ImportDataCommand command) {
        dataService.importData(command)
    }

}
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

import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExcelImportUtils

// import java.text.ParseException;
// import java.text.SimpleDateFormat;

import org.grails.plugins.excelimport.ExcelImportUtils
import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

import java.text.SimpleDateFormat

class InventoryExcelImporter extends AbstractExcelImporter {

    def inventoryService

	static Map cellMap = [ sheet:'Sheet1', startRow: 1, cellMap: [] ]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'productCode',
                'B':'product',
                'C':'lotNumber',
                'D':'expirationDate',
                'E':'manufacturer',
                'F':'manufacturerCode',
                'G':'quantity',
                'H':'binLocation',
                'I':'comments'
		]
	]

	static Map propertyMap = [
            productCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            product:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            lotNumber:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            expirationDate:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturer:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturerCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            quantity:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            binLocation:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            comments:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null])
	]


	public InventoryExcelImporter(String fileName) {
		super(fileName)
        inventoryService = ApplicationHolder.getApplication().getMainContext().getBean("inventoryService")
	}


	List<Map> getData() {
		return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }


    public void validateData(ImportDataCommand command) {
        inventoryService.validateInventoryData(command)
    }


    /**
     * Import data from given inventoryMapList into database.
     *
     * @param location
     * @param inventoryMapList
     * @param errors
     */
    public void importData(ImportDataCommand command) {
        inventoryService.importInventoryData(command)

    }

}
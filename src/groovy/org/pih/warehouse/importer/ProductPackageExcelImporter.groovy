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

import org.grails.plugins.excelimport.ExcelImportUtils

// import java.text.ParseException;
// import java.text.SimpleDateFormat;
class ProductPackageExcelImporter extends AbstractExcelImporter {

	def inventoryService

	static Map cellMap = [ sheet:'Sheet1', startRow: 1, cellMap: [] ]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'status',
                'B':'productCode',
                'C':'productName',
                'D':'category',
                'E':'tags',
                'F':'manufacturer',
                'G':'manufacturerCode',
                'H':'vendor',
                'I':'vendorCode',
                'J':'binLocation',
                'K':'unitOfMeasure',
                'L':'package',
                'M':'packageUom',
                'N':'packageSize',
                'O':'pricePerPackage',
                'P':'pricePerUnit',
                'Q':'minQuantity',
                'R':'reorderQuantity',
                'S':'maxQuantity',
                'T':'currentQuantity'
		]
	]

    static Map propertyMap = [
            status:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            productCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            productName: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            tags: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            category: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturer:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturerCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            vendor:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            vendorCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            binLocation:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            unitOfMeasure:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            package:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            packageUom:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            packageSize:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null]),
            pricePerPackage:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null]),
            pricePerUnit:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null]),
            //pricePerUnitStatic:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null]),
            minQuantity:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null]),
            reorderQuantity:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null]),
            maxQuantity:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null]),
            currentQuantity:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null])
	]




	public ProductPackageExcelImporter(String fileName) {
		super(fileName)
		//inventoryService = ApplicationHolder.getApplication().getMainContext().getBean("inventoryService")
	}


	List<Map> getData() {
		return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
	}



	public void validateData(ImportDataCommand command) { 
		//inventoryService.validateData(command)
	}

	public void importData(ImportDataCommand command) { 
		//inventoryService.importData(command)
	}






}
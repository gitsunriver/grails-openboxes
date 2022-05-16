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
class ProductGroupExcelImporter extends AbstractExcelImporter {

	def inventoryService

	static Map cellMap = [ sheet:'Sheet1', startRow: 1, cellMap: [] ]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'productCode',
                'B':'productName',
                'C':'genericProduct'
		]
	]

    static Map propertyMap = [
            productCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            productName: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            genericProduct: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null])
	]




	public ProductGroupExcelImporter(String fileName) {
		super(fileName)
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
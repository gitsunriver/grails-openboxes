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

import grails.converters.JSON
import grails.plugin.springcache.annotations.Cacheable
import org.pih.warehouse.core.Location
import org.pih.warehouse.data.DataService
import org.pih.warehouse.jobs.CalculateQuantityJob
import org.pih.warehouse.product.Product

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

class InventorySnapshotController {

    InventoryService inventoryService
    DataService dataService

    def index = {
        redirect(action:"list")

    }

    def list = {
        //def startDate = new Date() - 14
        //def endDate = new Date() + 14


        //def query = InventorySnapshot.createCriteria()
        //def inventorySnapshots = InventorySnapshot.list([max:100])

        //[inventorySnapshots:inventorySnapshots]
    }

    def show = {
        //def inventorySnapshot = InventorySnapshot.get(params.id)
        //[inventorySnapshot:inventorySnapshot]
    }

    def edit = {

    }

    def update = {
        println "Update inventory snapshot " + params
        try {

            log.info "Params.date = " + params.date
            log.info "Params.date.class = " + params.date.class

            def dateFormat = new SimpleDateFormat("MM/dd/yyyy")

            def date = new Date()
            date = dateFormat.parse(params.date)
            date.clearTime()

            def results = CalculateQuantityJob.triggerNow([locationId: params.location.id, date: date])
            render([results: results] as JSON)

        }
        catch (Exception e) {
            log.error("An error occurred while attempting to trigger inventory snapshot update: " + e.message, e)
            render([error:e.class.name, message:e.message]as JSON)
        }


    }

    def triggerCalculateQuantityOnHandJob = {
        println "triggerCalculateQuantityOnHandJob: " + params

        def results = CalculateQuantityJob.triggerNow([productId:params.product.id,locationId:params.location.id])
        //def location = Location.get(params.location.id)
        //def product = Product.get(params.product.id)
        //def results = inventoryService.getTransactionDates(location, product)

        println results
        render ([started:true, results:results] as JSON)

    }



    def dates = {
        Location location = Location.get(session.warehouse.id)
        def dates = inventoryService.getTransactionDates()
        render (dates as JSON)
    }

    def locations = {
        def locations = inventoryService.getDepotLocations()

        render (locations as JSON)

    }

    def download = {

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        Location location = Location.get(params?.location?.id?:session?.warehouse?.id)
        Date date = (params.date) ? dateFormat.parse(params.date) : new Date()

        def data = inventoryService.findInventorySnapshotByDateAndLocation(date, location)

        def csv = dataService.generateCsv(data)
        println "CSV: " + csv

        response.setHeader("Content-disposition", "attachment; filename='Stock-${location?.name}-${date.format("dd MMM yyyy")}.csv'")
        render(contentType:"text/csv", text: csv.toString(), encoding:"UTF-8")
        //response.outputStream.flush()

    }

    /**
     * Analytics > Inventory Snapshot data table
     */

    def findByDateAndLocation = {
        log.info "getInventorySnapshotsByDate: " + params
        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")

        try {
            def date
            if (params.date) {
                date = dateFormat.parse(params.date)
                date.clearTime()
            }
            def location = Location.get(params?.location?.id?:session?.warehouse?.id)

            List data = inventoryService.findInventorySnapshotByDateAndLocation(date, location)
            render(["aaData": data, "iTotalRecords": data.size() ?: 0, "iTotalDisplayRecords": data.size() ?: 0, "sEcho": 1] as JSON)


        }
        catch (Exception e) {
            response.status = 500
            render ([errorMessage: e.message] as JSON)
            return;
        }

    }

}

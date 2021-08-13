package org.pih.warehouse.putaway

import grails.plugin.rendering.pdf.PdfRenderingService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.order.Order

class PutAwayController {

    PdfRenderingService pdfRenderingService
    def productAvailabilityService

    def index = {
        redirect(action: "create")
    }

    def list = {
        redirect(action: "create")
    }

    // This template is generated by webpack during application start
    def create = {
        render(template: "/common/react")
    }


    def generatePdf = {
        log.info "Params " + params

        Putaway putaway
        JSONObject jsonObject

        if (request.method == "POST") {
            jsonObject = request.JSON
        } else if (params.id) {
            Order order = Order.get(params.id)
            putaway = Putaway.createFromOrder(order)
            putaway.putawayItems.each { PutawayItem putawayItem ->
                putawayItem.availableItems =
                        productAvailabilityService.getAllAvailableBinLocations(putawayItem.currentFacility, putawayItem.product)
                putawayItem.inventoryLevel = InventoryLevel.findByProductAndInventory(putawayItem.product, putaway.origin.inventory)
            }
            jsonObject = new JSONObject(putaway.toJson())
        }

        renderPdf(
                template: "/putAway/print",
                model: [jsonObject: jsonObject],
                filename: "Putaway ${putaway?.putawayNumber}.pdf"
        )
    }
}

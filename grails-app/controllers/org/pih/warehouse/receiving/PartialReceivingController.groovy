package org.pih.warehouse.receiving

import org.pih.warehouse.shipping.Shipment

class PartialReceivingController {

    def receiptService

    def index = {
        redirect(action: "create")
    }

    def create = {
        Shipment shipment = Shipment.get(params.id)

        receiptService.createTemporaryReceivingBin(shipment)

        // This template is generated by webpack during application start
        render(template: "/partialReceiving/create")
    }

    def rollbackLastReceipt = {
        Shipment shipment = Shipment.get(params.id)

        if (shipment) {
            try {
                receiptService.rollbackLastReceipt(shipment)
                flash.message = "Successfully rolled back last receipt in stock movement with ID ${params.id}"
            } catch (Exception e) {
                log.warn("Unable to rollback last receipt in stock movement with ID ${params.id}: " + e.message)
                flash.message = "Unable to rollback last receipt in stock movement with ID ${params.id}: " + e.message
            }
        }

        redirect(controller: "stockMovement", action: "show", id: params.id)
    }
}

package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class ViewShipmentPage extends Page {
    static url = TestFixture.baseUrl + "/shipment/showDetails"
    static at = { title == "View shipment"}
    static content = {
        shipmentName {$("#shipmentSummary span.title").text().trim()}
        status {$("#shipmentSummary span.status label").text().trim()}
        type {$("#shipmentSummary span.shipmentType label").text().trim()}
        shipmentOrigin {$("#shipmentOrigin").text().trim()}
        shipmentDestination {$("#shipmentDestination").text().trim()}
        firstShipmentItem {$(".shipmentItem").first()}
        product {firstShipmentItem.find(".product").text().trim()}
        lotNumber {firstShipmentItem.find(".lotNumber").text().trim()}
        quantity {firstShipmentItem.find(".quantity").text().trim()}
        actionButton {$("button.action-btn")}
        receiveShipmentLink(to: ReceiveShipmentPage) {$("div.action-menu-item a", name: "receiveShipmentLink")}
    }

    def verifyShipmentItemExist(product, quantity, container_unit, container_details=""){
        def row = $(".shipmentItem").find{it.find(".product", text:contains(product))}
        assert row != null
        assert row.find(".quantity", text:contains(quantity))
        assert row.find(".container", text:contains(container_unit) )
        assert row.find(".container", text:contains(container_details))
        true
    }
}

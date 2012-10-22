package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.ReceiveShipmentPage
import testutils.TestFixture

import org.pih.warehouse.pages.CreateEnterShipmentDetailsPage
import org.pih.warehouse.pages.EnterTrackingDetailsPage
import org.pih.warehouse.pages.EditPackingListPage
import org.pih.warehouse.pages.SendShipmentPage
import org.pih.warehouse.pages.ViewShipmentPage
import org.pih.warehouse.pages.ShipmentListPage


class ShipmentSpec extends GebReportingSpec{

    def "should send a sea shipment from Boston to Miami"(){
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        def shipment_name = product_name + "_shipment"

        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000)
            to CreateEnterShipmentDetailsPage
        when:
            at CreateEnterShipmentDetailsPage
            shipmentType.value("Sea")
            shipmentName.value(shipment_name)
            origin.value("Boston Headquarters [Depot]")
            destination.value("Miami Warehouse [Depot]")
            expectedShippingDate.click()
            datePicker.today.click()
            expectedArrivalDate.click()
            datePicker.tomorrow.click()
            nextButton.click()
        and:
            at EnterTrackingDetailsPage
            nextButton.click()
        and:
            at EditPackingListPage
            addUnpackedItems()
            addItem(product_name, 200)
            nextButton.click()
        and:
            at SendShipmentPage
            actualShippingDate.click()
            datePicker.today.click()
            nextButton.click()
        then:
            at ViewShipmentPage
            shipmentName ==  shipment_name
            status == "Shipped"
            type == "Sea"
            shipmentOrigin == "Boston Headquarters"
            shipmentDestination == "Miami Warehouse"
            product == product_name
            quantity == "200"
    }


    def "should be able to send a suitcase from Boston to Miami"() {
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        def shipment_name = product_name + "_shipment"

        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000)
            to CreateEnterShipmentDetailsPage
        when:
            at CreateEnterShipmentDetailsPage
            shipmentType.value("Suitcase")
            shipmentName.value(shipment_name)
            origin.value("Boston Headquarters [Depot]")
            destination.value("Miami Warehouse [Depot]")
            expectedShippingDate.click()
            datePicker.today.click()
            expectedArrivalDate.click()
            datePicker.tomorrow.click()
            nextButton.click()
        and:
            at EnterTrackingDetailsPage
            nextButton.click()
        and:
            at EditPackingListPage

            addSuitcase(unit:"box", weight:30, height:1, width:1.5, length:2)
            addItem(product_name, 200)
            nextButton.click()
        and:
            at SendShipmentPage
            actualShippingDate.click()
            datePicker.today.click()
            nextButton.click()
        then:
            at ViewShipmentPage
            shipmentName == shipment_name
            status == "Shipped"
            type == "Suitcase"
            shipmentOrigin == "Boston Headquarters"
            shipmentDestination == "Miami Warehouse"
            product == product_name
            quantity == "200"
    }

    def "view pending shipments"(){
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        def shipment_name = product_name + "_shipment"
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000)
            TestFixture.CreatePendingShipment(product_name, shipment_name, 20)
        when:
            to ShipmentListPage
        then:
            pendingItems.contains(shipment_name)

    }

    def "should be able to receive a shipment from Miami"() {
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        def shipment_name = product_name + "_shipment"

        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000)
            TestFixture.SendShipment(shipment_name, product_name, "Miami Warehouse [Depot]", "Boston Headquarters [Depot]")
        when:
            at ViewShipmentPage
            actionButton.click()
            receiveShipmentLink.click()
        and:
            at ReceiveShipmentPage
            deliveredOnDate.click()
            datePicker.today.click()
            saveButton.click()
        then:
            at ViewShipmentPage
            status == "Received"
    }
}

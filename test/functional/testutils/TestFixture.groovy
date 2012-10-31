package testutils

import geb.Browser
import org.pih.warehouse.core.Location
import org.pih.warehouse.pages.InventoryPage
import org.pih.warehouse.pages.LocationPage
import org.pih.warehouse.pages.LoginPage
import org.pih.warehouse.pages.CreateEnterShipmentDetailsPage
import org.pih.warehouse.pages.EnterTrackingDetailsPage
import org.pih.warehouse.pages.EditPackingListPage
import org.pih.warehouse.pages.ProductPage
import org.pih.warehouse.pages.SendShipmentPage
import org.pih.warehouse.pages.ViewShipmentPage
import org.pih.warehouse.pages.CreateLocationPage
import org.pih.warehouse.product.Product




class TestFixture{

    final  static  String baseUrl = "/openboxes"
    final  static  String Advil200mg = "Advil 200mg"  //expiring in 3 days
    final  static  String Tylenol325mg = "Tylenol 325mg"  //expiring in 20 days
    final  static  String Aspirin20mg = "Aspirin 20mg"   //expiring in 120 days
    final  static  String GeneralPainReliever = "General Pain Reliever"  //expiring in 200days
    final  static  String SimilacAdvanceLowiron400g = "Similac Advance low iron 400g"  //expired yesterday
    final  static  String SimilacAdvanceIron365g = "Similac Advance + iron 365g" //expired 30 days ago
    final  static  String MacBookPro8G = "MacBook Pro 8G"  //expiring in 1000 days
    final  static  String PrintPaperA4 = "Print Paper A4"  //no expiration date

    static String currentUser = null


    static def bostonManagerLogined(){
      currentUser = "BostonManager"
    }

    static def UserLoginedAsManagerForBoston(){
        if(currentUser == "BostonManager") return

        Browser.drive {
        to LoginPage
        loginForm.with{
            username = "manager"
            password = "password"
        }
        submitButton.click()

        at LocationPage

        boston.click()

        currentUser = "BostonManager"

        }
    }

    static String TestSupplierName = "Test Supplier"
    static Location CreateSupplierIfRequired() {
        def loc = Location.findByName(TestSupplierName)
        if(loc) return loc


        Browser.drive {
            to CreateLocationPage
            locationName.value(TestSupplierName)
            locationType.value("4") //supplier
            supportActivities.value("")
            saveButton.click()
        }
        return Location.findByName(TestSupplierName)
    }

    static void CreateProductInInventory(productName, quantity, expirationDate = new Date().plus(30)) {
        Browser.drive {
            to ProductPage

            productDescription.value(productName)
            productCategory.value("2") //supplies
            unitOfMeasure.value("pill")
            manufacturer.value("Xemon")
            manufacturerCode.value("ABC")

            saveButton.click()

            at InventoryPage
            assert productName == productName

            lotNumber.value("47")
            expires.click()
            datePicker.pickDate(expirationDate)
            newQuantity.click()
            newQuantity.value(7963)

            saveInventoryItem.click()

        }
    }

    static void CreatePendingShipment(product_name, shipment_name, quantity) {
        Browser.drive {
            to CreateEnterShipmentDetailsPage
            shipmentType.value("Sea")
            shipmentName.value(shipment_name)
            origin.value("Boston Headquarters [Depot]")
            destination.value("Miami Warehouse [Depot]")
            expectedShippingDate.click()
            datePicker.today.click()
            expectedArrivalDate.click()
            datePicker.pickDate(new Date().plus(1))
            nextButton.click()

            at EnterTrackingDetailsPage
            nextButton.click()
            and:
            at EditPackingListPage
            addUnpackedItems()
            addItem(product_name, quantity)
            saveAndExitButton.click()
            at ViewShipmentPage

        }
    }

    static void SendShipment(shipment_name, product_name, shipmentOrigin, shipmentDestination) {
        Browser.drive {
            to CreateEnterShipmentDetailsPage
            shipmentType.value("Land")
            shipmentName.value(shipment_name)
            origin.value(shipmentOrigin)
            destination.value(shipmentDestination)
            expectedShippingDate.click()
            datePicker.today.click()
            expectedArrivalDate.click()
            datePicker.pickDate(new Date().plus(1))
            nextButton.click()

            at EnterTrackingDetailsPage
            nextButton.click()

            at EditPackingListPage
            addUnpackedItems()
            addIncomingItem(product_name, 200)
            nextButton.click()

            at SendShipmentPage
            actualShippingDate.click()
            datePicker.today.click()
            nextButton.click()

            at ViewShipmentPage
            assert shipmentName == shipment_name && status == "Shipped"
        }
    }



}
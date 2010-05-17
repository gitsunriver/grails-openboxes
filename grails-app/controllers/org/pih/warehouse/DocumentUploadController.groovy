package org.pih.warehouse


class DocumentUploadCommand {
   String shipmentId
   Attachment document   
}

class DocumentUploadController {
   def upload = { DocumentUploadCommand command ->
      def shipment = Shipment.get(command.shipmentId)
      shipment.addToDocuments(command.document);
      redirect(action: 'view', id: command.shipmentId)
   }
   def form = {

      [ shipments : Shipment.list() ]
   }
   def view = {
      // pass through to "view shipment" page
   }
}

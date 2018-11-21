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

import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.api.StocklistLocation
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.Requisition

class StocklistService {

    def requisitionService
    def locationService

    boolean transactional = true

    List<StocklistLocation> getStocklistLocations() {
        List<Requisition> requisitions = requisitionService.getAllRequisitionTemplates(new Requisition(isTemplate: true), null)
        Map<String, List<Stocklist>> stocklistMap = requisitions?.collect { Requisition requisition ->
            return Stocklist.createFromRequisition(requisition)
        }?.groupBy { it.location.id }

        List<Location> locations = locationService.getAllLocations()

        return locations.collect { Location location ->
            return new StocklistLocation(location: location, stocklists: stocklistMap.get(location.id) ?: [])
        }
    }

    Stocklist getStocklist(String id) {
        Requisition requisition = Requisition.findByIdAndIsTemplate(id, true)

        if (!requisition) {
            return null
        }

        return Stocklist.createFromRequisition(requisition)
    }

    Stocklist createStocklist(Stocklist stocklist) {
        Requisition requisition = new Requisition()
        requisition.isTemplate = true
        requisition.name = stocklist.name
        requisition.destination = stocklist.location
        requisition.origin = stocklist.location
        requisition.requestedBy = stocklist.manager

        requisition = requisitionService.saveTemplateRequisition(requisition)

        return Stocklist.createFromRequisition(requisition)
    }

    Stocklist updateStocklist(Stocklist stocklist) {
        Requisition requisition = stocklist.requisition
        requisition.name = stocklist.name
        requisition.destination = stocklist.location
        requisition.origin = stocklist.location
        requisition.requestedBy = stocklist.manager

        requisition = requisitionService.saveTemplateRequisition(requisition)

        return Stocklist.createFromRequisition(requisition)
    }

    void deleteStocklist(String id) {
        Requisition requisition = Requisition.findByIdAndIsTemplate(id, true)
        if (!requisition) {
            throw new IllegalArgumentException("No Stock List found with ID ${id}")
        }

        requisitionService.deleteRequisition(requisition)
    }
}

package org.pih.warehouse.modules

import geb.Module

class SearchIncomingInventoryItemModule extends Module {
    static content = {
        searchCriteria(wait: true) {$("input", name: "productSearch")}
        firstSuggestion(wait: true) {$("table#results button.choose").first()}
    }

    def findProduct(product_name) {
        searchCriteria.value(product_name)
        firstSuggestion.click()
    }
}
import grails.validation.ValidationException
import org.apache.http.auth.AuthenticationException
import org.hibernate.ObjectNotFoundException

/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
class UrlMappings {
	static mappings = {

		"/snapshot/$action?"(controller: "inventorySnapshot")

		"/$controller/$action?/$id?" {
		      constraints {
				 // apply constraints here
			  }
		}

        // REST APIs with complex resource names or subresources

        "/api/categories"(parseRequest: true) {
            controller = { "categoryApi" }
            action = [GET: "list", POST: "save"]
        }
        "/api/categories/$id"(parseRequest: true) {
            controller = {"categoryApi" }
            action = [GET:"read", POST:"save", PUT:"save", DELETE:"delete"]
        }
//        "/api/products/$id/associatedProducts" {
//            controller = { "productApi" }
//            action = [GET: "associatedProducts"]
//        }
        "/api/products/$id/$action" {
            controller = { "productApi" }
        }

        // Stock Movement Item API

        "/api/stockMovementItems"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [GET:"list", POST: "update"]
        }

        "/api/stockMovementItems/$id"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [GET:"read", POST: "update"]
        }

        // Partial Receiving API

        "/api/partialReceiving"(parseRequest: true) {
            controller = "partialReceivingApi"
            action = [GET:"list", POST: "create"]
        }

        "/api/partialReceiving/$id"(parseRequest: true) {
            controller = "partialReceivingApi"
            action = [GET:"read", POST: "update"]
        }

        // Internal Locations API

        "/api/internalLocations/receiving"(parseRequest: true) {
            controller = "internalLocationApi"
            action = [GET:"listReceiving"]
        }

        // Stocklist Item API

        "/api/stocklistItems/availableStocklists"(parseRequest: true) {
            controller = "stocklistItemApi"
            action = [GET:"availableStocklists"]
        }

        // Standard REST APIs

        "/api/${resource}s"(parseRequest: true) {
            controller = { "${params.resource}Api" }
            action = [GET: "list", POST: "create"]
        }

        "/api/${resource}s/$id/status"(parseRequest: true) {
            controller = {"${params.resource}Api" }
            action = [GET: "status", DELETE: "deleteStatus", POST:"updateStatus"]
        }

        "/api/${resource}s/$id"(parseRequest: true) {
            controller = {"${params.resource}Api" }
            action = [GET:"read", POST:"update", PUT:"update", DELETE:"delete"]
        }



        // Anonymous REST APIs like Status, Login, Logout

		"/api/$action/$id?"(controller:"api", parseRequest:false){
			//action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
		}

        // Generic API for all other resources

        "/api/generic/${resource}/"(parseRequest: false) {
            controller = "genericApi"
            action = [GET: "list", POST:"create"]
        }

        "/api/generic/${resource}/search"(parseRequest: false) {
            controller = "genericApi"
            action = [GET: "search", POST:"search"]
        }

        "/api/generic/${resource}/$id"(parseRequest: false) {
            controller = "genericApi"
            action = [GET:"read", POST:"update", PUT:"update", DELETE:"delete"]
        }

        // Error handling

        "401"(controller:"errors", action:"handleUnauthorized")
		"404"(controller:"errors", action:"handleNotFound")
        "405"(controller:"errors", action:"handleMethodNotAllowed")
		"500"(controller:"errors", action:"handleException")
        "500"(controller:"errors", action:"handleNotFound", exception: ObjectNotFoundException)
        "500"(controller:"errors", action:"handleValidationErrors", exception: ValidationException)
        "500"(controller:"errors", action:"handleUnauthorized", exception: AuthenticationException)
        "/"(controller:"home", action:"index")
	}



}

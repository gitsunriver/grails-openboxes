package org.pih.warehouse.jobs


import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Product
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import util.LiquibaseUtil

@DisallowConcurrentExecution
class RefreshInventorySnapshotJob {

    def grailsApplication
    def inventorySnapshotService

    // Should never be triggered on a schedule - should only be triggered by persistence event listener
    static triggers = { }

    def execute(JobExecutionContext context) {

        Boolean enabled = grailsApplication.config.openboxes.jobs.refreshInventorySnapshotJob.enabled

        if (enabled) {
            log.info("Refresh inventory snapshots with data: " + context.mergedJobDataMap)

            def startTime = System.currentTimeMillis()
            def startDate = context.mergedJobDataMap.get('startDate')
            def locationId = context.mergedJobDataMap.get('location')

            Location location = Location.get(locationId)

            // Refresh inventory snapshot for today
            inventorySnapshotService.populateInventorySnapshots(new Date(), location)

            log.info "Refreshed inventory snapshot table for location ${location?.name} and start date ${startDate}: ${(System.currentTimeMillis() - startTime)} ms"
        }
    }
}
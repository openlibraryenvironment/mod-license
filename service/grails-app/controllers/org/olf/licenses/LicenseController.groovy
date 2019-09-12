package org.olf.licenses

import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j

import org.olf.licenses.License

import com.k_int.okapi.OkapiTenantAwareController
import com.k_int.web.toolkit.refdata.RefdataValue
import grails.converters.JSON


/**
 * Control access to subscription agreements.
 * A subscription agreement (SA) is the connection between a set of resources (Which could be packages or individual titles) and a license. 
 * SAs have start dates, end dates and renewal dates. This controller exposes functions for interacting with the list of SAs
 */
@CurrentTenant
class LicenseController extends OkapiTenantAwareController<License>  {

  LicenseController() {
    super(License)
  }
  
  // Override the show method
  def show() {
    // Applicable amendments might be present.
    final def am = params.list('amendments')
    if (!am) {
      // Just follow the super implementation
      return super.show()
    }
    
    // Otherwise let's mutate the original license and only supply the applicable terms.
    final License license = params.id ? License.read(params.id) : null
    
    if (license) {
      // Lookup the active status first.
      RefdataValue active = LicenseAmendment.lookupStatus('active')
      
      // We found a license. Lets append the active amendments by startdate
      List<LicenseAmendment> applicableAmendments = LicenseAmendment.findAllByOwnerAndStatus(license, active, [sort: "startDate", order: "desc"])
      
      if (applicableAmendments) {
        license += applicableAmendments.sum()
      }
    }
    
    respond license
  }
  
}


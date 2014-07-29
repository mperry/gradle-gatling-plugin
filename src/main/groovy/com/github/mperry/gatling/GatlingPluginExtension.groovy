package com.github.mperry.gatling

import groovy.transform.Canonical
import groovy.transform.Immutable
import groovy.transform.TypeChecked

/**
 * User: harebu
 * Date: 2/24/14
 * Time: 9:29 AM
 */
@TypeChecked
@Canonical
class GatlingPluginExtension {

    String include = null
    String exclude = null
    Boolean list = true
    Boolean dryRun = false

    boolean doTests() {
        !dryRun
    }
}

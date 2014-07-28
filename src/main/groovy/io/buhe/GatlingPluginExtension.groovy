package io.buhe

import groovy.transform.TypeChecked

/**
 * User: harebu
 * Date: 2/24/14
 * Time: 9:29 AM
 */
@TypeChecked
class GatlingPluginExtension {

    String include
    String exclude
    Boolean list = true
    Boolean dryRun = false

    boolean doTests() {
        !list
    }
}

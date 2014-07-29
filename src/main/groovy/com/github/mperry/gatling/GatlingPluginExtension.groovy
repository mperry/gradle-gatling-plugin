package com.github.mperry.gatling

import groovy.transform.Canonical
import groovy.transform.TypeChecked

@TypeChecked
@Canonical
class GatlingPluginExtension {

    String include = null
    String exclude = null
    Boolean list = true
    Boolean dryRun = false

}

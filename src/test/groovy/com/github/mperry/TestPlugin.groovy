package com.github.mperry

import com.github.mperry.gatling.GatlingPlugin
import com.github.mperry.gatling.GatlingPluginExtension
import fj.data.IO
import groovy.transform.TypeChecked
import org.junit.Assert
import org.junit.Test
import org.slf4j.LoggerFactory

import java.nio.file.Paths

/**
 * Created by mperry on 28/07/2014.
 */
@TypeChecked
class TestPlugin {

    @Test
    void test() {
        Assert.assertTrue(true)
        def io = GatlingPlugin.listRecursively(Paths.get("."))
        println io.run()
    }

    @Test
    void test2() {
        def e = new GatlingPluginExtension(dryRun: false, list: true, include: "com.github.mperry.scenario.*")
        def logger = LoggerFactory.getLogger(this.class)
        def mainDir = "/Users/mperry/repositories/gradle-gatling-plugin"
        def testDir = "$mainDir/build/classes/test"
        def report = "$mainDir/reports"

        GatlingPlugin.apply(e, logger, testDir, { String clazz ->
            { ->
                GatlingPlugin.run(clazz, report, testDir)
            } as IO
        })
    }

}

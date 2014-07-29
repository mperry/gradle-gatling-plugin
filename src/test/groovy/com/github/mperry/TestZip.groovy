package com.github.mperry

import groovy.transform.TypeChecked
import org.junit.Test

/**
 * Created by mperry on 29/07/2014.
 */
@TypeChecked
class TestZip {

    @Test
    void test1() {
        def list = [1, 2, 3].zip([4, 5, 6])
        println list
    }

}

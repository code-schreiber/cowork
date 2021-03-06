package com.toolslab.cowork.app.util

import org.amshove.kluent.shouldEqual
import org.junit.Test

class UtilsKtTest {

    @Test
    fun median() {
        listOf(1.0, 2.0, 1000.0).median() shouldEqual 2.0
        listOf(1.0, 2.0, 3.0, 1000.0).median() shouldEqual 2.5
        listOf(-123.456, -0.1, 0.1, 123.456).median() shouldEqual 0.0
        listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0).median() shouldEqual 5.0
        listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0).median() shouldEqual 5.5
        listOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0).median() shouldEqual 5.0
    }

    @Test
    fun medianOfEmptyList() {
        emptyList<Double>().median() shouldEqual 0.0
    }

    @Test
    fun min() {
        listOf(1.0, 2.0, 1000.0).min() shouldEqual 1.0
        emptyList<Double>().min() shouldEqual 0.0
    }

    @Test
    fun max() {
        listOf(1.0, 2.0, 1000.0).max() shouldEqual 1000.0
        emptyList<Double>().max() shouldEqual 0.0
    }

}

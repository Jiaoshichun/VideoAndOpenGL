package com.heng.ku.jnitest

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
//        assertEquals(4, 2 + 2)
        System.out.println(-90%360)
    }

    @Test
    fun test() {
        val jniTest = JniTest()
        System.out.println(jniTest.stringFromJNI())
    }
}

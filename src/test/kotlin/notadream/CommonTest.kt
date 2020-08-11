package notadream

import org.testng.Assert
import org.testng.annotations.Test

class CommonTest {
    @Test
    fun testGreeting() {
        println("testGreeting....")
        Assert.assertEquals(Common.greeting(), "Hello Gradle Kotlin DSL!")
    }
}
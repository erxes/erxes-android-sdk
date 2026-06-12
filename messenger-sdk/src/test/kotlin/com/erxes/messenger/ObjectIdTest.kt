package com.erxes.messenger

import com.erxes.messenger.session.ObjectId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObjectIdTest {

    @Test
    fun `is 24 lowercase hex characters`() {
        val id = ObjectId.generate()
        assertEquals(24, id.length)
        assertTrue("not hex: $id", id.matches(Regex("[0-9a-f]{24}")))
    }

    @Test
    fun `successive ids are unique`() {
        val ids = (0 until 1000).map { ObjectId.generate() }.toSet()
        assertEquals(1000, ids.size)
    }

    @Test
    fun `two ids differ`() {
        assertNotEquals(ObjectId.generate(), ObjectId.generate())
    }
}

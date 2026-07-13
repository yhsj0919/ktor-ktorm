package xyz.yhsj.ktor.auth.crypto

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class PasswordUtilTest {
    @Test
    fun `hashed password can be verified but cannot be reversed`() {
        val rawPassword = "admin#@."
        val encoded = PasswordUtil.hash(rawPassword)

        assertTrue(PasswordUtil.isHashed(encoded))
        assertNotEquals(rawPassword, encoded)
        assertTrue(PasswordUtil.matches(rawPassword, encoded))
        assertFalse(PasswordUtil.matches("wrong-password", encoded))
    }

    @Test
    fun `invalid stored password returns false`() {
        assertFalse(PasswordUtil.matches("password", "not-a-bcrypt-hash"))
        assertFalse(PasswordUtil.matches("password", null))
    }

    @Test
    fun `blank password is rejected when hashing`() {
        assertFailsWith<IllegalArgumentException> {
            PasswordUtil.hash(" ")
        }
    }
}

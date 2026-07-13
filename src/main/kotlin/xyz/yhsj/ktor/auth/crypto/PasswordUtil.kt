package xyz.yhsj.ktor.auth.crypto

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * 密码哈希工具。
 *
 * 使用 JDK 原生 PBKDF2，结果不能还原为原密码，只能用于匹配校验。
 * 格式：{pbkdf2-sha256}迭代次数$盐$哈希值。
 */
object PasswordUtil {
    private const val PREFIX = "{pbkdf2-sha256}"
    private const val ITERATIONS = 600_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private val random = SecureRandom()
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun hash(rawPassword: String): String {
        require(rawPassword.isNotBlank()) { "密码不能为空" }
        val salt = ByteArray(SALT_LENGTH).also(random::nextBytes)
        val derived = derive(rawPassword, salt, ITERATIONS)
        return PREFIX + ITERATIONS + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(derived)
    }

    fun matches(rawPassword: String, encodedPassword: String?): Boolean {
        if (rawPassword.isEmpty() || encodedPassword.isNullOrBlank()) return false
        if (!encodedPassword.startsWith(PREFIX)) return false

        return runCatching {
            val parts = encodedPassword.removePrefix(PREFIX).split('$')
            if (parts.size != 3) return false

            val iterations = parts[0].toInt()
            require(iterations > 0)
            val salt = decoder.decode(parts[1])
            val expected = decoder.decode(parts[2])
            val actual = derive(rawPassword, salt, iterations)
            MessageDigest.isEqual(actual, expected)
        }.getOrDefault(false)
    }

    fun isHashed(value: String?): Boolean = value?.startsWith(PREFIX) == true

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }
}

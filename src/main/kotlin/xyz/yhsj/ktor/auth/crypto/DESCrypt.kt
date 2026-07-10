package xyz.yhsj.ktor.auth.crypto

import xyz.yhsj.ktor.infrastructure.config.DES_KEY
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

object DESCrypt {
    /**
     * des加密
     */

    fun encode(value: String?, password: String = DES_KEY): String {
        if (value.isNullOrEmpty()) return ""
        //创建cipher对象
        val cipher = Cipher.getInstance("DES")

        //初始化cipher(参数：加密/解密模式)
        val kf = SecretKeyFactory.getInstance("DES")
        val keySpec = DESKeySpec(password.toByteArray())

        val key: Key = kf.generateSecret(keySpec)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        //加密/解密
        val encrypt = cipher.doFinal(value.toByteArray())

        //base64加密
        return String(Base64.getEncoder().encode(encrypt))
    }

    /**
     *  des解密
     */

    fun decode(value: String?, password: String = DES_KEY): String {
        if (value.isNullOrEmpty()) return ""
        //创建cipher对象
        val cipher = Cipher.getInstance("DES")

        //初始化cipher(参数：加密/解密模式)
        val kf = SecretKeyFactory.getInstance("DES")
        val keySpec = DESKeySpec(password.toByteArray())

        val key: Key = kf.generateSecret(keySpec)
        cipher.init(Cipher.DECRYPT_MODE, key)

        //base64解码
        val encrypt = try {
            cipher.doFinal(Base64.getDecoder().decode(value))
        } catch (e: Exception) {
            byteArrayOf()
        }


        return String(encrypt)
    }

    /**
     * 加密公司key
     */
    fun encodeCompanyId(id: Long?): String {
        return encode(String.format("%8s", id).replace(" ", "0"), DES_KEY)
    }

    /**
     * 解密key
     */
    fun decodeCompanyId(id: String?): Long? {
        return decode(id, DES_KEY).toLongOrNull()
    }

}

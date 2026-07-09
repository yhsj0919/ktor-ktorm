package xyz.yhsj.ktor.ext
import java.io.File
import java.util.Locale

object RuntimeEnv {

    /** 操作系统 */
    enum class OS {
        WINDOWS, LINUX, MAC, OTHER
    }

    /** 当前 OS */
    val os: OS by lazy {
        val name = System.getProperty("os.name").lowercase(Locale.getDefault())
        when {
            name.contains("win") -> OS.WINDOWS
            name.contains("mac") -> OS.MAC
            name.contains("nix") || name.contains("nux") -> OS.LINUX
            else -> OS.OTHER
        }
    }

    /** 是否 Windows */
    fun isWindows() = os == OS.WINDOWS

    /** 是否 Linux */
    fun isLinux() = os == OS.LINUX

    /** 是否 macOS */
    fun isMac() = os == OS.MAC

    /** 是否 Docker */
    fun isDocker(): Boolean {
        // 1️⃣ 强制标识（最可靠）
        if (System.getenv("RUN_ENV") == "docker") return true

        // 2️⃣ /.dockerenv
        if (File("/.dockerenv").exists()) return true

        // 3️⃣ cgroup
        val cgroup = File("/proc/1/cgroup")
        if (cgroup.exists()) {
            val text = cgroup.readText()
            if (text.contains("docker")
                || text.contains("kubepods")
                || text.contains("containerd")
            ) return true
        }
        return false
    }

    /** 是否 WSL（Windows Subsystem for Linux） */
    fun isWSL(): Boolean {
        if (!isLinux()) return false
        val version = File("/proc/version")
        return version.exists() &&
                version.readText().lowercase().contains("microsoft")
    }

    fun isDev(): Boolean {
        return System.getProperty("io.ktor.development")?.toBoolean() == true
    }
}

plugins {
    environment
}

art {
    java()
    kotlin()
    generator()
    tarantool {
        from("my url", "1.2.3")
        instance("storage-1") {
            """
               box.cfg{listen=3306}
            """.trimIndent()
        }
        instance("storage-2") {
            """
               box.cfg{listen=3305}
            """.trimIndent()
        }
    }
}

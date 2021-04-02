plugins {
    environment
}

art {
    java()
    kotlin()
    generator()
    tarantool {
        instance("storage-1") {
            """
                local a = "test"
            """.trimIndent()
        }
        instance("storage-2") {
            """
                local a = "test"
            """.trimIndent()
        }
    }
}

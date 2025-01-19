package com.f1db

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

@Testcontainers
class SqlDumpValidationTestUnique {

    companion object {
        @Container
        private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:13.3").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @Container
        private val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0.26").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @BeforeAll
        fun setUpContainers() {
            postgresContainer.start()
            mysqlContainer.start()
            // SQLite doesn't require a container; we'll use an in-memory database
        }
    }

    @Test
    fun `should validate PostgreSQL dump`() {
        val sqlDumpPath = Paths.get("build/data/sql/f1db-sql-postgresql.sql")
        if (!Files.exists(sqlDumpPath)) {
            fail<Unit>("PostgreSQL SQL dump not found at $sqlDumpPath")
        }
        val sqlDump = Files.readString(sqlDumpPath)

        val connection: Connection = DriverManager.getConnection(
            postgresContainer.jdbcUrl,
            postgresContainer.username,
            postgresContainer.password
        )

        connection.use { conn ->
            conn.autoCommit = false
            try {
                conn.createStatement().use { stmt ->
                    sqlDump.split(";").forEach { sql ->
                        val trimmedSql = sql.trim()
                        if (trimmedSql.isNotEmpty()) {
                            stmt.execute(trimmedSql)
                        }
                    }
                }
                conn.commit()
            } catch (ex: Exception) {
                conn.rollback()
                fail("PostgreSQL SQL dump execution failed: ${ex.message}")
            }

            // Validate tables
            val expectedTables = listOf("country", "driver")
            val actualTables = mutableListOf<String>()
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
                )
                while (rs.next()) {
                    actualTables.add(rs.getString("table_name"))
                }
            }

            expectedTables.forEach { table ->
                assertTrue(actualTables.contains(table), "Table '$table' should exist")
            }

            // Validate constraints
            val expectedConstraints = listOf(
                "cntn_name_uk",
                "cntr_pk",
                "cntr_alpha2_code_uk",
                "cntr_alpha3_code_uk",
                "cntr_name_uk",
                "cntr_continent_id_fk"
            )
            val actualConstraints = mutableListOf<String>()
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(
                    "SELECT constraint_name FROM information_schema.table_constraints WHERE table_schema='public'"
                )
                while (rs.next()) {
                    actualConstraints.add(rs.getString("constraint_name"))
                }
            }

            expectedConstraints.forEach { constraint ->
                assertTrue(actualConstraints.contains(constraint), "Constraint '$constraint' should exist")
            }

            // Validate indexes
            val expectedIndexes = listOf("cntr_continent_id_idx")
            val actualIndexes = mutableListOf<String>()
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(
                    "SELECT indexname FROM pg_indexes WHERE schemaname='public'"
                )
                while (rs.next()) {
                    actualIndexes.add(rs.getString("indexname"))
                }
            }

            expectedIndexes.forEach { index ->
                assertTrue(actualIndexes.contains(index), "Index '$index' should exist")
            }
        }
    }

    @Test
    fun `should validate MySQL dump`() {
        val sqlDumpPath = Paths.get("build/data/sql/f1db-sql-mysql.sql")
        if (!Files.exists(sqlDumpPath)) {
            fail<Unit>("MySQL SQL dump not found at $sqlDumpPath")
        }
        val sqlDump = Files.readString(sqlDumpPath)

        val connection: Connection = DriverManager.getConnection(
            mysqlContainer.jdbcUrl,
            mysqlContainer.username,
            mysqlContainer.password
        )

        connection.use { conn ->
            conn.autoCommit = false
            try {
                conn.createStatement().use { stmt ->
                    sqlDump.split(";").forEach { sql ->
                        val trimmedSql = sql.trim()
                        if (trimmedSql.isNotEmpty()) {
                            stmt.execute(trimmedSql)
                        }
                    }
                }
                conn.commit()
            } catch (ex: Exception) {
                conn.rollback()
                fail("MySQL SQL dump execution failed: ${ex.message}")
            }

            // Validate tables
            val expectedTables = listOf("country", "driver")
            val actualTables = mutableListOf<String>()
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(
                    "SHOW TABLES"
                )
                while (rs.next()) {
                    actualTables.add(rs.getString(1))
                }
            }

            expectedTables.forEach { table ->
                assertTrue(actualTables.contains(table), "Table '$table' should exist")
            }

            // Validate constraints (Note: MySQL handles constraints differently; some may be treated as indexes)
            val expectedConstraints = listOf(
                "cntn_name_uk",
                "cntr_pk",
                "cntr_alpha2_code_uk",
                "cntr_alpha3_code_uk",
                "cntr_name_uk",
                "cntr_continent_id_fk"
            )
            val actualConstraints = mutableListOf<String>()
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(
                    "SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = 'testdb'"
                )
                while (rs.next()) {
                    actualConstraints.add(rs.getString("CONSTRAINT_NAME"))
                }
            }

            expectedConstraints.forEach { constraint ->
                assertTrue(actualConstraints.contains(constraint), "Constraint '$constraint' should exist")
            }

            // Validate indexes
            val expectedIndexes = listOf("cntr_continent_id_idx")
            val actualIndexes = mutableListOf<String>()
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(
                    "SHOW INDEX FROM country"
                )
                while (rs.next()) {
                    actualIndexes.add(rs.getString("Key_name"))
                }
            }

            expectedIndexes.forEach { index ->
                assertTrue(actualIndexes.contains(index), "Index '$index' should exist")
            }
        }
    }

    @Test
    fun `should validate SQLite dump`() {
        val sqlDumpPath = Paths.get("build/data/sql/f1db-sqlite.sql")
        if (!Files.exists(sqlDumpPath)) {
            fail<Unit>("SQLite SQL dump not found at $sqlDumpPath")
        }
        val sqlDump = Files.readString(sqlDumpPath)

        // SQLite in-memory database
        val connection: Connection = DriverManager.getConnection("jdbc:sqlite::memory:")

        connection.use { conn ->
            conn.autoCommit = false
            try {
                conn.createStatement().use { stmt ->
                    sqlDump.split(";").forEach { sql ->
                        val trimmedSql = sql.trim()
                        if (trimmedSql.isNotEmpty()) {
                            stmt.execute(trimmedSql)
                        }
                    }
                }
                conn.commit()
            } catch (ex: Exception) {
                conn.rollback()
                fail("SQLite SQL dump execution failed: ${ex.message}")
            }

            // Validate tables
            val expectedTables = listOf("country", "driver")
            val actualTables = mutableListOf<String>()
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table'"
                )
                while (rs.next()) {
                    actualTables.add(rs.getString("name"))
                }
            }

            expectedTables.forEach { table ->
                assertTrue(actualTables.contains(table), "Table '$table' should exist")
            }

            // Validate constraints
            // val expectedConstraints = listOf(
            //     "cntn_name_uk",
            //     "cntr_pk",
            //     "cntr_alpha2_code_uk",
            //     "cntr_alpha3_code_uk",
            //     "cntr_name_uk",
            //     "cntr_continent_id_fk"
            // )
            val actualConstraints = mutableListOf<String>()
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(
                    "PRAGMA foreign_key_list(country)"
                )
                while (rs.next()) {
                    val constraintName = rs.getString("id") // SQLite doesn't support named constraints
                    actualConstraints.add(constraintName)
                }
            }

            // SQLite has limited support for named constraints, so some validations might differ
            // Here, we ensure the foreign key exists
            assertTrue(actualConstraints.isNotEmpty(), "Foreign key constraints should exist in SQLite")
        }
    }
}
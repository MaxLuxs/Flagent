package flagent.cache.impl

import flagent.config.AppConfig
import flagent.domain.entity.Flag
import flagent.domain.repository.IFlagRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue
import java.io.File
import java.nio.file.Files

class EvalCacheFetcherTest {
    @Test
    fun testDbFetcher_Fetch() = runBlocking {
        val flag = Flag(id = 1, key = "test_flag", description = "Test flag", enabled = true)
        val repository = mockk<IFlagRepository> {
            coEvery { findAll(preload = true) } returns listOf(flag)
        }
        
        val fetcher = DbFetcher(repository)
        val flags = fetcher.fetch()
        
        assertEquals(1, flags.size)
        assertEquals("test_flag", flags[0].key)
        coVerify { repository.findAll(preload = true) }
    }
    
    @Test
    fun testJsonFileFetcher_Fetch() = runBlocking {
        // Create temporary JSON file
        val tempFile = File.createTempFile("test_flags", ".json")
        tempFile.writeText("""
            {
                "flags": [
                    {
                        "id": 1,
                        "key": "test_flag",
                        "description": "Test flag",
                        "enabled": true
                    }
                ]
            }
        """.trimIndent())
        
        try {
            val fetcher = JsonFileFetcher(tempFile.absolutePath)
            val flags = fetcher.fetch()
            
            assertEquals(1, flags.size)
            assertEquals("test_flag", flags[0].key)
        } finally {
            tempFile.delete()
        }
    }
    
    @Test
    fun testJsonFileFetcher_ThrowsException_WhenFileNotFound() = runBlocking {
        val fetcher = JsonFileFetcher("/non/existent/file.json")
        
        assertFailsWith<IllegalArgumentException> {
            fetcher.fetch()
        }
    }
    
    @Test
    fun testCreateEvalCacheFetcher_ReturnsDbFetcher_WhenNotEvalOnlyMode() {
        val repository = mockk<IFlagRepository>()
        mockkObject(AppConfig)
        every { AppConfig.evalOnlyMode } returns false
        
        val fetcher = createEvalCacheFetcher(repository)
        
        assertTrue(fetcher is DbFetcher)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCreateEvalCacheFetcher_ReturnsJsonFileFetcher_WhenJsonFileDriver() {
        val repository = mockk<IFlagRepository>()
        mockkObject(AppConfig)
        every { AppConfig.evalOnlyMode } returns true
        every { AppConfig.dbDriver } returns "json_file"
        every { AppConfig.dbConnectionStr } returns "/tmp/test.json"
        
        val fetcher = createEvalCacheFetcher(repository)
        
        assertTrue(fetcher is JsonFileFetcher)
        
        unmockkObject(AppConfig)
    }
    
    @Test
    fun testCreateEvalCacheFetcher_ReturnsJsonHttpFetcher_WhenJsonHttpDriver() {
        val repository = mockk<IFlagRepository>()
        mockkObject(AppConfig)
        every { AppConfig.evalOnlyMode } returns true
        every { AppConfig.dbDriver } returns "json_http"
        every { AppConfig.dbConnectionStr } returns "http://example.com/flags.json"
        
        val fetcher = createEvalCacheFetcher(repository)
        
        assertTrue(fetcher is JsonHttpFetcher)
        
        unmockkObject(AppConfig)
    }

    @Test
    fun testCreateEvalCacheFetcher_Throws_WhenUnsupportedDriver() {
        val repository = mockk<IFlagRepository>()
        mockkObject(AppConfig)
        every { AppConfig.evalOnlyMode } returns true
        every { AppConfig.dbDriver } returns "sqlite3"
        every { AppConfig.dbConnectionStr } returns ":memory:"

        assertFailsWith<IllegalArgumentException> {
            createEvalCacheFetcher(repository)
        }

        unmockkObject(AppConfig)
    }
}

package flagent.repository.impl

import flagent.domain.entity.User
import flagent.repository.Database
import flagent.repository.tables.*
import flagent.test.PostgresTestcontainerExtension
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.*

@ExtendWith(PostgresTestcontainerExtension::class)
class UserRepositoryTest {

    private lateinit var repository: UserRepository

    @BeforeTest
    fun setup() {
        transaction(Database.getDatabase()) {
            SchemaUtils.createMissingTablesAndColumns(
                Flags, Segments, Variants, Constraints, Distributions,
                Tags, FlagsTags, FlagSnapshots, FlagEntityTypes, Webhooks,
                Users, EvaluationEvents, AnalyticsEvents, CrashReports
            )
        }
        repository = UserRepository()
    }

    @AfterTest
    fun cleanup() {
        try {
            transaction(Database.getDatabase()) { SchemaUtils.drop(Users) }
        } catch (_: Exception) { }
    }

    @Test
    fun create_returnsUserWithId() = runBlocking {
        val user = User(email = "a@b.com", passwordHash = "hash", name = "A")
        val created = repository.create(user)
        assertTrue(created.id > 0)
        assertEquals("a@b.com", created.email)
        assertEquals("hash", created.passwordHash)
    }

    @Test
    fun findById_returnsUser() = runBlocking {
        val created = repository.create(User(email = "x@y.com", passwordHash = "h"))
        val found = repository.findById(created.id)
        assertNotNull(found)
        assertEquals(created.id, found!!.id)
        assertEquals("x@y.com", found.email)
    }

    @Test
    fun findById_returnsNull_whenNotFound() = runBlocking {
        assertNull(repository.findById(99999))
    }

    @Test
    fun findByEmail_returnsUser() = runBlocking {
        repository.create(User(email = "unique@b.com", passwordHash = "h"))
        val found = repository.findByEmail("unique@b.com")
        assertNotNull(found)
        assertEquals("unique@b.com", found!!.email)
    }

    @Test
    fun findAll_returnsList() = runBlocking {
        repository.create(User(email = "u1@b.com", passwordHash = "h"))
        repository.create(User(email = "u2@b.com", passwordHash = "h"))
        val list = repository.findAll(10, 0)
        assertTrue(list.size >= 2)
    }

    @Test
    fun count_returnsCorrectCount() = runBlocking {
        repository.create(User(email = "c1@b.com", passwordHash = "h"))
        val n = repository.count()
        assertTrue(n >= 1)
    }

    @Test
    fun update_modifiesUser() = runBlocking {
        val created = repository.create(User(email = "up@b.com", passwordHash = "h", name = "Old"))
        val updated = repository.update(created.copy(name = "New"))
        assertEquals("New", updated.name)
    }

    @Test
    fun softDelete_hidesFromFindById() = runBlocking {
        val created = repository.create(User(email = "del@b.com", passwordHash = "h"))
        repository.softDelete(created.id)
        assertNull(repository.findById(created.id))
    }

    @Test
    fun setBlockedAt_setsBlockedAt() = runBlocking {
        val created = repository.create(User(email = "block@b.com", passwordHash = "h"))
        repository.setBlockedAt(created.id, java.time.LocalDateTime.now())
        val found = repository.findById(created.id)
        assertNotNull(found!!.blockedAt)
    }
}

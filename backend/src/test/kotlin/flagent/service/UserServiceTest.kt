package flagent.service

import flagent.domain.entity.User
import flagent.domain.repository.IUserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.mindrot.jbcrypt.BCrypt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UserServiceTest {

    @Test
    fun validatePassword_returnsUser_whenMatch() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        val hash = BCrypt.hashpw("pass123", BCrypt.gensalt())
        val user = User(id = 1, email = "a@b.com", passwordHash = hash)
        coEvery { repo.findByEmail("a@b.com") } returns user
        val service = UserService(repo)
        assertEquals(user, service.validatePassword("a@b.com", "pass123"))
    }

    @Test
    fun validatePassword_returnsNull_whenWrongPassword() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        val hash = BCrypt.hashpw("pass123", BCrypt.gensalt())
        val user = User(id = 1, email = "a@b.com", passwordHash = hash)
        coEvery { repo.findByEmail("a@b.com") } returns user
        val service = UserService(repo)
        assertNull(service.validatePassword("a@b.com", "wrong"))
    }

    @Test
    fun validatePassword_returnsNull_whenUserNotFound() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        coEvery { repo.findByEmail(any()) } returns null
        val service = UserService(repo)
        assertNull(service.validatePassword("nobody@x.com", "pass"))
    }

    @Test
    fun findAll_returnsList() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        val list = listOf(User(id = 1, email = "a@b.com"))
        coEvery { repo.findAll(50, 0) } returns list
        val service = UserService(repo)
        assertEquals(list, service.findAll(50, 0))
    }

    @Test
    fun count_returnsLong() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        coEvery { repo.count() } returns 42L
        val service = UserService(repo)
        assertEquals(42L, service.count())
    }

    @Test
    fun findById_returnsUser() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        val user = User(id = 1, email = "a@b.com")
        coEvery { repo.findById(1) } returns user
        val service = UserService(repo)
        assertEquals(user, service.findById(1))
    }

    @Test
    fun create_returnsUser() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        coEvery { repo.findByEmail("new@b.com") } returns null
        val created = User(id = 1, email = "new@b.com", passwordHash = "hash")
        coEvery { repo.create(any()) } returns created
        val service = UserService(repo)
        val result = service.create("new@b.com", "secret", "Name")
        assertEquals(1, result.id)
        assertEquals("new@b.com", result.email)
    }

    @Test
    fun create_throwsWhenEmailExists() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        coEvery { repo.findByEmail("existing@b.com") } returns User(id = 1, email = "existing@b.com")
        val service = UserService(repo)
        assertFailsWith<IllegalArgumentException> { service.create("existing@b.com", "pass", null) }
    }

    @Test
    fun update_returnsUser() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        val user = User(id = 1, email = "a@b.com", name = "Old")
        val updated = user.copy(name = "New")
        coEvery { repo.findById(1) } returns user
        coEvery { repo.update(any()) } returns updated
        val service = UserService(repo)
        assertEquals("New", service.update(1, "New", null).name)
    }

    @Test
    fun softDelete_callsRepo() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        coEvery { repo.findById(1) } returns User(id = 1, email = "a@b.com")
        coEvery { repo.softDelete(1) } returns Unit
        val service = UserService(repo)
        service.softDelete(1)
        coVerify(exactly = 1) { repo.softDelete(1) }
    }

    @Test
    fun block_callsSetBlockedAt() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        val user = User(id = 1, email = "a@b.com", blockedAt = null)
        coEvery { repo.findById(1) } returns user
        coEvery { repo.setBlockedAt(1, any()) } returns Unit
        val service = UserService(repo)
        service.block(1)
        coVerify(exactly = 1) { repo.setBlockedAt(1, any()) }
    }

    @Test
    fun unblock_callsSetBlockedAtNull() = runBlocking {
        val repo = mockk<IUserRepository>(relaxed = true)
        coEvery { repo.findById(1) } returns User(id = 1, email = "a@b.com")
        coEvery { repo.setBlockedAt(1, null) } returns Unit
        val service = UserService(repo)
        service.unblock(1)
        coVerify(exactly = 1) { repo.setBlockedAt(1, null) }
    }
}

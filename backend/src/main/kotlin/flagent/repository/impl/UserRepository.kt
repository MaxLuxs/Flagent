package flagent.repository.impl

import flagent.domain.entity.User
import flagent.domain.repository.IUserRepository
import flagent.repository.Database
import flagent.repository.tables.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.*
import java.time.LocalDateTime

/**
 * User repository implementation (admin users for UI login).
 */
class UserRepository : IUserRepository {

    override suspend fun findById(id: Int): User? = withContext(Dispatchers.IO) {
        Database.transaction {
            Users.selectAll()
                .where { (Users.id eq id) and (Users.deletedAt.isNull()) }
                .firstOrNull()
                ?.let { mapRow(it) }
        }
    }

    override suspend fun findByEmail(email: String): User? = withContext(Dispatchers.IO) {
        Database.transaction {
            Users.selectAll()
                .where { (Users.email eq email.trim()) and (Users.deletedAt.isNull()) }
                .firstOrNull()
                ?.let { mapRow(it) }
        }
    }

    override suspend fun findAll(limit: Int, offset: Int): List<User> = withContext(Dispatchers.IO) {
        Database.transaction {
            Users.selectAll()
                .where { Users.deletedAt.isNull() }
                .orderBy(Users.id, SortOrder.ASC)
                .limit(limit).offset(offset.toLong())
                .map { mapRow(it) }
        }
    }

    override suspend fun count(): Long = withContext(Dispatchers.IO) {
        Database.transaction {
            Users.selectAll().where { Users.deletedAt.isNull() }.count().toLong()
        }
    }

    override suspend fun create(user: User): User = withContext(Dispatchers.IO) {
        Database.transaction {
            val now = LocalDateTime.now()
            val id = Users.insert {
                it[Users.email] = user.email?.trim()?.takeIf { e -> e.isNotBlank() }
                it[Users.name] = user.name?.trim()?.takeIf { n -> n.isNotBlank() }
                it[Users.passwordHash] = user.passwordHash
                it[Users.createdAt] = now
            }[Users.id].value
            user.copy(id = id, createdAt = now)
        }
    }

    override suspend fun update(user: User): User = withContext(Dispatchers.IO) {
        Database.transaction {
            val now = LocalDateTime.now()
            Users.update({ Users.id eq user.id }) {
                it[Users.email] = user.email?.trim()?.takeIf { e -> e.isNotBlank() }
                it[Users.name] = user.name?.trim()?.takeIf { n -> n.isNotBlank() }
                if (user.passwordHash != null) it[Users.passwordHash] = user.passwordHash
                it[Users.updatedAt] = now
            }
            user.copy(updatedAt = now)
        }
    }

    override suspend fun softDelete(id: Int) = withContext(Dispatchers.IO) {
        Database.transaction {
            Users.update({ Users.id eq id }) {
                it[Users.deletedAt] = LocalDateTime.now()
            }
            Unit
        }
    }

    override suspend fun setBlockedAt(id: Int, blockedAt: LocalDateTime?): Unit = withContext(Dispatchers.IO) {
        Database.transaction {
            Users.update({ Users.id eq id }) {
                it[Users.blockedAt] = blockedAt
                it[Users.updatedAt] = LocalDateTime.now()
            }
            return@transaction Unit
        }
    }

    private fun mapRow(row: ResultRow): User = User(
        id = row[Users.id].value,
        email = row[Users.email],
        name = row[Users.name],
        passwordHash = row[Users.passwordHash],
        createdAt = row[Users.createdAt],
        updatedAt = row[Users.updatedAt],
        deletedAt = row[Users.deletedAt],
        blockedAt = row[Users.blockedAt]
    )
}

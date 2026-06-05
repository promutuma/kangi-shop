package ke.eelaminnovations.kangaishop.data.repository

import ke.eelaminnovations.kangaishop.data.local.dao.PersonDao
import ke.eelaminnovations.kangaishop.data.local.entity.toEntity
import ke.eelaminnovations.kangaishop.domain.model.Person
import ke.eelaminnovations.kangaishop.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val dao: PersonDao
) : PersonRepository {

    override fun getAllPeople(): Flow<List<Person>> =
        dao.getAllPeople().map { it.map { e -> e.toDomain() } }

    override fun searchPeople(query: String): Flow<List<Person>> =
        dao.searchPeople(query).map { it.map { e -> e.toDomain() } }

    override suspend fun getPersonById(id: String): Person? =
        dao.getPersonById(id)?.toDomain()

    override suspend fun getPersonByPhone(phone: String): Person? =
        dao.getPersonByPhone(phone)?.toDomain()

    override suspend fun insertPerson(person: Person) =
        dao.insertPerson(person.toEntity())

    override suspend fun updatePerson(person: Person) =
        dao.updatePerson(person.toEntity())

    override suspend fun deletePerson(id: String) =
        dao.softDeletePerson(id)

    override fun getPeopleWithDeliveries(): Flow<List<Person>> =
        dao.getPeopleWithDeliveries().map { it.map { e -> e.toDomain() } }

    override fun getPeopleWithCredit(): Flow<List<Person>> =
        dao.getPeopleWithCredit().map { it.map { e -> e.toDomain() } }
}

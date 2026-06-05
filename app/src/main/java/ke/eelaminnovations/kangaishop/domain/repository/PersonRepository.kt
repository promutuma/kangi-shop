package ke.eelaminnovations.kangaishop.domain.repository

import ke.eelaminnovations.kangaishop.domain.model.Person
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getAllPeople(): Flow<List<Person>>
    fun searchPeople(query: String): Flow<List<Person>>
    suspend fun getPersonById(id: String): Person?
    suspend fun getPersonByPhone(phone: String): Person?
    suspend fun insertPerson(person: Person)
    suspend fun updatePerson(person: Person)
    suspend fun deletePerson(id: String)
    fun getPeopleWithDeliveries(): Flow<List<Person>>
    fun getPeopleWithCredit(): Flow<List<Person>>
}

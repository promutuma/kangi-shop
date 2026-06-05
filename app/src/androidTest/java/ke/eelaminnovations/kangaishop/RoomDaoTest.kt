package ke.eelaminnovations.kangaishop

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ke.eelaminnovations.kangaishop.data.local.AppDatabase
import ke.eelaminnovations.kangaishop.data.local.dao.PersonDao
import ke.eelaminnovations.kangaishop.data.local.entity.PersonEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var personDao: PersonDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        personDao = db.personDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() = runBlocking {
        val person = PersonEntity(
            id = "test_1",
            name = "John Doe",
            phone = "0700112233",
            smsEnabled = true,
            notes = "Test supplier notes",
            createdAt = System.currentTimeMillis(),
            lastModifiedAt = System.currentTimeMillis(),
            isDeleted = false,
            syncStatus = "PENDING",
            deviceId = "device_test"
        )
        personDao.insertPerson(person)
        val retrieved = personDao.getPersonById("test_1")
        assertNotNull(retrieved)
        assertEquals("John Doe", retrieved?.name)
        assertEquals("0700112233", retrieved?.phone)
    }
}

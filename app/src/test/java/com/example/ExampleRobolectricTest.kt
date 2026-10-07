package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MakeItHappenDatabase
import com.example.data.model.PriorityLevel
import com.example.data.model.TaskEntity
import com.example.data.preferences.PreferencesManager
import com.example.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var db: MakeItHappenDatabase

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, MakeItHappenDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("MakeItHappen", appName)
  }

  @Test
  fun `test DateUtils today and overdue calculation`() {
    val today = DateUtils.getTodayStartOfDay()
    assertTrue(DateUtils.isDueToday(today))

    val yesterday = Calendar.getInstance().apply {
      timeInMillis = today
      add(Calendar.DAY_OF_YEAR, -1)
    }.timeInMillis

    assertTrue(DateUtils.isOverdue(yesterday, "12:00", isCompleted = false))
    assertFalse(DateUtils.isOverdue(yesterday, "12:00", isCompleted = true))

    val tomorrow = DateUtils.getTomorrowStartOfDay()
    assertTrue(DateUtils.isDueTomorrow(tomorrow))
    assertTrue(DateUtils.isUpcoming(tomorrow))
    assertFalse(DateUtils.isOverdue(tomorrow, "12:00", isCompleted = false))
  }

  @Test
  fun `test PriorityLevel parsing`() {
    assertEquals(PriorityLevel.HIGH, PriorityLevel.fromString("High"))
    assertEquals(PriorityLevel.MEDIUM, PriorityLevel.fromString("Medium"))
    assertEquals(PriorityLevel.LOW, PriorityLevel.fromString("Low"))
    assertEquals(PriorityLevel.MEDIUM, PriorityLevel.fromString("Unknown"))
  }

  @Test
  fun `test Room database CRUD and category reassignment`() = runBlocking {
    val taskDao = db.taskDao()

    val task1 = TaskEntity(
      title = "Buy Coffee",
      description = "Roasted beans",
      dueDate = DateUtils.getTodayStartOfDay(),
      priority = "High",
      category = "Shopping"
    )
    val id1 = taskDao.insertTask(task1)
    assertTrue(id1 > 0)

    val fetched = taskDao.getTaskById(id1)
    assertEquals("Buy Coffee", fetched?.title)
    assertEquals("Shopping", fetched?.category)
    assertFalse(fetched?.isCompleted ?: true)

    // Complete task
    val completed = fetched!!.copy(isCompleted = true, completedAt = System.currentTimeMillis())
    taskDao.updateTask(completed)
    val fetchedCompleted = taskDao.getTaskById(id1)
    assertTrue(fetchedCompleted?.isCompleted ?: false)

    // Reassign category
    taskDao.reassignCategory("Shopping", "Personal")
    val reassigned = taskDao.getTaskById(id1)
    assertEquals("Personal", reassigned?.category)

    // Delete completed tasks
    taskDao.deleteCompletedTasks()
    val all = taskDao.getAllTasks().first()
    assertTrue(all.isEmpty())
  }

  @Test
  fun `test PreferencesManager persistence`() {
    val prefs = PreferencesManager(context)
    prefs.setThemeMode("dark")
    assertEquals("dark", prefs.themeMode.value)

    prefs.setDefaultPriority("High")
    assertEquals("High", prefs.defaultPriority.value)

    prefs.setDefaultCategory("Work")
    assertEquals("Work", prefs.defaultCategory.value)

    prefs.setNotificationsEnabled(false)
    assertFalse(prefs.notificationsEnabled.value)
  }

  @Test
  fun `test Alphabetical and Due Date Sorting Logic`() {
    val tasks = listOf(
      TaskEntity(id = 1, title = "Zebra task", isCompleted = false),
      TaskEntity(id = 2, title = "Apple task", isCompleted = false),
      TaskEntity(id = 3, title = "Mango task", isCompleted = true)
    )

    // A to Z (active first)
    val sortedAsc = tasks.sortedWith(
      compareBy<TaskEntity> { it.isCompleted }.thenBy { it.title.lowercase() }
    )
    assertEquals("Apple task", sortedAsc[0].title)
    assertEquals("Zebra task", sortedAsc[1].title)
    assertEquals("Mango task", sortedAsc[2].title)

    // Z to A (active first)
    val sortedDesc = tasks.sortedWith(
      compareBy<TaskEntity> { it.isCompleted }.thenByDescending { it.title.lowercase() }
    )
    assertEquals("Zebra task", sortedDesc[0].title)
    assertEquals("Apple task", sortedDesc[1].title)
    assertEquals("Mango task", sortedDesc[2].title)
  }
}

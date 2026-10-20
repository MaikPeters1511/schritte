package de.schritte.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "day")
data class DayEntity(@PrimaryKey val date: String, val steps: Int, val lastRaw: Long)

@Dao
interface DayDao {
    @Upsert suspend fun upsert(day: DayEntity)

    @Query("SELECT * FROM day ORDER BY date DESC LIMIT 1")
    suspend fun newest(): DayEntity?

    @Query("SELECT * FROM day ORDER BY date DESC LIMIT 1")
    fun observeNewest(): Flow<DayEntity?>

    @Query("SELECT * FROM day WHERE date BETWEEN :from AND :to")
    fun observeRange(from: String, to: String): Flow<List<DayEntity>>
}

@Database(entities = [DayEntity::class], version = 1, exportSchema = false)
abstract class StepDatabase : RoomDatabase() {
    abstract fun dayDao(): DayDao

    companion object {
        fun create(context: Context): StepDatabase =
            Room.databaseBuilder(context, StepDatabase::class.java, "steps.db").build()
    }
}

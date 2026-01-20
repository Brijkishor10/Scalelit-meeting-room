package com.example.skalelit.data.dao

import androidx.room.*
import com.example.skalelit.data.entity.BookingEntity
import com.example.skalelit.data.entity.RoomEntity
import com.example.skalelit.data.entity.UserEntity

@Dao
interface AppDao {
    // --- USER ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registerUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email AND pass = :pass LIMIT 1")
    suspend fun login(email: String, pass: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'admin' LIMIT 1")
    suspend fun getAdmin(): UserEntity?

    // --- BOOKINGS ---
    @Insert
    suspend fun addBooking(booking: BookingEntity)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    // <--- NEW: CANCEL QUERY --->
    @Query("UPDATE bookings SET status = 'cancelled' WHERE id = :id")
    suspend fun cancelBooking(id: Int)

    @Query("SELECT * FROM bookings WHERE userEmail = :email ORDER BY startTime DESC")
    suspend fun getUserBookings(email: String): List<BookingEntity>

    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    suspend fun getAllBookings(): List<BookingEntity>

    // --- ROOMS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)

    @Query("SELECT * FROM rooms")
    suspend fun getAllRooms(): List<RoomEntity>

    @Query("UPDATE rooms SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Int, isFav: Boolean)

    // --- ANALYTICS ---
    @Query("SELECT COUNT(DISTINCT userEmail) FROM bookings")
    suspend fun getActiveUsersCount(): Int

    @Query("SELECT SUM(cost) FROM bookings WHERE status = 'confirmed'")
    suspend fun getMonthlyRevenue(): Double?

    data class WeekDayStats(val dayOfWeek: String, val count: Int)

    @Query("""
        SELECT strftime('%w', datetime(startTime/1000, 'unixepoch')) as dayOfWeek, COUNT(*) as count 
        FROM bookings 
        GROUP BY dayOfWeek
    """)
    suspend fun getWeeklyBookingStats(): List<WeekDayStats>
}
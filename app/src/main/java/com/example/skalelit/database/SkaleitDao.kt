package com.example.skalelit.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SkaleitDao {
    @Insert
    suspend fun registerUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email AND password = :pass LIMIT 1")
    suspend fun login(email: String, pass: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun checkEmail(email: String): UserEntity?

    @Insert
    suspend fun addBooking(booking: BookingEntity)

    @Query("SELECT * FROM bookings WHERE userEmail = :email")
    suspend fun getUserBookings(email: String): List<BookingEntity>

    @Delete
    suspend fun deleteBooking(booking: BookingEntity)


    @Insert
    suspend fun addFavorite(fav: FavoriteEntity)

    @Delete
    suspend fun removeFavorite(fav: FavoriteEntity)

    @Query("SELECT * FROM favorites WHERE userEmail = :email")
    suspend fun getUserFavorites(email: String): List<FavoriteEntity>

    @Query("DELETE FROM favorites WHERE userEmail = :email AND roomId = :roomId")
    suspend fun removeFavoriteById(email: String, roomId: String)
}
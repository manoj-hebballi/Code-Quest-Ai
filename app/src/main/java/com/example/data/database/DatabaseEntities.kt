package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "default_user",
    val name: String,
    val email: String,
    val overallStreak: Int = 0,
    val lastActiveDate: String = "", // yyyy-MM-dd
    val points: Int = 1250,           // Default starting points for high-tier profile representation
    val unlockedBadges: String = "first_solve" // Comma-separated list of unlocked badge IDs
)

@Entity(tableName = "problems")
data class ProblemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val difficulty: String, // Easy, Medium, Hard
    val description: String,
    val tags: String, // Comma separated, e.g. "Arrays, Hash Table"
    val templateKotlin: String,
    val templatePython: String,
    val templateJava: String,
    val testCasesJson: String // Serialized list of test cases, e.g. [{"input":"[2,7,11,15]\n9","expected":"[0,1]"}]
)

@Entity(tableName = "submissions")
data class SubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val problemId: String,
    val problemTitle: String,
    val problemDifficulty: String,
    val code: String,
    val language: String, // Kotlin, Python, Java
    val result: String, // Accepted, Wrong Answer, Compile Error, Runtime Error
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val timeTakenSeconds: Int = 0
)

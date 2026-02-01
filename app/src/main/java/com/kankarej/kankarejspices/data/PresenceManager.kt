package com.kankarej.kankarejspices.data

import android.os.Build
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PresenceManager {

    // CHANGE: Explicitly use your Asia-Southeast1 URL
    private val dbUrl = "https://kankarej-spices-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(dbUrl)
    
    private val rootRef = db.reference
    private val connectedRef = db.getReference(".info/connected")

    fun startTracking() {
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("PresenceManager", "Connected to Firebase! Writing session...")

                    // 1. Live Session (Auto-delete on disconnect)
                    val sessionRef = rootRef.child("active_sessions").push()
                    sessionRef.onDisconnect().removeValue()

                    val sessionData = mapOf(
                        "device" to deviceName,
                        "login_time" to ServerValue.TIMESTAMP,
                        "platform" to "Android"
                    )
                    
                    // Add a completion listener to check for Permission Errors
                    sessionRef.setValue(sessionData)
                        .addOnSuccessListener { 
                            Log.d("PresenceManager", "Session written successfully.") 
                        }
                        .addOnFailureListener { e -> 
                            Log.e("PresenceManager", "Failed to write session. Check Rules!", e) 
                        }

                    // 2. Permanent History
                    val historyRef = rootRef.child("session_history").push()
                    val historyData = mapOf(
                        "device" to deviceName,
                        "timestamp" to ServerValue.TIMESTAMP,
                        "readable_time" to getCurrentDateTime(),
                        "action" to "SESSION_START"
                    )
                    historyRef.setValue(historyData)
                } else {
                    Log.d("PresenceManager", "Not connected to Firebase yet...")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PresenceManager", "Listener cancelled", error.toException())
            }
        })
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
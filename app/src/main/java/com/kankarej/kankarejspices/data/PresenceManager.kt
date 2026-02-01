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

    // Ensure this matches your specific database URL
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
                    Log.d("PresenceManager", "Connected to Firebase! Setting up session...")

                    // ---------------------------------------------------------
                    // 1. LIVE COUNT (active_sessions)
                    // ---------------------------------------------------------
                    val sessionRef = rootRef.child("active_sessions").push()
                    
                    // Auto-delete this node when user disconnects (Live count goes down)
                    sessionRef.onDisconnect().removeValue()

                    val sessionData = mapOf(
                        "device" to deviceName,
                        "login_time" to ServerValue.TIMESTAMP,
                        "platform" to "Android"
                    )
                    sessionRef.setValue(sessionData)

                    // ---------------------------------------------------------
                    // 2. PERMANENT HISTORY (session_history)
                    // ---------------------------------------------------------
                    val historyRef = rootRef.child("session_history").push()
                    
                    val historyData = mapOf(
                        "device" to deviceName,
                        "start_time" to ServerValue.TIMESTAMP,
                        "start_time_readable" to getCurrentDateTime(),
                        "end_time" to "Active..." // Placeholder until they disconnect
                    )
                    historyRef.setValue(historyData)

                    // MAGIC STEP: Tell server to write the 'end_time' AUTOMATICALLY when we disconnect
                    val updates = mapOf(
                        "end_time" to ServerValue.TIMESTAMP,
                        "status" to "Disconnected"
                    )
                    historyRef.onDisconnect().updateChildren(updates)

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
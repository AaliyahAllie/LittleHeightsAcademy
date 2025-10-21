package com.example.littleheightsacademy

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ChatActivity : AppCompatActivity() {

    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var recyclerMessages: RecyclerView
    private lateinit var adapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    private val db = FirebaseFirestore.getInstance()
    private val senderId = FirebaseAuth.getInstance().uid
    private var receiverId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_layout)

        receiverId = intent.getStringExtra("receiverId")

        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        recyclerMessages = findViewById(R.id.recyclerMessages)

        adapter = MessageAdapter(messageList, senderId ?: "")
        recyclerMessages.layoutManager = LinearLayoutManager(this)
        recyclerMessages.adapter = adapter

        btnSend.setOnClickListener { sendMessage() }

        listenForMessages()
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty() || senderId == null || receiverId == null) return

        val message = Message(senderId, receiverId!!, text, System.currentTimeMillis())
        db.collection("messages").add(message)
        etMessage.text.clear()
    }

    private fun listenForMessages() {
        db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                messageList.clear()
                for (doc in value!!) {
                    val msg = doc.toObject(Message::class.java)
                    if ((msg.senderId == senderId && msg.receiverId == receiverId) ||
                        (msg.senderId == receiverId && msg.receiverId == senderId)) {
                        messageList.add(msg)
                    }
                }

                adapter.notifyDataSetChanged()
                recyclerMessages.scrollToPosition(messageList.size - 1)
            }
    }
}

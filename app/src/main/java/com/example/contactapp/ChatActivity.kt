package com.example.contactapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var messageEditText: EditText
    private lateinit var chatTitle: TextView
    private lateinit var chatAdapter: ChatAdapter
    private val messagesList = ArrayList<Message>()
    private val apiUrl = "https://nodejs-rest-api-75r4.onrender.com/messages"
    private var userId: Int = 0
    private var contactId: Int = 0
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Inicializa las vistas
        chatTitle = findViewById(R.id.chatTitle)
        sendButton = findViewById(R.id.sendButton)
        backButton = findViewById(R.id.backButton)
        messageEditText = findViewById(R.id.messageEditText)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)


        // Recupera datos del Intent
        val contactName = intent.getStringExtra("contact_name") ?: "Contacto"
        chatTitle.text = contactName // Actualiza el título del chat

        userId = getSharedPreferences("ContactAppPrefs", MODE_PRIVATE).getInt("userId", 0)
        contactId = intent.getIntExtra("contact_id", 0)

        // Configura el RecyclerView
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Asegura que la lista comience desde el final
        }
        chatAdapter = ChatAdapter(messagesList, userId)
        chatRecyclerView.adapter = chatAdapter

        backButton.setOnClickListener{
            onBackPressed()
        }

        fetchMessages()  // Llama al método inicialmente para cargar los mensajes
        startMessageRefresh() // Inicia la actualización periódica

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            if (messageText.isNotBlank()) {
                sendMessage(messageText)
                messageEditText.text.clear()
                startMessageRefresh()
            }
        }

    }

    private fun fetchMessages() {
        val url = "$apiUrl/$userId/$contactId"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                messagesList.clear()
                for (i in 0 until response.length()) {
                    val messageJson = response.getJSONObject(i)

                    // Agrega estos logs para depuración
                    Log.d("ChatActivity", "Message JSON: $messageJson")
                    Log.d("ChatActivity", "Sender ID: ${messageJson.getInt("sender_id")}")
                    Log.d("ChatActivity", "Recipient ID: ${messageJson.getInt("recipient_id")}")
                    Log.d("ChatActivity", "Current User ID: $userId")
                    Log.d("ChatActivity", "Contact ID: $contactId")

                    val message = Message(
                        id = messageJson.getInt("id"),
                        senderId = messageJson.getInt("sender_id"),
                        recipientId = messageJson.getInt("recipient_id"),
                        messageText = messageJson.getString("message_text")
                    )
                    messagesList.add(message)
                }
                chatAdapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messagesList.size - 1)
            },
            { error ->
                Toast.makeText(this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show()
                Log.e("ChatActivity", "Error fetching messages: ${error.message}")
            }
        )

        Volley.newRequestQueue(this).add(jsonArrayRequest)
    }

    private fun sendMessage(message: String) {
        val jsonObject = JSONObject().apply {
            put("sender_id", userId)
            put("recipient_id", contactId)
            put("message_text", message)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            apiUrl,
            jsonObject,
            { response ->
                Log.d("ChatActivity", "Mensaje enviado con éxito: $response")
                messageEditText.text.clear()

                // Añadir el mensaje enviado inmediatamente a la lista
                val sentMessage = Message(
                    id = response.optInt("id", 0),
                    senderId = userId,
                    recipientId = contactId,
                    messageText = message
                )
                messagesList.add(sentMessage)

                // Notificar al adaptador y desplazar
                chatAdapter.notifyItemInserted(messagesList.size - 1)
                chatRecyclerView.scrollToPosition(messagesList.size - 1)
            },
            { error ->
                Log.e("ChatActivity", "Error al enviar mensaje: ${error.message}")
            }
        )

        Volley.newRequestQueue(this).add(request)
    }



    // Inicia el refresco periódico de los mensajes
    private fun startMessageRefresh() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                fetchMessages()  // Vuelve a cargar los mensajes
                handler.postDelayed(this, 10000) // Cada 10 segundos
            }
        }

        handler.post(runnable)  // Ejecuta el runnable por primera vez
    }

    // Detener el refresco cuando la actividad se destruye
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)  // Elimina la tarea programada
    }
}

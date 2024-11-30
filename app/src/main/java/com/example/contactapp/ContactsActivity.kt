package com.example.contactapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

class ContactsActivity : AppCompatActivity() {

    private val apiUrl = "https://nodejs-rest-api-75r4.onrender.com/users/"
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private val contactsList = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        // Configura el RecyclerView
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView)
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        contactsAdapter = ContactsAdapter(contactsList) { contact ->
            openChat(contact) // Abre el chat al hacer clic en un contacto
        }
        contactsRecyclerView.adapter = contactsAdapter

        // Carga los contactos desde la API
        fetchContactsFromApi()
    }

    private fun fetchContactsFromApi() {
        val sharedPreferences = getSharedPreferences("ContactAppPrefs", MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("userId", -1)

        if (currentUserId == -1) {
            Toast.makeText(this, "No se pudo cargar el usuario actual.", Toast.LENGTH_SHORT).show()
            return
        }

        val apiUrlWithUserId = "$apiUrl$currentUserId"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            apiUrlWithUserId,
            null,
            { response: JSONArray ->
                try {
                    contactsList.clear()
                    for (i in 0 until response.length()) {
                        val user = response.getJSONObject(i)
                        val id = user.getInt("id")
                        val name = user.getString("full_name")
                        val phone = user.getString("phone")
                        contactsList.add(Contact(id, name, phone)) // Agrega el contacto
                    }
                    contactsAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    Log.e("ContactsActivity", "JSON Parsing Error: ${e.message}")
                    Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("ContactsActivity", "API Error: ${error.message}")
                Toast.makeText(this, "Error al cargar contactos", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(jsonArrayRequest)
    }

    private fun openChat(contact: Contact) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("contact_id", contact.id)
        intent.putExtra("contact_name", contact.name)
        startActivity(intent)

        /* val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("contact_id", contactId)
            intent.putExtra("contact_name", contactName)
            context.startActivity(intent)*/
    }
}

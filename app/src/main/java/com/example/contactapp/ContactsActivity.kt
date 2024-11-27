package com.example.contactapp

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

class ContactsActivity : AppCompatActivity() {

    private val apiUrl = "https://nodejs-rest-api-75r4.onrender.com/users"
    private lateinit var contactsListView: ListView
    private lateinit var requestQueue: RequestQueue
    private lateinit var adapter: ArrayAdapter<String>
    private val contactsList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        contactsListView = findViewById(R.id.contactsListView)

        // Configura el adaptador para el ListView
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contactsList)
        contactsListView.adapter = adapter

        // Inicializa la cola de solicitudes de Volley
        requestQueue = Volley.newRequestQueue(this)

        // Carga los contactos desde la API
        fetchContactsFromApi()
    }

    private fun fetchContactsFromApi() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            apiUrl,
            null,
            { response: JSONArray ->
                try {
                    // Limpia la lista actual
                    contactsList.clear()

                    // Itera sobre el array JSON y extrae los datos necesarios
                    for (i in 0 until response.length()) {
                        val user = response.getJSONObject(i)
                        val name = user.getString("full_name")
                        val phone = user.getString("phone")

                        // Agrega los datos al ArrayList
                        contactsList.add("$name - $phone")
                    }

                    // Notifica al adaptador para actualizar la vista
                    adapter.notifyDataSetChanged()
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

        // Agrega la solicitud a la cola
        requestQueue.add(jsonArrayRequest)
    }
}
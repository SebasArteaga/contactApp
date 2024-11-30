package com.example.contactapp

// Importaciones necesarias
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Desactivar modo oscuro (opcional)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a los elementos de la interfaz
        val phoneEditText = findViewById<EditText>(R.id.loginPhoneEditText)
        val passwordEditText = findViewById<EditText>(R.id.loginPasswordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)

        // Visibilidad de contraseña
        val passwordToggle = findViewById<ImageButton>(R.id.passwordVisibilityToggle)

        var isPasswordVisible = false
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_visibility_on)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordEditText.setSelection(passwordEditText.text.length) // Mantiene el cursor al final
        }

        // Actividad para regsitrarse
        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Acción del botón de inicio de sesión
        loginButton.setOnClickListener {
            // Obtener datos ingresados
            val phone = phoneEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validación básica
            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                login(phone, password)
            }
        }
    }

    // Validar datos ingresados
    private fun validateInput(phone: String, password: String): Boolean {
        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return false
        }

        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            Toast.makeText(this, "El número de teléfono debe tener exactamente 10 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // Función para manejar el inicio de sesión
    private fun login(phone: String, password: String) {
        val url = "https://nodejs-rest-api-75r4.onrender.com/login"

        // Crear JSON con los datos de inicio de sesión
        val jsonBody = JSONObject()
        jsonBody.put("phone", phone)
        jsonBody.put("password", password)

        // Crear solicitud HTTP POST
        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                // Procesar respuesta exitosa
                val responseJson = JSONObject(response)
                val userId = responseJson.getInt("user_id") // Obtener el ID del usuario

                // Guardar el ID del usuario en SharedPreferences
                val sharedPreferences = getSharedPreferences("ContactAppPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putInt("userId", userId).apply()

                // Notificar éxito y redirigir a la actividad principal
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ContactsActivity::class.java)
                startActivity(intent)
                finish()
            },
            { error ->
                // Manejar errores
                val errorMessage = error.networkResponse?.let {
                    val errorData = String(it.data, Charsets.UTF_8)
                    JSONObject(errorData).getString("error")
                } ?: error.message ?: "Error del servidor"
                Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }) {
            override fun getBody(): ByteArray {
                return jsonBody.toString().toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }

        // Agregar la solicitud a la cola de Volley
        Volley.newRequestQueue(this).add(request)
    }
}

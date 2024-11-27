package com.example.contactapp

// Importación de clases necesarias
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

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Deshabilitar el modo oscuro (opcional)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Referencias a los elementos de la interfaz
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.registerEmailEditText)
        val phoneEditText = findViewById<EditText>(R.id.registerPhoneEditText)
        val passwordEditText = findViewById<EditText>(R.id.registerPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        // Acción del botón de registro
        registerButton.setOnClickListener {
            // Obtener los valores ingresados por el usuario
            val fullName = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validar los campos antes de enviar la solicitud
            if (validateInput(fullName, email, phone, password)) {
                // Llamar a la función de registro si los datos son válidos
                register(fullName, email, phone, password)
            }
        }

        // Visibilidad de la contraseña (mostrar/ocultar)
        val passwordToggle = findViewById<ImageButton>(R.id.passwordVisibilityToggle)

        var isPasswordVisible = false // Controla el estado de visibilidad
        passwordToggle.setOnClickListener {
            // Alternar la visibilidad de la contraseña
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_visibility_on) // Cambiar el icono
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_visibility_off) // Cambiar el icono
            }
            // Mantener el cursor al final del texto
            passwordEditText.setSelection(passwordEditText.text.length)
        }
    }

    // Función para validar los datos ingresados
    private fun validateInput(fullName: String, email: String, phone: String, password: String): Boolean {
        // Verificar que ningún campo esté vacío
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar formato del correo electrónico usando una expresión regular
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        if (!email.matches(emailRegex)) {
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verificar que el número de teléfono tenga 10 dígitos y solo contenga números
        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            Toast.makeText(this, "El número de teléfono debe tener exactamente 10 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verificar que la contraseña tenga al menos 6 caracteres
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        // Si todas las validaciones pasan, retorna true
        return true
    }

    // Función para registrar un usuario en el servidor
    private fun register(fullName: String, email: String, phone: String, password: String) {
        // URL del endpoint de registro
        val url = "https://nodejs-rest-api-75r4.onrender.com/users"

        // Crear un objeto JSON con los datos del usuario
        val jsonBody = JSONObject()
        jsonBody.put("full_name", fullName)
        jsonBody.put("phone", phone)
        jsonBody.put("email", email)
        jsonBody.put("password", password)

        // Crear la solicitud HTTP POST
        val request = object : StringRequest(Request.Method.POST, url,
            { response ->

                // Manejar respuesta exitosa
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                // Redirigir a la actividad de inicio de sesión
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Finalizar esta actividad
            },
            { error ->
                // Manejar error en la solicitud
                if (error.networkResponse?.statusCode == 409) {
                    // Obtener datos del error desde la respuesta del servidor
                    val errorData = String(error.networkResponse.data, Charsets.UTF_8)
                    val errorMessage = JSONObject(errorData).getString("error")

                    // Mostrar mensajes específicos según el error
                    when (errorMessage) {
                        "El correo ya está registrado." -> {
                            Toast.makeText(this, "El correo ya está registrado.", Toast.LENGTH_LONG).show()
                        }
                        "El número de teléfono ya está registrado." -> {
                            Toast.makeText(this, "El número de teléfono ya está registrado.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(this, "Error desconocido: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    // Otros errores no específicos
                    val errorMessage = error.message ?: "Error desconocido"
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }) {
            // Configurar el cuerpo de la solicitud (JSON)
            override fun getBody(): ByteArray {
                return jsonBody.toString().toByteArray()
            }

            // Configurar el tipo de contenido de la solicitud
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }

        // Agregar la solicitud a la cola de solicitudes de Volley
        Volley.newRequestQueue(this).add(request)
    }
}

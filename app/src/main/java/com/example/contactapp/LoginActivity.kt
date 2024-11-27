package com.example.contactapp

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validación básica
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                login(email, password)
            }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

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
    }


    private fun login(email: String, password: String) {
        val url = "https://nodejs-rest-api-75r4.onrender.com/login"
        val queue = Volley.newRequestQueue(this)

        val params = JSONObject()
        params.put("email", email)
        params.put("password", password)

        val request = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                try {
                    // Verificar si la respuesta es exitosa
                    val message = response.getString("message")
                    if (message == "Inicio de sesión exitoso") {
                        // Navegar a la actividad de contactos
                        val intent = Intent(this, ContactsActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error al iniciar sesión: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}

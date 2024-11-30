package com.example.contactapp

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
        // Deshabilitar el modo oscuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Referencias a los elementos de la interfaz
        val countryCodeSpinner = findViewById<Spinner>(R.id.countryCodeSpinner)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.registerEmailEditText)
        val phoneEditText = findViewById<EditText>(R.id.registerPhoneEditText)
        val passwordEditText = findViewById<EditText>(R.id.registerPasswordEditText)
        val otpEditText = findViewById<EditText>(R.id.verificationCodeEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val sendOtpButton = findViewById<Button>(R.id.sendOtpButton)

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

        // Configuración del spinner para los códigos de país
        val countryCodes = arrayOf("+52", "+44", "+34", "+91", "+1") // Lista de códigos
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countryCodes)
        countryCodeSpinner.adapter = spinnerAdapter

        // Acción para enviar OTP
        sendOtpButton.setOnClickListener {
            val phone = phoneEditText.text.toString()
            if (phone.isEmpty() || phone.length != 10 || !phone.all { it.isDigit() }) {
                Toast.makeText(this, "Ingrese un número de teléfono válido", Toast.LENGTH_SHORT).show()
            } else {
                val selectedCode = countryCodeSpinner.selectedItem.toString()
                val fullPhoneNumber = selectedCode + phone
                sendOtp(fullPhoneNumber) { success ->
                    if (success) {
                        Toast.makeText(this, "Código enviado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al enviar código", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Acción del botón de registro
        registerButton.setOnClickListener {
            val fullName = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val phone = phoneEditText.text.toString() // Sin código de país
            val otpCode = otpEditText.text.toString()

            if (validateInput(fullName, email, phone, password)) {
                val selectedCode = countryCodeSpinner.selectedItem.toString()
                val fullPhoneNumber = selectedCode + phone

                verifyOtp(fullPhoneNumber, otpCode) { otpVerified ->
                    if (otpVerified) {
                        register(fullName, email, phone, password) // Registrar solo el número base
                    } else {
                        Toast.makeText(this, "Error verificando código", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Función para validar los datos ingresados
    private fun validateInput(fullName: String, email: String, phone: String, password: String): Boolean {
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return false
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        if (!email.matches(emailRegex)) {
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show()
            return false
        }

        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            Toast.makeText(this, "El número de teléfono debe tener 10 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // Función para enviar OTP
    private fun sendOtp(phone: String, callback: (Boolean) -> Unit) {
        val url = "https://httpsms-api-o963.onrender.com/send-otp"
        val jsonBody = JSONObject().apply {
            put("phone", phone)
        }

        val request = object : StringRequest(Request.Method.POST, url,
            { callback(true) },
            { callback(false) }) {
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }

        Volley.newRequestQueue(this).add(request)
    }

    // Función para verificar OTP
    private fun verifyOtp(phone: String, otpCode: String, callback: (Boolean) -> Unit) {
        val url = "https://httpsms-api-o963.onrender.com/verify-otp"
        val jsonBody = JSONObject().apply {
            put("phone", phone)
            put("otpCode", otpCode)
        }

        val request = object : StringRequest(Request.Method.POST, url,
            { callback(true) },
            { callback(false) }) {
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }

        Volley.newRequestQueue(this).add(request)
    }

    // Función para registrar al usuario
    private fun register(fullName: String, email: String, phone: String, password: String) {
        val url = "https://nodejs-rest-api-75r4.onrender.com/users"
        val jsonBody = JSONObject().apply {
            put("full_name", fullName)
            put("phone", phone) // Guardar solo el número base
            put("email", email)
            put("password", password)
        }

        val request = object : StringRequest(Request.Method.POST, url,
            {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            },
            { error ->
                Toast.makeText(this, "Error al registrar: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }

        Volley.newRequestQueue(this).add(request)
    }
}

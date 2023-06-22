package com.example.bebidasapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
        @SuppressLint("MissingInflatedId")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            mAuth = FirebaseAuth.getInstance()

            val buttonLogin = findViewById<Button>(R.id.buttonLogin)
            val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
            val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
            val buttonRegistrarse = findViewById<Button>(R.id.btnRegistrarse)

            buttonLogin.setOnClickListener {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Por favor ingrese su correo y contraseña", Toast.LENGTH_SHORT).show()
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Inicio de sesión exitoso
                                val user = mAuth.currentUser
                                val intent = Intent(this, inicio::class.java)
                                startActivity(intent)
                            } else {
                                // Error al iniciar sesión
                                Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                }

            buttonRegistrarse.setOnClickListener {
                val intent = Intent(this, Registro::class.java)
                startActivity(intent)
            }
            }
    }

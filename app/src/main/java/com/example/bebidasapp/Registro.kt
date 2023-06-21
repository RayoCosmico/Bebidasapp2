package com.example.bebidasapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Registro : AppCompatActivity(){
    companion object {
        private const val DATABASE_USERS = "usuarios"
        private const val DATABASE_NAME = "nombre"
        private const val DATABASE_EMAIL = "email"
        private const val ERROR_CREATING_ACCOUNT = "Error al crear la cuenta."
        private const val ACCOUNT_CREATED_SUCCESSFULLY = "Cuenta creada con éxito."
        private const val ERROR_INVALID_EMAIL = "Correo electrónico inválido."
        private const val ERROR_EMPTY_FIELDS = "Por favor complete todos los campos."
    }

    private lateinit var nombreEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        nombreEditText = findViewById(R.id.etNombre)
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        registerButton = findViewById(R.id.btnRegistro)

        auth = FirebaseAuth.getInstance()

        registerButton.setOnClickListener {
            val nombre = nombreEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val uid = user?.uid

                                database = FirebaseDatabase.getInstance().reference.child(DATABASE_USERS).child(uid!!)
                                database.child(DATABASE_NAME).setValue(nombre)
                                database.child(DATABASE_EMAIL).setValue(email)

                                Toast.makeText(this, ACCOUNT_CREATED_SUCCESSFULLY, Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, inicio::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, ERROR_CREATING_ACCOUNT, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    emailEditText.error = ERROR_INVALID_EMAIL
                }
            } else {
                Toast.makeText(this, ERROR_EMPTY_FIELDS, Toast.LENGTH_SHORT).show()
            }
        }
    }

}
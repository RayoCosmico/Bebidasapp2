package com.example.bebidasapp

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class inicio : AppCompatActivity(), View.OnClickListener {
    private lateinit var imgCocaCola: ImageView
    private lateinit var imgPepsi: ImageView
    private lateinit var imgSevenUp: ImageView
    private lateinit var database: FirebaseDatabase
    private lateinit var referenciaEnProgreso: DatabaseReference
    private lateinit var referenciaContadorGeneral: DatabaseReference
    private lateinit var referenciaContadoresBebida: DatabaseReference
    private lateinit var referenciaBebidaActual: DatabaseReference
    private var contadorGeneral: Int = 0
    private var bebidaEnProgreso: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        // Inicializar Firebase
        Firebase.database.setPersistenceEnabled(true)
        database = Firebase.database

        // Obtener referencias a las imágenes
        imgCocaCola = findViewById(R.id.imgCocaCola)
        imgPepsi = findViewById(R.id.imgPepsi)
        imgSevenUp = findViewById(R.id.imgSevenUp)

        // Establecer el Listener de clic para cada imagen
        imgCocaCola.setOnClickListener(this)
        imgPepsi.setOnClickListener(this)
        imgSevenUp.setOnClickListener(this)

        // Obtener referencia a la colección "enprogreso"
        referenciaEnProgreso = database.reference.child("enprogreso")

        // Obtener referencia al contador general
        referenciaContadorGeneral = referenciaEnProgreso.child("contador_general")

        // Obtener referencia a la colección "contadores_bebida"
        referenciaContadoresBebida = database.reference.child("contadores_bebida")

        // Obtener referencia a la bebida actual
        referenciaBebidaActual = referenciaEnProgreso.child("bebida_actual")

        // Obtener el valor actual del contador general
        referenciaContadorGeneral.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contadorGeneral = snapshot.getValue(Int::class.java) ?: 0
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error, si es necesario
                Toast.makeText(this@inicio, "Error en Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Verificar si la colección "enprogreso" existe
        referenciaEnProgreso.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bebidaEnProgreso = snapshot.child("bebida_actual").exists()
                if (!bebidaEnProgreso) {
                    // La colección "enprogreso" no existe, mostrar mensaje
                    Toast.makeText(this@inicio, "¡No hay una bebida en progreso!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error, si es necesario
                Toast.makeText(this@inicio, "Error en Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun incrementarContadorBebida(nombreBebida: String) {
        val referenciaContadorBebidaActual = database.reference.child("contadores_bebida").child(nombreBebida)

        referenciaContadorBebidaActual.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contadorBebida = snapshot.getValue(Int::class.java) ?: 0

                // Incrementar el contador de la bebida
                val nuevoContadorBebida = contadorBebida + 1
                referenciaContadorBebidaActual.setValue(nuevoContadorBebida)
                    .addOnSuccessListener {
                        // El contador de la bebida se actualizó correctamente

                        // Obtener la fecha y hora actual
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                        // Crear la bebida actual con el nombre, número, fecha, hora y contador
                        val bebidaActual = hashMapOf(
                            "nombre" to nombreBebida,
                            "numero" to obtenerNumeroBebida(nombreBebida),
                            "fecha" to currentDate,
                            "hora" to currentTime,
                            "contador" to nuevoContadorBebida
                        )

                        // Actualizar el estado de la bebida en progreso
                        bebidaEnProgreso = true

                        referenciaBebidaActual.setValue(bebidaActual).addOnSuccessListener {
                            // La bebida actual se creó correctamente

                            // Mostrar mensaje Toast
                            Toast.makeText(this@inicio, "¡Bebida $nombreBebida creada!", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { error ->
                            // Ocurrió un error al crear la bebida actual
                            Toast.makeText(this@inicio, "Error al crear la bebida actual: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { error ->
                        // Ocurrió un error al actualizar el contador de la bebida
                        Toast.makeText(this@inicio, "Error al actualizar el contador de la bebida: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error, si es necesario
                Toast.makeText(this@inicio, "Error en Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onClick(view: View) {
        if (bebidaEnProgreso) {
            // Ya hay una bebida en progreso, mostrar mensaje
            Toast.makeText(this, "¡Ya hay una bebida en progreso!", Toast.LENGTH_SHORT).show()
            return
        }

        // Incrementar el contador general
        contadorGeneral++

        // Actualizar el contador general en Firebase
        referenciaContadorGeneral.setValue(contadorGeneral).addOnSuccessListener {
            // El contador general se actualizó correctamente

            // Obtener el nombre de la bebida
            val nombreBebida = obtenerNombreBebida(view.id)

            // Incrementar el contador de la bebida particular
            incrementarContadorBebida(nombreBebida)

        }.addOnFailureListener { error ->
            // Ocurrió un error al actualizar el contador general
            Toast.makeText(this@inicio, "Error al actualizar el contador general: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerNombreBebida(idImagen: Int): String {
        return when (idImagen) {
            R.id.imgCocaCola -> "Coca Cola"
            R.id.imgPepsi -> "Pepsi"
            R.id.imgSevenUp -> "Seven Up"
            else -> "Bebida Desconocida"
        }
    }

    private fun obtenerNumeroBebida(nombreBebida: String): Int {
        return when (nombreBebida) {
            "Coca Cola" -> 1
            "Pepsi" -> 2
            "Seven Up" -> 3
            else -> 0
        }
    }
    }

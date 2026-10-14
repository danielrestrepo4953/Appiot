package com.example.elitecybersecurityhub

import android.hardware.camera2.CameraManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

private lateinit var linterna: ImageView
private lateinit var Linternatxt: TextView
lateinit var fecha: TextView
lateinit var temp: TextView
lateinit var hum: android.widget.TextView
lateinit var imagenTemp: ImageView

lateinit var datos: RequestQueue

private lateinit var bombilla: ImageView

private lateinit var bombillatxt: TextView

private lateinit var cameraManager: CameraManager

private lateinit var cameraId: String

private var flashEncendido = false

val mHandler = Handler(Looper.getMainLooper())

class Sensores : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sensores)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        linterna = findViewById(R.id.btnlinterna)
        Linternatxt = findViewById(R.id.textoLinterna)
        fecha = findViewById(R.id.fechaActual)
        temp = findViewById(R.id.temptexto)
        hum = findViewById(R.id.humtexto)
        imagenTemp = findViewById(R.id.tempimagen)
        
        bombilla = findViewById(R.id.bombillaoffimg)
        bombillatxt = findViewById(R.id.textobombilla)

        datos = Volley.newRequestQueue(this)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]


        //fecha.text = fechahora()
        //obtenerDatos()

        mHandler.post(refrescar)


        var imagenOriginal = true
        var bombillaOriginal = true



        linterna.setOnClickListener {

            if (imagenOriginal) {
                Linternatxt.setText("LINTERNA: ON")
                linterna.setImageResource(R.drawable.flashlanternon)
                flashEncendido = !flashEncendido
                cameraManager.setTorchMode(cameraId, flashEncendido)
            } else {
                Linternatxt.setText("LINTERNA: OFF")
                linterna.setImageResource(R.drawable.flashlighton)
                flashEncendido = !flashEncendido
                cameraManager.setTorchMode(cameraId, flashEncendido)
            }

            imagenOriginal = !imagenOriginal
        }

        bombilla.setOnClickListener {
            if (bombillaOriginal) {
                bombillatxt.text = "BOMBILLA: ON"
                bombilla.setImageResource(R.drawable.bombillaencendida)
            } else {
                bombillatxt.text = "BOMBILLA: OFF"
                bombilla.setImageResource(R.drawable.bombillaapagada)
            }
            bombillaOriginal = !bombillaOriginal
        }



    }

    fun fechahora(): String {
        val c: Calendar = Calendar.getInstance()
        val sdf: SimpleDateFormat = SimpleDateFormat("dd MMMM YYYY, hh:mm:ss a")
        val strDate: String = sdf.format(c.getTime())
        return strDate
    }



    private fun obtenerDatos() {
        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    temp?.text = "${response.getString("temperatura")} C"
                    hum?.text = "${response.getString("humedad")} %"

                    val valor = response.getString("temperatura").toFloat()
                    cambiarImagen(valor)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                error.printStackTrace()
            }
        )
        datos.add(request)
    }

    private fun cambiarImagen(valor: Float) {
        if (valor >= 25) {
            imagenTemp.setImageResource(R.drawable.termalta)
        } else {
            imagenTemp.setImageResource(R.drawable.termometro)
        }
    }

    private val refrescar = object : Runnable {
        override fun run() {
            fecha?.text = fechahora() // Asignación de la hora actual
            obtenerDatos() // Llamada a la función que actualiza datos desde la API
            mHandler.postDelayed(this, 1000) // Se vuelve a ejecutar en 1 segundo
        }
    }




}



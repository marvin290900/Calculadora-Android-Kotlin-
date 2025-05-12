package com.example.calculadora_byca20076

//Jose Marvin Cuellar Aguilar -- CA20076

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    val suma = "+"
    val resta = "-"
    val multiplicacion = "*"
    val division = "/"
    val porcentaje = "%"
    val divideEntreUno = "1/x"
    val raizCuadrada = "√x"
    val alCuadrado = "x²"
    val cambiarSigno = "+/-"

    var operacionActual = ""
    var primerNumero: Double = Double.NaN
    var segundoNumero: Double = Double.NaN
    var resultadoMostrado = false
    var enError = false

    lateinit var textView_valorTemporal: TextView
    lateinit var textView_resultado: TextView
    lateinit var formatoDecimal: DecimalFormat



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        formatoDecimal = DecimalFormat("#.##########")
        textView_valorTemporal = findViewById(R.id.textView_valorTemporal)
        textView_resultado = findViewById(R.id.textView_resultado)

    }

    //Esta funcion se encarga de obtener el numero que se selecciona en el boton y ponerlo en el text de resultado
    fun seleccionarNumero(b: View) {
        if (enError) {
            resetearCalculadora()  // Resetear si está en estado de error
        }

        val boton: Button = b as Button

        if (textView_resultado.text.toString() == "0") {
            textView_resultado.text = ""
            resultadoMostrado = false
        }
        textView_resultado.text = textView_resultado.text.toString() + boton.text.toString()
    }

    //Esta funcion obtiene el operador que se selecciona, ya sea +,-,*,/..etc
    fun cambiarOperador(b: View) {
        if (enError) return  // No permite cambiar el operador si hay error

        val boton: Button = b as Button
        val operador = boton.text.toString().trim()

        // prevenir crash cuando el campo está vacío
        if (textView_resultado.text.isEmpty() && primerNumero.isNaN()) return

        // Si se selecciona el cambiador de signo se le coloca al numero que se ingresa
        if (operador == cambiarSigno) {
            if (textView_resultado.text.isNotEmpty()) {
                try {
                    val valor = textView_resultado.text.toString().toDouble()
                    textView_resultado.text = formatoDecimal.format(-valor)
                } catch (e: NumberFormatException) {
                    textView_resultado.text = ""
                }
            }
            return
        }

        // Si selecciona los cuatro diferentes operadores se manda a otra función dedicada
        if (operador in listOf(divideEntreUno, alCuadrado, raizCuadrada, porcentaje)) {
            calcularOperacionUnaria(operador)
            return
        }

        if (!primerNumero.isNaN()) {
            // Si ya se ha seleccionado una operación pero aún no hay segundo número,
            // solo cambia la operación
            if (textView_resultado.text.isEmpty()) {
                operacionActual = when (operador) {
                    "÷" -> division
                    "x" -> multiplicacion
                    else -> operador
                }
                textView_valorTemporal.text = formatoDecimal.format(primerNumero) + " " + operacionActual
                return
            } else if (operacionActual.isNotEmpty()) {
                calcular()
            }
        }

        try {
            primerNumero = textView_resultado.text.toString().toDouble()
        } catch (e: NumberFormatException) {
            return
        }
        operacionActual = when (operador) {
            "÷" -> division
            "x" -> multiplicacion
            else -> operador
        }

        textView_valorTemporal.text = formatoDecimal.format(primerNumero) + " " + operacionActual
        textView_resultado.text = ""
    }

    fun calcular() {
        if (operacionActual.isEmpty() || textView_resultado.text.isEmpty()) return

        try {
            segundoNumero = textView_resultado.text.toString().toDouble()
        } catch (e: NumberFormatException) {
            return
        }

        val resultado = when (operacionActual) {
            suma -> primerNumero + segundoNumero
            resta -> primerNumero - segundoNumero
            multiplicacion -> primerNumero * segundoNumero
            division -> {
                if (segundoNumero == 0.0) {
                    textView_resultado.text = "No se puede dividir entre cero"
                    bloquearBotones()
                    return
                } else {
                    primerNumero / segundoNumero
                }
            }
            else -> return
        }

        textView_valorTemporal.text =
            "${formatoDecimal.format(primerNumero)} $operacionActual ${formatoDecimal.format(segundoNumero)} ="
        textView_resultado.text = formatoDecimal.format(resultado)

        primerNumero = resultado
        segundoNumero = Double.NaN
        operacionActual = ""
        resultadoMostrado = true
    }

    fun calcularOperacionUnaria(operador: String) {
        if (textView_resultado.text.isEmpty()) return

        val numero = textView_resultado.text.toString().toDoubleOrNull() ?: return
        val resultado: Double
        val operacionTexto: String

        when (operador) {
            divideEntreUno -> {
                if (numero == 0.0) {
                    textView_resultado.text = "No se puede dividir entre cero"
                    bloquearBotones()
                    return
                }
                resultado = 1 / numero
                operacionTexto = "1/(${formatoDecimal.format(numero)})"
            }
            alCuadrado -> {
                resultado = numero.pow(2)
                operacionTexto = "(${formatoDecimal.format(numero)})²"
            }
            raizCuadrada -> {
                if (numero < 0) {
                    textView_resultado.text = "Entrada no valida"
                    bloquearBotones()
                    return
                }
                resultado = sqrt(numero)
                operacionTexto = "√(${formatoDecimal.format(numero)})"
            }
            porcentaje -> {
                // Si hay una operación activa, aplicamos el porcentaje sobre el primer número
                resultado = if (operacionActual.isNotEmpty() && !primerNumero.isNaN()) {
                    when (operacionActual) {
                        "+" -> (primerNumero * numero / 100)  // Porcentaje sumado al primer número
                        "-" -> (primerNumero * numero / 100)  // Porcentaje restado del primer número
                        "×" -> primerNumero * (numero / 100)  // Porcentaje multiplicado por el primer número
                        "÷" -> primerNumero / (numero / 100)  // Porcentaje dividido por el primer número
                        else -> numero / 100  // Si no hay operación, solo el porcentaje del número
                    }
                } else {
                    // Si no hay operación activa, solo calculamos el porcentaje sobre el número actual
                    numero / 100
                }
                operacionTexto = "${formatoDecimal.format(numero)}%"
            }

            else -> return
        }

        // Ahora, si no estamos en la última operación, el texto temporal mostrará la operación y el signo igual
        if (operacionActual.isNotEmpty() && !resultadoMostrado) {
            textView_valorTemporal.text = "${formatoDecimal.format(primerNumero)} $operacionActual $operacionTexto"
        } else {
            // Cuando ya se mostró el resultado, solo mostramos la operación sin el signo igual
            textView_valorTemporal.text = operacionTexto
        }
        textView_resultado.text = formatoDecimal.format(resultado)
        resultadoMostrado = true
    }

    fun igual(b: View) {
        if (operacionActual.isEmpty() || textView_resultado.text.isEmpty()) return

        try {
            segundoNumero = textView_resultado.text.toString().toDouble()
        } catch (e: NumberFormatException) {
            return
        }

        val resultado = when (operacionActual) {
            suma -> primerNumero + segundoNumero
            resta -> primerNumero - segundoNumero
            multiplicacion -> primerNumero * segundoNumero
            division -> {
                if (segundoNumero == 0.0) {
                    textView_resultado.text = "No se puede dividir entre cero"
                    bloquearBotones()
                    return
                } else {
                    primerNumero / segundoNumero
                }
            }
            else -> return
        }

        // Mostrar operación completa
        textView_valorTemporal.text =
            "${formatoDecimal.format(primerNumero)} $operacionActual ${formatoDecimal.format(segundoNumero)} ="
        textView_resultado.text = formatoDecimal.format(resultado)

        primerNumero = resultado
        segundoNumero = Double.NaN
        operacionActual = ""
        resultadoMostrado = true
    }

    // Función para bloquear todos los botones excepto los números y borrar
    fun bloquearBotones() {
        enError = true
        findViewById<Button>(R.id.button_porcentaje).isEnabled = false
        findViewById<Button>(R.id.button_entreUno).isEnabled = false
        findViewById<Button>(R.id.button_elevadoAlCuadrado).isEnabled = false
        findViewById<Button>(R.id.button_raizCuadrada).isEnabled = false
        findViewById<Button>(R.id.button_division).isEnabled = false
        findViewById<Button>(R.id.button_multiplicacion).isEnabled = false
        findViewById<Button>(R.id.button_resta).isEnabled = false
        findViewById<Button>(R.id.button_suma).isEnabled = false
        findViewById<Button>(R.id.button_puntoDecimal).isEnabled = false
        findViewById<Button>(R.id.button_cambiarSigno).isEnabled = false
        findViewById<Button>(R.id.button_borraDigito).isEnabled = false
        findViewById<Button>(R.id.button_borraElemento).isEnabled = false
        findViewById<Button>(R.id.button_igual).isEnabled = false

    }

    // Función para habilitar los botones cuando no hay error
    fun habilitarBotones() {
        enError = false
        findViewById<Button>(R.id.button_porcentaje).isEnabled = true
        findViewById<Button>(R.id.button_entreUno).isEnabled = true
        findViewById<Button>(R.id.button_elevadoAlCuadrado).isEnabled = true
        findViewById<Button>(R.id.button_raizCuadrada).isEnabled = true
        findViewById<Button>(R.id.button_division).isEnabled = true
        findViewById<Button>(R.id.button_multiplicacion).isEnabled = true
        findViewById<Button>(R.id.button_resta).isEnabled = true
        findViewById<Button>(R.id.button_suma).isEnabled = true
        findViewById<Button>(R.id.button_puntoDecimal).isEnabled = true
        findViewById<Button>(R.id.button_cambiarSigno).isEnabled = true
        findViewById<Button>(R.id.button_borraDigito).isEnabled = true
        findViewById<Button>(R.id.button_borraElemento).isEnabled = true
        findViewById<Button>(R.id.button_igual).isEnabled = true
    }

    // Función para borrar todo y reiniciar el estado
    fun resetearCalculadora() {
        textView_resultado.text = ""
        textView_valorTemporal.text = ""
        primerNumero = Double.NaN
        segundoNumero = Double.NaN
        operacionActual = ""
        habilitarBotones()
    }

    fun borrar(b: View) {
        val boton: Button = b as Button
        when (boton.text.toString().trim()) {
            "C" -> {
                // Borra todo
                primerNumero = Double.NaN
                segundoNumero = Double.NaN
                operacionActual = ""
                textView_valorTemporal.text = ""
                textView_resultado.text = ""
                habilitarBotones()  // Asegura que todos los botones estén habilitados
            }
            "CE" -> {
                // Borra solo la última operación
                textView_resultado.text = ""
            }
            "⌫" -> {
                // Borra el último dígito del número en curso
                val texto = textView_resultado.text.toString()
                if (texto.isNotEmpty()) {
                    textView_resultado.text = texto.dropLast(1)
                }
            }
        }
    }

}
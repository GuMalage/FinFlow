package com.example.finflow

import androidx.test.core.app.ApplicationProvider
import com.example.finflow.model.Registro
import com.example.finflow.viewModel.RegistroViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {

    @Test
    fun calcularTotalCreditos_somaApenasOsRegistrosDeCredito() {
        val contextoSimulado = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = RegistroViewModel(contextoSimulado)

        val listaMockada = listOf(
            Registro(id = "1", valor = 100.0, ehCredito = true, data = 0L, observacao = ""),
            Registro(id = "2", valor = 50.0, ehCredito = false, data = 0L, observacao = ""),
            Registro(id = "3", valor = 250.0, ehCredito = true, data = 0L, observacao = "")
        )

        val totalCreditos = viewModel.calcularTotalCreditos(listaMockada)

        assertEquals(350.0, totalCreditos, 0.001)
    }

    @Test
    fun calcularTotalDebitos_somaApenasOsRegistrosDeDebito() {
        val contextoSimulado = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = RegistroViewModel(contextoSimulado)

        val listaMockada = listOf(
            Registro(id = "1", valor = 100.0, ehCredito = true, data = 0L, observacao = ""),
            Registro(id = "2", valor = 50.0, ehCredito = false, data = 0L, observacao = ""),
            Registro(id = "3", valor = 25.50, ehCredito = false, data = 0L, observacao = "")
        )

        val totalDebitos = viewModel.calcularTotalDebitos(listaMockada)

        assertEquals(75.50, totalDebitos, 0.001)
    }
}
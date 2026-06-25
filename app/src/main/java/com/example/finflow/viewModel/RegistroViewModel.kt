package com.example.finflow.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finflow.database.AppDatabase
import com.example.finflow.model.Registro
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val appDao = AppDatabase.getDatabase(application).appDao()

    var valor by mutableStateOf("")
        private set

    var observacao by mutableStateOf("")
        private set

    var data by mutableStateOf("")
        private set

    var ehCredito by mutableStateOf(true)
        private set

    fun onValorChange(novoValor: String) {
        valor = novoValor
    }

    fun onObservacaoChange(novaObservacao: String) {
        observacao = novaObservacao
    }

    fun onDataChange(novaData: String) {
        data = novaData
    }

    fun onEhCreditoChange(novoEhCredito: Boolean) {
        ehCredito = novoEhCredito
    }

    var mensagemErro by mutableStateOf<String?>(null)
        private set

    fun limparTela() {
        valor = ""
        observacao = ""
        data = ""
    }

    fun limparErro() {
        mensagemErro = null
    }
    val listaRegistros = appDao.listarTodas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val registros  = listaRegistros


    fun salvarRegistro(onSucesso: () -> Unit) {
        if (valor.isBlank() || data.isBlank()) {
            mensagemErro = "Por favor, preencha o valor e a data!"
            return
        }

        val dataEmMilissegundos = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.parse(data)?.time ?: System.currentTimeMillis()

        val valorNumerico = valor.replace(",", ".").toDoubleOrNull() ?: 0.0

        val novoRegistro = Registro(
            valor = valorNumerico,
            ehCredito = ehCredito,
            data = dataEmMilissegundos,
            observacao = observacao
        )

        viewModelScope.launch {
            appDao.salvar(novoRegistro)
            limparTela()
            onSucesso()
        }
    }

    fun calcularTotalCreditos(registros: List<Registro>): Double {
        return registros
            .filter { it.ehCredito }
            .sumOf { it.valor }
    }

    fun calcularTotalDebitos(registros: List<Registro>): Double {
        return registros
            .filter { !it.ehCredito }
            .sumOf { it.valor }
    }
}
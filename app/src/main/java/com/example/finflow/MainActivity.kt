package com.example.finflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finflow.model.Registro
import com.example.finflow.ui.theme.EntradaSucesso
import com.example.finflow.ui.theme.SaidaAlerta
import com.example.finflow.ui.theme.TextoSecundario
import com.example.finflow.ui.theme.GerenciadorContasTheme
import com.example.finflow.viewModel.RegistroViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.finflow.ui.theme.corCredito
import com.example.finflow.ui.theme.corDebito

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GerenciadorContasTheme {

                val navController = rememberNavController()
                val viewModel: RegistroViewModel = viewModel()

                NavHost(navController, startDestination = "home") {

                    composable("home") {
                        TelaInicial(
                            viewModel = viewModel,
                            onNavegateToTelaExtrato = {
                                navController.navigate("extrato")
                            }
                        )
                    }

                    composable("extrato") {

                        val registros by viewModel.registros.collectAsState()

                        TelaExtrato(
                            registros = registros,
                            totalCreditos = viewModel.calcularTotalCreditos(registros),
                            totalDebitos = viewModel.calcularTotalDebitos(registros),
                            onVoltar = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicial(
    viewModel: RegistroViewModel = viewModel(),
    onNavegateToTelaExtrato: () -> Unit
) {
    val valor = viewModel.valor
    val observacao = viewModel.observacao
    val data = viewModel.data
    val ehCredito = viewModel.ehCredito

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Novo Lançamento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            CreditoDebito(
                ehCredito = ehCredito,
                onTipoAlterado = { viewModel.onEhCreditoChange(it) }
            )

            OutlinedTextField(
                value = valor,
                onValueChange = {viewModel.onValorChange(it)},
                label = { Text("Valor do lançamento") },
                prefix = { Text("R$ ") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Data(
                dataText = data,
                onDataAlterada = { millis ->
                    val formatador = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formatador.timeZone = TimeZone.getTimeZone("UTC")
                    viewModel.onDataChange(formatador.format(Date(millis)))
                }
            )

            OutlinedTextField(
                value = observacao,
                onValueChange = { viewModel.onObservacaoChange(it) },
                label = { Text("Observações adicionais") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                placeholder = { Text("Opcional") },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Buttons(
                ehCredito = ehCredito,
                onRegistrarClick = {viewModel.salvarRegistro(onSucesso = {
                    onNavegateToTelaExtrato()
                })},
                onLimparClick = { viewModel.limparTela() },
                onNavegarTelaExtrato = onNavegateToTelaExtrato
            )
        }
    }
}

@Composable
fun CreditoDebito(ehCredito: Boolean, onTipoAlterado: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = { onTipoAlterado(true) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ehCredito) EntradaSucesso.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                contentColor = if (ehCredito) EntradaSucesso else TextoSecundario
            ),
            elevation = null
        ) {
            Text(if (ehCredito) "● CRÉDITO" else "CRÉDITO", fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = { onTipoAlterado(false) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!ehCredito) SaidaAlerta.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                contentColor = if (!ehCredito) SaidaAlerta else TextoSecundario
            ),
            elevation = null
        ) {
            Text(if (!ehCredito) "● DÉBITO" else "DÉBITO", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun Data(
    dataText: String,
    onDataAlterada: (Long) -> Unit
) {
    var mostrarCalendario by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = dataText,
            onValueChange = {},
            label = { Text("Data da transação") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            placeholder = { Text("DD/MM/AAAA") },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { mostrarCalendario = true }
        )
    }

    if (mostrarCalendario) {
        CalendarioDialog(
            onDataSelecionada = { millis ->
                onDataAlterada(millis)
            },
            onDispensa = { mostrarCalendario = false }
        )
    }
}

@Composable
fun Buttons(
    ehCredito: Boolean,
    onRegistrarClick: () -> Unit,
    onLimparClick: () -> Unit,
    onNavegarTelaExtrato: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onRegistrarClick,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ehCredito) EntradaSucesso else MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Confirmar Lançamento", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        OutlinedButton(
            onClick = onLimparClick,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text("Limpar Campos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        TextButton(
            onClick = onNavegarTelaExtrato,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Acessar Extrato Completo",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioDialog(
    onDataSelecionada: (Long) -> Unit,
    onDispensa: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = { onDispensa() },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onDataSelecionada(millis)
                }
                onDispensa()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDispensa() }) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaExtrato(
    registros: List<Registro>,
    totalCreditos: Double,
    totalDebitos: Double,
    onVoltar: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extrato") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {



            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CRÉDITOS",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "R$ %.2f".format(totalCreditos),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF166534)
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(50.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DÉBITOS",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "R$ %.2f".format(totalDebitos),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF9A3412)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(registros, key = { it.id }) { registro ->
                    ItemRegistro(registro)
                }
            }
        }
    }
}

@Composable
fun ItemRegistro(registro: Registro) {

    val dataFormatada = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date(registro.data))

    val corCredito = Color(0xFF166534)
    val corDebito = Color(0xFF9A3412)

    val corFundo = if (registro.ehCredito) {
        Color(0xFFF6FBF7)
    } else {
        Color(0xFFFFF8F6)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = corFundo
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    text = registro.observacao.ifEmpty { "Sem descrição" },
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = dataFormatada,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "R$ %.2f".format(registro.valor),
                style = MaterialTheme.typography.titleMedium,
                color = if (registro.ehCredito)
                    corCredito
                else
                    corDebito
            )
        }
    }
}
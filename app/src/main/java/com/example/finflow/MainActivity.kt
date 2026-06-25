package com.example.finflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.finflow.ui.theme.GerenciadorContasTheme
import com.example.finflow.viewModel.RegistroViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.stringResource
import java.text.NumberFormat
import androidx.compose.ui.tooling.preview.Preview

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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.mensagemErro) {
        viewModel.mensagemErro?.let { mensagem ->
            snackbarHostState.showSnackbar(
                message = mensagem,
                duration = SnackbarDuration.Short
            )
            viewModel.limparErro()
        }
    }

    val simboloMoeda = NumberFormat.getCurrencyInstance().currency?.symbol ?: ""

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.novo_lancamento),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            CreditoDebito(
                ehCredito = ehCredito,
                onTipoAlterado = { viewModel.onEhCreditoChange(it) }
            )

            OutlinedTextField(
                value = valor,
                onValueChange = { viewModel.onValorChange(it) },
                label = { Text(stringResource(R.string.valor_do_lancamento)) },
                prefix = { Text("$simboloMoeda ") },
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
                label = { Text(stringResource(R.string.observacoes_adicionais)) },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.opcional)) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Buttons(
                ehCredito = ehCredito,
                onRegistrarClick = {
                    viewModel.salvarRegistro(onSucesso = {
                        onNavegateToTelaExtrato()
                    })
                },
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
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ehCredito) EntradaSucesso.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                contentColor = if (ehCredito) EntradaSucesso else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = null
        ) {
            Text(
                text = if (ehCredito) stringResource(R.string.credito_selecionado) else stringResource(R.string.credito),
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = { onTipoAlterado(false) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!ehCredito) SaidaAlerta.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                contentColor = if (!ehCredito) SaidaAlerta else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = null
        ) {
            Text(
                text = if (!ehCredito) stringResource(R.string.debitos_selecionado) else stringResource(R.string.debito),
                fontWeight = FontWeight.Bold
            )
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
            label = { Text(stringResource(R.string.data_da_transacao)) },
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
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDispensa() }) {
                Text(stringResource(R.string.cancelar))
            }
        }
    ) {
        DatePicker(state = datePickerState)
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
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ehCredito) EntradaSucesso else MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(stringResource(R.string.confirmar_lancamento), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        OutlinedButton(
            onClick = onLimparClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(stringResource(R.string.limpar_campos), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        TextButton(
            onClick = onNavegarTelaExtrato,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.acessar_extrato_completo),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        }
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
    val formatadorNumero = NumberFormat.getInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val simboloMoeda = NumberFormat.getCurrencyInstance().currency?.symbol ?: ""

    val isDark = isSystemInDarkTheme()
    val corTextoCredito = if (isDark) Color(0xFF4ADE80) else Color(0xFF166534)
    val corTextoDebito = if (isDark) Color(0xFFF87171) else Color(0xFF9A3412)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.extrato)) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.voltar)
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.creditos),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$simboloMoeda ${formatadorNumero.format(totalCreditos)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = corTextoCredito
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(50.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.debitos),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$simboloMoeda ${formatadorNumero.format(totalDebitos)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = corTextoDebito
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(registros, key = { it.id }) { registro ->
                    ItemRegistro(registro)
                }
            }
        }
    }
}
@Composable
fun ItemRegistro(registro: Registro) {
    val dataFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(registro.data))
    val valorMonetarioFormatado = NumberFormat.getCurrencyInstance().format(registro.valor)

    val isDark = isSystemInDarkTheme()

    val corFundoItem = if (registro.ehCredito) {
        if (isDark) Color(0xFF064E3B) else Color(0xFFF6FBF7)
    } else {
        if (isDark) Color(0xFF7F1D1D) else Color(0xFFFFF8F6)
    }

    val corTextoValor = if (registro.ehCredito) {
        if (isDark) Color(0xFF34D399) else Color(0xFF166534)
    } else {
        if (isDark) Color(0xFFF87171) else Color(0xFF9A3412)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = corFundoItem),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = registro.observacao.ifEmpty { stringResource(R.string.sem_descricao) },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = dataFormatada,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = valorMonetarioFormatado,
                style = MaterialTheme.typography.titleMedium,
                color = corTextoValor
            )
        }
    }
}


@Preview(showBackground = true, name = "Crédito Selecionado ")
@Composable
fun CreditoDebitoCreditoPreview() {
    GerenciadorContasTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                CreditoDebito(ehCredito = true, onTipoAlterado = {})
            }
        }
    }
}

@Preview(showBackground = true, name = "Campo de Data ")
@Composable
fun DataPreview() {
    GerenciadorContasTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                Data(dataText = "25/06/2026", onDataAlterada = {})
            }
        }
    }
}

@Preview(showBackground = true, name = "Botões")
@Composable
fun ButtonsCreditoPreview() {
    GerenciadorContasTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                Buttons(
                    ehCredito = true,
                    onRegistrarClick = {},
                    onLimparClick = {},
                    onNavegarTelaExtrato = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Item Extrato Crédito")
@Composable
fun ItemRegistroCreditoPreview() {
    GerenciadorContasTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                ItemRegistro(
                    registro = Registro(
                        id = "1",
                        valor = 1500.0,
                        observacao = "Salário Mensal",
                        data = System.currentTimeMillis(),
                        ehCredito = true
                    )
                )
            }
        }
    }
}
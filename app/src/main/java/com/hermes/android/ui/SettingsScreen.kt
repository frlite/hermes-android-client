package com.hermes.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.hermes.android.HermesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: HermesViewModel = viewModel()
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    var keyInput by remember { mutableStateOf(apiKey) }
    var urlInput by remember { mutableStateOf(baseUrl) }
    var testResult by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "API 配置",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("API Base URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.setBaseUrl(urlInput)
                        viewModel.setApiKey(keyInput)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存")
                }

                OutlinedButton(
                    onClick = {
                        viewModel.setBaseUrl(urlInput)
                        viewModel.setApiKey(keyInput)
                        scope.launch {
                            testResult = viewModel.api.testConnection()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("测试连接")
                }
            }

            testResult?.let { ok ->
                Text(
                    text = if (ok) "连接成功" else "连接失败",
                    color = if (ok) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider()

            Text(
                "使用说明",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "1. 在 OpenRouter 或其他兼容 OpenAI API 的平台获取 API Key\n" +
                        "2. 填入 Base URL 和 API Key\n" +
                        "3. 点击保存\n" +
                        "4. 返回聊天界面开始对话\n\n" +
                        "默认使用 OpenRouter 免费模型，可修改代码切换其他模型。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

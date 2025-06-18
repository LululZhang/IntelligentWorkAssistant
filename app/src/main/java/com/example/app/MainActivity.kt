package com.example.app

// 导入必要的Android和Compose相关库
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme
import androidx.compose.ui.graphics.Color

// 主活动类，继承自ComponentActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置Compose内容
        setContent {
            AppTheme {
                // 使用Surface作为根容器，设置米白色背景
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F0)  // 米白色背景
                ) {
                    LoginScreen(this)
                }
            }
        }
    }
}

// 登录界面的预览函数
@Preview
@Composable
fun LoginScreenPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            LoginScreen(MainActivity())
        }
    }
}

// 登录界面的主要Composable函数
@Composable
fun LoginScreen(activity: MainActivity) {
    // 使用remember和mutableStateOf管理状态
    var username by remember { mutableStateOf("") }  // 用户名状态
    var password by remember { mutableStateOf("") }  // 密码状态
    var isLoginEnabled by remember { mutableStateOf(false) }  // 登录按钮启用状态

    // 主列布局
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,  // 水平居中
        verticalArrangement = Arrangement.Center  // 垂直居中
    ) {
        // 标题文本
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 用户名输入框
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                // 当用户名和密码都不为空时启用登录按钮
                isLoginEnabled = username.isNotEmpty() && password.isNotEmpty()
            },
            label = { Text("用户名") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(12.dp)),  // 圆角效果
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        // 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                // 当用户名和密码都不为空时启用登录按钮
                isLoginEnabled = username.isNotEmpty() && password.isNotEmpty()
            },
            label = { Text("密码") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .clip(RoundedCornerShape(12.dp)),  // 圆角效果
            visualTransformation = PasswordVisualTransformation(),  // 密码掩码
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),  // 密码键盘类型
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        // 登录按钮
        Button(
            onClick = {
                Log.d("Login", "Login button clicked")
                Log.d("Login", "Login with: $username, $password")
                try {
                    // 登录成功后跳转到项目选择界面
                    val intent = Intent(activity, ProjectSelectionActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()  // 结束当前活动
                    Log.d("Login", "Successfully started ProjectSelectionActivity")
                } catch (e: Exception) {
                    Log.e("Login", "Error starting ProjectSelectionActivity", e)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isLoginEnabled  // 根据状态启用/禁用按钮
        ) {
            Text("登录")
        }
    }
}


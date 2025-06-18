package com.example.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme

data class ProgressItem(
    val title: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val status: String
)

class ProgressAdjustmentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val projectName = intent.getStringExtra("project_name") ?: "未知项目"
        Log.d("ProgressAdjustment", "Activity onCreate with project: $projectName")
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F0)  // 米白色背景
                ) {
                    ProgressAdjustmentContent(projectName)
                }
            }
        }
    }
}

@Composable
fun ProgressAdjustmentContent(projectName: String) {
    var selectedItem by remember { mutableStateOf<ProgressItem?>(null) }
    
    // 示例进度项目列表
    val progressItems = listOf(
        ProgressItem("基础工程", 75, 100, "进行中"),
        ProgressItem("主体结构", 45, 100, "进行中"),
        ProgressItem("机电安装", 30, 100, "进行中"),
        ProgressItem("内部装修", 20, 100, "进行中"),
        ProgressItem("外部装饰", 15, 100, "进行中")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$projectName - 进度调整",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { /* TODO: 添加刷新功能 */ }) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }
        }

        // 进度项目列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(progressItems) { item ->
                ProgressItemCard(
                    item = item,
                    isSelected = selectedItem == item,
                    onItemClick = { selectedItem = item }
                )
            }
        }
    }
}

@Composable
fun ProgressItemCard(
    item: ProgressItem,
    isSelected: Boolean,
    onItemClick: () -> Unit
) {
    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                Color(0xFFF5F5F0)  // 米白色背景
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = item.currentProgress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF2196F3),  // Material Blue
                trackColor = Color(0xFFE3F2FD)  // Light Blue Background
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "当前进度: ${item.currentProgress}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "目标进度: ${item.targetProgress}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressAdjustmentContentPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            ProgressAdjustmentContent(projectName = "Project Alpha")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressItemCardPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            val item = ProgressItem(
                title = "Task 1",
                currentProgress = 50,
                targetProgress = 100,
                status = "In Progress"
            )
            ProgressItemCard(
                item = item,
                isSelected = false,
                onItemClick = {}
            )
        }
    }
}
 
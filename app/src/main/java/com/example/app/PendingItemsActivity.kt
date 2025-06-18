package com.example.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme

data class PendingItem(
    val title: String,
    val department: String,
    val submitter: String,
    val submitTime: String,
    val priority: String,
    val description: String
)

class PendingItemsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val projectName = intent.getStringExtra("project_name") ?: "未知项目"
        Log.d("PendingItems", "Activity onCreate with project: $projectName")
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F0)  // 米白色背景
                ) {
                    PendingItemsContent(projectName)
                }
            }
        }
    }
}

@Composable
fun PendingItemsContent(projectName: String) {
    var selectedItem by remember { mutableStateOf<PendingItem?>(null) }
    
    // 示例待批事项列表
    val pendingItems = listOf(
        PendingItem(
            "施工图纸审核",
            "设计部",
            "张工",
            "2024-03-15 14:30",
            "高",
            "请审核新提交的施工图纸，包括结构图和电气图"
        ),
        PendingItem(
            "材料采购申请",
            "采购部",
            "李经理",
            "2024-03-15 10:15",
            "中",
            "需要采购一批钢筋和水泥，请审批采购清单"
        ),
        PendingItem(
            "安全培训计划",
            "安全部",
            "王主管",
            "2024-03-14 16:45",
            "高",
            "下周一的安全培训计划，请审批培训内容和时间安排"
        ),
        PendingItem(
            "设备维护申请",
            "工程部",
            "赵工",
            "2024-03-14 09:20",
            "中",
            "塔吊需要定期维护，请审批维护计划"
        )
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
                text = "$projectName - 待批事项",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { /* TODO: 添加刷新功能 */ }) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }
        }

        // 待批事项列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pendingItems) { item ->
                PendingItemCard(
                    item = item,
                    isSelected = selectedItem == item,
                    onItemClick = { selectedItem = item }
                )
            }
        }
    }
}

@Composable
fun PendingItemCard(
    item: PendingItem,
    isSelected: Boolean,
    onItemClick: () -> Unit
) {
    val buttonColor = when(item.priority) {
        "高" -> Color(0xFFE53935)  // 深红色
        "中" -> Color(0xFFFFA000)  // 橙色
        else -> Color(0xFF43A047)  // 绿色
    }
    
    val textColor = Color.White
    val selectedTextColor = Color(0xFF1A237E)  // 深蓝色

    Button(
        onClick = onItemClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                buttonColor
        ),
        contentPadding = PaddingValues(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isSelected) selectedTextColor else textColor
                )
                Surface(
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = item.priority,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) selectedTextColor else textColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "部门: ${item.department}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor
                )
                Text(
                    text = "提交人: ${item.submitter}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "提交时间: ${item.submitTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) selectedTextColor else textColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PendingItemsContentPreview() {
    AppTheme {
            Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            PendingItemsContent(projectName = "Project Alpha")
        }
    }
} 
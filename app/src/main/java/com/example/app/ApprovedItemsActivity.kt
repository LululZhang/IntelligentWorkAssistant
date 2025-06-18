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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme

data class ApprovedItem(
    val title: String,
    val department: String,
    val submitter: String,
    val submitTime: String,
    val approveTime: String,
    val approver: String,
    val status: String,
    val description: String
)

class ApprovedItemsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val projectName = intent.getStringExtra("project_name") ?: "未知项目"
        Log.d("ApprovedItems", "Activity onCreate with project: $projectName")
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F0)  // 米白色背景
                ) {
                    ApprovedItemsContent(projectName)
                }
            }
        }
    }
}

@Composable
fun ApprovedItemsContent(projectName: String) {
    var selectedItem by remember { mutableStateOf<ApprovedItem?>(null) }
    
    // 示例已批事项列表
    val approvedItems = listOf(
        ApprovedItem(
            "施工方案审批",
            "工程部",
            "张工",
            "2024-03-14 09:30",
            "2024-03-14 15:20",
            "王总",
            "已通过",
            "主体结构施工方案已审核通过，可以开始施工"
        ),
        ApprovedItem(
            "材料验收申请",
            "采购部",
            "李经理",
            "2024-03-13 14:15",
            "2024-03-13 16:30",
            "赵总",
            "已通过",
            "钢筋材料验收合格，可以入库使用"
        ),
        ApprovedItem(
            "设备进场申请",
            "工程部",
            "王主管",
            "2024-03-12 10:45",
            "2024-03-12 11:30",
            "张总",
            "已通过",
            "新采购的塔吊可以进场安装"
        ),
        ApprovedItem(
            "施工人员培训",
            "人事部",
            "刘经理",
            "2024-03-11 16:20",
            "2024-03-11 17:00",
            "李总",
            "已通过",
            "新进施工人员安全培训计划已批准"
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
                text = "$projectName - 已批事项",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { /* TODO: 添加刷新功能 */ }) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }
        }

        // 已批事项列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(approvedItems) { item ->
                ApprovedItemCard(
                    item = item,
                    isSelected = selectedItem == item,
                    onItemClick = { selectedItem = item }
                )
            }
        }
    }
}

@Composable
fun ApprovedItemCard(
    item: ApprovedItem,
    isSelected: Boolean,
    onItemClick: () -> Unit
) {
    val buttonColor = Color(0xFF43A047)  // 绿色
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
                        text = item.status,
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "提交时间: ${item.submitTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor
                )
                Text(
                    text = "审批人: ${item.approver}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "审批时间: ${item.approveTime}",
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
fun ApprovedItemsContentPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            ApprovedItemsContent(projectName = "Project Alpha")
        }
    }
} 
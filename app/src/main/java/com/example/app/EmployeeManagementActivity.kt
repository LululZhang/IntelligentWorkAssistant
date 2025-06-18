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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme

data class Employee(
    val name: String,
    val position: String,
    val department: String,
    val phone: String,
    val email: String,
    val status: String
)

class EmployeeManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val projectName = intent.getStringExtra("project_name") ?: "未知项目"
        Log.d("EmployeeManagement", "Activity onCreate with project: $projectName")
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F0)  // 米白色背景
                ) {
                    EmployeeManagementContent(projectName)
                }
            }
        }
    }
}

@Composable
fun EmployeeManagementContent(projectName: String) {
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    
    // 示例员工列表
    val employees = listOf(
        Employee(
            "张三",
            "项目经理",
            "工程部",
            "13800138000",
            "zhangsan@example.com",
            "在职"
        ),
        Employee(
            "李四",
            "安全主管",
            "安全部",
            "13800138001",
            "lisi@example.com",
            "在职"
        ),
        Employee(
            "王五",
            "施工员",
            "工程部",
            "13800138002",
            "wangwu@example.com",
            "在职"
        ),
        Employee(
            "赵六",
            "材料员",
            "采购部",
            "13800138003",
            "zhaoliu@example.com",
            "在职"
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
                text = "$projectName - 员工管理",
                style = MaterialTheme.typography.headlineMedium
            )
            Row {
                IconButton(onClick = { /* TODO: 添加员工 */ }) {
                    Icon(Icons.Default.Add, contentDescription = "添加员工")
                }
                IconButton(onClick = { /* TODO: 刷新列表 */ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        }

        // 员工列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(employees) { employee ->
                EmployeeCard(
                    employee = employee,
                    isSelected = selectedEmployee == employee,
                    onEmployeeClick = { selectedEmployee = employee }
                )
            }
        }
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    isSelected: Boolean,
    onEmployeeClick: () -> Unit
) {
    val buttonColor = Color(0xFF2196F3)  // 蓝色
    val textColor = Color.White
    val selectedTextColor = Color(0xFF1A237E)  // 深蓝色

    Button(
        onClick = onEmployeeClick,
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
                    text = employee.name,
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
                        text = employee.status,
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
                    text = "职位: ${employee.position}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor
                )
                Text(
                    text = "部门: ${employee.department}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "电话: ${employee.phone}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) selectedTextColor else textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "邮箱: ${employee.email}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) selectedTextColor else textColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmployeeManagementContentPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            EmployeeManagementContent(projectName = "Project Alpha")
        }
    }
} 
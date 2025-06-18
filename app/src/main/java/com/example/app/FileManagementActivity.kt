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

data class ProjectFile(
    val name: String,
    val type: String,
    val size: String,
    val uploadTime: String,
    val uploader: String,
    val status: String
)

class FileManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val projectName = intent.getStringExtra("project_name") ?: "未知项目"
        Log.d("FileManagement", "Activity onCreate with project: $projectName")
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F0)  // 米白色背景
                ) {
                    FileManagementContent(projectName)
                }
            }
        }
    }
}

@Composable
fun FileManagementContent(projectName: String) {
    var selectedFile by remember { mutableStateOf<ProjectFile?>(null) }
    
    // 示例文件列表
    val files = listOf(
        ProjectFile(
            "施工图纸.pdf",
            "PDF",
            "2.5MB",
            "2024-03-15 14:30",
            "张工",
            "已审核"
        ),
        ProjectFile(
            "材料清单.xlsx",
            "Excel",
            "1.2MB",
            "2024-03-15 10:15",
            "李经理",
            "待审核"
        ),
        ProjectFile(
            "安全培训计划.docx",
            "Word",
            "0.8MB",
            "2024-03-14 16:45",
            "王主管",
            "已审核"
        ),
        ProjectFile(
            "设备维护记录.docx",
            "Word",
            "1.5MB",
            "2024-03-14 09:20",
            "赵工",
            "待审核"
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
                text = "$projectName - 文件管理",
                style = MaterialTheme.typography.headlineMedium
            )
            Row {
                IconButton(onClick = { /* TODO: 上传文件 */ }) {
                    Icon(Icons.Default.Upload, contentDescription = "上传文件")
                }
                IconButton(onClick = { /* TODO: 刷新列表 */ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        }

        // 文件列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(files) { file ->
                FileCard(
                    file = file,
                    isSelected = selectedFile == file,
                    onFileClick = { selectedFile = file }
                )
            }
        }
    }
}

@Composable
fun FileCard(
    file: ProjectFile,
    isSelected: Boolean,
    onFileClick: () -> Unit
) {
    val buttonColor = Color(0xFF9C27B0)  // 紫色
    val textColor = Color.White
    val selectedTextColor = Color(0xFF1A237E)  // 深蓝色

    Button(
        onClick = onFileClick,
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
                    text = file.name,
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
                        text = file.status,
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
                    text = "类型: ${file.type}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor
                )
                Text(
                    text = "大小: ${file.size}",
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
                    text = "上传时间: ${file.uploadTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor
                )
                Text(
                    text = "上传人: ${file.uploader}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) selectedTextColor else textColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FileManagementContentPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            FileManagementContent(projectName = "Project Alpha")
        }
    }
} 
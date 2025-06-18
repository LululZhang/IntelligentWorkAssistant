package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

data class ProjectGroup(
    val groupName: String,
    val projects: List<String>
)

class ProjectSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ProjectSelection", "Activity onCreate")
        setContent {
            Log.d("ProjectSelection", "Setting content")
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F0)  // 米白色背景
                ) {
                    ProjectSelectionContent()
                }
            }
        }
    }
}

@Composable
fun ProjectSelectionContent() {
    Log.d("ProjectSelection", "ProjectSelectionContent composable")
    val context = LocalContext.current
    var selectedProject by remember { mutableStateOf<String?>(null) }
    
    // 示例项目列表，你可以根据实际项目内容修改
    val projectGroups = listOf(
        ProjectGroup(
            "商业建筑项目",
            listOf("国际金融中心", "环球贸易广场UTC", "云端大厦", "绿洲商务中心","万象城","星光天地购物广场")
        ),
        ProjectGroup(
            "住宅建筑项目",
            listOf("滨江壹号院", "翡翠湖畔别墅区","青年创客公寓","梧桐国际社区","阳光家园安居工程")
        ),
        ProjectGroup(
            "公共设施项目",
            listOf("市立图书馆新馆", "科技馆穹顶计划","虹桥交通枢纽扩建工程","滨海国际机场T3航站楼","仁和医院新院区")
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(projectGroups) { group ->
            Text(
                text = group.groupName,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.projects.forEach { project ->
                    ProjectCard(
                        projectName = project,
                        isSelected = selectedProject == project,
                        onClick = { 
                            Log.d("ProjectSelection", "Project selected: $project")
                            selectedProject = project
                            // 直接跳转到项目详情界面
                            val intent = Intent(context, ProjectDetailActivity::class.java).apply {
                                putExtra("project_name", project)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    projectName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = projectName,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectSelectionContentPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            ProjectSelectionContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectCardPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            ProjectCard(
                projectName = "示例项目",
                isSelected = false,
                onClick = {}
            )
        }
    }
}

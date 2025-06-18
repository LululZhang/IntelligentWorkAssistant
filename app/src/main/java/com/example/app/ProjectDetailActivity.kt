package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.app.ui.theme.AppTheme

data class ProjectFeature(
    val title: String,
    val icon: ImageVector,
    val route: String
)

class ProjectDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val projectName = intent.getStringExtra("project_name") ?: "未知项目"
        Log.d("ProjectDetail", "Activity onCreate with project: $projectName")
        setContent {
            Log.d("ProjectDetail", "Setting content")
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F0)  // 米白色背景
                ) {
                    ProjectDetailScreen(projectName)
                }
            }
        }
    }
}

@Composable
fun ProjectDetailScreen(projectName: String) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "project_detail") {
        composable("project_detail") {
            ProjectDetailContent(projectName)
        }
        // 这里可以添加其他功能页面的路由
    }
}

@Composable
fun ProjectDetailContent(projectName: String) {
    val context = LocalContext.current
    
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
                text = projectName,
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { /* TODO: 添加更多功能 */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "更多")
            }
        }

        // 功能按钮网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FunctionButton(
                    icon = Icons.Default.Timeline,
                    text = "进度调整",
                    onClick = {
                        Log.d("ProjectDetail", "Navigating to ProgressAdjustmentActivity")
                        val intent = Intent(context, ProgressAdjustmentActivity::class.java).apply {
                            putExtra("project_name", projectName)
                        }
                        context.startActivity(intent)
                    }
                )
            }
            item {
                FunctionButton(
                    icon = Icons.Default.Person,
                    text = "员工管理",
                    onClick = {
                        Log.d("ProjectDetail", "Navigating to EmployeeManagementActivity")
                        val intent = Intent(context, EmployeeManagementActivity::class.java).apply {
                            putExtra("project_name", projectName)
                        }
                        context.startActivity(intent)
                    }
                )
            }
            item {
                FunctionButton(
                    icon = Icons.Default.Folder,
                    text = "文件管理",
                    onClick = {
                        val intent = Intent(context, FileManagementActivity::class.java).apply {
                            putExtra("project_name", projectName)
                        }
                        context.startActivity(intent)
                    }
                )
            }
            item {
                FunctionButton(
                    icon = Icons.Default.Pending,
                    text = "待批事项",
                    onClick = {
                        val intent = Intent(context, PendingItemsActivity::class.java).apply {
                            putExtra("project_name", projectName)
                        }
                        context.startActivity(intent)
                    }
                )
            }
            item {
                FunctionButton(
                    icon = Icons.Default.CheckCircle,
                    text = "已批事项",
                    onClick = {
                        val intent = Intent(context, ApprovedItemsActivity::class.java).apply {
                            putExtra("project_name", projectName)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun FunctionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}

@Composable
fun FeatureCard(
    feature: ProjectFeature,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        onClick = {
            when (feature.route) {
                "progress" -> {
                    val intent = Intent(context, ProgressAdjustmentActivity::class.java).apply {
                        putExtra("project_name", feature.title)
                    }
                    context.startActivity(intent)
                }
                "pending" -> {
                    val intent = Intent(context, PendingItemsActivity::class.java).apply {
                        putExtra("project_name", feature.title)
                    }
                    context.startActivity(intent)
                }
                "approved" -> {
                    val intent = Intent(context, ApprovedItemsActivity::class.java).apply {
                        putExtra("project_name", feature.title)
                    }
                    context.startActivity(intent)
                }
                else -> onClick()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectDetailScreenPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            ProjectDetailScreen(projectName = "Sample Project")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectDetailContentPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            val navController = rememberNavController()
            ProjectDetailContent(projectName = "Sample Project")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeatureCardPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F0)  // 米白色背景
        ) {
            val sampleFeature = ProjectFeature(
                title = "Sample Feature",
                icon = Icons.Default.Star,
                route = "sample_route"
            )
            FeatureCard(feature = sampleFeature, onClick = { /* Do nothing */ })
        }
    }
}

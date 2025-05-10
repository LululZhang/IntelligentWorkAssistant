# 智工管家工程管理平台

这是一个基于 Flask 的工程项目管理系统，提供项目进度管理、任务分配等功能。

## 功能特点

- 用户登录和认证
- 项目选择和概览
- 项目进度管理（甘特图）
- 任务创建和编辑
- 资源分配
- 项目文件管理

## 运行步骤

1. 安装依赖：
```bash
pip install -r requirements.txt
```

2. 初始化数据库：
系统会自动创建数据库并添加默认管理员账户

3. 运行应用：
```bash
python app.py
```

4. 访问应用：
打开浏览器访问 http://localhost:5000

## 默认管理员账户
- 用户名：admin
- 密码：admin123

## 技术栈

- 后端：Flask + SQLAlchemy
- 前端：Bootstrap 5 + DHTMLX Gantt
- 数据库：SQLite

## 项目结构

```
├── app.py              # 主应用文件
├── requirements.txt    # 项目依赖
├── static/            # 静态资源
├── templates/         # HTML模板
└── database.db        # SQLite数据库
``` 
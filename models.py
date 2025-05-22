from flask_sqlalchemy import SQLAlchemy
from flask_login import UserMixin
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime

db = SQLAlchemy()

class User(db.Model, UserMixin):
    """User model for authentication and task management."""
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(128))
    project_permissions = db.Column(db.String(500), default='')  # 存储用户有权限访问的项目列表，用逗号分隔
    tasks = db.relationship('Task', backref='user', lazy=True)

    def set_password(self, password):
        """Set the user's password hash."""
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        """Check if the provided password matches the hash."""
        return check_password_hash(self.password_hash, password)

    def has_project_permission(self, project_name):
        """检查用户是否有权限访问指定项目"""
        if not self.project_permissions:
            return False
        return project_name in self.project_permissions.split(',')

    def __repr__(self):
        return f'<User {self.username}>'

class Task(db.Model):
    """Task model for todo items."""
    id = db.Column(db.Integer, primary_key=True)
    text = db.Column(db.String(200), nullable=False)
    start_date = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    duration = db.Column(db.Integer, default=1)  # Duration in days
    progress = db.Column(db.Float, default=0)    # Progress percentage (0-1)
    parent = db.Column(db.Integer, default=0)    # Parent task ID for hierarchical structure
    resource = db.Column(db.String(100))         # Resource assigned to the task
    dependencies = db.Column(db.String(200))     # Comma-separated list of dependent task IDs
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    created_at = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)

    def to_dict(self):
        return {
            'id': self.id,
            'text': self.text,
            'start_date': self.start_date.strftime("%Y-%m-%d %H:%M"),
            'duration': self.duration,
            'progress': self.progress,
            'parent': self.parent,
            'resource': self.resource,
            'dependencies': self.dependencies,
            'user_id': self.user_id,
            'created_at': self.created_at.strftime("%Y-%m-%d %H:%M")
        }

    def __repr__(self):
        return f'<Task {self.text}>' 
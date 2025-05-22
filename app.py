from flask import Flask, render_template, request, redirect, url_for, flash, jsonify
from flask_login import LoginManager, login_user, login_required, logout_user, current_user
import os
from datetime import datetime, timedelta
from models import db, User, Task
import pandas as pd

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key'  # 用于会话安全
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///database.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db.init_app(app)
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'

def import_staff_permissions():
    """从Excel文件导入员工权限"""
    try:
        # 读取Excel文件
        df = pd.read_excel('Staff_list.xlsx')
        
        # 遍历每一行数据
        for _, row in df.iterrows():
            username = str(row['账号'])  # 假设Excel中有"账号"列
            password = str(row['密码'])  # 假设Excel中有"密码"列
            
            # 查找或创建用户
            user = User.query.filter_by(username=username).first()
            if not user:
                user = User(username=username, email=f"{username}@example.com")
                user.set_password(password)
            
            # 添加国际金融中心项目权限
            if not user.project_permissions:
                user.project_permissions = '国际金融中心'
            elif '国际金融中心' not in user.project_permissions:
                user.project_permissions += ',国际金融中心'
            
            db.session.add(user)
        
        db.session.commit()
        print("成功导入员工权限")
    except Exception as e:
        print(f"导入员工权限时出错: {str(e)}")
        db.session.rollback()

# 错误处理中间件
@app.errorhandler(404)
def not_found_error(error):
    return jsonify({'success': False, 'error': 'Resource not found'}), 404

@app.errorhandler(500)
def internal_error(error):
    db.session.rollback()
    return jsonify({'success': False, 'error': 'Internal server error'}), 500

@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))

@app.route('/')
def index():
    return redirect(url_for('login'))

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        remember = request.form.get('remember') == 'yes'
        
        user = User.query.filter_by(username=username).first()
        
        if user and user.check_password(password):
            login_user(user, remember=remember)
            return redirect(url_for('project_selection'))
        else:
            flash('用户名或密码错误')
    
    return render_template('login.html')

@app.route('/project_selection')
@login_required
def project_selection():
    # 定义所有项目及其权限要求
    projects = {
        '国际金融中心': '国际金融中心',
        '环球贸易广场UTC': 'UTC',
        '云端大厦': '云端大厦',
        '绿洲商务中心': '绿洲商务中心',
        '万象城': '万象城',
        '星光天地购物广场': '星光天地',
        '滨江壹号院': '滨江壹号院',
        '翡翠湖畔别墅区': '翡翠湖畔',
        '市立图书馆新馆': '图书馆',
        '科技馆穹顶计划': '科技馆'
    }
    
    # 检查用户对每个项目的权限
    project_permissions = {}
    for project, permission in projects.items():
        project_permissions[project] = current_user.has_project_permission(permission)
    
    return render_template('project_selection.html', project_permissions=project_permissions)

@app.route('/project_overview')
@login_required
def project_overview():
    # 检查用户是否有权限访问国际金融中心项目
    if not current_user.has_project_permission('国际金融中心'):
        flash('您没有权限访问该项目')
        return redirect(url_for('project_selection'))
    return render_template('project_overview.html')

@app.route('/project_work')
@login_required
def project_work():
    return render_template('project_work.html')

@app.route('/project_progress')
@login_required
def project_progress():
    return render_template('project_progress.html')

@app.route('/api/tasks', methods=['GET'])
@login_required
def get_tasks():
    tasks = Task.query.all()
    return jsonify([task.to_dict() for task in tasks])

@app.route('/api/task/<int:task_id>', methods=['GET'])
@login_required
def get_task(task_id):
    task = Task.query.get_or_404(task_id)
    return jsonify(task.to_dict())

@app.route('/api/tasks', methods=['POST'])
@login_required
def create_task():
    data = request.json
    try:
        new_task = Task(
            text=data['text'],
            start_date=datetime.strptime(data['start_date'], "%Y-%m-%d %H:%M"),
            duration=int(data['duration']),
            progress=float(data.get('progress', 0)),
            parent=int(data.get('parent', 0)) if data.get('parent') else None,
            resource=data.get('resource'),
            dependencies=data.get('dependencies', ''),
            user_id=current_user.id
        )
        db.session.add(new_task)
        db.session.commit()
        return jsonify({'success': True, 'task': new_task.to_dict()})
    except Exception as e:
        db.session.rollback()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/api/task/<int:task_id>', methods=['PUT'])
@login_required
def update_task(task_id):
    task = Task.query.get_or_404(task_id)
    data = request.json
    
    try:
        if 'progress' in data:
            task.progress = float(data['progress'])
        if 'start_date' in data:
            task.start_date = datetime.strptime(data['start_date'], "%Y-%m-%d %H:%M")
        if 'duration' in data:
            task.duration = int(data['duration'])
        if 'text' in data:
            task.text = data['text']
        if 'resource' in data:
            task.resource = data['resource']
        if 'dependencies' in data:
            task.dependencies = data['dependencies']
        if 'parent' in data:
            task.parent = int(data['parent']) if data['parent'] else None
        
        db.session.commit()
        return jsonify({'success': True, 'task': task.to_dict()})
    except Exception as e:
        db.session.rollback()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/api/task/<int:task_id>', methods=['DELETE'])
@login_required
def delete_task(task_id):
    task = Task.query.get_or_404(task_id)
    try:
        db.session.delete(task)
        db.session.commit()
        return jsonify({'success': True})
    except Exception as e:
        db.session.rollback()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/api/tasks/batch', methods=['PUT'])
@login_required
def batch_update_tasks():
    data = request.json
    try:
        for task_data in data:
            task = Task.query.get(task_data['id'])
            if task:
                if 'progress' in task_data:
                    task.progress = float(task_data['progress'])
                if 'start_date' in task_data:
                    task.start_date = datetime.strptime(task_data['start_date'], "%Y-%m-%d %H:%M")
                if 'duration' in task_data:
                    task.duration = int(task_data['duration'])
                if 'text' in task_data:
                    task.text = task_data['text']
                if 'resource' in task_data:
                    task.resource = task_data['resource']
                if 'dependencies' in task_data:
                    task.dependencies = task_data['dependencies']
                if 'parent' in task_data:
                    task.parent = int(task_data['parent']) if task_data['parent'] else None
        
        db.session.commit()
        return jsonify({'success': True})
    except Exception as e:
        db.session.rollback()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('login'))

if __name__ == '__main__':
    with app.app_context():

        
        # 创建默认管理员账户
        if not User.query.filter_by(username='admin').first():
            admin = User(username='admin', email='admin@example.com')
            admin.set_password('admin123')
            admin.project_permissions = '国际金融中心'  # 给管理员添加国际金融中心项目权限
            db.session.add(admin)
            db.session.commit()
            print("已创建默认管理员账户")
        
        # 导入员工权限
        import_staff_permissions()
        
    app.run(host='0.0.0.0', port=5000, debug=True, use_reloader=False) 
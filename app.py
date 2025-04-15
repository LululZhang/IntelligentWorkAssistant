from flask import Flask, render_template, request, redirect, url_for, flash, jsonify
from flask_login import LoginManager, login_user, login_required, logout_user, current_user
import os
from datetime import datetime, timedelta
from models import db, User, Task

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key'  # 用于会话安全
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///database.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db.init_app(app)
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'

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
    return render_template('project_selection.html')

@app.route('/project_overview')
@login_required
def project_overview():
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
        # 删除所有表并重新创建
        db.drop_all()
        db.create_all()
        
        # 创建默认管理员账户
        if not User.query.filter_by(username='admin').first():
            admin = User(username='admin', email='admin@example.com')
            admin.set_password('admin123')
            db.session.add(admin)
            db.session.commit()
            print("已创建默认管理员账户")
    app.run(host='0.0.0.0', port=5000, debug=True, use_reloader=False) 
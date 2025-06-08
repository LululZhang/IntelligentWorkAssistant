from flask import Flask, render_template, request, redirect, url_for, flash, jsonify, send_file, Response
from flask_login import LoginManager, login_user, login_required, logout_user, current_user, UserMixin
import os
import mysql.connector
from datetime import datetime, timedelta
import pandas as pd
from modes import authenticate_user, allowed_file, save_file_info_to_db, get_all_files, delete_file_from_db, get_file_path
import io
import csv

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key'  # 用于会话安全

app.config['UPLOAD_FOLDER'] = os.path.join('D:', 'data', 'file')  # 文件上传目录
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 限制上传文件大小为16MB

# MySQL数据库配置
db_config = {
    'host': 'localhost',
    'port': '3306',  # 替换为你的数据库名
    'user': 'root',  # 替换为你的数据库用户名
    'password': '1314520maobo@L',  # 替换为你的数据库密码
    'database': "users"
}

# 确保上传目录存在
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

# 用户类
class User(UserMixin):
    def __init__(self, user_data):
        self.id = user_data['UserID']
        self.username = user_data['Username']
        self.email = user_data['Email']
        self.project_permissions = user_data.get('Project_Permissions')

    def has_project_permission(self, project_name):
        """检查用户是否有权限访问指定项目"""
        if not self.project_permissions:
            return False
        permissions_str = str(self.project_permissions)
        return project_name in permissions_str.split(',')

login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'

@login_manager.user_loader
def load_user(user_id):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        cursor.execute('SELECT * FROM User WHERE UserID = %s', (user_id,))
        user_data = cursor.fetchone()
        cursor.close()
        conn.close()
        if user_data:
            return User(user_data)
        return None
    except Exception as e:
        print(f"Error loading user: {str(e)}")
        return None


# 错误处理中间件
@app.errorhandler(404)
def not_found_error(error):
    return jsonify({'success': False, 'error': 'Resource not found'}), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({'success': False, 'error': 'Internal server error'}), 500



@app.route('/')
def index():
    return redirect(url_for('login'))

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        
        result = authenticate_user(username, password)
        if result[0] == True:
            # 获取用户对象并登录
            try:
                conn = mysql.connector.connect(**db_config)
                cursor = conn.cursor(dictionary=True)
                cursor.execute('SELECT * FROM User WHERE Username = %s', (username,))
                user_data = cursor.fetchone()
                # print(user_data)
                cursor.close()
                conn.close()
                
                if user_data:
                    user = User(user_data)
                    login_user(user)
                    return redirect(url_for('project_selection'))
            except Exception as e:
                flash('登录过程中出现错误')
                print(f"Login error: {str(e)}")
        else:
            flash('用户名或密码错误')
    
    return render_template('login.html')

@app.route('/project_selection')
@login_required
def project_selection():
    # 定义所有项目及其权限要求
    projects = {
        '国际金融中心': '1000000000',
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
    if not current_user.has_project_permission('1000000000'):
        flash('您没有权限访问该项目')
        return redirect(url_for('project_selection'))
    return render_template('project_overview.html')

@app.route('/project_work')
@login_required
def project_work():
    return render_template('project_work.html')

@app.route('/smart_dashboard')
@login_required
def smart_dashboard():
    return render_template('MapBox-lujiazui_dashboard .html')

@app.route('/text')
@login_required
def text():
    return render_template('file_manager.html')

@app.route('/upload/files', methods=['POST'])
@login_required
def upload_files():
    """处理多文件上传"""
    if 'files[]' not in request.files:
        return jsonify({'success': False, 'error': '没有选择文件'})

    files = request.files.getlist('files[]')
    if not files or files[0].filename == '':
        return jsonify({'success': False, 'error': '没有选择文件'})

    uploaded_files = []
    errors = []

    for file in files:
        if file and allowed_file(file.filename):
            try:
                # 生成唯一文件名
                filename = f"{datetime.utcnow().timestamp()}_{file.filename}"
                file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
                
                # 保存文件
                file.save(file_path)
                
                # 保存到数据库
                if save_file_info_to_db(filename, file.content_type, file_path):
                    uploaded_files.append({
                        'filename': filename,
                        'original_name': file.filename,
                        'content_type': file.content_type
                    })
                else:
                    errors.append(f'文件 {file.filename} 保存到数据库失败')
                    # 如果数据库保存失败，删除已上传的文件
                    if os.path.exists(file_path):
                        os.remove(file_path)
            except Exception as e:
                errors.append(f'文件 {file.filename} 上传失败: {str(e)}')
        else:
            errors.append(f'文件 {file.filename} 类型不允许')

    if uploaded_files:
        response = {
            'success': True,
            'message': f'成功上传 {len(uploaded_files)} 个文件',
            'uploaded_files': uploaded_files
        }
        if errors:
            response['warnings'] = errors
        return jsonify(response)
    else:
        return jsonify({
            'success': False,
            'error': '所有文件上传失败',
            'errors': errors
        })

@app.route('/files')
@login_required
def list_files():
    """获取文件列表"""
    files = get_all_files()
    return jsonify({'success': True, 'files': files})

@app.route('/download/<filename>')
@login_required
def download_file(filename):
    """下载文件"""
    try:
        file_path = get_file_path(filename)
        if file_path and os.path.exists(file_path):
            return send_file(file_path, as_attachment=True)
        else:
            return jsonify({'success': False, 'error': '文件不存在'}), 404
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/delete/<filename>', methods=['DELETE'])
@login_required
def delete_file_route(filename):
    """删除文件"""
    try:
        file_path = get_file_path(filename)
        if file_path and os.path.exists(file_path):
            # 从数据库中删除记录
            if delete_file_from_db(filename):
                # 删除物理文件
                os.remove(file_path)
                return jsonify({'success': True, 'message': '文件删除成功'})
            else:
                return jsonify({'success': False, 'error': '从数据库删除文件记录失败'}), 500
        else:
            return jsonify({'success': False, 'error': '文件不存在'}), 404
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/project_progress')
@login_required
def project_progress():
    return render_template('project_progress.html')

@app.route('/pending_approvals')
@login_required
def pending_approvals():
    return render_template('pending_approvals.html')

@app.route('/approved_approvals')
@login_required
def approved_approvals():
    return render_template('approved_approvals.html')

@app.route('/api/tasks', methods=['GET'])
@login_required
def get_tasks():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        # 获取所有任务
        cursor.execute("""
            SELECT 
                TaskID as id,
                TaskName as text,
                DATE_FORMAT(CreateTime, '%Y-%m-%d %H:%i') as start_date,
                DATEDIFF(Deadline, CreateTime) as duration,
                CompletionPercentage as progress,
                TaskDescription as resource,
                Status as status
            FROM Task
            WHERE ProjectID = %s
        """, (1000000000,))  # 这里的project_id先hardcode为1，后续可以根据实际需求修改
        
        tasks = cursor.fetchall()
        
        # 处理数据格式
        for task in tasks:
            task['progress'] = float(task['progress']) if task['progress'] else 0
            task['duration'] = int(task['duration']) if task['duration'] else 1
            
        cursor.close()
        conn.close()
        return jsonify(tasks)
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/api/task/<int:task_id>', methods=['GET'])
@login_required
def get_task(task_id):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT task_id as id, task_name as text, 
                   DATE_FORMAT(create_time, '%Y-%m-%d %H:%i') as start_date,
                   DATEDIFF(deadline, create_time) as duration,
                   completion_percentage as progress,
                   responsible_user_id as resource,
                   status
            FROM Task
            WHERE task_id = %s
        """, (task_id,))
        
        task = cursor.fetchone()
        if task:
            task['progress'] = float(task['progress']) if task['progress'] else 0
            task['duration'] = int(task['duration']) if task['duration'] else 1
            
        cursor.close()
        conn.close()
        
        if task:
            return jsonify(task)
        return jsonify({'success': False, 'error': 'Task not found'}), 404
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/api/tasks', methods=['POST'])
@login_required
def create_task():
    try:
        data = request.json
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        # 计算截止时间
        start_date = datetime.strptime(data['start_date'], "%Y-%m-%d %H:%M")
        deadline = start_date + timedelta(days=int(data['duration']))
        
        # 插入新任务
        cursor.execute("""
            INSERT INTO Task (
                ProjectID,
                TaskName,
                TaskDescription,
                CreateTime,
                AssignerID,
                ResponsiblePersonID,
                CompletionPercentage,
                Deadline,
                Status
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
        """, (
            1000000000,  # ProjectID (默认为1000000000)
            data['text'],  # TaskName
            data.get('resource', ''),  # TaskDescription (包含原始resource信息)
            start_date,  # CreateTime
            2400001,  # AssignerID (默认为2400001)
            2400001,  # ResponsiblePersonID (默认为2400001)
            min(9.99, max(0, float(data.get('progress', 0)))),  # CompletionPercentage
            deadline,  # Deadline
            '进行中'  # Status
        ))
        
        task_id = cursor.lastrowid
        conn.commit()
        
        # 获取新创建的任务信息
        cursor.execute("""
            SELECT task_id as id, task_name as text, 
                   DATE_FORMAT(create_time, '%Y-%m-%d %H:%i') as start_date,
                   DATEDIFF(deadline, create_time) as duration,
                   completion_percentage as progress,
                   responsible_user_id as resource,
                   status
            FROM Task
            WHERE task_id = %s
        """, (task_id,))
        
        new_task = cursor.fetchone()
        cursor.close()
        conn.close()
        
        return jsonify({'success': True, 'task': new_task})
    except Exception as e:
        if 'conn' in locals():
            conn.rollback()
            conn.close()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/api/task/<int:task_id>', methods=['PUT'])
@login_required
def update_task(task_id):
    try:
        data = request.json
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        update_fields = []
        update_values = []
        
        if 'progress' in data:
            update_fields.append('CompletionPercentage = %s')
            update_values.append(min(9.99, max(0, float(data['progress']))))
            
        if 'start_date' in data:
            update_fields.append('CreateTime = %s')
            start_date = datetime.strptime(data['start_date'], "%Y-%m-%d %H:%M")
            update_values.append(start_date)
            
        if 'duration' in data:
            update_fields.append('Deadline = DATE_ADD(CreateTime, INTERVAL %s DAY)')
            update_values.append(int(data['duration']))
            
        if 'text' in data:
            update_fields.append('TaskName = %s')
            update_values.append(data['text'])
            
        if 'resource' in data:
            update_fields.append('TaskDescription = %s')
            update_values.append(data['resource'])
            
        if update_fields:
            update_values.append(task_id)
            query = f"""
                UPDATE Task 
                SET {', '.join(update_fields)}
                WHERE task_id = %s
            """
            cursor.execute(query, update_values)
            conn.commit()
            
            # 获取更新后的任务信息
            cursor.execute("""
                SELECT 
                    TaskID as id,
                    TaskName as text,
                    DATE_FORMAT(CreateTime, '%Y-%m-%d %H:%i') as start_date,
                    DATEDIFF(Deadline, CreateTime) as duration,
                    CompletionPercentage as progress,
                    TaskDescription as resource,
                    Status as status
                FROM Task
                WHERE TaskID = %s
            """, (task_id,))
            
            updated_task = cursor.fetchone()
            cursor.close()
            conn.close()
            
            return jsonify({'success': True, 'task': updated_task})
    except Exception as e:
        if 'conn' in locals():
            conn.rollback()
            conn.close()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/api/task/<int:task_id>', methods=['DELETE'])
@login_required
def delete_task(task_id):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute('DELETE FROM Task WHERE TaskID = %s', (task_id,))
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({'success': True})
    except Exception as e:
        if 'conn' in locals():
            conn.rollback()
            conn.close()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/api/tasks/batch', methods=['PUT'])
@login_required
def batch_update_tasks():
    try:
        data = request.json
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        for task_data in data:
            update_fields = []
            update_values = []
            
            if 'progress' in task_data:
                update_fields.append('completion_percentage = %s')
                update_values.append(float(task_data['progress']))
                
            if 'start_date' in task_data:
                update_fields.append('create_time = %s')
                start_date = datetime.strptime(task_data['start_date'], "%Y-%m-%d %H:%M")
                update_values.append(start_date)
                
            if 'duration' in task_data:
                update_fields.append('deadline = DATE_ADD(create_time, INTERVAL %s DAY)')
                update_values.append(int(task_data['duration']))
                
            if 'text' in task_data:
                update_fields.append('task_name = %s')
                update_values.append(task_data['text'])
                
            if 'resource' in task_data:
                update_fields.append('responsible_user_id = %s')
                update_values.append(task_data['resource'])
                
            if update_fields:
                update_values.append(task_data['id'])
                query = f"""
                    UPDATE Task 
                    SET {', '.join(update_fields)}
                    WHERE TaskID = %s
                """
                cursor.execute(query, update_values)
        
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({'success': True})
    except Exception as e:
        if 'conn' in locals():
            conn.rollback()
            conn.close()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('login'))

@app.route('/api/tasks/upload', methods=['POST'])
@login_required
def upload_tasks():
    if 'file' not in request.files:
        return jsonify({'success': False, 'error': '没有选择文件'})
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({'success': False, 'error': '没有选择文件'})
    
    try:
        # 根据文件扩展名选择读取方式
        if file.filename.endswith('.csv'):
            df = pd.read_csv(file)
        elif file.filename.endswith(('.xls', '.xlsx')):
            df = pd.read_excel(file)
        else:
            return jsonify({'success': False, 'error': '不支持的文件格式'})
        
        # 验证必要的列是否存在
        required_columns = ['task_name', 'create_time', 'deadline', 'completion_percentage']
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            return jsonify({'success': False, 'error': f'缺少必要的列: {", ".join(missing_columns)}'})
        
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        # 插入任务数据
        for _, row in df.iterrows():
            cursor.execute("""
                INSERT INTO Task (project_id, task_name, task_description, create_time, 
                                assigner_id, responsible_user_id, completion_percentage, deadline, status)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            """, (
                1,  # project_id，暂时hardcode
                row['task_name'],
                row.get('task_description', ''),
                pd.to_datetime(row['create_time']),
                current_user.id,
                row.get('responsible_user_id'),
                float(row['completion_percentage']),
                pd.to_datetime(row['deadline']),
                row.get('status', '进行中')
            ))
        
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({'success': True, 'message': '任务数据上传成功'})
    except Exception as e:
        if 'conn' in locals():
            conn.rollback()
            conn.close()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/users_manager')
@login_required
def users_manager():
    return render_template('users_manager.html')

@app.route('/api/users', methods=['GET'])
@login_required
def get_users():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT UserID, Username, Role, Email, PhoneNumber, Project_Permissions 
            FROM User
        """)
        
        users = cursor.fetchall()
        cursor.close()
        conn.close()
        
        return jsonify(users)
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/api/users', methods=['POST'])
@login_required
def create_user():
    try:
        data = request.json
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        # 插入新用户
        cursor.execute("""
            INSERT INTO User (Username, PasswordHash, Role, Email, PhoneNumber, Project_Permissions)
            VALUES (%s, %s, %s, %s, %s, %s)
        """, (
            data['username'],
            data['password'],  # 注意：实际应用中应该对密码进行哈希处理
            data['role'],
            data['email'],
            data['phone'],
            data['permissions']
        ))
        
        conn.commit()
        new_user_id = cursor.lastrowid
        cursor.close()
        conn.close()
        
        return jsonify({'success': True, 'user_id': new_user_id})
    except Exception as e:
        if 'conn' in locals():
            conn.rollback()
            conn.close()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/api/users/<int:user_id>', methods=['GET'])
@login_required
def get_user(user_id):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT UserID, Username, Role, Email, PhoneNumber, Project_Permissions 
            FROM User 
            WHERE UserID = %s
        """, (user_id,))
        
        user = cursor.fetchone()
        cursor.close()
        conn.close()
        
        if user:
            return jsonify(user)
        return jsonify({'success': False, 'error': 'User not found'}), 404
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/api/users/<int:user_id>', methods=['PUT'])
@login_required
def update_user(user_id):
    try:
        data = request.json
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute("""
            UPDATE User 
            SET Username = %s, Role = %s, Email = %s, PhoneNumber = %s, Project_Permissions = %s
            WHERE UserID = %s
        """, (
            data['username'],
            data['role'],
            data['email'],
            data['phone'],
            data['permissions'],
            user_id
        ))
        
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({'success': True})
    except Exception as e:
        if 'conn' in locals():
            conn.rollback()
            conn.close()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/api/users/<int:user_id>', methods=['DELETE'])
@login_required
def delete_user(user_id):
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        
        cursor.execute('DELETE FROM User WHERE UserID = %s', (user_id,))
        conn.commit()
        cursor.close()
        conn.close()
        
        return jsonify({'success': True})
    except Exception as e:
        if 'conn' in locals():
            conn.rollback()
            conn.close()
        return jsonify({'success': False, 'error': str(e)}), 400

@app.route('/api/users/export')
@login_required
def export_users():
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT UserID, Username, Role, Email, PhoneNumber, Project_Permissions 
            FROM User
        """)
        
        users = cursor.fetchall()
        cursor.close()
        conn.close()
        
        # 创建CSV数据
        output = io.StringIO()
        writer = csv.writer(output)
        
        # 写入表头
        writer.writerow(['用户ID', '用户名', '角色', '邮箱', '电话号码', '项目权限'])
        
        # 写入数据
        for user in users:
            writer.writerow([
                user['UserID'],
                user['Username'],
                user['Role'],
                user['Email'],
                user['PhoneNumber'],
                user['Project_Permissions']
            ])
        
        # 创建响应
        output.seek(0)
        return Response(
            output.getvalue(),
            mimetype='text/csv',
            headers={'Content-Disposition': 'attachment; filename=users.csv'}
        )
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True, use_reloader=False)

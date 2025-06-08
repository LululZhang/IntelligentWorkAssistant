import mysql.connector
from mysql.connector import Error
from typing import Tuple, Optional, List, Dict

connection = None

# 数据库连接配置
db_config = {
    'host': 'localhost',
    'port': '3306',  # 替换为你的数据库名
    'user': 'root',  # 替换为你的数据库用户名
    'password': '1314520maobo@L',  # 替换为你的数据库密码
    'database': "users"
}

def authenticate_user(username: str, password: str) -> Tuple[bool, Optional[str]]:
    """
    验证用户账号密码并返回用户信息

    参数:
        username (str): 用户名
        password (str): 密码

    返回:
        Tuple[bool, Optional[dict], Optional[str]]:
            - 验证是否成功 (True/False)
            - 错误消息 (如果验证失败)
    """
    try:
        connection = mysql.connector.connect(**db_config)

        if connection.is_connected():
            cursor = connection.cursor(dictionary=True)

            # 查询
            query = """
                SELECT UserID, Username, PasswordHash 
                FROM users.user 
                WHERE Username = %s AND PasswordHash = %s
            """
            cursor.execute(query, (username, password))

            user = cursor.fetchone()

            if user:
                return True, "登录成功"
            else:
                # 验证失败
                return False, "用户名或密码错误"

    finally:
        if 'connection' in locals() and connection.is_connected():
            cursor.close()
            connection.close()


def allowed_file(filename):
    """检查文件扩展名是否允许"""
    # 配置上传文件夹和允许的文件扩展名
    ALLOWED_EXTENSIONS = {'txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif', 'doc', 'docx'}


    return '.' in filename and \
        filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


def get_document_type(filename: str) -> str:
    """
    根据文件名获取文档类型

    参数:
        filename (str): 文件名

    返回:
        str: 文档类型（符合数据库ENUM类型的值）
    """
    ext = filename.rsplit('.', 1)[1].lower() if '.' in filename else ''
    # 直接返回扩展名，因为它已经符合ENUM类型的定义
    return ext

def get_valid_project_id() -> int:
    """
    获取一个有效的项目ID
    如果没有项目，则创建一个默认项目

    返回:
        int: 有效的项目ID
    """
    try:
        connection = mysql.connector.connect(**db_config)

        if connection.is_connected():
            cursor = connection.cursor(dictionary=True)

            # 首先尝试获取第一个可用的项目
            query = "SELECT ProjectID FROM Users.project LIMIT 1"
            cursor.execute(query)
            result = cursor.fetchone()

            if result:
                return result['ProjectID']
            else:
                # 如果没有项目，创建一个默认项目
                cursor = connection.cursor()
                insert_query = """
                INSERT INTO Users.project (ProjectName, CreateTime) 
                VALUES ('默认项目', NOW())
                """
                cursor.execute(insert_query)
                connection.commit()
                return cursor.lastrowid

    except Error as e:
        print(f"数据库错误: {str(e)}")
        raise
    finally:
        if 'connection' in locals() and connection.is_connected():
            cursor.close()
            connection.close()

def save_file_info_to_db(filename: str, content_type: str, file_path: str) -> bool:
    """
    将文件信息保存到数据库

    参数:
        filename (str): 存储的文件名
        content_type (str): 文件类型
        file_path (str): 文件路径

    返回:
        bool: 是否保存成功
    """
    try:
        # 获取有效的项目ID
        project_id = get_valid_project_id()
        
        connection = mysql.connector.connect(**db_config)

        if connection.is_connected():
            cursor = connection.cursor()

            # 获取正确的文档类型
            doc_type = get_document_type(filename)

            query = """
            INSERT INTO Users.document 
                (DocumentName, DocumentType, UploadTime, FilePath, ProjectID) 
            VALUES 
                (%s, %s, NOW(), %s, %s)
            """
            cursor.execute(query, (filename, doc_type, file_path, project_id))
            connection.commit()
            return True

    except Error as e:
        print(f"数据库错误: {str(e)}")
        return False
    finally:
        if 'connection' in locals() and connection.is_connected():
            cursor.close()
            connection.close()

def get_all_files() -> List[Dict]:
    """
    获取所有文件信息

    返回:
        List[Dict]: 文件信息列表
    """
    try:
        connection = mysql.connector.connect(**db_config)

        if connection.is_connected():
            cursor = connection.cursor(dictionary=True)

            query = """
            SELECT DocumentID, ProjectID, DocumentName as filename,
                   DocumentType as content_type, UploadTime as upload_time, FilePath as file_path
            FROM Users.document
            ORDER BY UploadTime DESC
            """
            cursor.execute(query)
            files = cursor.fetchall()
            
            # 转换日期时间为字符串
            for file in files:
                if 'upload_time' in file:
                    file['upload_time'] = file['upload_time'].isoformat()
                # 使用DocumentName作为original_name（因为我们没有单独的原始文件名字段）
                file['original_name'] = file['filename'].split('_', 1)[1] if '_' in file['filename'] else file['filename']
            
            return files

    except Error as e:
        print(f"数据库错误: {str(e)}")
        return []
    finally:
        if 'connection' in locals() and connection.is_connected():
            cursor.close()
            connection.close()

def get_file_path(filename: str) -> Optional[str]:
    """
    根据文件名获取文件路径

    参数:
        filename (str): 文件名

    返回:
        Optional[str]: 文件路径，如果文件不存在则返回None
    """
    try:
        connection = mysql.connector.connect(**db_config)

        if connection.is_connected():
            cursor = connection.cursor(dictionary=True)

            query = "SELECT FilePath FROM Users.document WHERE DocumentName = %s"
            cursor.execute(query, (filename,))
            result = cursor.fetchone()
            
            return result['FilePath'] if result else None

    except Error as e:
        print(f"数据库错误: {str(e)}")
        return None
    finally:
        if 'connection' in locals() and connection.is_connected():
            cursor.close()
            connection.close()

def delete_file_from_db(filename: str) -> bool:
    """
    从数据库中删除文件记录

    参数:
        filename (str): 文件名

    返回:
        bool: 是否删除成功
    """
    try:
        connection = mysql.connector.connect(**db_config)

        if connection.is_connected():
            cursor = connection.cursor()

            query = "DELETE FROM Users.document WHERE DocumentName = %s"
            cursor.execute(query, (filename,))
            connection.commit()
            
            return cursor.rowcount > 0

    except Error as e:
        print(f"数据库错误: {str(e)}")
        return False
    finally:
        if 'connection' in locals() and connection.is_connected():
            cursor.close()
            connection.close()

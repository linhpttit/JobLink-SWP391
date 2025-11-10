"""
Database Connector - Kết nối SQL Server
"""
import os
import pyodbc
from dotenv import load_dotenv

load_dotenv()


class DatabaseConnector:
    """Kết nối và query database"""
    
    def __init__(self):
        self.server = os.getenv('DB_SERVER', 'localhost')
        self.database = os.getenv('DB_NAME', 'joblink')
        self.username = os.getenv('DB_USER', 'sa')
        self.password = os.getenv('DB_PASSWORD', '123456')
        self.driver = os.getenv('DB_DRIVER', 'ODBC Driver 17 for SQL Server')
        self.connection = None
        
    def connect(self):
        """Kết nối database"""
        try:
            connection_string = (
                f'DRIVER={{{self.driver}}};'
                f'SERVER={self.server};'
                f'DATABASE={self.database};'
                f'UID={self.username};'
                f'PWD={self.password}'
            )
            self.connection = pyodbc.connect(connection_string)
            print(f"✅ Đã kết nối database: {self.database}")
            return True
        except Exception as e:
            print(f"❌ Lỗi kết nối database: {e}")
            return False
    
    def disconnect(self):
        """Ngắt kết nối"""
        if self.connection:
            self.connection.close()
            print("Đã ngắt kết nối database")
    
    def get_all_jobs(self, status='ACTIVE'):
        """Lấy tất cả jobs"""
        if not self.connection:
            self.connect()
        
        try:
            cursor = self.connection.cursor()
            query = """
                SELECT 
                    jp.job_id,
                    jp.position,
                    jp.title,
                    jp.job_desc,
                    jp.job_requirements,
                    jp.benefits,
                    jp.salary_min,
                    jp.salary_max,
                    jp.work_type,
                    jp.year_experience,
                    jp.status,
                    c.name as category_name,
                    s.name as skill_name,
                    p.province_name as location
                FROM JobsPosting jp
                LEFT JOIN Categories c ON jp.category_id = c.category_id
                LEFT JOIN Skills s ON jp.skill_id = s.skill_id
                LEFT JOIN Provinces p ON jp.province_id = p.province_id
                WHERE jp.status = ?
                ORDER BY jp.posted_at DESC
            """
            cursor.execute(query, (status,))
            
            columns = [column[0] for column in cursor.description]
            results = []
            
            for row in cursor.fetchall():
                results.append(dict(zip(columns, row)))
            
            print(f"✅ Lấy được {len(results)} jobs")
            return results
            
        except Exception as e:
            print(f"❌ Lỗi: {e}")
            return []
    
    def get_job_by_id(self, job_id):
        """Lấy chi tiết job"""
        if not self.connection:
            self.connect()
        
        try:
            cursor = self.connection.cursor()
            query = """
                SELECT 
                    jp.*,
                    c.name as category_name,
                    s.name as skill_name,
                    p.province_name as location
                FROM JobsPosting jp
                LEFT JOIN Categories c ON jp.category_id = c.category_id
                LEFT JOIN Skills s ON jp.skill_id = s.skill_id
                LEFT JOIN Provinces p ON jp.province_id = p.province_id
                WHERE jp.job_id = ?
            """
            cursor.execute(query, (job_id,))
            
            columns = [column[0] for column in cursor.description]
            row = cursor.fetchone()
            
            if row:
                return dict(zip(columns, row))
            return None
            
        except Exception as e:
            print(f"❌ Lỗi: {e}")
            return None
    
    def get_job_statistics(self):
        """Lấy thống kê jobs"""
        if not self.connection:
            self.connect()
        
        try:
            cursor = self.connection.cursor()
            
            # Tổng số jobs active
            cursor.execute("SELECT COUNT(*) FROM JobsPosting WHERE status = 'ACTIVE'")
            total_active = cursor.fetchone()[0]
            
            # Jobs đăng hôm nay
            cursor.execute("""
                SELECT COUNT(*) 
                FROM JobsPosting 
                WHERE status = 'ACTIVE' 
                AND CAST(posted_at AS DATE) = CAST(GETDATE() AS DATE)
            """)
            today_jobs = cursor.fetchone()[0]
            
            # Jobs theo category
            cursor.execute("""
                SELECT c.name, COUNT(*) as count
                FROM JobsPosting jp
                LEFT JOIN Categories c ON jp.category_id = c.category_id
                WHERE jp.status = 'ACTIVE'
                GROUP BY c.name
                ORDER BY count DESC
            """)
            categories = []
            for row in cursor.fetchall():
                categories.append({'category': row[0], 'count': row[1]})
            
            # Jobs theo location
            cursor.execute("""
                SELECT p.province_name, COUNT(*) as count
                FROM JobsPosting jp
                LEFT JOIN Provinces p ON jp.province_id = p.province_id
                WHERE jp.status = 'ACTIVE'
                GROUP BY p.province_name
                ORDER BY count DESC
            """)
            locations = []
            for row in cursor.fetchall():
                locations.append({'location': row[0], 'count': row[1]})
            
            # Top positions
            cursor.execute("""
                SELECT TOP 5 position, COUNT(*) as count
                FROM JobsPosting
                WHERE status = 'ACTIVE'
                GROUP BY position
                ORDER BY count DESC
            """)
            positions = []
            for row in cursor.fetchall():
                positions.append({'position': row[0], 'count': row[1]})
            
            return {
                'total_active': total_active,
                'today_jobs': today_jobs,
                'categories': categories,
                'locations': locations,
                'top_positions': positions
            }
            
        except Exception as e:
            print(f"❌ Lỗi: {e}")
            return None

# 🔧 ADMIN API - QUẢN LÝ TIER USER

## 📋 **DANH SÁCH API:**

### **1. Reset Tier User (Thủ công)**
Cập nhật tier cho user bất kỳ

**Endpoint:** `POST /api/admin/payment/reset-tier`

**Request Body:**
```json
{
  "userId": 1,
  "tierLevel": 2,
  "durationDays": 30
}
```

**Parameters:**
- `userId` (required): ID của user cần cập nhật
- `tierLevel` (required): Tier level (0=Free, 1=Basic, 2=Premium, 3=Enterprise)
- `durationDays` (optional): Số ngày hết hạn (mặc định: 30)

**Response Success:**
```json
{
  "success": true,
  "message": "Đã cập nhật tier thành công",
  "data": {
    "userId": 1,
    "tierLevel": 2,
    "tierName": "Premium",
    "expiresAt": "2025-12-11T23:30:00"
  }
}
```

**Ví dụ Postman/cURL:**
```bash
curl -X POST http://localhost:8081/api/admin/payment/reset-tier \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "tierLevel": 2,
    "durationDays": 30
  }'
```

---

### **2. Xem Tier Hiện Tại**
Kiểm tra tier của user

**Endpoint:** `GET /api/admin/payment/tier/{userId}`

**Example:** `GET /api/admin/payment/tier/1`

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "tierLevel": 2,
    "tierName": "Premium",
    "subscriptionExpiresAt": "2025-12-11T23:30:00",
    "isActive": true
  }
}
```

**Ví dụ cURL:**
```bash
curl http://localhost:8081/api/admin/payment/tier/1
```

---

### **3. Reset Về Free Tier**
Nhanh chóng reset user về Free tier

**Endpoint:** `POST /api/admin/payment/reset-to-free/{userId}`

**Example:** `POST /api/admin/payment/reset-to-free/1`

**Response:**
```json
{
  "success": true,
  "message": "Đã cập nhật tier thành công",
  "data": {
    "userId": 1,
    "tierLevel": 0,
    "tierName": "Free",
    "expiresAt": "2026-11-11T23:30:00"
  }
}
```

**Ví dụ cURL:**
```bash
curl -X POST http://localhost:8081/api/admin/payment/reset-to-free/1
```

---

## 🎯 **TIER LEVELS:**

| Tier Level | Tên | Mô tả |
|------------|-----|-------|
| 0 | Free | Gói miễn phí |
| 1 | Basic | Gói cơ bản |
| 2 | Premium | Gói cao cấp |
| 3 | Enterprise | Gói doanh nghiệp |

---

## 📝 **USE CASES:**

### **Case 1: Nâng user lên Premium (30 ngày)**
```json
POST /api/admin/payment/reset-tier
{
  "userId": 5,
  "tierLevel": 2,
  "durationDays": 30
}
```

### **Case 2: Nâng user lên Enterprise (90 ngày)**
```json
POST /api/admin/payment/reset-tier
{
  "userId": 5,
  "tierLevel": 3,
  "durationDays": 90
}
```

### **Case 3: Test gói Free**
```bash
POST /api/admin/payment/reset-to-free/5
```

### **Case 4: Kiểm tra tier hiện tại**
```bash
GET /api/admin/payment/tier/5
```

---

## ⚠️ **LƯU Ý:**

1. **API này dành cho ADMIN/TESTING** - Không nên expose ra production
2. **Không cần authentication** hiện tại - Nên thêm security sau
3. **User phải có EmployerProfile** - Nếu chưa có sẽ báo lỗi
4. **Subscription expires** được tính từ thời điểm hiện tại + durationDays

---

## 🔒 **BẢO MẬT (TODO):**

Để bảo mật API này trong production, thêm:

```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/reset-tier")
public ResponseEntity<Map<String, Object>> resetUserTier(...) {
    // ...
}
```

Hoặc thêm vào SecurityConfig:
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

---

## 🧪 **TESTING:**

### **Postman Collection:**

1. **Import vào Postman:**
   - Method: POST
   - URL: `http://localhost:8081/api/admin/payment/reset-tier`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
   ```json
   {
     "userId": 1,
     "tierLevel": 3,
     "durationDays": 60
   }
   ```

2. **Test flow:**
   - Xem tier hiện tại: `GET /api/admin/payment/tier/1`
   - Nâng lên Enterprise: `POST /reset-tier` với tierLevel=3
   - Verify: `GET /api/admin/payment/tier/1`
   - Reset về Free: `POST /reset-to-free/1`

---

**Happy Testing! 🎉**

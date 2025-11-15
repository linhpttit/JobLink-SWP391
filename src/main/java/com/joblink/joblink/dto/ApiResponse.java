package com.joblink.joblink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: Tự tạo getters, setters, toString, equals, hashCode
@Builder // Lombok: Cho phép dùng .builder() để tạo đối tượng
@NoArgsConstructor // Lombok: Constructor không tham số
@AllArgsConstructor // Lombok: Constructor với tất cả tham số
public class ApiResponse<T> { // Dùng Generic <T> để kiểu dữ liệu data linh hoạt

    private boolean success; // Trạng thái thành công/thất bại
    private String message; // Thông báo
    private T data; // Dữ liệu trả về (có thể là bất kỳ kiểu gì)
    private String errorCode; // Mã lỗi (tùy chọn)

    // --- Static Factory Methods cho tiện lợi ---

    /**
     * Tạo response thành công với data và message.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Tạo response thành công chỉ với message (không có data).
     */
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .data(null)
                .build();
    }

    /**
     * Tạo response lỗi với message.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    /**
     * Tạo response lỗi với message và mã lỗi (tùy chọn).
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .data(null)
                .build();
    }

    /**
     * Tạo response lỗi với message, mã lỗi và data (tùy chọn).
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .data(data)
                .build();
    }
}
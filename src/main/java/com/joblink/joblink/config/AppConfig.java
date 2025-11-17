package com.joblink.joblink.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import vn.payos.PayOS;

@Configuration
public class AppConfig {

    @Bean
    public PayOS payOS() {
        // Thay các giá trị bên dưới bằng thông tin thật của bạn
        String clientId = "48e7127a-1620-4f70-b49a-9ee88ef8f0d9";
        String apiKey = "2d66588d-5c95-46a2-ac30-9f3ea8ac5bca";
        String checksumKey = "b8d4758d9a7c513f1e2b4dd1779e3052d558d4dfe33f197b5e207f259db7a0f2";

        return new PayOS(clientId, apiKey, checksumKey);
    }

}
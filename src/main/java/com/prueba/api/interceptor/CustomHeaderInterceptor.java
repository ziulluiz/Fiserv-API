package com.prueba.api.interceptor;

import com.prueba.api.models.Data;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

public class CustomHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        return true;
    }


    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex, Data data) {
        // Agregar los headers personalizados a la respuesta
        response.addHeader("Client-Request-Id", data.getUuid());
        response.addHeader("Api-Key", data.getApiKey());
        response.addHeader("Timestamp", data.getTimestamp());
        response.addHeader("Message-Signature", data.getMsgSignature());

    }
}

package com.jasolar.mis.framework.filter.intercepter;

import com.jasolar.mis.framework.filter.holder.AuthHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 27/07/2025 21:59
 * Version : 1.0
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpServletRequest req = (HttpServletRequest) request;
        AuthHolder.setCurrentUserInfo(req);
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        AuthHolder.release();
    }
}

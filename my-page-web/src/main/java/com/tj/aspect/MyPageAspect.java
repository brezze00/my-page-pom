package com.tj.aspect;

import com.tj.entity.BasePage;
import com.tj.uitils.MyPageUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * author tongjia
 * create_time 2023/3/7
 **/
@Aspect
public class MyPageAspect {

    //在开发者方打RestController注解的都起作用
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object pageAspect(ProceedingJoinPoint joinPoint){

        try {
            //获取请求中的参数并封装
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String pageNum = request.getParameter("pageNum");
            String pageSize = request.getParameter("pageSize");

            if (StringUtils.isEmpty(pageNum) || StringUtils.isEmpty(pageSize)) {
                return joinPoint.proceed();
            }

            //放入threadlocal
            MyPageUtils.setThreadLocal(Integer.parseInt(pageNum),Integer.parseInt(pageSize));

            //放行
            Object result = joinPoint.proceed();

            //判断结果类型
            if(result instanceof BasePage){
                //类型匹配 存入值
                ((BasePage) result).setPage(MyPageUtils.getThreadlocal());
            }
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }finally {
            //移除threadlocal
            MyPageUtils.removeThreadlocal();
        }

    }
}

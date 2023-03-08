package com.tj.config;

import com.tj.aspect.MyPageAspect;
import com.tj.intercept.MyIntercept;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * author tongjia
 * create_time 2023/3/7
 **/
@Configuration
public class AppMyPageConguration {

    //注册分页插件
    @Bean
    @ConditionalOnProperty(prefix = "mypage.page", name = "enable", havingValue = "true", matchIfMissing = true)
    public MyIntercept getMyIntercept(){
        System.out.println("我的插件被加载啦！！！");
        return new MyIntercept();
    }

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnBean(MyIntercept.class)
    @ConditionalOnProperty(prefix = "mypage.web", name = "enable", havingValue = "true", matchIfMissing = true)
    public MyPageAspect getMyPageAspect(){
        System.out.println("我的AOP 被加载拉！！！");
        return new MyPageAspect();
    }

}

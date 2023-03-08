package com.tj.uitils;

import com.tj.entity.Page;

/**
 * author tongjia
 * create_time 2023/3/7
 **/

public class MyPageUtils {
    private static ThreadLocal<Page> threadLocal=new ThreadLocal<>();

    //设置threadlocal
    public static  void setThreadLocal(Integer pageNum ,Integer pageSize){
        Page page = new Page().setPageNum(pageNum).setPageSize(pageSize);
        threadLocal.set(page);
    }

    //获取threadlocal
    public static Page getThreadlocal(){
        return threadLocal.get();
    }

    //移除threalocal
    public static void removeThreadlocal(){
        threadLocal.remove();
    }
}

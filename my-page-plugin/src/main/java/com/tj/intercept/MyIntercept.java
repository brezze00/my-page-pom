package com.tj.intercept;

import com.tj.entity.Page;
import com.tj.uitils.MyPageUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * author tongjia
 * create_time 2023/3/7
 **/
@Intercepts(
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class,Integer.class}
        )
)
public class MyIntercept implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        //获取目标对象
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        //根据threadlocal判断是否需要分页
        Page page;
        if ((page = MyPageUtils.getThreadlocal()) == null){
            return invocation.proceed();
        }

        if (page.getPageNum()==null&&page.getPageSize()==null) {
            //不需要分页
            Object proceed = invocation.proceed();
            return proceed;
        }
        if (page.getPageNum()==0) {
            //不需要分页
            Object proceed = invocation.proceed();
            return proceed;
        }


        //需要分页，获取sql
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql().trim().replaceAll("/r/n", " ");

        //通过select 和limit 拦截sql
        if (!sql.startsWith("select")) {
            return invocation.proceed();
        }
        if (sql.lastIndexOf("limit")!=-1) {
            return invocation.proceed();
        }

        //查询总条数
        int pageSum = getCount(sql, invocation);

        //最大页码
        int pageMax=  pageSum % page.getPageSize()==0 ?(pageSum/page.getPageSize() ):(pageSum/page.getPageSize()+1);
        page.setPageMix(pageMax);

        //去掉原来sql的“；”
        sql.replace(";","");

        //改造原来的sql
        String lastSql=sql+" limit " +(page.getPageNum()-1)*page.getPageSize()+","+page.getPageSize();

        //通过反射去拿到原来对象的sql，并更改它
        MetaObject metaObject = SystemMetaObject.forObject(boundSql);
        metaObject.setValue("sql",lastSql);

        //放行并执行该方法
        return invocation.proceed();
    }

    private int getCount(String sql, Invocation invocation) {
        //获取目标对象,并拿到目标对象方法的形参列表中的连接
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        Connection connection = (Connection) invocation.getArgs()[0];

        //改造sql并执行
        String newSql = "select count(*) as pageSum from (" + sql + ") as `table`";
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            //获取预编译对象
            ps = connection.prepareStatement(newSql);
            //进行参数化设置，利用原来的statementHandle对象
            statementHandler.parameterize(ps);
            //执行sql
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("pageSum");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return 0;

    }
}

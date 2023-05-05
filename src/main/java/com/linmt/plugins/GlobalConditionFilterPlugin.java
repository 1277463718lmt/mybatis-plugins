package com.linmt.plugins;

import com.alibaba.druid.util.JdbcConstants;
import com.linmt.plugins.core.DruidSql;
import com.linmt.plugins.core.MybatisPluginHelper;
import com.linmt.plugins.core.WhereConditionAppender;
import com.linmt.plugins.handle.GlobalConditionFilterHandler;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Description: 全局条件过滤插件
 * 支持多表，union all,子查询等复杂SQL
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class GlobalConditionFilterPlugin implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(GlobalConditionFilterPlugin.class);
    private GlobalConditionFilterHandler globalConditionFilterHandler;

    public GlobalConditionFilterPlugin(GlobalConditionFilterHandler globalConditionFilterHandler) {
        this.globalConditionFilterHandler = globalConditionFilterHandler;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();

        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql originBoundSql = mappedStatement.getBoundSql(parameter);

        // 获取原始sql
        String sql = originBoundSql.getSql();
        log.info("原始sql：\n{}", sql);

        // 添加过滤条件转换新的sql
        DruidSql druidSql = new DruidSql(sql, JdbcConstants.MYSQL);
        druidSql.appendFilterCondition(
                new WhereConditionAppender(
                        globalConditionFilterHandler.getColumn(),
                        globalConditionFilterHandler.getColumnValue(),
                        (tableName) -> globalConditionFilterHandler.support(tableName)
                )
        );
        sql = druidSql.getSql();
        log.info("转换sql：\n{}", sql);

        MappedStatement newMappedStatement = MybatisPluginHelper.newMappedStatement(
                mappedStatement,
                MybatisPluginHelper.newBoundSql(mappedStatement, originBoundSql, sql)
        );
        args[0] = newMappedStatement;
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}

package com.linmt.plugins.core;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;

public class MybatisPluginHelper {

    /**
     * 使用sql创建新的BoundSql对象
     * @param mappedStatement
     * @param originBoundSql
     * @param newSql
     * @return
     */
    public static BoundSql newBoundSql(MappedStatement mappedStatement, BoundSql originBoundSql, String newSql) {
        BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), newSql, originBoundSql.getParameterMappings(), originBoundSql.getParameterObject());
        for (ParameterMapping mapping : originBoundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (originBoundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, originBoundSql.getAdditionalParameter(prop));
            }
        }

        return newBoundSql;
    }

    /**
     * 新创建一个MappedStatement
     *
     * @param mappedStatement 旧MappedStatement
     * @param newBoundSql
     * @return
     */
    public static MappedStatement newMappedStatement(MappedStatement mappedStatement, BoundSql newBoundSql) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
                mappedStatement.getConfiguration(),
                mappedStatement.getId(),
                (v) -> newBoundSql,
                mappedStatement.getSqlCommandType()
        );
        builder.resource(mappedStatement.getResource());
        builder.fetchSize(mappedStatement.getFetchSize());
        builder.statementType(mappedStatement.getStatementType());
        builder.keyGenerator(mappedStatement.getKeyGenerator());
        if (mappedStatement.getKeyProperties() != null && mappedStatement.getKeyProperties().length > 0) {
            builder.keyProperty(mappedStatement.getKeyProperties()[0]);
        }
        builder.timeout(mappedStatement.getTimeout());
        builder.parameterMap(mappedStatement.getParameterMap());
        builder.resultMaps(mappedStatement.getResultMaps());
        builder.resultSetType(mappedStatement.getResultSetType());
        builder.cache(mappedStatement.getCache());
        builder.flushCacheRequired(mappedStatement.isFlushCacheRequired());
        builder.useCache(mappedStatement.isUseCache());
        return builder.build();
    }
}

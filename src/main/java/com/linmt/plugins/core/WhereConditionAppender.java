package com.linmt.plugins.core;


import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;

public class WhereConditionAppender {
    private String column;
    private Object columnValue;
    private TableNameSupport tableNameSupport;

    public WhereConditionAppender(String column, Object columnValue, TableNameSupport tableNameSupport) {
        this.column = column;
        this.columnValue = columnValue;
        this.tableNameSupport = tableNameSupport;
    }

    public void appendCondition(SQLSelectQueryBlock sqlSelectQueryBlock, DruidSql.Table table, DbType dbType) {
        // 不支持则跳过
        if (!tableNameSupport.support(table.getTableName())) return;

        sqlSelectQueryBlock.addWhere(
                SQLUtils.toSQLExpr(
                        String.format(
                                "%s.%s=%s",
                                table.getAlias(),
                                this.column,
                                this.columnValue),
                        dbType
                )
        );
    }
}

package com.linmt.plugins.core;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.*;

import java.util.ArrayList;
import java.util.List;

public class DruidSql {
    private String sql;
    private DbType dbType;
    private List<SQLStatement> sqlStatements;

    public DruidSql(String sql, DbType dbType) {
        this.sql = sql;
        this.dbType = dbType;

        this.sqlStatements = SQLUtils.parseStatements(sql, dbType);
    }

    /**
     * 获取SQLStatement
     *
     * @return
     */
    public List<SQLStatement> getSqlStatements() {
        return sqlStatements;
    }

    /**
     * 获取sql
     *
     * @return
     */
    public String getSql() {
        return sql;
    }

    /**
     * 刷新sql
     */
    public void refreshSql() {
        this.sql = SQLUtils.toSQLString(this.sqlStatements, this.dbType);
    }

    /**
     * 添加过滤条件
     *
     * @param whereConditionAppender 条件添加器
     */
    public void appendFilterCondition(WhereConditionAppender whereConditionAppender) {
        for (SQLStatement sqlStatement : sqlStatements) {
            if (sqlStatement instanceof SQLSelectStatement) {
                SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
                SQLSelect select = sqlSelectStatement.getSelect();
                SQLWithSubqueryClause withSubQuery = select.getWithSubQuery();

                if (withSubQuery != null) {
                    // 存在with语句，遍历所有的with定义的语句
                    List<SQLWithSubqueryClause.Entry> entries = withSubQuery.getEntries();
                    for (SQLWithSubqueryClause.Entry entry : entries) {
                        SQLSelect subQuery = entry.getSubQuery();
                        appendFilterCondition(subQuery.getQuery(), whereConditionAppender);
                    }
                } else {
                    // 不存在with语句
                    SQLSelectQuery sqlSelectQuery = select.getQuery();
                    appendFilterCondition(sqlSelectQuery, whereConditionAppender);
                }
            }
        }

        refreshSql();
    }

    /**
     * 对查询语句添加过滤条件
     *
     * @param sqlSelectQuery
     * @param whereConditionAppender
     */
    private void appendFilterCondition(SQLSelectQuery sqlSelectQuery, WhereConditionAppender whereConditionAppender) {
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            SQLTableSource sqlTableSource = sqlSelectQueryBlock.getFrom();
            SQLExpr sqlExprWhere = sqlSelectQueryBlock.getWhere();
            // 对where语句添加过滤条件
            appendFilterCondition(sqlExprWhere, whereConditionAppender);

            // 获取所有涉及到的表
            List<Table> tables = getExprTables(sqlTableSource, whereConditionAppender);
            for (Table table : tables) {
                whereConditionAppender.appendCondition(sqlSelectQueryBlock, table, this.dbType);
            }
        }

        // 如果是union语句，则遍历所有
        if (sqlSelectQuery instanceof SQLUnionQuery) {
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            List<SQLSelectQuery> relations = sqlUnionQuery.getRelations();
            for (SQLSelectQuery query : relations) {
                appendFilterCondition(query, whereConditionAppender);
            }
        }
    }

    /**
     * 对where语句添加过滤条件
     *
     * @param whereSqlExpr
     * @param whereConditionAppender
     */
    private void appendFilterCondition(SQLExpr whereSqlExpr, WhereConditionAppender whereConditionAppender) {
        if (whereSqlExpr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) whereSqlExpr;
            SQLExpr left = sqlBinaryOpExpr.getLeft();
            SQLExpr right = sqlBinaryOpExpr.getRight();

            appendFilterCondition(left, whereConditionAppender);
            appendFilterCondition(right, whereConditionAppender);
        } else if (whereSqlExpr instanceof SQLInSubQueryExpr) {
            SQLInSubQueryExpr sqlInSubQueryExpr = (SQLInSubQueryExpr) whereSqlExpr;
            SQLSelectQuery sqlSelectQuery = sqlInSubQueryExpr.getSubQuery().getQuery();

            appendFilterCondition(sqlSelectQuery, whereConditionAppender);
        }
    }

    /**
     * 获取所有的表
     *
     * @param sqlTableSource
     * @param whereConditionAppender
     * @return
     */
    private List<Table> getExprTables(SQLTableSource sqlTableSource, WhereConditionAppender whereConditionAppender) {
        List<Table> tables = new ArrayList<>();
        if (sqlTableSource instanceof SQLJoinTableSource) {
            // 如果是join语句，则获取其左右两边的表
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) sqlTableSource;
            SQLTableSource left = sqlJoinTableSource.getLeft();
            SQLTableSource right = sqlJoinTableSource.getRight();
            tables.addAll(getExprTables(left, whereConditionAppender));
            tables.addAll(getExprTables(right, whereConditionAppender));
        } else if (sqlTableSource instanceof SQLSubqueryTableSource) {
            // 如果是子查询
            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) sqlTableSource;
            SQLSelectQuery sqlSelectQuery = sqlSubqueryTableSource.getSelect().getQuery();
            appendFilterCondition(sqlSelectQuery, whereConditionAppender);
        } else if (sqlTableSource instanceof SQLExprTableSource) {
            // 正常的表，获取其表名和别名
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
            String tableName = sqlExprTableSource.getName().getSimpleName();
            String alias = sqlExprTableSource.getAlias();

            tables.add(new Table(tableName, alias));
        }

        return tables;
    }

    protected static class Table {
        private String tableName;
        private String alias;

        public Table(String tableName, String alias) {
            this.tableName = tableName;
            this.alias = alias == null ? tableName : alias;
        }

        public String getTableName() {
            return tableName;
        }

        public String getAlias() {
            return alias;
        }
    }
}

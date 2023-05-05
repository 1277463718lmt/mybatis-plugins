package com.linmt.plugins.handle;

import java.util.Arrays;

public interface GlobalConditionFilterHandler {
    /**
     * 返回需要过滤的列
     *
     * @return
     */
    String getColumn();

    /**
     * 返回过滤的条件值
     *
     * @return
     */
    Object getColumnValue();

    /**
     * 返回包含的表名
     *
     * @return
     */
    String[] getIncludeTable();

    /**
     * 返回排除的表名
     *
     * @return
     */
    String[] getExcludeTable();

    /**
     * 判断表名是否需要过滤
     *
     * @param tableName 表名
     * @return
     */
    default boolean support(String tableName) {
        String[] includeTable = this.getIncludeTable();
        String[] excludeTable = this.getExcludeTable();

        // 存在在排除的名单中则返回false
        if (excludeTable != null && excludeTable.length > 0 && Arrays.asList(excludeTable).contains(tableName)) {
            return false;
        }

        // 不存在包含名单中则返回false
        if (includeTable != null && includeTable.length > 0 && !Arrays.asList(includeTable).contains(tableName)) {
            return false;
        }

        // 默认返回true
        return true;
    }
}

package com.linmt.config;

import com.linmt.plugins.GlobalConditionFilterPlugin;
import com.linmt.plugins.handle.GlobalConditionFilterHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPluginsConfig {

    @Bean
    public Interceptor[] interceptors() {
        return new Interceptor[]{globalConditionFilterPlugin()};
    }

    private GlobalConditionFilterPlugin globalConditionFilterPlugin() {
        return new GlobalConditionFilterPlugin(new GlobalConditionFilterHandler() {
            @Override
            public String getColumn() {
                return "project_id";
            }

            @Override
            public Object getColumnValue() {
                return 1L;
            }

            @Override
            public String[] getIncludeTable() {
                return new String[]{"t_table1", "t_table3"};
            }

            @Override
            public String[] getExcludeTable() {
                return new String[0];
            }
        });
    }
}

package net.lab1024.smartadmin.service.module.support.datascope;

import com.google.common.collect.Maps;
import net.lab1024.smartadmin.service.common.util.SmartStringUtil;
import net.lab1024.smartadmin.service.module.support.datascope.domain.dto.DataScopeSqlConfigDTO;
import net.lab1024.smartadmin.service.module.support.datascope.service.DataScopeSqlConfigService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * [ mybaits sql 拦截 ]
 *
 * @author yandanyang
 * @version 1.0
 * @company 1024lab.net
 * @copyright (c) 2019 1024lab.netInc. All rights reserved.
 * @date
 * @since JDK1.8
 */
@Intercepts({@Signature(type = org.apache.ibatis.executor.Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
@Component
public class MyBatisPlugin implements Interceptor {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql().trim();
        String id = mappedStatement.getId();
        List<String> methodStrList = SmartStringUtil.splitConvertToList(id, "\\.");
        String path = methodStrList.get(methodStrList.size() - 2) + "." + methodStrList.get(methodStrList.size() - 1);
        DataScopeSqlConfigService dataScopeSqlConfigService = this.dataScopeSqlConfigService();
        if (dataScopeSqlConfigService == null) {
            return invocation.proceed();
        }
        DataScopeSqlConfigDTO sqlConfigDTO = dataScopeSqlConfigService.getSqlConfig(path);
        if (sqlConfigDTO != null) {
            Map<String, Object> paramMap = this.getParamList(sqlConfigDTO.getParamName(), parameter);
            BoundSql newBoundSql = copyFromBoundSql(mappedStatement, boundSql, this.joinSql(originalSql, paramMap, sqlConfigDTO));
            ParameterMap map = mappedStatement.getParameterMap();
            MappedStatement newMs = copyFromMappedStatement(mappedStatement, new BoundSqlSqlSource(newBoundSql), map);
            invocation.getArgs()[0] = newMs;
        }

        Object obj = invocation.proceed();
        return obj;
    }


    private Map<String, Object> getParamList(String paramName, Object parameter) {
        Map<String, Object> paramMap = Maps.newHashMap();
        if (StringUtils.isEmpty(paramName)) {
            return paramMap;
        }
        if (parameter == null) {
            return paramMap;
        }
        if (parameter instanceof Map) {
            String[] paramNameArray = paramName.split(",");
            Map<?, ?> parameterMap = (Map) parameter;
            for (String param : paramNameArray) {
                if(parameterMap.containsKey(param)){
                    paramMap.put(param, parameterMap.get(param));
                }
            }
        }
        return paramMap;
    }

    private String joinSql(String sql, Map<String, Object> paramMap, DataScopeSqlConfigDTO sqlConfigDTO) {
        if (null == sqlConfigDTO) {
            return sql;
        }
        String appendSql = this.dataScopeSqlConfigService().getJoinSql(paramMap, sqlConfigDTO);
        if (StringUtils.isEmpty(appendSql)) {
            return sql;
        }
        Integer appendSqlWhereIndex = sqlConfigDTO.getWhereIndex();
        String where = "where";
        String order = "order by";
        String group = "group by";
        int whereIndex = StringUtils.ordinalIndexOf(sql.toLowerCase(), where, appendSqlWhereIndex + 1);
        int orderIndex = sql.toLowerCase().indexOf(order);
        int groupIndex = sql.toLowerCase().indexOf(group);
        if (whereIndex > -1) {
            String subSql = sql.substring(0, whereIndex + where.length() + 1);
            subSql = subSql + " " + appendSql + " AND " + sql.substring(whereIndex + where.length() + 1);
            return subSql;
        }

        if (groupIndex > -1) {
            String subSql = sql.substring(0, groupIndex);
            subSql = subSql + " where " + appendSql + " " + sql.substring(groupIndex);
            return subSql;
        }
        if (orderIndex > -1) {
            String subSql = sql.substring(0, orderIndex);
            subSql = subSql + " where " + appendSql + " " + sql.substring(orderIndex);
            return subSql;
        }
        sql += " where " + appendSql;
        return sql;
    }

    public DataScopeSqlConfigService dataScopeSqlConfigService() {
        return (DataScopeSqlConfigService) applicationContext.getBean("dataScopeSqlConfigService");
    }

    public class BoundSqlSqlSource implements SqlSource {

        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    /**
     * 复制MappedStatement对象
     */
    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource, ParameterMap parameterMap) {

        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(parameterMap);
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    /**
     * 复制BoundSql对象
     */
    private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql, String sql) {
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, boundSql.getParameterMappings(), boundSql.getParameterObject());
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBoundSql;
    }

    @Override
    public Object plugin(Object arg0) {
        return Plugin.wrap(arg0, this);
    }

    @Override
    public void setProperties(Properties arg0) {

    }

}
package net.lab1024.smartadmin.module.support.datascope.domain;

import lombok.Data;
import net.lab1024.smartadmin.module.support.datascope.constant.DataScopeTypeEnum;
import net.lab1024.smartadmin.module.support.datascope.constant.DataScopeWhereInTypeEnum;

/**
 * [  ]
 *
 * @author 罗伊
 * @date 2019/4/28 0028 下午 17:21
 */
@Data
public class DataScopeSqlConfig {

    /**
     * 数据范围类型
     * {@link DataScopeTypeEnum}
     */
    private DataScopeTypeEnum dataScopeType;

    /**
     * join sql 具体实现类
     */
    private Class joinSqlImplClazz;

    private String joinSql;

    private Integer whereIndex;

    private String paramName;

    /**
     * whereIn类型
     * {@link DataScopeWhereInTypeEnum}
     */
    private DataScopeWhereInTypeEnum dataScopeWhereInType;
}
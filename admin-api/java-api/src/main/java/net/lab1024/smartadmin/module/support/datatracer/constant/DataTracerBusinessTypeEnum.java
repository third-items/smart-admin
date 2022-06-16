package net.lab1024.smartadmin.module.support.datatracer.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.smartadmin.common.enumeration.BaseEnum;

/**
 * [ 数据业务类型 ]
 *
 * @author 罗伊
 */
@AllArgsConstructor
@Getter
public enum DataTracerBusinessTypeEnum implements BaseEnum {

    GOODS(1, "商品"),

    ;

    private final Integer value;

    private final String desc;
}
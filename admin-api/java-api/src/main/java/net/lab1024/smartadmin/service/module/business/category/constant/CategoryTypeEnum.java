package net.lab1024.smartadmin.service.module.business.category.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.smartadmin.service.common.enumconst.BaseEnum;

/**
 * 分类类型 枚举
 *
 * @author listen
 * @date 2021/08/05 15:26
 */
@AllArgsConstructor
@Getter
public enum CategoryTypeEnum implements BaseEnum {

    /**
     * 1 商品
     */
    GOODS(1, "商品"),

    /**
     * 2 测试分类
     */
    DEMO(2, "测试分类"),

    ;

    private final Integer value;

    private final String desc;
}

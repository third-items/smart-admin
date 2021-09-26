package net.lab1024.smartadmin.service.module.business.goods.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品 添加 DTO
 *
 * @author 胡克
 * @date 2021/8/5 14:42
 */
@Data
public class GoodsAdminVO extends GoodsBaseDTO {

    @ApiModelProperty("商品id")
    private Long goodsId;

    @ApiModelProperty("商品分类")
    private String categoryName;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
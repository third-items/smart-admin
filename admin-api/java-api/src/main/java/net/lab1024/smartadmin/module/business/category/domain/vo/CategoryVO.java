package net.lab1024.smartadmin.module.business.category.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.lab1024.smartadmin.module.business.category.domain.dto.CategoryBaseDTO;

import java.time.LocalDateTime;

/**
 * 类目 VO 类
 *
 * @author 胡克
 * @date 2021/1/20 16:24
 */
@Data
public class CategoryVO extends CategoryBaseDTO {

    @ApiModelProperty("类目id")
    private Long categoryId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
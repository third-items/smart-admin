package net.lab1024.smartadmin.module.system.role.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import net.lab1024.smartadmin.module.system.role.domain.entity.RoleDataScopeEntity;

import java.util.List;

/**
 * [  ]
 *
 * @author 罗伊
 */
@Mapper
@Component
public interface RoleDataScopeDao extends BaseMapper<RoleDataScopeEntity> {

    /**
     * 获取某个角色的设置信息
     * @param roleId
     * @return
     */
    List<RoleDataScopeEntity> listByRoleId(@Param("roleId") Long roleId);

    /**
     * 获取某批角色的所有数据范围配置信息
     * @param roleIdList
     * @return
     */
    List<RoleDataScopeEntity> listByRoleIdList(@Param("roleIdList") List<Long> roleIdList);

    /**
     * 删除某个角色的设置信息
     * @param roleId
     * @return
     */
    void deleteByRoleId(@Param("roleId") Long roleId);

}
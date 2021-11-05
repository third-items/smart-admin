/*
 * @Author: zhuoda
 * @Date: 2021-08-16 15:53:46
 * @LastEditTime: 2021-08-30 15:18:18
 * @LastEditors: zhuoda
 * @Description:
 * @FilePath: /smart-admin/src/api/system/role/role-api.ts
 */
import { getRequest, postRequest } from '/@/lib/axios';

export const roleApi = {
  /**
   * @description: 获取所有角色
   * @param {*}
   * @return {*}
   */
  queryAll: () => {
    return getRequest('role/getAll');
  },
  /**
   * @description:添加角色
   * @param {*}
   * @return {*}
   */
  addRole: (data) => {
    return postRequest('role/add', data);
  },
  /**
   * @description:更新角色
   * @param {*}
   * @return {*}
   */
  updateRole: (data) => {
    return postRequest('role/update', data);
  },
  /**
   * @description: 删除角色
   * @param {number} roleId
   * @return {*}
   */
  deleteRole: (roleId) => {
    return getRequest(`role/delete/${roleId}`);
  },
  /**
   * @description: 批量设置某角色数据范围
   * @param {DataScopeBatchSetRoleDto} data
   * @return {*}
   */
  updateDataScope: (data) => {
    return postRequest('/dataScope/batchSet', data);
  },
  /**
   * @description: 获取当前系统所配置的所有数据范围
   * @param {*}
   * @return {*}
   */
  getDataScopeList: () => {
    return getRequest('/dataScope/list');
  },
  /**
   * @description: 获取某角色所设置的数据范围
   * @param {number} roleId
   * @return {*}
   */
  getDataScopeByRoleId: (roleId) => {
    return getRequest(`/dataScope/listByRole/${roleId}`);
  },
  /**
   * @description: 获取角色成员-员工列表
   * @param {*}
   * @return {*}
   */
  queryRoleEmployee: (params) => {
    return postRequest('/role/listEmployee', params);
  },
  /**
   * @description: 从角色成员列表中移除员工
   * @param {number} employeeId
   * @param {number} roleId
   * @return {*}
   */
  deleteEmployeeRole: (employeeId, roleId) => {
    return getRequest('/role/removeEmployee?employeeId=' + employeeId + '&roleId=' + roleId);
  },
  /**
   * @description: 从角色成员列表中批量移除员工
   * @param {RoleEmployeeBatchDto} data
   * @return {*}
   */
  deleteEmployeeList: (data) => {
    return postRequest('/role/removeEmployeeList', data);
  },
  /**
   * @description: 根据角色id获取角色员工列表(无分页)
   * @param {*}
   * @return {*}
   */
  getRoleAllEmployee: (roleId) => {
    return getRequest(`role/listAllEmployee/${roleId}`);
  },
  /**
   * @description: 角色成员列表中批量添加员工
   * @param {RoleEmployeeBatchDto} data
   * @return {*}
   */
  addRoleEmployeeList: (data) => {
    return postRequest('/role/addEmployeeList', data);
  },
};
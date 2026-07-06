package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SysUserPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 DAO
 */
@Mapper
public interface ISysUserDao {

    /** 新增用户 */
    int insert(SysUserPO po);

    /** 根据ID查询用户（不含已删除） */
    SysUserPO queryById(Long id);

    /** 根据登录账号查询用户（不含已删除） */
    SysUserPO queryByAccount(String account);

    /** 根据ID更新用户 */
    int update(SysUserPO po);

    /** 根据ID逻辑删除用户 */
    int delete(Long id);
}

package com.qx32871.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qx32871.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * 数据库中的用户信息
 */
@Data
@TableName("db_account")
@AllArgsConstructor
public class AccountDTO implements BaseData {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String role;
    private Date registerTime;
    private String clients;
}

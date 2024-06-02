package com.qx32871.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qx32871.entity.BaseData;
import lombok.Data;

@Data
@TableName("db_client_ssh")
public class ClientSshDTO implements BaseData {
    @TableId
    private Integer id;
    private Integer port;
    private String username;
    private String password;
}

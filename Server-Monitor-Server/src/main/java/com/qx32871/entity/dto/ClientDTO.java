package com.qx32871.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qx32871.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@TableName("db_client")
@AllArgsConstructor
public class ClientDTO implements BaseData {
    @TableId
    private Integer id;
    private String name;
    private String token;
    private String location;
    private String node;
    private Date registerTime;
}

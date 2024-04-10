package com.qx32871.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@TableName("db_client")
@AllArgsConstructor
public class ClientDTO {
    @TableId
    private Integer id;
    private String name;
    private String token;
    private Date registerTime;
}

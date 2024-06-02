package com.qx32871.entity.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SshConnectionVO {
    private int id;
    private int port;
    @NotNull
    @Length(min = 1)
    private String username;
    @NotNull
    @Length(min = 1)
    private String password;
}

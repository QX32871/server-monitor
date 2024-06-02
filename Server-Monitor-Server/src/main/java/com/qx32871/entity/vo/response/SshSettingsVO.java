package com.qx32871.entity.vo.response;

import lombok.Data;

@Data
public class SshSettingsVO {
    private String ip;
    private int port = 22;
    private String username;
    private String password;
}

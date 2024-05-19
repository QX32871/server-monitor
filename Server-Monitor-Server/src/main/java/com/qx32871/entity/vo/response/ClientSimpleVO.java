package com.qx32871.entity.vo.response;

import lombok.Data;

@Data
public class ClientSimpleVO {
    private int id;
    private String name;
    private String location;
    private String osName;
    private String osVersion;
    private String ip;
}

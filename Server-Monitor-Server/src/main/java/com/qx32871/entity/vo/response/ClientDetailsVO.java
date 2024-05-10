package com.qx32871.entity.vo.response;

import lombok.Data;

@Data
public class ClientDetailsVO {
    private int id;
    private String name;
    private boolean online;
    private String node;
    private String location;
    private String ip;
    private String osName;
    private String cpuName;
    private String osVersion;
    private double memory;
    private int cpuCore;
    private double disk;
}

package com.qx32871.entity.vo.response;

import lombok.Data;

@Data
public class ClientPreviewVO {
    private int id;
    private boolean online;
    private String name;
    private String location;
    private String osName;
    private String osVersion;
    private String ip;
    private String cpuName;
    private int cpuCore;
    private double memory;
    private double cpuUsage;
    private double memoryUsage;
    private double networkUpload;
    private double networkDownload;
}

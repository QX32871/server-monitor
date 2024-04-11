package com.qx32871.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseDetail {
    private String osArch;
    private String osName;
    private String osVersion;
    private int osBit;
    private int cpuCore;
    private String cpuName;
    private double memory;
    private double disk;
    private String ip;
}

package com.qx32871.entity.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientDetailVO {
    @NotNull
    private String osArch;
    @NotNull
    private String osName;
    @NotNull
    private String osVersion;
    @NotNull
    private int osBit;
    @NotNull
    private String cpuName;
    @NotNull
    private int cpuCore;
    @NotNull
    private double memory;
    @NotNull
    private double disk;
    @NotNull
    private String ip;
}
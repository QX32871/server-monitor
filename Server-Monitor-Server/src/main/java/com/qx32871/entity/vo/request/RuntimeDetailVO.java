package com.qx32871.entity.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RuntimeDetailVO {
    @NotNull
    private long timesTamp;
    @NotNull
    private double cpuUsage;
    @NotNull
    private double memoryUsage;
    @NotNull
    private double diskUsage;
    @NotNull
    private double networkUpload;
    @NotNull
    private double networkDownload;
    @NotNull
    private double diskRead;
    @NotNull
    private double diskWrite;
}

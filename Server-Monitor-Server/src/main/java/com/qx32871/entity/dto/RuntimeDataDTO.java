package com.qx32871.entity.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

@Data
@Measurement(name = "runtime")
public class RuntimeDataDTO {
    @Column(tag = true)
    private int clientId;
    @Column(timestamp = true)
    private Instant timestamp;
    @Column
    private double cpuUsage;
    @Column
    private double memoryUsage;
    @Column
    private double diskUsage;
    @Column
    private double networkUpload;
    @Column
    private double networkDownload;
    @Column
    private double diskRead;
    @Column
    private double diskWrite;
}

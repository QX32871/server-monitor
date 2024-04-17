package com.qx32871.utils;

import com.qx32871.entity.BaseDetail;
import com.qx32871.entity.RuntimeDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

@Slf4j
@Component
public class MonitorUtils {

    private final SystemInfo info = new SystemInfo();

    private final Properties properties = System.getProperties();

    /**
     * 获取客户端主机基本信息
     * 系统架构，系统名称，系统版本，系统位数，CPU核数(逻辑核)，CPU名，内存大小，总硬盘大小，主要ip地址
     *
     * @return 客户端主机基本信息对象
     */
    public BaseDetail monitorBaseDetail() {
        OperatingSystem os = info.getOperatingSystem();
        HardwareAbstractionLayer hardware = info.getHardware();
        double memory = hardware.getMemory().getTotal() / 1024.0 / 1024 / 1024;
        double diskSize = Arrays.stream(File.listRoots()).mapToLong(File::getTotalSpace).sum() / 1024.0 / 1024 / 1024;
        String ip = Objects.requireNonNull(Objects.requireNonNull(this.findNetworkInterface(hardware)).getIPv4addr()[0]);

        return new BaseDetail()
                .setOsArch(properties.getProperty("os.arch"))
                .setOsName(os.getFamily())
                .setOsVersion(os.getVersionInfo().getVersion())
                .setOsBit(os.getBitness())
                .setCpuName(hardware.getProcessor().getProcessorIdentifier().getName())
                .setCpuCore(hardware.getProcessor().getLogicalProcessorCount())
                .setMemory(memory)
                .setDisk(diskSize)
                .setIp(ip);
    }

    /**
     * 获取客户端主机运行时信息，每5秒获取一次
     *
     * @return 客户端主机运行时信息对象
     */
    public RuntimeDetail monitorRuntimeDetail() {
        double statisticTime = 0.5;
        try {
            HardwareAbstractionLayer hardware = info.getHardware();
            NetworkIF networkInterface = Objects.requireNonNull(this.findNetworkInterface(hardware));
            CentralProcessor processor = hardware.getProcessor();
            double upload = networkInterface.getBytesSent();
            double download = networkInterface.getBytesRecv();
            double read = hardware.getDiskStores().stream().mapToLong(HWDiskStore::getReadBytes).sum();
            double write = hardware.getDiskStores().stream().mapToLong(HWDiskStore::getWriteBytes).sum();
            long[] ticks = processor.getSystemCpuLoadTicks();
            Thread.sleep((long) (statisticTime * 1000));

            networkInterface = Objects.requireNonNull(this.findNetworkInterface(hardware)); //重新回去一次networkInterface，用以计算
            upload = (networkInterface.getBytesSent() - upload) / statisticTime;
            download = (networkInterface.getBytesRecv() - download) / statisticTime;
            read = (hardware.getDiskStores().stream().mapToLong(HWDiskStore::getReadBytes).sum() - read) / statisticTime;
            write = (hardware.getDiskStores().stream().mapToLong(HWDiskStore::getWriteBytes).sum() - write) / statisticTime;
            double memory = (hardware.getMemory().getTotal() - hardware.getMemory().getAvailable()) / 1024.0 / 1024 / 1024; //以GB为单位
            double disk = Arrays.stream(File.listRoots())
                    .mapToLong(file -> file.getTotalSpace() - file.getFreeSpace()).sum() / 1024.0 / 1024.1024;
            return new RuntimeDetail()
                    .setCpuUsage(this.calculateCpuUsage(processor, ticks))
                    .setMemoryUsage(memory)
                    .setDiskUsage(disk)
                    .setNetworkUpload(upload / 1024)
                    .setNetworkDownload(download / 1024)
                    .setDiskRead(read / 1024 / 1024)
                    .setDiskWrite(write / 1024 / 1024)
                    .setTimesTamp(new Date().getTime());
        } catch (Exception e) {
            log.error("读取运行时数据出现问题！", e);
        }
        return null;
    }

    private double calculateCpuUsage(CentralProcessor processor, long[] prevTicks) {
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];//用户态进程中被降低优先级执行的时间
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];//硬件中断时间
        long softIrq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];//软中断时间
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];//被虚拟化环境偷取的时间
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()]; //内核态时间
        long cUser = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];   //用户态进程时间
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];//CPU等待IO时间
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()]; //CPU空闲时间
        long totalCpu = cUser + nice + cSys + idle + ioWait + irq + softIrq + steal;
        return (cSys + cUser) * 1.0 / totalCpu;
    }

    /***
     *找到主要网络接口并且获取ipv4地址
     *
     * @param hardware 硬件信息
     * @return 主要网卡
     */
    private NetworkIF findNetworkInterface(HardwareAbstractionLayer hardware) {
        try {
            for (NetworkIF networkIF : hardware.getNetworkIFs()) {
                String[] ipv4Addr = networkIF.getIPv4addr();
                NetworkInterface ni = networkIF.queryNetworkInterface();
                if (!ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                        && (ni.getName().startsWith("eth") || ni.getName().startsWith("en")) && ipv4Addr.length > 0) {
                    return networkIF;
                }
            }
        } catch (IOException e) {
            log.error("获取主要网络接口信息失败!", e);
        }
        return null;
    }
}

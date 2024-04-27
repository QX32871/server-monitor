package com.qx32871.task;

import com.qx32871.entity.RuntimeDetail;
import com.qx32871.utils.MonitorUtils;
import com.qx32871.utils.NetUtils;
import jakarta.annotation.Resource;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class MonitorJobBean extends QuartzJobBean {

    @Resource
    private NetUtils netUtils;

    @Resource
    private MonitorUtils monitorUtils;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        RuntimeDetail runtimeDetail = monitorUtils.monitorRuntimeDetail();
        netUtils.updateRuntimeDetails(runtimeDetail);
    }
}

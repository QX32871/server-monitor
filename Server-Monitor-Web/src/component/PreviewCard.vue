<script setup>

</script>

<template>
  <div class="instance-card">
    <div style="display: flex;justify-content: space-between">
      <div>
        <div class="name">
          <span class="flag-icon flag-icon-cn"></span>
          <span style="margin: 0 10px">服务器1</span>
          <i class="fa-solid fa-pen-to-square"></i>
        </div>
        <div class="os">
          操作系统：Windows Server 2012
        </div>

      </div>

      <div class="status">
        <i style="color: #1fa81f" class="fa-solid fa-circle-play"></i>
        <span style="margin-left: 5px">运行中</span>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>
    <!--网络信息-->
    <div class="network">
      <span style="margin-right: 10px">公网IP：192.168.23.203</span>
      <i style="color: dodgerblue" class="fa-solid fa-copy"></i>
    </div>
    <!--    硬件信息-->
    <div class="hardware">
      <i style="margin-right: 5px" class="fa-solid fa-microchip"></i>
      <span style="margin-right: 15px">2 CPU</span>
      <i style="margin-right: 5px" class="fa-solid fa-memory"></i>
      <span>4 GB</span>
    </div>

    <div class="progress">
      <span>CPU: 2.5%</span>
      <el-progress status="success" :percentage="2.5" :stroke-width="5" :show-text="false"/>
    </div>
    <div class="progress">
      <span>内存: 1.2 GB</span>
      <el-progress status="success" :percentage="1.2 / 4 *100" :stroke-width="5" :show-text="false"/>
    </div>
    <div class="network-flow">
      <div>网络流量</div>
      <div>
        <i class="fa-solid fa-arrow-up"></i>
        <span>52 KB/s</span>
        <el-divider direction="vertical"/>
        <i class="fa-solid fa-arrow-down"></i>
        <span>256 KB/s</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.el-progress-bar__outer) {
  background-color: #18cb1822;
}

:deep(.el-progress-bar__inner) {
  color: #18cb18;
}

.dark .instance-card {
  color: #d9d9d9;
}

.instance-card {
  width: 330px;
  padding: 15px;
  background-color: var(--el-bg-color);
  border-radius: 5px;
  box-sizing: border-box;
  color: #383434;

  .name {
    font-size: 15px;
    font-weight: bold;
  }

  .status {
    font-size: 14px;
  }

  .os {
    font-size: 13px;
    color: grey;
  }

  .network {
    font-size: 13px;

  }

  .hardware {
    font-size: 13px;
    margin-top: 5px;
  }

  .progress {
    font-size: 12px;
    margin-top: 10px;
  }

  .network-flow {
    margin-top: 10px;
    font-size: 12px;
    display: flex;
    justify-content: space-between;
  }
}


</style>
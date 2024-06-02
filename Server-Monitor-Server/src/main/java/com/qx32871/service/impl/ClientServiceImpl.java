package com.qx32871.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qx32871.entity.dto.ClientDTO;
import com.qx32871.entity.dto.ClientDetailDTO;
import com.qx32871.entity.dto.ClientSshDTO;
import com.qx32871.entity.vo.request.*;
import com.qx32871.entity.vo.response.*;
import com.qx32871.mapper.ClientDetailMapper;
import com.qx32871.mapper.ClientMapper;
import com.qx32871.mapper.ClientSshMapper;
import com.qx32871.service.ClientService;
import com.qx32871.utils.InfluxDBUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientServiceImpl extends ServiceImpl<ClientMapper, ClientDTO> implements ClientService {

    //随机生成新token
    private String registerToken = this.generateNewToken();

    //作为客户端信息的缓存，根据客户端ID缓存
    private final Map<Integer, ClientDTO> clientIdCache = new ConcurrentHashMap<>();

    //作为客户端信息的缓存，根据客户端Token缓存
    private final Map<String, ClientDTO> clientTokenCache = new ConcurrentHashMap<>();


    @Resource
    private ClientDetailMapper clientDetailMapper;

    @Resource
    private ClientSshMapper clientSshMapper;

    @Resource
    private InfluxDBUtils influxDBUtils;

    /**
     * 初始化客户端缓存，将所有已注册的客户端都先加载进两个Map中
     */
    @PostConstruct
    public void initClientCache() {
        clientIdCache.clear();
        clientTokenCache.clear();
        this.list().forEach(this::addClientCache);
    }

    /**
     * 注册主机并验证token是否为客户端生成并且传输给客户端的token
     * 如果通过验证就将客户端信息存入数据库，包括客户端ID，初始默认主机名，token，注册日期
     *
     * @param token 客户端传过来的token
     * @return 是否注册并验证成功
     */
    @Override
    public boolean verifyAndRegister(String token) {
        if (this.registerToken.equals(token)) {
            int id = this.randomClientID();
            ClientDTO client = new ClientDTO(id, "未命名主机", token, "cn", "未命名节点", new Date());
            if (this.save(client)) {
                registerToken = this.generateNewToken();
                this.addClientCache(client);
                return true;
            }
        }
        return false;
    }

    /**
     * 生成新Token
     *
     * @return 新的Token
     */
    @Override
    public String registerToken() {
        return registerToken;
    }

    /**
     * 根据id查询客户端
     *
     * @param id 对应客户端的id
     * @return 对应客户端
     */
    @Override
    public ClientDTO findClientById(int id) {
        return clientIdCache.get(id);
    }

    /**
     * 根据Token查询客户端
     *
     * @param token 对应客户端的Token
     * @return 对应客户端
     */
    @Override
    public ClientDTO findClientByToken(String token) {
        return clientTokenCache.get(token);
    }

    /**
     * 向数据库更新客户端基本信息
     *
     * @param vo     客户端基本信息对象
     * @param client 客户端连接信息对象
     */
    @Override
    public void updateClientDetail(ClientDetailVO vo, ClientDTO client) {
        ClientDetailDTO detail = new ClientDetailDTO();
        BeanUtils.copyProperties(vo, detail);
        detail.setId(client.getId());
        if (Objects.nonNull(clientDetailMapper.selectById(client.getId()))) {
            clientDetailMapper.updateById(detail);
        } else {
            clientDetailMapper.insert(detail);
        }
    }

    //作为客户端主机运行时的缓存，根据客户端ID进行缓存，直接存储主机运行时数据对象
    private final Map<Integer, RuntimeDetailVO> currentRuntime = new ConcurrentHashMap<>();

    @Override
    public void updateRuntimeDetail(RuntimeDetailVO vo, ClientDTO client) {
        currentRuntime.put(client.getId(), vo);
        influxDBUtils.writeRuntimeData(client.getId(), vo);
    }

    /**
     * 获取实例服务器卡片上的信息
     *
     * @return 实例服务器信息对象集合
     */
    @Override
    public List<ClientPreviewVO> listClients() {
        return clientIdCache.values().stream().map(clientDTO -> {
            ClientPreviewVO vo = clientDTO.asViewObject(ClientPreviewVO.class);
            BeanUtils.copyProperties(clientDetailMapper.selectById(vo.getId()), vo);
            RuntimeDetailVO runtime = currentRuntime.get(clientDTO.getId());
            if (this.isOnline(runtime)) {
                //如果runtime为空或者runtime中的数据存在超过一分钟，那就认为客户端离线
                BeanUtils.copyProperties(runtime, vo);
                vo.setOnline(true);
            }
            return vo;
        }).toList();
    }

    /**
     * 获取简略客户端信息以供分配子用户主机页面使用
     *
     * @return 简略的客户端信息实体类
     */
    @Override
    public List<ClientSimpleVO> listSimpleList() {
        return clientIdCache.values().stream().map(clientDTO -> {
            ClientSimpleVO vo = clientDTO.asViewObject(ClientSimpleVO.class);
            BeanUtils.copyProperties(clientDetailMapper.selectById(vo.getId()), vo);
            return vo;
        }).toList();
    }

    /**
     * 重命名实例服务器名字
     *
     * @param vo 包含重命名信息实体类
     */
    @Override
    public void renameClient(RenameClientVO vo) {
        this.update(Wrappers.<ClientDTO>update().eq("id", vo.getId())
                .set("name", vo.getName()));
        this.initClientCache(); //更新客户端名后更新缓存
    }

    /**
     * 重命名实例服务器节点
     *
     * @param vo 包含重命名信息实体类
     */
    @Override
    public void renameNode(RenameNodeVO vo) {
        this.update(Wrappers.<ClientDTO>update().eq("id", vo.getId())
                .set("node", vo.getNode())
                .set("location", vo.getLocation()));
        this.initClientCache(); //更新客户端名后更新缓存
    }

    /**
     * 根据ID找出指定实例主机的基本信息
     *
     * @param clientId 指定实例主机的ID
     * @return 指定实例主机的基本信息
     */
    @Override
    public ClientDetailsVO clientDetails(int clientId) {
        ClientDetailsVO vo = this.clientIdCache.get(clientId).asViewObject(ClientDetailsVO.class);
        BeanUtils.copyProperties(clientDetailMapper.selectById(clientId), vo);
        vo.setOnline(this.isOnline(currentRuntime.get(clientId)));
        return vo;
    }

    /**
     * 获取实时运行数据
     *
     * @param clientId 目标实例主机客户端ID
     * @return 实时运行数据实体类
     */
    @Override
    public RuntimeDetailVO clientRuntimeDetailsNow(int clientId) {
        return currentRuntime.get(clientId);
    }

    /**
     * 获取历史运行时数据   (供前端图表使用)
     *
     * @param clientId 目标实例主机客户端ID
     * @return 历史运行时数据实体类
     */
    @Override
    public RuntimeHistoryVO clientRuntimeHistoryDetails(int clientId) {
        RuntimeHistoryVO vo = influxDBUtils.readRuntimeData(clientId);
        ClientDetailDTO detail = clientDetailMapper.selectById(clientId);
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }

    /**
     * 根据客户端ID删除客户端
     *
     * @param clientId 客户端ID
     */
    @Override
    public void deleteClient(int clientId) {
        this.removeById(clientId);
        clientDetailMapper.deleteById(clientId);
        this.initClientCache();
        currentRuntime.remove(clientId);
    }

    @Override
    public void saveSshClientConnection(SshConnectionVO vo) {
        ClientDTO client = clientIdCache.get(vo.getId());
        if (client == null) {
            return;
        }
        ClientSshDTO ssh = new ClientSshDTO();
        BeanUtils.copyProperties(vo, ssh);
        if (Objects.nonNull(clientSshMapper.selectById(client.getId()))) {
            clientSshMapper.updateById(ssh);
        } else {
            clientSshMapper.insert(ssh);
        }
    }

    @Override
    public SshSettingsVO sshSettings(int clientId) {
        ClientDetailDTO clientDetail = clientDetailMapper.selectById(clientId);
        ClientSshDTO ssh = clientSshMapper.selectById(clientId);
        SshSettingsVO vo;
        if (ssh == null) {
            vo = new SshSettingsVO();
        } else {
            vo = ssh.asViewObject(SshSettingsVO.class);
        }
        vo.setIp(clientDetail.getIp());
        return vo;
    }

    /**
     * 根据客户端的ID和Token分别做缓存,存在两个ConcurrentHashMap中
     *
     * @param client 查询出来的客户端对象
     */
    private void addClientCache(ClientDTO client) {
        clientIdCache.put(client.getId(), client);
        clientTokenCache.put(client.getToken(), client);
    }

    /**
     * 随机生成一个8位数字的客户端ID
     *
     * @return 生成的ID
     */
    private int randomClientID() {
        return new Random().nextInt(90000000) + 10000000;
    }

    /**
     * 从大小写英文字母和十个数字中随机生成一串24个字符长的token
     *
     * @return 生成的token
     */
    private String generateNewToken() {
        String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(24);
        for (int i = 0; i < 24; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 判断实例服务器是否在线
     *
     * @param runtime 对应实例服务器的运行时信息
     * @return 是否在线
     */
    private Boolean isOnline(RuntimeDetailVO runtime) {
        return runtime != null && System.currentTimeMillis() - runtime.getTimesTamp() < 60 * 1000;
    }
}

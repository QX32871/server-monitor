package com.qx32871.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qx32871.entity.dto.ClientDTO;
import com.qx32871.entity.dto.ClientDetailDTO;
import com.qx32871.entity.vo.request.ClientDetailVO;
import com.qx32871.entity.vo.request.RuntimeDetailVO;
import com.qx32871.mapper.ClientDetailMapper;
import com.qx32871.mapper.ClientMapper;
import com.qx32871.service.ClientService;
import com.qx32871.utils.InfluxDBUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
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
    private ClientDetailMapper detailMapper;

    @Resource
    private InfluxDBUtils dbUtils;

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
        if (Objects.nonNull(detailMapper.selectById(client.getId()))) {
            detailMapper.updateById(detail);
        } else {
            detailMapper.insert(detail);
        }
    }

    //作为客户端主机运行时的缓存，根据客户端ID进行缓存，直接存储主机运行时数据对象
    private final Map<Integer, RuntimeDetailVO> currentRuntime = new ConcurrentHashMap<>();

    @Override
    public void updateRuntimeDetail(RuntimeDetailVO vo, ClientDTO client) {
        currentRuntime.put(client.getId(), vo);
        dbUtils.writeRuntimeData(client.getId(), vo);
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
        System.out.println(sb);
        return sb.toString();
    }
}

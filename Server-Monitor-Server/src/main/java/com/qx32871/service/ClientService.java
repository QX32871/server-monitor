package com.qx32871.service;

import com.qx32871.entity.dto.ClientDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qx32871.entity.vo.request.ClientDetailVO;

public interface ClientService extends IService<ClientDTO> {
    boolean verifyAndRegister(String token);

    String registerToken();

    ClientDTO findClientById(int id);

    ClientDTO findClientByToken(String token);

    void updateClientDetail(ClientDetailVO vo, ClientDTO client);
}

package com.qx32871.service;

import com.qx32871.entity.dto.ClientDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ClientService extends IService<ClientDTO> {
    boolean verifyAndRegister(String token);

    String registerToken();

    ClientDTO findClientById(int id);

    ClientDTO findClientByToken(String token);
}

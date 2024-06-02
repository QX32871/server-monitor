package com.qx32871.service;

import com.qx32871.entity.dto.ClientDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qx32871.entity.vo.request.*;
import com.qx32871.entity.vo.response.*;

import java.util.List;

public interface ClientService extends IService<ClientDTO> {
    boolean verifyAndRegister(String token);

    String registerToken();

    ClientDTO findClientById(int id);

    ClientDTO findClientByToken(String token);

    void updateClientDetail(ClientDetailVO vo, ClientDTO client);

    void updateRuntimeDetail(RuntimeDetailVO vo, ClientDTO client);

    List<ClientPreviewVO> listClients();

    List<ClientSimpleVO> listSimpleList();

    void renameClient(RenameClientVO vo);

    void renameNode(RenameNodeVO vo);

    ClientDetailsVO clientDetails(int clientId);

    RuntimeDetailVO clientRuntimeDetailsNow(int clientId);

    RuntimeHistoryVO clientRuntimeHistoryDetails(int clientId);

    void deleteClient(int clientId);

    void saveSshClientConnection(SshConnectionVO vo);

    SshSettingsVO sshSettings(int clientId);
}

package com.qx32871.service;

import com.qx32871.entity.dto.ClientDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qx32871.entity.vo.request.ClientDetailVO;
import com.qx32871.entity.vo.request.RenameClientVO;
import com.qx32871.entity.vo.request.RenameNodeVO;
import com.qx32871.entity.vo.request.RuntimeDetailVO;
import com.qx32871.entity.vo.response.ClientDetailsVO;
import com.qx32871.entity.vo.response.ClientPreviewVO;
import com.qx32871.entity.vo.response.RuntimeHistoryVO;

import java.util.List;

public interface ClientService extends IService<ClientDTO> {
    boolean verifyAndRegister(String token);

    String registerToken();

    ClientDTO findClientById(int id);

    ClientDTO findClientByToken(String token);

    void updateClientDetail(ClientDetailVO vo, ClientDTO client);

    void updateRuntimeDetail(RuntimeDetailVO vo, ClientDTO client);

    List<ClientPreviewVO> listClients();

    void renameClient(RenameClientVO vo);

    void renameNode(RenameNodeVO vo);

    ClientDetailsVO clientDetails(int clientId);

    RuntimeDetailVO clientRuntimeDetailsNow(int clientId);

    RuntimeHistoryVO clientRuntimeHistoryDetails(int clientId);
}

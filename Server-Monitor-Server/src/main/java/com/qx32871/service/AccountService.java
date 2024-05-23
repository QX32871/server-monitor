package com.qx32871.service;

import com.qx32871.entity.dto.AccountDTO;
import com.qx32871.entity.vo.request.ConfirmResetVO;
import com.qx32871.entity.vo.request.CreateSubAccountVO;
import com.qx32871.entity.vo.request.EmailResetVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qx32871.entity.vo.request.ModifyEmailVO;
import com.qx32871.entity.vo.response.SubAccountVO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface AccountService extends IService<AccountDTO>, UserDetailsService {
    AccountDTO findAccountByNameOrEmail(String text);

    String registerEmailVerifyCode(String type, String email, String address);

    String resetEmailAccountPassword(EmailResetVO info);

    String resetConfirm(ConfirmResetVO info);

    boolean resetPassword(int id, String passwordBefore, String passwordNew);

    void createSubAccount(CreateSubAccountVO vo);

    void deleteSubAccount(int uid);

    List<SubAccountVO> listSubAccount();

    String modifyEmail(int id, ModifyEmailVO vo);
}

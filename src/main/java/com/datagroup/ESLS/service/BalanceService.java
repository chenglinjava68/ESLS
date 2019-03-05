package com.datagroup.ESLS.service;

import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.entity.Balance;
import com.datagroup.ESLS.entity.Logs;

import java.util.List;
import java.util.Optional;

public interface BalanceService extends Service{
    List<Balance> findAll();
    List<Balance> findAll(Integer page, Integer count);
    Balance saveOne(Balance balance);
    Balance findById(Long id);
    boolean deleteById(Long id);
    ResponseBean sendGetBalance(RequestBean requestBean);
    ResponseBean sendBalanceToZero(RequestBean requestBean);
    ResponseBean sendBalanceToFlay(RequestBean requestBean);
    ResponseBean sendGetBalancePower(RequestBean requestBean);
}

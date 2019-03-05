package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.dao.BalanceDao;
import com.datagroup.ESLS.entity.Balance;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.service.BalanceService;
import com.datagroup.ESLS.utils.NettyUtil;
import com.datagroup.ESLS.utils.RequestBeanUtil;
import com.datagroup.ESLS.utils.SendCommandUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("BalanceService")
public class BalanceServiceImpl extends BaseServiceImpl implements BalanceService {
    @Autowired
    private BalanceDao balanceDao;
    @Autowired
    private NettyUtil nettyUtil;
    @Override
    public List<Balance> findAll() {
        return balanceDao.findAll();
    }

    @Override
//    @Cacheable(value = RedisConstant.CACHE_LOGS)
    public List<Balance> findAll(Integer page, Integer count) {
        List<Balance> content = balanceDao.findAll(PageRequest.of(page, count, Sort.Direction.DESC, "id")).getContent();
        return content;
    }

    @Override
//    @Cacheable(value = RedisConstant.CACHE_LOGS)
    public Balance saveOne(Balance balance) {
        return balanceDao.save(balance);
    }

    @Override
//    @Cacheable(value = RedisConstant.CACHE_LOGS)
    public Balance findById(Long id) {
        Optional<Balance> balance = balanceDao.findById(id);
        if(balance.isPresent())
            return balance.get();
        return null;
    }

    @Override
    public boolean deleteById(Long id) {
        try{
            balanceDao.deleteById(id);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public ResponseBean sendGetBalance(RequestBean requestBean) {
        List<Tag> tags = RequestBeanUtil.getTagsByRequestBean(requestBean);
        ResponseBean responseBean = null ;
        if (tags.size() > 1) {
            nettyUtil.awakeFirst(tags);
            responseBean = SendCommandUtil.sendGetBalance(tags);
            nettyUtil.awakeOverLast(tags);
        } else
            responseBean = SendCommandUtil.sendGetBalance(tags);
        return responseBean;
    }

    @Override
    public ResponseBean sendBalanceToZero(RequestBean requestBean) {
        List<Tag> tags = RequestBeanUtil.getTagsByRequestBean(requestBean);
        ResponseBean responseBean = null ;
        if (tags.size() > 1) {
            nettyUtil.awakeFirst(tags);
            responseBean = SendCommandUtil.sendBalanceToZero(tags);
            nettyUtil.awakeOverLast(tags);
        } else
            responseBean = SendCommandUtil.sendBalanceToZero(tags);
        return responseBean;
    }

    @Override
    public ResponseBean sendBalanceToFlay(RequestBean requestBean) {
        List<Tag> tags = RequestBeanUtil.getTagsByRequestBean(requestBean);
        ResponseBean responseBean = null ;
        if (tags.size() > 1) {
            nettyUtil.awakeFirst(tags);
            responseBean = SendCommandUtil.sendBalanceToFlay(tags);
            nettyUtil.awakeOverLast(tags);
        } else
            responseBean = SendCommandUtil.sendBalanceToFlay(tags);
        return responseBean;
    }

    @Override
    public ResponseBean sendGetBalancePower(RequestBean requestBean) {
        List<Tag> tags = RequestBeanUtil.getTagsByRequestBean(requestBean);
        ResponseBean responseBean = null ;
        if (tags.size() > 1) {
            nettyUtil.awakeFirst(tags);
            responseBean = SendCommandUtil.sendGetBalancePower(tags);
            nettyUtil.awakeOverLast(tags);
        } else
            responseBean = SendCommandUtil.sendGetBalancePower(tags);
        return responseBean;
    }
}

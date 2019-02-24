package com.datagroup.ESLS.serviceImpl;

import com.datagroup.ESLS.common.constant.ModeConstant;
import com.datagroup.ESLS.common.constant.TableConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.cycleJob.DynamicTask;
import com.datagroup.ESLS.dao.CycleJobDao;
import com.datagroup.ESLS.dao.RouterDao;
import com.datagroup.ESLS.dao.TagDao;
import com.datagroup.ESLS.entity.CycleJob;
import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.entity.SystemVersionArgs;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.netty.command.CommandConstant;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.utils.NettyUtil;
import com.datagroup.ESLS.utils.RequestBeanUtil;
import com.datagroup.ESLS.utils.SendCommandUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service("RouterService")
public class RouterServiceImpl extends BaseServiceImpl implements RouterService {
    @Autowired
    private RouterDao routerDao;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private NettyUtil nettyUtil;
    @Autowired
    private CycleJobDao cycleJobDao;
    @Autowired
    private DynamicTask dynamicTask;

    @Override
    public List<Router> findAll() {
        return routerDao.findAll();
    }
    @Override
    public List<Router> findAll(Integer page, Integer count) {
        List<Router> content = routerDao.findAll(PageRequest.of(page, count, Sort.Direction.DESC, "id")).getContent();
        return content;
    }
    @Override
    public Router saveOne(Router router) {
        return routerDao.save(router);
    }

    @Override
    public Optional<Router> findById(Long id) {
        return routerDao.findById(id);
    }

    @Override
    public boolean deleteById(Long id) {
        try{
            List<Tag> tagList = tagDao.findByRouterId(id);
            if(tagList!=null && tagList.size()>0)
                return false;
            routerDao.deleteById(id);
            return true;
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }
    }

    @Override
    public Router findByIp(String ip) {
        return routerDao.findByIp(ip);
    }

    @Override
    public ResponseBean changeRouter(String sourceQuery, String sourceQueryString, String targetQuery, String targetQueryString) {
        Router source = (Router)findByArrtribute(TableConstant.TABLE_ROUTERS, sourceQuery, sourceQueryString, Router.class).get(0);
        Router target = (Router)findByArrtribute(TableConstant.TABLE_ROUTERS, targetQuery, targetQueryString, Router.class).get(0);
        if(source==null  || target == null)
            return new ResponseBean(0, 0);
        // 选用该样式的所有标签
        List<Tag> tags = tagDao.findByRouterId(source.getId());
        Long targetId = target.getId();
        int sum = tags.size();
        int successNumber = 0;
        // 更换标签路由器ID
        for(Tag tag : tags){
            Router router = new Router();
            router.setId(targetId);
            tag.setRouter(router);
            Tag save = tagDao.save(tag);
            if(save!=null)
                successNumber++;
        }
        return new ResponseBean(sum, successNumber);
    }

    // 路由器巡检
    @Override
    public ResponseBean routerScan(RequestBean requestBean) {
        String contentType = CommandConstant.QUERYROUTER;
        List<Router> routerList = new ArrayList<>();
        for (RequestItem items : requestBean.getItems()) {
            routerList.addAll(findByArrtribute(TableConstant.TABLE_ROUTERS, items.getQuery(), items.getQueryString(), Router.class));
        }
        ResponseBean responseBean = SendCommandUtil.sendCommandWithRouters(routerList, contentType,CommandConstant.COMMANDTYPE_ROUTER);
        return responseBean;
    }

    @Override
    public ResponseBean routerScanByCycle(RequestBean requestBean) {
        // 设置定期巡检
        CycleJob cyclejob = new CycleJob();
        // cron表达式
        cyclejob.setCron("0 0/1 * * * ?");
        cyclejob.setArgs(RequestBeanUtil.getRequestBeanAsString(requestBean));
        cyclejob.setMode(ModeConstant.DO_BY_ROUTER);
        cyclejob.setType(ModeConstant.DO_BY_ROUTER_SCAN);
        cycleJobDao.save(cyclejob);
        dynamicTask.addRouterScanTask(cyclejob.getCron(),requestBean);
        return new ResponseBean(requestBean.getItems().size(), requestBean.getItems().size());
    }

    @Override
    public ResponseBean settingRouter(RequestBean requestBean) {
        List<Router> routerList = new ArrayList<>();
        for (RequestItem items : requestBean.getItems()) {
            routerList.addAll(findByArrtribute(TableConstant.TABLE_ROUTERS, items.getQuery(), items.getQueryString(), Router.class));
        }
        ResponseBean responseBean = SendCommandUtil.sendCommandWithSettingRouters(routerList);
        return responseBean;
    }
    public Router updateRouter(Router router) {
        Router r = findById(router.getId()).get();
        router.setHeartBeat(new Timestamp(System.currentTimeMillis()));
        // 更新路由器 发送设置命令
        if(router.getId()!=0 ){
            //getBytesByType
            byte[] message = new byte[16];
            message[0]=0x02;
            message[1]=0x05;
            message[2]=0x0D;
            // mac地址
            byte[] mac = setAttribute("mac", r, router, 6);
            for(int i = 0 ;i<mac.length;i++)
                message[3+i] = mac[i];
            // IP地址
            if(!router.getIp().equals(r.getIp())){
                String ip = router.getIp();
                String[] ips = ip.split("\\.");
                for(int i=0;i<4;i++)
                    message[9+i] = (byte) Integer.parseInt(ips[i]);
            }
            // 信道
            if(!router.getChannelId().equals(r.getChannelId())){
                message[13] = Byte.parseByte(router.getChannelId());
            }
            // 频率
            byte[] frequency = setAttribute("frequency", r, router, 2);
            for(int i = 0 ;i<frequency.length;i++)
                message[14+i] = frequency[i];
            byte[] realMessage = CommandConstant.getBytesByType(null, message, CommandConstant.COMMANDTYPE_ROUTER);
            Channel channel = SpringContextUtil.getChannelByRouter(r);
            String result = nettyUtil.sendMessageWithRepeat(channel, realMessage,Integer.valueOf(SystemVersionArgs.commandRepeatTime));
            if(result!=null && result.equals("成功")){
                System.out.println("路由器设置成功");
            }
        }
        return routerDao.save(router);
    }
    private byte[] setAttribute(String name,Router source,Router target,int len){
        byte[] result = null;
        try {
            String sourceData = SpringContextUtil.getSourceData(name, source);
            String targetData = SpringContextUtil.getSourceData(name, target);
            if(targetData!=null  && !targetData.equals(sourceData)){
                result = SpringContextUtil.int2ByteArr(Integer.valueOf(targetData), len);
            }
            else{
                result = new byte[len];
                for(int i=0;i<len;i++)
                    result[i] = (byte) 0xff;
            }
        }
        catch (Exception e){}
        return result;
    }
}

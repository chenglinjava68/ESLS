package com.datagroup.ESLS.cycleJob;

import com.datagroup.ESLS.common.constant.ModeConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.dao.CycleJobDao;
import com.datagroup.ESLS.entity.cyclejob;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.RequestBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Component
public class DynamicTask {
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private TagService tagService;
    @Autowired
    private CycleJobDao cycleJobDao;
    private ScheduledFuture<?> future;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }
    public void addUpdateTask(String cron,RequestBean requestBean, Integer mode){
        future = threadPoolTaskScheduler.schedule(new UpdateTag(requestBean,mode), new CronTrigger(cron));
    }
    public void addScanTask(String cron,RequestBean requestBean, Integer mode){
        future = threadPoolTaskScheduler.schedule(new ScanTag(requestBean,mode), new CronTrigger(cron));
    }
    private class UpdateTag implements Runnable {
        private RequestBean requestBean;
        private Integer mode;
        public UpdateTag(RequestBean requestBean, Integer mode){
            this.requestBean = requestBean;
            this.mode = mode;
        }
        @Override
        public void run() {
            if(mode.equals(ModeConstant.DO_BY_TAG)) {
                System.out.println("标签定期刷新:" + new Date());
                tagService.flushTags(requestBean);
            }
            else if(mode.equals(ModeConstant.DO_BY_ROUTER)) {
                System.out.println("路由器定期刷新:" + new Date());
                tagService.flushTagsByRouter(requestBean);
            }
        }
    }
    private class ScanTag implements Runnable {
        private RequestBean requestBean;
        private Integer mode;
        public ScanTag(RequestBean requestBean, Integer mode){
            this.requestBean = requestBean;
            this.mode = mode;
        }
        @Override
        public void run() {
            if(mode.equals(ModeConstant.DO_BY_TAG)) {
                System.out.println("标签定期巡检:" + new Date());
                tagService.flushTags(requestBean);
            }
            else if(mode.equals(ModeConstant.DO_BY_ROUTER)) {
                System.out.println("路由器定期巡检:" + new Date());
                tagService.flushTagsByRouter(requestBean);
            }
        }
    }
    @PostConstruct
    public void init(){
        List<cyclejob> cyclejobs = cycleJobDao.findAll();
        for(cyclejob job:cyclejobs){
            RequestBean requestBean = RequestBeanUtil.stringtoRequestBean(job.getArgs());
            if(job.getType().equals(ModeConstant.DO_BY_UPDATE))
                addUpdateTask(job.getCron(),requestBean,job.getMode());
            else if(job.getType().equals(ModeConstant.DO_BY_FLUSH))
                addUpdateTask(job.getCron(),requestBean,job.getMode());
        }
    }
}

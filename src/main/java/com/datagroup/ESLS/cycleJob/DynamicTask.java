package com.datagroup.ESLS.cycleJob;

import com.datagroup.ESLS.common.constant.ModeConstant;
import com.datagroup.ESLS.common.constant.SqlConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.dao.BaseDao;
import com.datagroup.ESLS.dao.CycleJobDao;
import com.datagroup.ESLS.entity.CycleJob;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.service.Service;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.FileUtil;
import com.datagroup.ESLS.utils.PoiUtil;
import com.datagroup.ESLS.utils.RequestBeanUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class DynamicTask {
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private TagService tagService;
    @Autowired
    private RouterService routerService;
    @Autowired
    private CycleJobDao cycleJobDao;
    private ScheduledFuture<?> future;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }
    public void addFlushTask(String cron, RequestBean requestBean, Integer mode){
        future = threadPoolTaskScheduler.schedule(new FlushTask(requestBean,mode), new CronTrigger(cron));
    }
    public void addTagScanTask(String cron, RequestBean requestBean, Integer mode){
        future = threadPoolTaskScheduler.schedule(new TagScanTask(requestBean,mode), new CronTrigger(cron));
    }
    public void addRouterScanTask(String cron, RequestBean requestBean){
        future = threadPoolTaskScheduler.schedule(new RouterScanTask(requestBean), new CronTrigger(cron));
    }
    public void addBaseGoodsScanTask(String cron,String filePath){
        future = threadPoolTaskScheduler.schedule(new BaseGoodScanTask(filePath), new CronTrigger(cron));
    }
    public void addChangeGoodsScanTask(String cron, String filePath){
        future = threadPoolTaskScheduler.schedule(new ChangeGoodScanTask(filePath), new CronTrigger(cron));
    }
    private class FlushTask implements Runnable {
        private RequestBean requestBean;
        private Integer mode;
        public FlushTask(RequestBean requestBean, Integer mode){
            this.requestBean = requestBean;
            this.mode = mode;
        }
        @Override
        public void run() {
            if(mode.equals(ModeConstant.DO_BY_TAG)) {
                System.out.println("对标签标签定期刷新:" + new Date());
                tagService.flushTags(requestBean);
            }
            else if(mode.equals(ModeConstant.DO_BY_ROUTER)) {
                System.out.println("对路由器下的标签定期刷新:" + new Date());
                tagService.flushTagsByRouter(requestBean);
            }
        }
    }
    private class TagScanTask implements Runnable {
        private RequestBean requestBean;
        private Integer mode;
        public TagScanTask(RequestBean requestBean, Integer mode){
            this.requestBean = requestBean;
            this.mode = mode;
        }
        @Override
        public void run() {
            if(mode.equals(ModeConstant.DO_BY_TAG)) {
                System.out.println("对指定的标签定期巡检:" + new Date());
                tagService.scanTags(requestBean);
            }
            else if(mode.equals(ModeConstant.DO_BY_ROUTER)) {
                System.out.println("对指定的路由器的所有标签定期巡检:" + new Date());
                tagService.scanTagsByRouter(requestBean);
            }
        }
    }
    private class RouterScanTask implements Runnable {
        private RequestBean requestBean;
        public RouterScanTask(RequestBean requestBean){
            this.requestBean = requestBean;
        }
        @Override
        public void run() {
            System.out.println("对指定的路由器定期巡检:" + new Date());
            routerService.routerScan(requestBean);
        }
    }
    private class BaseGoodScanTask implements Runnable {
        private String filePath;
        public BaseGoodScanTask(String filePath){
            this.filePath = filePath;
        }
        @Override
        public void run() {
            Service service = (Service) SpringContextUtil.getBean("BaseService");
            List dataColumnList = service.findBySql(SqlConstant.QUERY_TABLIE_COLUMN + "\'" + "goods" + "\'");
            System.out.println("扫描指定目录下的商品基本文件  "+filePath+" " + new Date());
            File file = new File(filePath);
            File[] files = file.listFiles();
            try {
                for(int i = 0;i<files.length;i++){
                    try {
                        // 添加
                        PoiUtil.importCsvDataFile(new FileInputStream(files[i]), dataColumnList, "goods",0);
                        String startPath = filePath + File.separator + files[i].getName();
                        String endPath  = filePath + "_finish"+ File.separator+files[i].getName();
                        File startFile = new File(startPath);
                        FileUtils.copyFile(startFile,new File(endPath));
                        System.gc();
                        FileUtils.forceDelete(startFile);
                    }
                    catch (Exception e){
                        System.out.println(files[i].getName()+"导入失败  -   "+e);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private class ChangeGoodScanTask implements Runnable {
        private String filePath;
        public ChangeGoodScanTask(String filePath){
            this.filePath = filePath;
        }
        @Override
        public void run() {
            System.out.println("扫描指定目录下的商品变价文件" +filePath+ " " + new Date());
            File file = new File(filePath);
            File[] files = file.listFiles();
            for(int i = 0;i<files.length;i++){
                Service service = (Service) SpringContextUtil.getBean("BaseService");
                List dataColumnList = service.findBySql(SqlConstant.QUERY_TABLIE_COLUMN + "\'" + "goods" + "\'");
                try {
                    System.out.println(files[i].getName()+"开始导入数据库");
                    try {
                        // 修改
                        PoiUtil.importCsvDataFile(new FileInputStream(files[i]), dataColumnList, "goods",1);
                        String startPath = filePath + File.separator + files[i].getName();
                        String endPath  = filePath + "_finish"+ File.separator+files[i].getName();
                        File startFile = new File(startPath);
                        FileUtils.copyFile(startFile,new File(endPath));
                        System.gc();
                        FileUtils.forceDelete(startFile);
                    }
                    catch (Exception e){
                        System.out.println(files[i].getName()+"导入失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @PostConstruct
    public void init(){
        List<CycleJob> CycleJobs = cycleJobDao.findAll();
        for(CycleJob job: CycleJobs){
            // 扫描商品基本文件
            if(job.getType().equals(ModeConstant.DO_BY_BASEGOODS_SCAN)) {
                addBaseGoodsScanTask(job.getCron(), job.getArgs());
                continue;
            }
            // 扫描商品变价文件
            else if(job.getType().equals(ModeConstant.DO_BY_CHANGEGOODS_SCAN)) {
                addChangeGoodsScanTask(job.getCron(), job.getArgs());
                continue;
            }

            RequestBean requestBean = RequestBeanUtil.stringtoRequestBean(job.getArgs());
            // 标签刷新（0对标签 1对路由器）
            if(job.getType().equals(ModeConstant.DO_BY_TAG_FLUSH))
                addFlushTask(job.getCron(),requestBean,job.getMode());
                // 标签巡检（0对标签 1对路由器）
            else if(job.getType().equals(ModeConstant.DO_BY_TAG_SCAN))
                addTagScanTask(job.getCron(),requestBean,job.getMode());
                // 路由器巡检
            else if(job.getType().equals(ModeConstant.DO_BY_ROUTER_SCAN))
                addRouterScanTask(job.getCron(),requestBean);
        }
    }
}

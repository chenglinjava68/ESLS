package com.datagroup.ESLS.utils;

import com.datagroup.ESLS.dto.TagsAndRouter;
import com.datagroup.ESLS.entity.Router;
import com.datagroup.ESLS.entity.Style;
import com.datagroup.ESLS.entity.Tag;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.service.TagService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TagUtil {

    public static List<TagsAndRouter> splitTagsByRouter(List<Tag> tags){
        ArrayList<TagsAndRouter> tagsAndRouters = new ArrayList<>();
        for(Tag tag : tags){
            TagsAndRouter tagsAndRouter = new TagsAndRouter(tag.getRouter());
            if(tagsAndRouters.contains(tagsAndRouter)){
                tagsAndRouter = getTagsAndRouter(tagsAndRouters,tagsAndRouter.getRouter().getId());
                tagsAndRouter.addTag(tag);
            }
            else{
                tagsAndRouter.addTag(tag);
                tagsAndRouters.add(tagsAndRouter);
            }
        }
        return tagsAndRouters;
    }
    public static TagsAndRouter getTagsAndRouter( ArrayList<TagsAndRouter> tagsAndRouters,Long routerId){
        for(int i=0;i<tagsAndRouters.size();i++)
            if(tagsAndRouters.get(i).getRouter().getId() == routerId)
                return tagsAndRouters.get(i);
        return null;
    }
    public static synchronized String judgeResultAndSettingTag(String result,long begin,Tag tag){
        TagService tagService = ((TagService)SpringContextUtil.getBean("TagService"));
        if ("成功".equals(result)) {
            Tag newTag = SettingUtil.settingTag(tag, begin);
            tagService.saveOne(newTag);
        }
        else {
            // 变价超时
            tag.setCompleteTime(new Timestamp(System.currentTimeMillis()));
            tag.setCompleteTime(null);
            tagService.saveOne(tag);
        }
        return result;
    }
    public static String judgeResultAndSettingTagWaitUpdate(String result,long begin,Tag tag){
        TagService tagService = ((TagService)SpringContextUtil.getBean("TagService"));
        if ("成功".equals(result)) {
            Tag newTag = SettingUtil.settingTag(tag, begin);
            newTag.setWaitUpdate(1);
            tagService.saveOne(newTag);
        }
        else {
            // 变价超时
            tag.setCompleteTime(new Timestamp(System.currentTimeMillis()));
            tag.setCompleteTime(null);
            tagService.saveOne(tag);
        }
        return result;
    }
    public static String judgeResultAndSettingRouter(String result, long begin, Router router,byte[] message){
        RouterService routerService = ((RouterService)SpringContextUtil.getBean("RouterService"));
        if(message[8]==5 && message[9]==2) {
            return result;
        }
        else{
            Router newRouter = SettingUtil.settintRouter(router, begin);
            routerService.saveOne(newRouter);
        }
        return result;
    }
    public static List<Tag> getTagsByRouters(List<Router> routers){
        TagService tagService = (TagService)SpringContextUtil.getBean("TagService");
        List<Tag> result = new ArrayList<>();
        for(Router r : routers){
            List<Tag> itemTags = tagService.findByRouterId(r.getId());
            result.addAll(itemTags);
        }
        return result;
    }
    public static void setTagIsNotWorking(List<Tag> tags){
        TagService tagService = (TagService)SpringContextUtil.getBean("TagService");
        for(Tag tag:tags){
            tag.setIsWorking((byte) 0);
            tagService.saveOne(tag);
        }
    }
    public static void setRouterIsNotWorking(List<Router> routers){
        RouterService routerService = (RouterService)SpringContextUtil.getBean("RouterService");
        for(Router r:routers){
            r.setIsWorking((byte) 0);
            routerService.saveOne(r);
        }
    }
    public static boolean judgeTagMatchStyle(Tag tag, Style style){
        String resolutionWidth = tag.getResolutionWidth();
        String styleNumber = style.getStyleNumber();
        if(resolutionWidth.substring(0,2).equals(styleNumber.substring(0,2)))
            return true;
        return false;
    }
}

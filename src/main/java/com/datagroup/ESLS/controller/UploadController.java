package com.datagroup.ESLS.controller;

import com.datagroup.ESLS.common.constant.COSConstant;
import com.datagroup.ESLS.common.constant.FileConstant;
import com.datagroup.ESLS.common.request.RequestBean;
import com.datagroup.ESLS.common.request.RequestItem;
import com.datagroup.ESLS.common.response.ResponseBean;
import com.datagroup.ESLS.common.response.ResultBean;
import com.datagroup.ESLS.entity.Dispms;
import com.datagroup.ESLS.entity.Good;
import com.datagroup.ESLS.entity.Style;
import com.datagroup.ESLS.service.DispmsService;
import com.datagroup.ESLS.service.GoodService;
import com.datagroup.ESLS.service.TagService;
import com.datagroup.ESLS.utils.*;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@Api(description = "文件上传API")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class UploadController {
    @Value("${project.profile}")
    private String UPLOAD_FOLDER;
    @Autowired
    private GoodService goodService;
    @Autowired
    private DispmsService dispmsService;
    @ApiOperation("上传单个文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = "0为商品基本信息 1商品变价信息 2为商品特征图片 3为样式特征图片", dataType = "mode", paramType = "query", required = true)
    })
    @PostMapping("/uploadFile")
    @RequiresPermissions("添加信息")
    public ResponseEntity<ResultBean> singleFileUpload(@ApiParam(value = "文件信息", required = true) @RequestParam("file") MultipartFile file, @RequestParam Integer mode,@RequestParam(required = false) String query,@RequestParam(required = false)  String queryString ) {
        if (Objects.isNull(file) || file.isEmpty()) {
            log.error("文件为空");
            return new ResponseEntity<>(ResultBean.error("文件为空，请重新上传"), HttpStatus.NOT_ACCEPTABLE);
        }
        RequestBean requestBean = new RequestBean();
        RequestItem requestItem = new RequestItem(query, queryString);
        requestBean.getItems().add(requestItem);
        String fileName =  UUID.randomUUID().toString()+file.getOriginalFilename();
        String filePath = UPLOAD_FOLDER + FileConstant.ModeMap.get(mode);
        try {
            if (FileUtil.judeFileExists(filePath, fileName))
                return new ResponseEntity<>(ResultBean.error("文件已经存在，请重新上传"), HttpStatus.NOT_ACCEPTABLE);
            if(mode == 0 ||  mode == 1) {
                // 商品基本信息及变价信息
                FileUtil.uploadFile(file.getBytes(), filePath, fileName);
            }
            // 商品图片
            else if(mode == 2){
                fileName =  "/goods_image/"+fileName;
                List<Good> goods = RequestBeanUtil.getGoodsByRequestBean(requestBean);
                for (Good good : goods) {
                    String url = COSUtil.PutObjectRequest(file, fileName);
                    good.setImageUrl(url);
                    goodService.saveOne(good);
                }
            }
            // 样式图片
            else if(mode == 3){
                fileName =  "/dismps_image/"+fileName;
                List<Dispms> dispms = RequestBeanUtil.getDispmsByRequestBean(requestBean);
                for (Dispms dispm : dispms) {
                    String url = COSUtil.PutObjectRequest(file, fileName);
                    dispm.setImageUrl(url);
                    dispmsService.saveOne(dispm);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("文件写入成功...");
        return new ResponseEntity<>(ResultBean.success("文件上传成功"), HttpStatus.OK);
    }

    @ApiOperation("上传多个文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mode", value = "0为商品基本信息 1商品变价信息 2为商品特征图片 3为样式特征图片", dataType = "mode", paramType = "query", required = true)
    })
    @PostMapping("/uploadFiles")
    public ResponseEntity<ResultBean> multiFileUpload(@ApiParam(value = "文件信息", required = true) @RequestParam("file") MultipartFile[] file, @RequestParam Integer mode,@RequestBody @ApiParam("图片对应实体信息集合") RequestBean requestBean) {
        int successNumber = 0;
        for (int i = 0; i < file.length; i++) {
            if (file[i] != null) {
                System.out.println(file[i].getName());
            }
        }
        return new ResponseEntity<>(ResultBean.success(new ResponseBean(file.length,successNumber)), HttpStatus.OK);
    }
}

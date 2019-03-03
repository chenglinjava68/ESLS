package com.datagroup.ESLS;

import com.datagroup.ESLS.common.constant.SqlConstant;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.graphic.BarcodeUtil;
import com.datagroup.ESLS.netty.command.CommandCategory;
import com.datagroup.ESLS.netty.command.ProtocolConstant;
import com.datagroup.ESLS.dao.*;
import com.datagroup.ESLS.netty.executor.AsyncTask;
import com.datagroup.ESLS.redis.RedisConstant;
import com.datagroup.ESLS.service.DispmsService;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.service.StyleService;
import com.datagroup.ESLS.service.UserService;
import com.datagroup.ESLS.utils.CopyUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ESLSApplicationTests {
    @Autowired
    private TagDao tagsDao;
    @Autowired
    private GoodDao goodDao;
    @Autowired
    private AsyncTask asyncTask;
    @Autowired
    private RedisConstant redisConstant;
    @Autowired
    private RouterService routerService;

    @Autowired
    private BaseDao baseDao;
    @Autowired
    private UserService userService;
    @Autowired
    private DispmsService dispmsService;
    @Autowired
    private StyleService styleService;
    @Test
    public void testAsync() throws IOException, URISyntaxException {
        Font font = new Font("微软雅黑", Font.BOLD, 32);
        String content = "0";
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
        System.out.println(metrics.stringWidth(content));
        System.out.println(metrics.getWidths()[0]);
        System.out.println(metrics.getHeight());
        BufferedImage bufferedImage = new BufferedImage(metrics.stringWidth(content), metrics.getHeight(),12);
        Graphics2D graphics = bufferedImage.createGraphics();
//        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawLine(0,metrics.getHeight()/2,metrics.stringWidth(content),metrics.getHeight()/2);
        // graphics.drawString(content, 0, metrics.getAscent());//图片上写文字
        graphics.drawString(content, 0, metrics.getHeight());//图片上写文字
        System.out.println(metrics.getHeight() + "  " + metrics.getAscent());
        ImageIO.write(bufferedImage, "BMP", new File("D:\\styles\\5"+".bmp"));
        graphics.dispose();
    }

    @Test
    public void testRedisKeys() {
        String msg = "123456789";
        String path = "barcode.png";
        BarcodeUtil.generateFile(msg, path);
    }

    @Test
    public void testNettyClient() throws Exception {
    }

    @Test
    public void testProperty() {
        System.out.println(ProtocolConstant.COMMAND);
        System.out.println(CommandCategory.COMMAND_CATEGORY);
        System.out.println(CommandCategory.COMMAND_CATEGORY.get("ACK").getCommand_class() == 0x01);
        // Tag tag = new Tag();
        //   Good good = new Good();
    }

    @Test
    public void testPOI() {
        List goodlist = baseDao.findBySql(SqlConstant.QUERY_TABLIE_COLUMN + "\'goods\'");
        List<Good> goods = goodDao.findAll();
        List goodVos = CopyUtil.copyGood(goods);
        // PoiUtil.exportEmp2Excel(goodVos,goodlist);
    }

    @Test
    public void testShiro() {
    }
    @Test
    public void testuserService(){
        User eco = userService.findByName("ECO");
        System.out.println(eco.getRoleList());
        for(Role role:eco.getRoleList()){
            List<Permission> permissions = role.getPermissions();
            for(Permission permission:permissions)
                System.out.println(permission.toString());
        }
    }
    @Test
    public void testRouterService(){
        Router router = new Router();
        router.setIp("192.168.1.30");
        routerService.saveOne(router);
    }
}

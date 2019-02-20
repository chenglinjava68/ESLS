package com.datagroup.ESLS;

import com.datagroup.ESLS.common.constant.SqlConstant;
import com.datagroup.ESLS.entity.*;
import com.datagroup.ESLS.netty.command.CommandCategory;
import com.datagroup.ESLS.netty.command.ProtocolConstant;
import com.datagroup.ESLS.dao.*;
import com.datagroup.ESLS.netty.executor.AsyncTask;
import com.datagroup.ESLS.redis.RedisConstant;
import com.datagroup.ESLS.service.RouterService;
import com.datagroup.ESLS.service.TagAndGoodService;
import com.datagroup.ESLS.service.UserService;
import com.datagroup.ESLS.utils.CopyUtil;
import com.datagroup.ESLS.utils.SpringContextUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ESLSApplicationTests {
    @Autowired
    private TagDao tagsDao;
    @Autowired
    private GoodDao goodDao;
    @Autowired
    private TagAndGoodDao tagAndGoodDao;
    @Autowired
    private PhotoDao photoDao;
    @Autowired
    private TagAndGoodService tagAndGoodService;
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
    @Test
    public void displayStyle() {

    }

    @Test
    public void testAsync() {
        // asyncTask.executeRequest("handler22");
    }

    @Test
    public void testRedisKeys() {
        redisConstant.getExpiresMap().forEach((key, value) -> {
            System.out.println(key + " " + value);
        });
    }

    @Test
    public void testNettyClient() {
        byte[] message = new byte[2];
        message[0] = 0x02;
        message[1] = 0x03;
        InetSocketAddress target = new InetSocketAddress("localhost", 9001);
        // System.out.println("执行结果："+NettyClient.startAndWrite(target,message));
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

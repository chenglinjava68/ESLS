各文件夹解释如下：
java/com/datagroup/ESLS
aop：日志切面记录
common：常用程序常量类
component：springboot组件初始化类
config:springboot配置类
controller:接受前端请求进行响应的controll层
dao:与数据库进行读写操作的相关类
dto:用于向前端展示数据的实体类
entity：与数据库表相对应的类
graphic：生成条形码和二维码的类
netty：与路由器通讯的网络监听类
redis: 程序缓存redis配置类
service：service层接口，供controll层调用
serviceImpl: service层接口实现类，供controll层调用
springbatch: 定期读写指定目录的商品文件，并添加数据库表的相关类
utils：常用工具类
ESLSApplication: 启动主程序

resources
application.yml
application-command.yml
application-dev.yml
application-prod.yml
以上为程序配置
logback-sprng.xml
日志配置
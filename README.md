<div align="center">
<h1>sb-man</h1>
</div>


----------
**目录**

[📢 说明](#说明)

[✨ 功能](#功能)

[💻 本地启动](#本地启动)

[🚀 docker部署](#docker部署)

----------
## 说明

一个基于`Mirai`的QQ机器人，使用`Java` + `Maven`实现部分功能。

官方文档：https://github.com/mamoe/mirai/blob/dev/docs/CoreAPI.md

文档大部分是用`Kotlin`描述的，可以参考着这个项目转换为`Java`代码。


## 功能
- [x] 多机器人同时在线
- [x] AI对话
- [x] 联通流量机器人（可绑定多个手机号）

## 本地启动

springboot项目，配置`application.yml`后，启动`SbApplication.java`即可

## docker部署
1. 运行安装
    ```shell
    docker run -d \
        -e IS_USE_BUILT_IN_GMC=true \
        -p 9001:9000 \
        -v /data/sb/config:/sb/config \
        -v /data/sb/data:/sb/data \
        -v /data/sb/gmc:/sb/gmc \
        --name sb \
        registry.cn-beijing.aliyuncs.com/yiidii-hub/sb-man:v1.0.3
    ```
2. 修改配置
    运行完成之后，先到`/data/sb/config/config.json`修改配置QQ
    `ltMonitorCron`：联通监控的定时任务cron
   
    `robot`：配置多个机器人的`名称` 和`QQ` （**如果不配置QQ即使待会登录了QQ，也不会真正的配置上**）

    ```json
    {
      "ltMonitorCron": "0 0/1 * * * ?", 
      "robot": [
        {
          "name": "机器人",
          "qq": 18048600
        }
      ]
    }
    ```

3. 访问`http://ip:9001`扫码添加机器人

4. 最后看日志`docker logs -f sb`有如下即可
    ```
    机器人(QQ: xxxxxx)上线
    ```


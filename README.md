# sb-man

## 一. 项目说明

一个基于`Mirai`的QQ机器人，使用`Java` + `Maven`实现部分功能。

官方文档：https://github.com/mamoe/mirai/blob/dev/docs/CoreAPI.md

文档大部分是用`Kotlin`描述的，可以参考着这个项目转换为`Java`代码。

- [x] 多机器人同时在线
- [x] AI对话
- [x] 联通流量机器人（可绑定多个手机号）

## 二. 核心功能

## 三. 其他功能

## 四. 本地启动

springboot项目，配置`application.yml`后，启动`SbApplication.java`即可

## 五. 部署

### jar方式

前提：需要`maven`和`Java`环境

1. 在项目目录，使用`mvn package`将源码打包成jar文件（路径：项目目录/target/sb-man.jar）
2. 使用`java -jar sb-man.jar`启动即可

### docker方式

这里可以根据`Dockerfile`自行构建镜像并使用。

推荐:
```shell

```

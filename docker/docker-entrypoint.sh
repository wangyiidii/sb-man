#!/bin/bash
set -e

if [ ! -d ${SB_DIR}/gmc ]; then
  echo "没有映射gmc目录给本容器，请先按教程映射gmc配置目录...\n"
  exit 1
fi

if [ ! -d ${SB_DIR}/config ]; then
  echo "没有映射config配置目录给本容器，请先映射config配置目录...\n"
  exit 1
fi

if [ ! -d ${SB_DIR}/data ]; then
  echo "没有映射data目录给本容器，请先按教程映射data配置目录...\n"
  exit 1
fi

echo "======================== 1. 检测必要目录文件 ========================\n"

if [ ! -s ${SB_DIR}/gmc/Go-Mirai-Client ]; then
  echo "检测到gmc目录下不存在Go-Mirai-Client，从示例文件复制一份用于初始化...\n"
  cp -fv ${SB_DIR}/sample/Go-Mirai-Client ${SB_DIR}/gmc/Go-Mirai-Client
fi

if [ ! -s ${SB_DIR}/config/config.json ]; then
  echo "检测到config配置目录下不存在config.json，从示例文件复制一份用于初始化...\n"
  cp -fv ${SB_DIR}/sample/config.json.sample ${SB_DIR}/config/config.json
fi

echo "======================== 2. 启动gmc ========================\n"

if [ 0"$IS_USE_BUILT_IN_GMC" = "0" ]; then
  chmod +x ${SB_DIR}/gmc/Go-Mirai-Client
  nohup ${SB_DIR}/gmc/Go-Mirai-Client > /dev/null 2 >&1 &
else

echo "======================== 3. 启动Server ========================\n"
java -jar ${SB_DIR}/app.jar

exec "$@"

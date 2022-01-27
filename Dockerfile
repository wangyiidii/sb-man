FROM java:8
MAINTAINER yd <3087233411@qq.com>
# envs
ENV SB_DIR=/sb
ENV IS_USE_BUILT_IN_GMC ${IS_USE_BUILT_IN_GMC:-false}

# 工作目录
WORKDIR ${SB_DIR}

# 添加jar和docker目录
ADD target/sb-man.jar ${SB_DIR}/app.jar
ADD docker/ ${SB_DIR}/

# 暴露文件
VOLUME ["${SB_DIR}/config", "${SB_DIR}/data", "${SB_DIR}/gmc"]

# 入口
ENTRYPOINT ["sh", "docker-entrypoint.sh"]
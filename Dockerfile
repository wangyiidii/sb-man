FROM java:8
MAINTAINER yd <3087233411@qq.com>
ENV SB_DIR=/sb
WORKDIR ${SB_DIR}

ADD target/sb-man.jar ${SB_DIR}/app.jar
ADD docker/ ${SB_DIR}/

VOLUME ["${SB_DIR}/config", "${SB_DIR}/data", "${SB_DIR}/gmc"]

ENTRYPOINT ["sh", "docker-entrypoint.sh"]
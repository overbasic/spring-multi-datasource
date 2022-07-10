#!/usr/bin/env bash

# JQ 설치
JQ_OK=$(dpkg -s jq 2>&1 | grep -c "ok installed")
echo Checking for "$JQ_OK"
if [ "$JQ_OK" -eq 0 ]; then
  echo Installing jq
  apt-get update
  apt-get install -y jq
fi

# Docker compose 실행
docker-compose up -d --build

# Master Host 변수 저장
MASTER_HOST=$(docker network inspect mysql-replica | jq -r '.[].Containers | .[] | select(.Name=="master").IPv4Address' | cut -d'/' -f1)
MASTER_USER='root'
MASTER_PASSWORD='password'

sleep 3

# Master Log 변수 저장
MASTER_LOG_FILE=$(docker exec -i master mysql -u$MASTER_USER -p$MASTER_PASSWORD -e "show master status\G" | grep mysql-bin | cut -d':' -f2 | xargs)
MASTER_LOG_POS=$(docker exec -i master mysql -u$MASTER_USER -p$MASTER_PASSWORD -e "show master status\G" | grep Position | cut -d':' -f2 | xargs)

# Slave 설정
docker exec -i slave mysql -u$MASTER_USER -p$MASTER_PASSWORD -e "stop slave;
change master to master_host='$MASTER_HOST',
master_user='$MASTER_USER',
master_password='$MASTER_PASSWORD',
master_log_file='$MASTER_LOG_FILE',
master_log_pos=$MASTER_LOG_POS;
start slave;"

echo "============================================================"
echo Master Host is "$MASTER_HOST"
echo Master Log File is "$MASTER_LOG_FILE"
echo Master Log Position is "$MASTER_LOG_POS"
docker exec -i slave mysql -u$MASTER_USER -p$MASTER_PASSWORD -e "show slave status\G" | grep Slave

# multi 데이터베이스 생성 후 slave 에서 확인
docker exec -i master mysql -u$MASTER_USER -p$MASTER_PASSWORD -e "create database if not exists multi;"
sleep 1
docker exec -i slave mysql -u$MASTER_USER -p$MASTER_PASSWORD -e "show databases;" | grep multi

# docker-compose down
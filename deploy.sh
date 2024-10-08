REPOSITORY=/home/ubuntu/app/build
cd $REPOSITORY/

echo "> 현재 구동중인 애플리케이션 pid 확인"

CURRENT_PID=$(pgrep -f server-0.0.1-SNAPSHOT.jar)

echo "> CURRENT_PID: $CURRENT_PID"

if [ -z $CURRENT_PID ]; then
        echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
        echo "> kill -15 $CURRENT_PID"
        kill -15 $CURRENT_PID
        sleep 5
fi

echo "> 새 애플리케이션 배포"

JAR_NAME=$(ls $REPOSITORY/ | grep 'demo' | tail -n 1)

echo "> JAR_NAME: $JAR_NAME"

nohup java -jar $REPOSITORY/$JAR_NAME/server-0.0.1-SNAPSHOT.jar &>/dev/null &
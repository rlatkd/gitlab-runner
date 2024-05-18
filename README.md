# GitLab Runner를 이용한 CI/CD로 SpringBoot EC2에 자동배포

## 목표

GitLab의 CI/CD 기능을 이용해서 main branch에 push하게 되면 AWS EC2에 SpringBoot jar를 빌드 및 배포까지 완료

## 시작

### GitLab CI/CD 코드 작성

-   최대한 민감한 정보는 환경변수로 설정
-   gradle-8.7과 jdk-17 사용
-   CI/CD 단계: `build`->`test`->`deploy`
-   Ubuntu서버의 `/home/ubuntu/app/build/` 경로에 배포

**.gitlab-ci.yml**

```yaml
image: gradle:8.7-jdk17-alpine

stages: # List of stages for jobs, and their order of execution
    - build
    - test
    - deploy

cache:
    paths:
        - .gradle/wrapper
        - .gradle/caches

build:
    stage: build
    script:
        - git update-index --add --chmod=+x gradlew
        - ./gradlew clean build
    artifacts:
        paths:
            - build/libs/*.jar
        expire_in: 1 week
    only:
        - main

test:
    stage: test
    script: ./gradlew test

deploy-to-ec2:
    stage: deploy
    before_script:
        - "command -v ssh-agent >/dev/null || ( apk update && apk add openssh )"
        - eval $(ssh-agent -s)
        - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add - > /dev/null
        - mkdir -p ~/.ssh
        - chmod 700 ~/.ssh
        - echo "$SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
        - chmod 644 ~/.ssh/known_hosts

    script:
        - ssh -o StrictHostKeyChecking=no ubuntu@"$DEPLOY_SERVER" 'rm -rf ~/app/build/*.jar'
        - ssh -o StrictHostKeyChecking=no ubuntu@"$DEPLOY_SERVER" 'mkdir -p ~/app/build'
        - scp build/libs/*.jar ubuntu@"$DEPLOY_SERVER":~/app/build/demo.jar
        - ssh -o StrictHostKeyChecking=no ubuntu@"$DEPLOY_SERVER" '~/deploy.sh'
    only:
        - main
```

### GitLab Variables 등록

settings - CI/CD - variables

```plain text
DEPLOY_SERVER: EC2 public ip
SSH_PRIVATE_KEY: EC2 접속용 PEM
```

**deploy.sh**

-   shell script 파일에는 현재 실행중인 java 어플리케이션이 있다면 중지시키고(없다면 생략; 최초 배포시) 새로 업로드 된 애플리케이션을 실행하는 코드가 포함됨
-   원격으로 해당 코드를 작성하면 불편하니 `/home/ubuntu/deploy.sh` 경로에 미리 작성

```shell
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
```

-   마지막 `&>/dev/null &`은 GitLab console에서 log때문에 멈춰버리는 현상 방지
-   GitLab에서 실행할 수 있도록 권한 변경

```shell
sudo chmod 755 deploy.sh
```

-   main branch에 SpringBoot 소스 push
    -   이 때 gredlew의 권한이 permission denied가 뜰 수 있음 -> 권한 허용해줘야함
        -   `.git`있는 디렉터리에서 bash shell로 명령어 입력

```bash
git update-index --chmod=+x gradlew
```

## 각 단계별 Jobs 로그

**build**

```shell
Running with gitlab-runner 16.11.0~pre.21.gaa21be2d (aa21be2d)
  on blue-1.saas-linux-small.runners-manager.gitlab.com/default j1aLDqxS, system ID: s_ccdc2f364be8
  feature flags: FF_USE_IMPROVED_URL_MASKING:true
Resolving secrets
00:00
Preparing the "docker+machine" executor
00:12
Using Docker executor with image gradle:8.7-jdk17-alpine ...
Pulling docker image gradle:8.7-jdk17-alpine ...
Using docker image sha256:e2fca0d966895442c272c3752a58e1e7b6adcf09a1a0f716ce1e1450682bdb42 for gradle:8.7-jdk17-alpine with digest gradle@sha256:9c74a0bc86971b60c9b14a8a5d69ffddc243da104cd5a2251af34dcc2748046d ...
Preparing environment
00:05
Running on runner-j1aldqxs-project-57389325-concurrent-0 via runner-j1aldqxs-s-l-s-amd64-1714477937-6e1efe28...
Getting source from Git repository
00:01
Fetching changes with git depth set to 20...
Initialized empty Git repository in /builds/team5racle/test2/.git/
Created fresh repository.
Checking out b353f7f6 as detached HEAD (ref is main)...
Skipping Git submodules setup
$ git remote set-url origin "${CI_REPOSITORY_URL}"
Restoring cache
00:01
Checking cache for default-protected...
Downloading cache from https://storage.googleapis.com/gitlab-com-runners-cache/project/57389325/default-protected
Successfully extracted cache
Executing "step_script" stage of the job script
00:50
Using docker image sha256:e2fca0d966895442c272c3752a58e1e7b6adcf09a1a0f716ce1e1450682bdb42 for gradle:8.7-jdk17-alpine with digest gradle@sha256:9c74a0bc86971b60c9b14a8a5d69ffddc243da104cd5a2251af34dcc2748046d ...
$ git update-index --add --chmod=+x gradlew
$ ./gradlew clean build
Downloading https://services.gradle.org/distributions/gradle-8.7-bin.zip
............10%.............20%.............30%.............40%............50%.............60%.............70%.............80%.............90%............100%
Welcome to Gradle 8.7!
Here are the highlights of this release:
 - Compiling and testing with Java 22
 - Cacheable Groovy script compilation
 - New methods in lazy collection properties
For more details see https://docs.gradle.org/8.7/release-notes.html
Starting a Gradle Daemon (subsequent builds will be faster)
> Task :clean UP-TO-DATE
> Task :compileJava
> Task :processResources
> Task :classes
> Task :resolveMainClassName
> Task :bootJar
> Task :jar
> Task :assemble
> Task :compileTestJava
> Task :processTestResources NO-SOURCE
> Task :testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :test
> Task :check
> Task :build
Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/8.7/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD SUCCESSFUL in 49s
8 actionable tasks: 7 executed, 1 up-to-date
Saving cache for successful job
00:01
Creating cache default-protected...
WARNING: .gradle/wrapper: no matching files. Ensure that the artifact path is relative to the working directory (/builds/team5racle/test2)
WARNING: .gradle/caches: no matching files. Ensure that the artifact path is relative to the working directory (/builds/team5racle/test2)
Archive is up to date!
Created cache
Uploading artifacts for successful job
00:04
Uploading artifacts...
build/libs/*.jar: found 2 matching artifact files and directories
WARNING: Upload request redirected                  location=https://gitlab.com/api/v4/jobs/6748578402/artifacts?artifact_format=zip&artifact_type=archive&expire_in=1+week new-url=https://gitlab.com
WARNING: Retrying...                                context=artifacts-uploader error=request redirected
Uploading artifacts as "archive" to coordinator... 201 Created  id=6748578402 responseStatus=201 Created token=glcbt-65
Cleaning up project directory and file based variables
00:01
Job succeeded
```

**build**

```shell
Running with gitlab-runner 16.11.0~pre.21.gaa21be2d (aa21be2d)
  on blue-2.saas-linux-small-amd64.runners-manager.gitlab.com/default XxUrkriX, system ID: s_f46a988edce4
  feature flags: FF_USE_IMPROVED_URL_MASKING:true
Resolving secrets
00:00
Preparing the "docker+machine" executor
00:13
Using Docker executor with image gradle:8.7-jdk17-alpine ...
Pulling docker image gradle:8.7-jdk17-alpine ...
Using docker image sha256:e2fca0d966895442c272c3752a58e1e7b6adcf09a1a0f716ce1e1450682bdb42 for gradle:8.7-jdk17-alpine with digest gradle@sha256:9c74a0bc86971b60c9b14a8a5d69ffddc243da104cd5a2251af34dcc2748046d ...
Preparing environment
00:04
Running on runner-xxurkrix-project-57389325-concurrent-0 via runner-xxurkrix-s-l-s-amd64-1714478091-dd2e7375...
Getting source from Git repository
00:01
Fetching changes with git depth set to 20...
Initialized empty Git repository in /builds/team5racle/test2/.git/
Created fresh repository.
Checking out b353f7f6 as detached HEAD (ref is main)...
Skipping Git submodules setup
$ git remote set-url origin "${CI_REPOSITORY_URL}"
Restoring cache
00:01
Checking cache for default-protected...
Downloading cache from https://storage.googleapis.com/gitlab-com-runners-cache/project/57389325/default-protected
Successfully extracted cache
Downloading artifacts
00:01
Downloading artifacts for build (6748578402)...
Downloading artifacts from coordinator... ok        host=storage.googleapis.com id=6748578402 responseStatus=200 OK token=glcbt-65
Executing "step_script" stage of the job script
00:51
Using docker image sha256:e2fca0d966895442c272c3752a58e1e7b6adcf09a1a0f716ce1e1450682bdb42 for gradle:8.7-jdk17-alpine with digest gradle@sha256:9c74a0bc86971b60c9b14a8a5d69ffddc243da104cd5a2251af34dcc2748046d ...
$ ./gradlew test
Downloading https://services.gradle.org/distributions/gradle-8.7-bin.zip
............10%.............20%.............30%.............40%............50%.............60%.............70%.............80%.............90%............100%
Welcome to Gradle 8.7!
Here are the highlights of this release:
 - Compiling and testing with Java 22
 - Cacheable Groovy script compilation
 - New methods in lazy collection properties
For more details see https://docs.gradle.org/8.7/release-notes.html
Starting a Gradle Daemon (subsequent builds will be faster)
> Task :compileJava
> Task :processResources
> Task :classes
> Task :compileTestJava
> Task :processTestResources NO-SOURCE
> Task :testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
> Task :test
Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/8.7/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD SUCCESSFUL in 48s
4 actionable tasks: 4 executed
Saving cache for successful job
00:00
Creating cache default-protected...
WARNING: .gradle/wrapper: no matching files. Ensure that the artifact path is relative to the working directory (/builds/team5racle/test2)
WARNING: .gradle/caches: no matching files. Ensure that the artifact path is relative to the working directory (/builds/team5racle/test2)
Archive is up to date!
Created cache
Cleaning up project dir
```

**deploy**

```bash
Running with gitlab-runner 16.11.0~pre.21.gaa21be2d (aa21be2d)
  on blue-5.saas-linux-small-amd64.runners-manager.gitlab.com/default -AzERasQ, system ID: s_4cb09cee29e2
  feature flags: FF_USE_IMPROVED_URL_MASKING:true
Resolving secrets
00:00
Preparing the "docker+machine" executor
00:12
Using Docker executor with image gradle:8.7-jdk17-alpine ...
Pulling docker image gradle:8.7-jdk17-alpine ...
Using docker image sha256:e2fca0d966895442c272c3752a58e1e7b6adcf09a1a0f716ce1e1450682bdb42 for gradle:8.7-jdk17-alpine with digest gradle@sha256:9c74a0bc86971b60c9b14a8a5d69ffddc243da104cd5a2251af34dcc2748046d ...
Preparing environment
00:05
Running on runner--azerasq-project-57389325-concurrent-0 via runner-azerasq-s-l-s-amd64-1714478166-87170077...
Getting source from Git repository
00:01
Fetching changes with git depth set to 20...
Initialized empty Git repository in /builds/team5racle/test2/.git/
Created fresh repository.
Checking out b353f7f6 as detached HEAD (ref is main)...
Skipping Git submodules setup
$ git remote set-url origin "${CI_REPOSITORY_URL}"
Restoring cache
00:00
Checking cache for default-protected...
Downloading cache from https://storage.googleapis.com/gitlab-com-runners-cache/project/57389325/default-protected
Successfully extracted cache
Downloading artifacts
00:02
Downloading artifacts for build (6748578402)...
Downloading artifacts from coordinator... ok        host=storage.googleapis.com id=6748578402 responseStatus=200 OK token=glcbt-65
Executing "step_script" stage of the job script
00:28
Using docker image sha256:e2fca0d966895442c272c3752a58e1e7b6adcf09a1a0f716ce1e1450682bdb42 for gradle:8.7-jdk17-alpine with digest gradle@sha256:9c74a0bc86971b60c9b14a8a5d69ffddc243da104cd5a2251af34dcc2748046d ...
$ command -v ssh-agent >/dev/null || ( apk update && apk add openssh )
fetch https://dl-cdn.alpinelinux.org/alpine/v3.19/main/x86_64/APKINDEX.tar.gz
fetch https://dl-cdn.alpinelinux.org/alpine/v3.19/community/x86_64/APKINDEX.tar.gz
v3.19.1-476-g0fc5112363c [https://dl-cdn.alpinelinux.org/alpine/v3.19/main]
v3.19.1-478-g9c03b9c8a03 [https://dl-cdn.alpinelinux.org/alpine/v3.19/community]
OK: 22995 distinct packages available
(1/8) Installing openssh-keygen (9.6_p1-r0)
(2/8) Installing libedit (20230828.3.1-r3)
(3/8) Installing openssh-client-common (9.6_p1-r0)
(4/8) Installing openssh-client-default (9.6_p1-r0)
(5/8) Installing openssh-sftp-server (9.6_p1-r0)
(6/8) Installing openssh-server-common (9.6_p1-r0)
(7/8) Installing openssh-server (9.6_p1-r0)
(8/8) Installing openssh (9.6_p1-r0)
Executing busybox-1.36.1-r15.trigger
OK: 137 MiB in 77 packages
$ eval $(ssh-agent -s)
Agent pid 19
$ echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add - > /dev/null
Identity added: (stdin) ((stdin))
$ mkdir -p ~/.ssh
$ chmod 700 ~/.ssh
$ echo "$SSH_KNOWN_HOSTS" >> ~/.ssh/known_hosts
$ chmod 644 ~/.ssh/known_hosts
$ ssh -o StrictHostKeyChecking=no ubuntu@"$DEPLOY_SERVER" 'rm -rf ~/app/build/*.jar'
Warning: Permanently added '43.203.253.137' (ED25519) to the list of known hosts.
$ ssh -o StrictHostKeyChecking=no ubuntu@"$DEPLOY_SERVER" 'mkdir -p ~/app/build'
$ scp build/libs/*.jar ubuntu@"$DEPLOY_SERVER":~/app/build/demo.jar
$ ssh -o StrictHostKeyChecking=no ubuntu@"$DEPLOY_SERVER" '~/deploy.sh'
> 현재 구동중인 애플리케이션 pid 확인
8037
> kill -15 8037
> 새 애플리케이션 배포
> JAR_NAME: demo.jar
Saving cache for successful job
00:01
Creating cache default-protected...
WARNING: .gradle/wrapper: no matching files. Ensure that the artifact path is relative to the working directory (/builds/team5racle/test2)
WARNING: .gradle/caches: no matching files. Ensure that the artifact path is relative to the working directory (/builds/team5racle/test2)
Archive is up to date!
Created cache
Cleaning up project directory an
```

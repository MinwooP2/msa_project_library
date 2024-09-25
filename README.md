# 파이널 프로젝트 - Library Project
- 본 프로젝트는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전 단계를 커버하도록 구성한 프로젝트입니다.
- 도서 등록, 대출, 반납, 내역 조회 등 도서관에서 사용하는 기본적인 도서 관리 시스템을 쿠버네티스 환경에서 MSA로 구현하였습니다. 

# 목차
- [파이널 프로젝트 - Library Project](#---)
  - [서비스 정의](#🌱-서비스-정의)
  - [클라우드 네이티브 모델링](#🌱-클라우드-네이티브-모델링)
  - [클라우드 네이티브 개발 with MSA](#🌱-클라우드-네이티브-개발-with-MSA)
  - [클라우드 네이티브 운영](#🌱-클라우드-네이티브-운영계)
 
<br>

-----
# 🌱 서비스 정의
### 기능적 요구사항
1. 사용자가 도서 목록을 조회한다.
2. 사용자가 도서를 대출한다.
3. 사용자가 도서를 반납한다.
4. 도서 반납 기한이 지나면 연체료가 부과된다.

   연체 기간에 따른 연체료
    - 1일 ~ 7일: 500원
    - 8일 ~ 14일: 1000원
    - 이후:  2000원
6. 관리자가 도서 정보를 등록, 수정, 삭제할 수 있다. 
7. 사용자가 자신이 대출한 책의 양과, 현재 누적 연체료를 확인할 수 있다. 

<br>

### 비기능적 요구사항

1. **트랜잭션**

- 재고보다 많은 양의 도서는 대출할 수 없어야 한다. (Sync 호출)
- 반납이 이루어지지 않은 상태에서는 연체료가 부과될 수 없어야 한다.
- 특정 책에 대해 대출된 수보다 많은 수의 반납이 이루어질 수 없어야 한다.
    
<br>

2. **장애격리**
- 도서 조회 기능이 정상적으로 작동하지 않더라도 대출 및 반납은 365일 24시간 이루어질 수 있어야 한다. (Async, Eventual Consistency)
- 대출 및 반납 시스템에 과부하가 발생하면 사용자가 잠시 대기하도록 유도하며, 시스템이 안정되면 다시 진행할 수 있어야 한다. (Circuit breaker, fallback)


<br>

3. **성능**
- 사용자가 특정 도서의 재고량을 실시간으로 확인할 수 있어야 한다. 
- 사용자가 자신이 대출한 책의 수와, 현재 누적 연체료를 실시간으로 확인할 수 있어야 한다. (CQRS)
- 도서의 재고보다 많은 수를 대출하면 해당 대출은 취소될 수 있어야 한다. (Event driven)

<br>


-----
# 🌱 클라우드 네이티브 모델링

## Event Storming 결과
![image](https://github.com/user-attachments/assets/3b9aea65-79dc-4b5e-a46b-49c2a859eef2)


## 클라우드, MSA 아키텍쳐 구성도
![image](https://github.com/user-attachments/assets/0cf7a6af-eda4-4d7c-8da7-013eab410005)



<br>

-----
# 🌱 클라우드 네이티브 개발 with MSA
클라우드 네이티브 모델링 단계에서 도출된 아키텍처에 따라, 각 Bounded Context 별로 대변되는 마이크로 서비스들을 Spring boot로 구현했다. 


## 단일 진입점 - gateway

프로젝트 내 마이크로 서비스들을 외부에서 접근하기 위해서는 단일 진입점인 gateway를 거쳐야한다. 

<br>

각 마이크로 서비스는 **ClusterIP type**으로 생성하고, gateway service는 **LoadBalancer type**으로 지정한다. 

<img width="788" alt="image" src="https://github.com/user-attachments/assets/2f2b0d1f-5ae0-4e61-a61b-977aeb99a382">

<br>
<br>


라우팅 룰을 지정해, 라우팅 경로와 각 서비스들을 mapping 한다. 

<img width="353" alt="image" src="https://github.com/user-attachments/assets/a433f795-70bd-488d-9724-8d6ead9ac159">


<br>
<br>

## 분산 데이터 프로젝션 - CQRS

관련 서비스 기능: 사용자는 자신이 **대출한 책의 수량**과 **현재 누적 연체료**를 확인할 수 있어야 한다.

  => `dashboard` 서비스에서 이를 조회할 수 있다.

![image](https://github.com/user-attachments/assets/a7f932bb-5a11-43a3-a0c1-249fda2f2eed)

#### `dashboard` 서비스의 `userInfo` table
- 사용자가 책을 대출할 때(`BookBorrowed` 이벤트가 발생할 때)
  - 해당 유저에 해당하는 entity가 있다면, 기존 entity의 대출권수를 증가시킨다. 
  - 해당 유저에 해당하는 entity가 없다면, 새로운 entity를 생성해 대출권수와 연체료를 설정한다. 
- 사용자가 책을 반납할 때(`BookReturned` 이벤트가 발생할 때), 대출권수를 감소시킨다. 
- 사용자가 책을 연체할 때(`BookExpired` 이벤트가 발생할 때), 연체료를 증가시킨다.

<br>

### 실제 구현 예시

##### 1. 사용자가 책을 4권 대여한다. 
<img width="1153" alt="image" src="https://github.com/user-attachments/assets/b7338dbf-554f-4c7d-b2c0-8e2753e11ab1">

<br>


##### 2. 사용자가 책을 3권 반납하고, 이는 7일 연체되었다. 
<img width="1153" alt="image" src="https://github.com/user-attachments/assets/d75ab7da-7901-4f65-9e43-50b4d59b7a36">

<br>

##### 3. 해당 사용자는 현재 총 1권의 책을 대여 중이고, 누적 연체료는 500원임을 확인할 수 있다. 
<img width="1153" alt="image" src="https://github.com/user-attachments/assets/15ab2ced-dbc9-4349-b76e-0a23f4adf39b">

<br>
<br>


## 분산 처리 및 보상 트랜잭션 - SAGA, Compensation 
사용자가 책을 **대출**했을 때, 해당 책의 재고보다 많은 양을 대출한 경우 해당 대출에 대해 **재고없음 이벤트**를 발생시켜 해당 대출의 **상태를 cancel로 변경**한다. 


### 실제 구현 예시

##### 1. 재고가 4개인 도서를 등록한다. 
=> Book BC에서 entity를 생성한다.

<img width="918" alt="image" src="https://github.com/user-attachments/assets/39fcfe4f-e2a7-4fee-81aa-fd1af433bf0a">



##### 2. 해당 도서를 5권 대출한다. 
=> Borrow Service 내의 borrow book command를 실행한다.

=> BookBorrowed event가 publish 된다.

=> 이를 sub하고 있던 Book Service가 이를 수신해 decrease stock command를 실행한다.
  
<img width="920" alt="image" src="https://github.com/user-attachments/assets/83ea5ba5-af75-48e5-9dce-8c30f962170d">


##### 3. 재고없음 이벤트가 발생한다. 
=> decrease stock command 내부 로직에 의해 재고보다 많은 양의 대출이므로 OutOfStock Event가 Publish된다.


<img width="910" alt="image" src="https://github.com/user-attachments/assets/08ef1508-a7df-4d68-b956-d4fce802b4ef">



##### 4. 해당 대출의 상태가 canceled로 변경된다. 
=> borrow 서비스는 이를 subscribe해 OutOfStock event를 수신하고, 해당 event에 대응하는 borrow entity의 status를 canceled로 변경한다.


<img width="922" alt="image" src="https://github.com/user-attachments/assets/7de1a9a7-5410-4dc8-af4f-094aeb67e968">

<br>

 
-----
# 🌱 클라우드 네이티브 운영

## HPA

- borrow 서비스에 대해 Horizontal Pod Autoscaler(HPA)를 적용해보았다.
- 서비스로 유입되는 요청의 양에 따라 Pod를 Kubernetes에서 수평적으로 확장한다. 
- Kubernetes의 metric server 를 통해 CPU 사용량을 측정해 이를 바탕으로 POD를 확장한다. 

<br>

siege Pod를 생성해 정상적으로 작동 하는지 확인한다. 

![image](https://github.com/user-attachments/assets/482b7145-e1a5-4a15-a492-6c90a6c794b8)

<br>

Auto Scaler를 설정한다

```
kubectl autoscale deployment borrow  --cpu-percent=50 --min=1 --max=3
```

- deployment라는 Deployment의 파드를 CPU 사용량에 따라 자동으로 1개에서 최대 3개까지 확장하도록 설정한다.
- CPU 사용률이 50%를 넘으면 확장이 트리거된다.

  
![image](https://github.com/user-attachments/assets/ba5c703d-e2bc-4fa4-b011-fc6715e49aa0)


<br>

`kubectl get hpa` 명령어로 설정값을 확인한다.

![image](https://github.com/user-attachments/assets/bf0e6c37-1d79-44bc-87dd-dad2ff06af47)


<br>

배포파일에 CPU 요청에 대한 값을 지정하고, 현재 배포된 borrow 서비스를 삭제하고 재배포한다.

![image](https://github.com/user-attachments/assets/74059ea7-0266-4c27-b8a8-20983acde5ca)


<br>


새로운 터미널을 열어서 seige 명령으로 부하를 주어서 Pod 가 늘어나도록 한다.

```
kubectl exec -it siege -- /bin/bash
siege -c20 -t40S -v http://borrow:8080/borrows 
# siege 도구를 사용하여 borrow 서비스의 `/borrows` 엔드포인트에 20명의 동시 접속자와 40초 동안 부하 테스트를 수행합니다.
```

<br>

`kubectl get po -w` 명령을 사용하여 pod 가 생성되는 것을 확인한다. 

![image](https://github.com/user-attachments/assets/14f8f236-ca9d-4916-91ba-73c986d70266)


<br>

`kubectl get hpa` 명령어로 CPU 값이 늘어난 것을 확인한다. 

![image](https://github.com/user-attachments/assets/6d4501bd-7727-4e83-8710-b272f57717fb)


<br>


## 무정지 배포 

- borrow 서비스에 대해 readiness probe 설정을 적용해 무정지 배포를 구현했다. 


readiness probe 설정 전 배포시 정지시간이 발생하는 것을 확인한다. 

![image](https://github.com/user-attachments/assets/23e0e2f0-824f-41fb-b5e7-9b7b6d1ccf95)

<br>

borrow 서비스의 `deployment.yaml` 파일에 readinessProbe 를 설정하고 배포를 진행한다. 

![image](https://github.com/user-attachments/assets/53c013ab-7e83-4fc8-a3d8-2b0144a9de09)

<br>

siege 터미널을 열어서 충분한 시간만큼 부하를 준다.

```
kubectl exec -it siege -- /bin/bash
siege -c1 -t60S -v http://borrow:8080/borrows --delay=1S
```

<br>

수정된 주문 서비스를 적용하여 배포한다.

`kubectl apply -f deployment.yaml`

<br>

siege 로그를 보면서 배포시 무정지로 배포된 것을 확인한다.

![image](https://github.com/user-attachments/assets/25c939f3-997b-4d65-965b-b8c0954ba99d)


<br>


## ConfigMap

- ConfigMap으로부터 필요한 환경정보를 전달받아 borrow 서비스를 실행한다.

다음과 같은 스펙을 가지는 ConfigMap 객체를 생성한다. 
```
apiVersion: v1
kind: ConfigMap
metadata:
  name: config-dev
  namespace: default
data:
  BORROW_LOG_LEVEL: DEBUG
```

<br>

borrow > kubernetes > deployment.yaml 파일에 configmap을 사용하는 코드를 추가한다.
```
          envFrom:
            - configMapRef:
                name: config-dev
```

<br>

borrow 서비스의 Logging 레벨을 Configmap의 `BORROW_DEBUG_INFO` 참조하도록 `application.yml` 파일을 수정한다. 
```
logging:
  level:
    org.hibernate.type: ${BORROW_LOG_LEVEL}
    org.springframework.cloud: ${BORROW_LOG_LEVEL}
```

<br>

docker hub 이미지를 생성하고 Cluster에 배포 후, 컨테이너 Log를 통해 DEBUG 로그레벨이 적용되었음을 확인한다. 

![image](https://github.com/user-attachments/assets/b54282e4-5ed4-4889-aad6-629b9f27e236)

<br>

Configmap에서 각 Container로 전달된 환경정보를 확인하기 위해 아래 커맨드를 실행하면 
=> 배포시 전달된 BORROW_LOG_LEVEL 정보가 borrow 서비스의 컨테이너 OS에 설정되었음을 알 수 있다.

![image](https://github.com/user-attachments/assets/b84f35ea-4eea-4da7-ab95-9978aca54a10)


<br>

## PVC

- NFS 파일시스템을 Azure 클라우드에 생성하고, 이를 borrow 서비스에서 마운트시켜 비정형 정보 저장소로 활용한다. 

<br>
다음과 같은 스펙을 가지는 Volume을 생성한다.

```
apiVersion: v1
kind: Pod
metadata:
  name: hostpath
spec:
  containers:
  - name: redis
    image: redis
    volumeMounts:
    - name: somepath
      mountPath: /data/shared
  volumes:
  - name : somepath
    hostPath:
      path: /tmp
      type: Directory
```

<br>

컨테이너로 접속하여 마운트된 볼륨을 확인한다.

![image](https://github.com/user-attachments/assets/c857d059-5edf-4c9c-a3c5-f041f33e0f5b)


<br>

다음 스펙을 가지는 PVC를 생성한다.

```
apiVersion: v1
kind: PersistentVolumeClaim
metadata: 
  name: azurefile
spec:
  accessModes:
  - ReadWriteMany
  storageClassName: azurefile
  resources:
    requests:
      storage: 1Gi
```

<br>
생성을 확인한다.  

![image](https://github.com/user-attachments/assets/5d9cba0f-2f6e-4097-ae9e-1f36260ee764)

<br>

NFS 볼륨을 가지는 borrow 서비스를 배포하기 위해 deploy 객체를 생성한다. 
```
apiVersion: "apps/v1"
kind: "Deployment"
metadata:
  name: borrow
  labels:
    app: "borrow"
spec:
  selector:
    matchLabels:
      app: "borrow"
  replicas: 1
  template:
    metadata:
      labels:
        app: "borrow"
    spec:
      containers:
      - name: "borrow"
        image: "minwoop/borrow:2"
        ports:
          - containerPort: 80
        volumeMounts:
          - mountPath: "/mnt/data"
            name: volume
      volumes:
      - name: volume
        persistentVolumeClaim:
          claimName: azurefile
```

<br>

배포 후 borrow 서비스의 컨테이너에 접속하여 제대로 파일시스템이 마운트되었는지 확인한다.

![image](https://github.com/user-attachments/assets/a9010733-3088-4400-b4bf-1e22c9ebbea5)

<br>


이후, borrow 서비스를 2개로 Scale Out하고 확장된 borrow 서비스에서도 `test.txt`가 확인되는지 검증한다.
또한, 2번째 컨테이너에서도 리소스를 생성해 본다. (ReadWriteMany)

![image](https://github.com/user-attachments/assets/2f300a34-4054-4d38-85f6-cfbb85acb505)
![image](https://github.com/user-attachments/assets/ac271a81-b7ba-4c0b-9bb2-c45015620012)

<br>


## CI/CD 파이프라인

- Book 서비스에 대해 Azure를 통해 CI/CD 파이프라인을 구성한다.  
- git repository에 commit이 발생했을 경우 자동으로 파이프라인이 실행되도록 trigger를 설정한다.

<br>

전체적인 pipeline flow는 다음과 같다.

![image](https://github.com/user-attachments/assets/13c747b6-fc82-4b08-9261-1896ec2dd564)


<br>

book service에 대한 CI

![image](https://github.com/user-attachments/assets/7810ddd5-73be-4829-b912-804aad11bf86)

<br>


book service에 대한 CD 

![image](https://github.com/user-attachments/assets/68824ee0-eef4-4beb-a012-78e154d7eb02)

<br>

CI/CD commit trigger

![image](https://github.com/user-attachments/assets/33bbe969-0a6c-4c9d-8a95-70a2a518b785)

<br>

CI/CD pipeline을 통해 자동으로 쿠버네티스 환경에 배포된 것을 확인할 수 있다.

![image](https://github.com/user-attachments/assets/3d27b45e-7786-4f79-86ce-b9f2f979b8db)

<br>


<br>


## Loggregation 

- Loki-stack을 이용해 book, borrow 서비스에 대한 로그리게이션을 설정한다. 

<br>

Helm으로 PLG 스텍 설치 및 Loki pod 생성 

<img width="925" alt="image" src="https://github.com/user-attachments/assets/2e8283a7-ba92-4dbd-a299-441aa61b8d9c">

<br>
<br>

통합 로깅 대상 서비스 설치

<img width="925" alt="image" src="https://github.com/user-attachments/assets/e686a22c-9241-451c-a2f1-803c5e9de27d">

<br>
<br>

Grafana External-IP생성 후 접속

<img width="823" alt="image" src="https://github.com/user-attachments/assets/9f09a013-41f3-46dd-b9e3-6ba8a9ab3bc4">


<br>
<br>

Loki를 통해 book, borrow 서비스에 대한 로그 확인 
<img width="1082" alt="image" src="https://github.com/user-attachments/assets/3ad03eab-c1f8-4b18-9864-cd3e42c22561">




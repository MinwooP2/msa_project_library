# 파이널 프로젝트 - Library Project
- 본 프로젝트는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전 단계를 커버하도록 구성한 프로젝트입니다.
- 도서 등록, 대출, 반납, 내역 조회 등 도서관에서 사용하는 기본적인 도서 관리 시스템을 쿠버네티스 환경에서 MSA로 구현하였습니다. 

# 목차
- [예제 - 음식배달](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

<br>

-----
# 🌱 서비스 정의
### 기능적 요구사항
1. 사용자가 도서 목록을 조회한다.
2. 사용자가 도서를 대출한다.
3. 사용자가 도서를 반납한다.
4. 도서 반납 기한이 지나면 연체료가 부과된다. 
5. 관리자가 도서 정보를 등록, 수정, 삭제한다
6. 사용자가 자신이 대출한 책의 양과, 현재 누적 연체료를 확인할 수 있다. 

<br>

### 비기능적 요구사항

1. **트랜잭션**

- 재고가 0인 도서는 다른 사용자가 대출할 수 없어야 한다. (Sync 호출)
- 반납이 이루어지지 않은 상태에서는 연체료가 부과될 수 없어야 한다.
  연체 기간에 따른 연체료
    - 1일 ~ 7일: 500원
    - 8일 ~ 14일: 1000원
    - 이후:  2000원
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

### flow 설명 

- 사용자는 


## 클라우드, MSA 아키텍쳐 구성도
![image](https://github.com/user-attachments/assets/0cf7a6af-eda4-4d7c-8da7-013eab410005)



<br>

-----
# 🌱 클라우드 네이티브 개발 with MSA
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 Spring boot로 구현했다. 


## 단일 진입점 - gateway
도서관 프로젝트 내 여러 서비스를 외부에서 접근하기 위해서는 단일 진입점인 gateway를 거쳐야한다. 
- 각 micro service는 CluseterIP type으로 생성하고, gateway service는 LoadBalancer type으로 지정한다. 

<img width="788" alt="image" src="https://github.com/user-attachments/assets/2f2b0d1f-5ae0-4e61-a61b-977aeb99a382">


라우팅 룰을 지정해, 각 서비스로 연결한다. 

<img width="353" alt="image" src="https://github.com/user-attachments/assets/a433f795-70bd-488d-9724-8d6ead9ac159">


<br>
<br>

## 분산 데이터 프로젝션 - CQRS

관련 서비스 기능: 사용자는 자신이 **대출한 책의 수량**과 **현재 누적 연체료**를 확인할 수 있어야 한다.

  => `dashboard` 서비스에서 이를 조회할 수 있다.

![image](https://github.com/user-attachments/assets/a7f932bb-5a11-43a3-a0c1-249fda2f2eed)

### `dashboard` 서비스의 `userInfo` table
- 사용자가 책을 대출할 때(`BookBorrowed` 이벤트가 발생할 때)
  - 해당 유저에 해당하는 entity가 있다면, 기존 entity의 대출권수를 증가시킨다. 
  - 해당 유저에 해당하는 entity가 없다면, 새로운 entity를 생성해 대출권수와 연체료를 설정한다. 
- 사용자가 책을 반납할 때(`BookReturned` 이벤트가 발생할 때), 대출권수를 감소시킨다. 
- 사용자가 책을 연체할 때(`BookExpired` 이벤트가 발생할 때), 연체료를 증가시킨다.

<br>

### 실제 구현 예시

사용자가 책을 4권 대여한다. 
<img width="1153" alt="image" src="https://github.com/user-attachments/assets/b7338dbf-554f-4c7d-b2c0-8e2753e11ab1">

<br>


사용자가 책을 3권 반납하고, 이는 7일 연체되었다. 
<img width="1153" alt="image" src="https://github.com/user-attachments/assets/d75ab7da-7901-4f65-9e43-50b4d59b7a36">

<br>

해당 사용자는 현재 총 1권의 책을 대여중이고, 누적 연체료는 500원임을 확인할 수 있다. 
<img width="1153" alt="image" src="https://github.com/user-attachments/assets/15ab2ced-dbc9-4349-b76e-0a23f4adf39b">

<br>
<br>


## 분산처리 및 보상 트랜잭션 - SAGA, Compensation 
사용자가 책을 대출했을 때, 해당 책의 재고보다 많은 양을 대출한 경우 해당 대출에 대해 재고없음 이벤트를 발생시켜 해당 대출의 상태를 cancel로 변경한다. 


### 실제 구현 예시

#### 재고가 4개인 도서를 등록한다. 
=> Book BC에서 entity를 생성한다.

<img width="918" alt="image" src="https://github.com/user-attachments/assets/39fcfe4f-e2a7-4fee-81aa-fd1af433bf0a">



#### 해당 도서를 5권 대출한다. 
=> Borrow BC 내의 borrow book command를 실행한다.
=> BookBorrowed event가 publish 된다.
=> 이를 sub하고 있던 Book Service가 이를 수신해 decrease stock command를 실행한다.
  
<img width="920" alt="image" src="https://github.com/user-attachments/assets/83ea5ba5-af75-48e5-9dce-8c30f962170d">


#### 재고없음 이벤트가 발생한다. 
=> decrease stock command 내부 로직에 의해 재고보다 많은 양의 대출이므로 OutOfStock Event가 Publish된다.


<img width="910" alt="image" src="https://github.com/user-attachments/assets/08ef1508-a7df-4d68-b956-d4fce802b4ef">




#### 해당 대출의 상태가 canceled로 변경된다. 
=> borrow 서비스는 이를 subscribe해 OutOfStock event를 수신하고, 해당 event에 대응하는 borrow entity의 status를 canceled로 변경한다.


<img width="922" alt="image" src="https://github.com/user-attachments/assets/7de1a9a7-5410-4dc8-af4f-094aeb67e968">

<br>

 
-----
# 🌱 클라우드 네이티브 운영

## HPA
생성된 siege Pod 안쪽에서 정상작동 확인

![image](https://github.com/user-attachments/assets/482b7145-e1a5-4a15-a492-6c90a6c794b8)


Auto Scaler를 설정한다

- 오토 스케일링 설정명령어 호출

```
kubectl autoscale deployment order --cpu-percent=50 --min=1 --max=3
```

- “cpu-percent=50 : Pod 들의 요청 대비 평균 CPU 사용율

    (YAML Spec.에서 요청량이 200 milli-cores일때, 모든 Pod의 평균 CPU 사용율이 100 milli-cores(50%)를 넘게되면 HPA 발생)”

![image](https://github.com/user-attachments/assets/ba5c703d-e2bc-4fa4-b011-fc6715e49aa0)


kubectl get hpa 명령어로 설정값을 확인 한다.

![image](https://github.com/user-attachments/assets/bf0e6c37-1d79-44bc-87dd-dad2ff06af47)

배포파일에 CPU 요청에 대한 값을 지정
현재, 배포된 주문서비스를 삭제하고 재배포한다.

![image](https://github.com/user-attachments/assets/74059ea7-0266-4c27-b8a8-20983acde5ca)

새로운 터미널을 열어서 seige 명령으로 부하를 주어서 Pod 가 늘어나도록 한다.

```
kubectl exec -it siege -- /bin/bash
siege -c20 -t40S -v http://order:8080/orders 
# siege 도구를 사용하여 order 서비스의 /orders 엔드포인트에 20명의 동시 접속자와 40초 동안 부하 테스트를 수행합니다.
```



kubectl get po -w 명령을 사용하여 pod 가 생성되는 것을 확인

![image](https://github.com/user-attachments/assets/14f8f236-ca9d-4916-91ba-73c986d70266)

kubectl get hpa 명령어로 CPU 값이 늘어난 것을 확인

![image](https://github.com/user-attachments/assets/6d4501bd-7727-4e83-8710-b272f57717fb)


<br>


## 무정지 배포 

rediness probe 설정 전  배포시 정지시간이 발생

![image](https://github.com/user-attachments/assets/23e0e2f0-824f-41fb-b5e7-9b7b6d1ccf95)


readinessProbe 를 설정하고 배포 진행

![image](https://github.com/user-attachments/assets/53c013ab-7e83-4fc8-a3d8-2b0144a9de09)


siege 터미널을 열어서 충분한 시간만큼 부하를 준다.

```
kubectl exec -it siege -- /bin/bash
siege -c1 -t60S -v http://borrow:8080/borrows --delay=1S
```

수정된 주문 서비스를 적용하여 배포한다

- kubectl apply -f deployment.yaml

siege 로그를 보면서 배포시 무정지로 배포된 것을 확인한다.

![image](https://github.com/user-attachments/assets/25c939f3-997b-4d65-965b-b8c0954ba99d)


<br>


## configMap

다음과 같은 스펙을 가지는 ConfigMap 객체를 생성
```
apiVersion: v1
kind: ConfigMap
metadata:
  name: config-dev
  namespace: default
data:
  BORROW_LOG_LEVEL: DEBUG
```

borrow > kubernetes > deployment.yaml 파일에 configmap을 사용하는 코드를 추가한다.
```
          envFrom:
            - configMapRef:
                name: config-dev
```


borrow 서비스의 Logging 레벨을 Configmap의 BORROW_DEBUG_INFO 참조하도록 application.yml 파일 수정 
```
logging:
  level:
    org.hibernate.type: ${BORROW_LOG_LEVEL}
    org.springframework.cloud: ${BORROW_LOG_LEVEL}
```

docker hub 이미지를 생성하고 Cluster에 배포 후, 컨테이너 Log를 통해 DEBUG 로그레벨이 적용되었음을 확인

![image](https://github.com/user-attachments/assets/b54282e4-5ed4-4889-aad6-629b9f27e236)


Configmap에서 각 Container로 전달된 환경정보를 확인하기 위해 아래 커맨드를 실행
=> 배포시 전달된 BORROW_LOG_LEVEL 정보가 borrow 서비스의 컨테이너 OS에 설정되었음을 알 수 있다.

![image](https://github.com/user-attachments/assets/b84f35ea-4eea-4da7-ab95-9978aca54a10)


<br>

## PVC

다음과 같은 스펙을 가지는 Volume을 생성한다
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

컨테이너로 접속하여 마운트된 볼륨을 확인한다.

![image](https://github.com/user-attachments/assets/c857d059-5edf-4c9c-a3c5-f041f33e0f5b)

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

생성 확인 

![image](https://github.com/user-attachments/assets/5d9cba0f-2f6e-4097-ae9e-1f36260ee764)


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

배포 후 borrow 서비스의 컨테이너에 접속하여 제대로 파일시스템이 마운트되었는지 확인한다.

![image](https://github.com/user-attachments/assets/a9010733-3088-4400-b4bf-1e22c9ebbea5)

이후, borrow 서비스를 2개로 Scale Out하고 확장된 borrow 서비스에서도 test.txt가 확인되는지 검증한다.
또한, 2번째 컨테이너에서도 리소스를 생성해 본다. (ReadWriteMany)

![image](https://github.com/user-attachments/assets/2f300a34-4054-4d38-85f6-cfbb85acb505)
![image](https://github.com/user-attachments/assets/ac271a81-b7ba-4c0b-9bb2-c45015620012)

<br>


## CI/CD 파이프라인

![image](https://github.com/user-attachments/assets/13c747b6-fc82-4b08-9261-1896ec2dd564)
다음과 같은 flow로 Pipeline이 구성됨



book service에 대한 CI 성공

![image](https://github.com/user-attachments/assets/7810ddd5-73be-4829-b912-804aad11bf86)

CD 성공

![image](https://github.com/user-attachments/assets/68824ee0-eef4-4beb-a012-78e154d7eb02)


커밋 후 CI/CD 자동화

![image](https://github.com/user-attachments/assets/33bbe969-0a6c-4c9d-8a95-70a2a518b785)

쿠버네티스 환경에 배포된 것을 확인할 수 있음
![image](https://github.com/user-attachments/assets/3d27b45e-7786-4f79-86ce-b9f2f979b8db)



<br>





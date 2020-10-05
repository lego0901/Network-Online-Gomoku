# Network-Online-Gomoku
서울대학교 2020년 가을학기 컴퓨터 네트워크 과목 과제로 만든 온라인 오목 프로그램입니다.

모든 소스코드는 **자바(Java)**로 제작했으며, **OpenJDK 11 이상 버전**에서 정상적으로 실행됨을 확인하였습니다.



## 컴파일 및 실행

우선 `make`, `java`가 설치되어있는 환경에서만 실행할 수 있습니다.

```bash
make
```

명령어를 통해 `server.jar`, `client.jar` 두 파일을 생성합니다. 이후 `configure.xml` 파일을 수정하여 서버의 호스트 주소와 포트 번호를 수정할 수 있습니다. 서버의 정보에 맞게 수정한 뒤, 해당 호스트에서

```bash
java -jar server.jar
```

로 콘솔 기반 서버를 실행합니다. 이후 클라이언트는 `configure.xml`을 서버와 같은 것으로 만든 뒤,

```bash
java -jar client.jar
```

을 통해 GUI 기반 프로그램을 실행합니다. 만약 `.jar` 확장자 파일에 대한 기본 프로그램을 JDK Platform Binary로 설정한다면 아이콘을 더블 클릭하여 클라이언트 프로그램을 실행할 수 있습니다. 단, 서버와 클라이언트 `.jar` 파일을 다른 곳으로 옮긴다면 정상적으로 실행되지 않습니다.



## 클라이언트 사용법

### 로그인

클라이언트 프로그램을 실행했을 때, 서버와 성공적으로 연결이 되면 아래와 같이 플레이어 ID를 생성할 수 있는 화면이 나옵니다.

<p align="center">
<img height="360" src=document/resource/login.PNG>
</p>


여기서 보이는 텍스트 필드에 2글자 이상, 20글자 이하의 영문 플레이어 ID를 입력하고 엔터키를 누르거나 **Confirm 버튼**을 누르면 로그인할 수 있습니다. 만약 접속 중인 중복 ID가 있다면 다시 시도해야합니다.



### 게임 방 선택하기

플레이어 ID를 성공적으로 생성하고나면 접속할 방을 고르는 화면이 나옵니다. **Refresh 버튼**을 누르면 방 리스트를 다시 서버에서 받아옵니다. 이 버튼을 누르지 않더라도 방 정보가 바뀐 것이 있다면 자동으로 새로고침합니다.

<p align="center">
<img height="360" src=document/resource/room_search.PNG>
</p>


- **(wait)** 접두어로 된 방은 한 명의 플레이어만 접속하고 있는 곳으로, 접속이 가능합니다.
- **(full)** 접두어는 이미 두 명의 플레이어가 있는 방으로 접속이 불가능합니다.

리스트 안의 방 라벨을 누르면 아래 텍스트 필드에 해당 방의 이름이 채워집니다.

- 텍스트 필드를 채운 뒤 **Join 버튼**을 누르면 해당 방에 접속 시도를 할 수 있습니다.
- 새로운 방을 만들고자하면 텍스트 필드에 2글자 이상, 20글자 이하 영문 방 이름을 넣고 **Create 버튼**을 누르면 생성할 수 있습니다. 물론 중복은 허용하지 않습니다.



### 게임 방 대기 화면

방에 접속하고 나면 아래와 같은 화면이 보입니다.

<p align="center">
<img height="360" src=document/resource/room.PNG>
</p>


상대 플레이어가 아직 들어오지 않는다면 (Waiting) 라벨이 보입니다. 상대 플레이어가 들어오면 해당 ID가 보입니다.

- **Ready 버튼**을 통해 준비할 수 있고,
- **Cancel 버튼**으로 준비를 취소할 수 있습니다.

**Leave 버튼**을 누르면 방을 나가서 다시 게임 방 선택 화면으로 돌아갑니다. 두 플레이어가 모두 준비 상태이면 게임이 시작됩니다.



### 게임 플레이 화면

게임이 시작되면 상단에 누구의 차례인지 표기가 되며, 각 차례에 놓는 돌을 색깔도 굵게 표시됩니다.

<p align="center">
<img height="360" src=document/resource/game.PNG>
</p>

각 차례마다 플레이어에게 남은 고민할 시간이 중앙에 초단위로 실시간으로 표시됩니다. 중복된 위치에 돌을 놓거나, 보드 밖에 돌을 놓는 경우 붉은 색으로 경고 문구가 나타납니다. 한 플레이어가 연속된 5개의 돌을 놓으면 승리합니다. 게임이 끝나면 다시 대기 방으로 돌아갑니다.



## 게임 플레이 규칙

- 먼저 방에 접속한 플레이어가 선수(흑), 나중 접속한 플레이어가 후수(백) 입니다.
- 먼저 연속된 5개의 돌을 배치하는 플레이어가 승리합니다.
- 각 차례마다 고민할 수 있는 최대 시간은 60초 입니다.
- 만약 보드 가장자리에 돌을 2회 이상 둔다면 곧바로 패배합니다. (과제 조건)
- 만약 보드 위에 놓인 돌의 총 개수가 50개 이상이라면 무승부로 간주합니다.

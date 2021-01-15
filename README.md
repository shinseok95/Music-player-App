# Music-player-App
> '모바일 프로그래밍' 수업에서 배운 내용에 대해 연습 목적으로 간단히 구현해본 앱입니다.<br><br>

**4대 컴포넌트를 모두 활용**하였고, 특히나 서비스를 통해 액티비티가 닫히더라도 재생 가능하능하도록 구현하였습니다.<br>
또한, **Notification을 구현**하여 서비스를 통해 Background에서 재생되는 음악을 컨트롤 할 수 있도록 기능을 추가하였습니다.<br>
otification 클래스는 **Singleton으로 제작**하여 동기화 문제를 방지하였으며, **Observer 패턴**을 통해 기존 재생 상태에 변화가 생긴다면 이를 감지하고 대응하도록 하였습니다.

## 사용된 4대 컴포넌트

> Activity, BroadCast Receiver, Service, Content Provider

## 스크린샷

<div>
<img src="https://user-images.githubusercontent.com/48644958/103906379-2deb8300-5143-11eb-8a0c-f85de9741e2b.png" height="20%" width="20%"></img>
<img src="https://user-images.githubusercontent.com/48644958/103906385-2e841980-5143-11eb-97f0-185289175ed5.png" height="20%" width="20%"></img>
<img src="https://user-images.githubusercontent.com/48644958/103906386-2f1cb000-5143-11eb-86b6-68b5c47aca0a.png" height="20%" width="20%"></img>
</div>

## 개발 환경

> 운영체제 : Windows10<br>
> 개발언어 : Java<br>

# Music-player-App (Toy Project)

<div>
<img src="https://user-images.githubusercontent.com/48644958/103906379-2deb8300-5143-11eb-8a0c-f85de9741e2b.png" height="20%" width="20%"></img>
<img src="https://user-images.githubusercontent.com/48644958/103906385-2e841980-5143-11eb-97f0-185289175ed5.png" height="20%" width="20%"></img>
<img src="https://user-images.githubusercontent.com/48644958/103906386-2f1cb000-5143-11eb-86b6-68b5c47aca0a.png" height="20%" width="20%"></img>
</div>



 연습 목적으로 간단한 Music player app을 제작하였습니다. 서비스를 통해 액티비티가 닫히더라도 재생 가능하도록 구현하였으며, Notification을 구현하여 Backgound에서 재생되는 음악을 컨트롤 할 수 있도록 하였습니다. 
또한, Notificaion 클래스는 singleton으로 제작하여 동기화 문제를 방지하였으며, Observer 패턴을 통해 기존 재생 상태에 변화가 생긴다면, 이를 감지하고 대응하도록 하였습니다. 

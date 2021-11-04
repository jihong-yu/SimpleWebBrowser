# SimpleWebBrowser
모바일 웹브라우져의 간단한 기능 뒤로가기 앞으로가기 홈으로가기 새로고침 등을 구현하는 간단한 웹브라우져 app 입니다.

### 1. 개발환경
* IDE: Android Studio Arctic Fox | 2020.3.1 Canary 1
* Language : Kotlin
---
### 2. 사용 라이브러리
* swiperefreshlayout 라이브러리 사용
* WebViewClient,WebChromeClient 안드로이드 내장 기능 사용
---
### 3. 지원기능
1. 뒤로가기,앞으로가기,홈으로가기 버튼을 지원합니다.(뒤로가기나 앞으로갈 페이지 없으면 버튼이 disabled 됩니다.)
2. 페이지를 아래로 잡아 당길경우 새로고침 되도록 하였습니다.
3. 주소창에 https:// 와 같이 완전한 인터넷 주소(url)을 입력하지 않아도 해당 페이지로 이동되도록 설정하였습니다.
<img src="https://user-images.githubusercontent.com/57440834/140285033-9c9249f5-0b25-411f-af79-31368ab66b49.png" width="700" height="1000">
<img src="https://user-images.githubusercontent.com/57440834/140285060-dbc0f50c-15ea-4a44-bb4e-c31556590411.png" width="700" height="1000">
---



### 4. 추가설명
기초적인 어플이기 때문에 ViewBinding 과 같은 라이브러리는 사용하지 않았으며 모두 각각 findViewById로 xml 뷰에 접근하였습니다.<br>

```kotlin
private val seekBar:SeekBar by lazy {
        findViewById(R.id.seekbar)
    }
```
<br>

시크바를 조정하면 각 상황(시크바를 터치했을때, 멈췄을 때, 움직이고 있을 때)에 맞게 설정할 수 있도록 setOnSeekBarChangeListener를 오버라이드하여 구현하였습니다.
```kotlin
//시크바에 타이머 적용(뷰에 기능추가)
    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    //시크바에 움직임에 때라 남은 시간을 화면에 표시
                    updateRemainTime(progress * 60 * 1000L)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //카운트다운 정지
                stopCountDown()
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //엘비스 연산자를 통해 null처리
                seekBar ?: return

                if (seekBar.progress == 0){
                    stopCountDown()
                } else{
                    //사용자가 손 때자 말자 카운트다운 시작
                    startCountDown()
                }
            }
        })
    }
```
<br>



카운트다운 시작 메서드(startCountDown)는 CountDownTimer의 익명 객체를 생성하면서 생성하도록 하였으며 2개의 메서드(onTick,onFinish)를 오버라이드 하여 각각 주기마다 실행할 작업과 완료되었을 때 실행할 작업을 구현하였습니다.
  ```kotlin
  //CountTimer 익명클래스 생성
    private fun createCountDownTimer() : CountDownTimer{

        //1st : 카운트 다운 총 시간, 2nd: 카운트다운 주기
        return object : CountDownTimer(seekBar.progress * 60 * 1000L,1000L){

            //2nd인자인 1초 주기마다 해당 메서드 실행(인자로는 총 시간에서 남은시간까지의 시간이 계속 전달된다.)
            override fun onTick(millisUntilFinished: Long) {
                updateRemainTime(millisUntilFinished)
                //시크바도 계속해서 왼쪽으로 줄어들어야 하기때문에 업데이트 해준다.
                updateSeekBar(millisUntilFinished)

            }
            //완료된 후 처리
            override fun onFinish() {
                completeCountDown()
            }
        }

    }
  ```
  <br>
  
  

  사운드를 load하고 play는 nullsafe하게 하기 위해 변수를 null을 허용하게 선언하였고 let을 사용하여 다음과 같은 방식으로 구현하였습니다.
  ```kotlin
 //시간이 흐름에 따라 발생하는 효과음을 적용할 2개의 사운드 Int형 변수 선언(Nullsafe)
    private var tickingSoundId : Int? = null
    private var bellSoundId:Int? = null
    
    //2개의 사운드 load하여 변수에 대입
    private fun initSounds(){
        tickingSoundId = soundPool.load(this@MainActivity,R.raw.timer_ticking,1)
        bellSoundId = soundPool.load(this@MainActivity,R.raw.timer_bell,1)
    }
    
    //thickingSoundId가 만약 null일때를 nullsafe하게 처리하기 위한 let사용
        tickingSoundId?.let {
            soundPool.play(it,1.0F,1.0F,0,0,1.0F)
        } //play는 streamID를 리턴한다.(음악을 정지,시작할때 필요함) load는 SoundId를 
    
  ```
  <br>
  
  
마지막으로 SoundPool 과 같은 기능들은 메모리를 많이 먹기 때문에 생명주기 (onpause에서는 소리를 멈추게 하였고 onResume에서 시작하게 하여 백그라운드에서 실행되지 않도록 하였습니다.) 또한 onDestroy에서 앱이 꺼지기 직전에 메모리를 완전히 release 하도록 하였습니다.
```kotlin
override fun onPause() {
        super.onPause()
        soundPool.autoPause()
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        //soundPoll과 같은 기능들은 메모리를 많이 먹기 때문에 앱이 꺼진다면 메모리를 없애주는게 좋음
        soundPool.release()
    }
```


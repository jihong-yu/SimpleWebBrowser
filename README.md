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
<img src="https://user-images.githubusercontent.com/57440834/140349532-41f8dcb8-7b6b-4e16-b458-f05ec26dd781.png" width="700" height="1000">
---



### 4. 추가설명
기초적인 어플이기 때문에 ViewBinding 과 같은 라이브러리는 사용하지 않았으며 모두 각각 findViewById로 xml 뷰에 접근하였습니다.<br>

```kotlin
private val webView: WebView by lazy {
        findViewById(R.id.webView)
    }
```
<br>

기본적으로 인터넷 주소를 로딩하게 되면 휴대폰 내부의 기본 인터넷 어플리케이션으로 실행이 되기 때문에 내부 webViewClient로 실행되게끔 설정 해주었습니다. 또한 페이지 내부의 자바스크립트도 사용가능하도록 하였습니다.
```kotlin
webView.apply {
            //웹 사이트는 원래 휴대폰내장 기본웹어플레이케이션으로 구동하게 되어있는데 웹뷰로
            //웹사이트를 구동하게끔 client를 바꿔준다.
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            loadUrl(DEFAUT_URL) //인자로 전달된 페이지 로드
            settings.javaScriptEnabled = true //페이지 내부의 자바스크립트를 사용가능하게 한다.
        }
```
<br>



페이지 로딩이 시작되었을때 그리고 완료되었을 때 ContentLoadingProgressbar를 설정하여 페이지의 로딩정도를 표시하였습니다. 단, 페이지의 로딩정도는 WebChromeClient를 상속받아서 onProgressChanged 의 콜백메서드에서 인자로 전달되기 관련 메서드는 때문에 WebChromeClient에서 구현하였습니다. 나머지 뒤로가기 앞으로가기 버튼 disabled 등과 같은 간단한 구현은 WebViewClient를 상속받아서 오버라이드로 구현하였습니다.
  ```kotlin
  inner class WebChromeClient() : android.webkit.WebChromeClient() {

        //현재페이지 로딩상태를 숫자로 불러온다.(1st 해당웹뷰,2nd: 진행상태)
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            //현재 로딩정도를 컨텐트 프로그레스바에 대입
            progressBar.progress = newProgress
        }
    }

    inner class WebViewClient() : android.webkit.WebViewClient() {

        //페이지 로딩이 시작되었을 때
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            //로딩 프로그레스 바를 보여준다.
            progressBar.show()

        }

        //페이지 로딩이 완료되었을 때(2nd url에는 완전한 full 주소가 인자로 넘어옴)
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            //리프레싱 되고 있다는 동그라미 표시를 false를 주면서 없앤다.
            refreshLayout.isRefreshing = false

            //로딩 프로그레스 바를 뷰에서 없앤다.
            progressBar.hide()

            //주소창에 full 주소 표시하기
            addressBar.setText(url)

            //뒤로가기 버튼이 만약 웹뷰에서 뒤로갈 수 없다면 disabled시킨다.
            if (!webView.canGoBack()) {
                goBackButton.isEnabled = false
            }

            //앞으로가기 버튼이 만약 웹뷰에서 앞으로 갈수 없다면 disabled시킨다.
            goForwardButton.isEnabled = webView.canGoForward()
        }
    }
  ```
  <br>
  
  

  주소창에 주소를 입력하고 휴대폰 키보드 내부에서 엔터키를 눌렀을 경우 해당 주소로 이동할 수 있도록 editText의 setOnEditorActionListener 의 콜백메서드를 이용하여 구현하였습니다. 또한 EditTExt에 XML상에서 imeOptions 값을 actionGo로 설정하여 엔터키를 눌렀을 경우 해당 주소로 이동하며 키보드창은 내려가도록 구현하였습니다. 사용자가 http://를 입력하지 않았을 경우 URLUtil.isValidUrl 메소드를 이용하여 없다면 추가하도록 만들어 주었습니다. 
  ```kotlin
 //휴대폰 키보드 엔터키(Action) 리스너. v에는 해당 view가 반환, actionId에는 해당 actionId가 반환
        addressBar.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_GO) {
                val loadingUrl = v.text.toString()
                //만약 URL에 https 나 http 가 존재하지 않는다면
                if (URLUtil.isValidUrl(loadingUrl)) {//(주소가 Url형식인지 체크)
                    webView.loadUrl(loadingUrl)
                } else {
                    webView.loadUrl("http://$loadingUrl")
                }
            }

            return@setOnEditorActionListener false
            //Action을 계속 유지해야한다면 true , 눌리고 Action이 필요가 없으면 false
        }
  ```
  <br>
  
  
마지막으로 companion object를 이용하여 기본URL값을 등록하였습니다.
```kotlin
companion object {
        private const val DEFAUT_URL = "http://www.google.com"
    }
```


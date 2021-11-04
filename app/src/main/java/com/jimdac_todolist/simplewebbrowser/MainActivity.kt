package com.jimdac_todolist.simplewebbrowser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEFAUT_URL = "http://www.google.com"
    }

    private val goHomeButton: ImageButton by lazy {
        findViewById(R.id.goHomeButton)
    }
    private val goBackButton: ImageButton by lazy {
        findViewById(R.id.goBackButton)
    }
    private val goForwardButton: ImageButton by lazy {
        findViewById(R.id.goForwardButton)
    }

    private val refreshLayout: SwipeRefreshLayout by lazy {
        findViewById(R.id.refreshLayout)
    }

    private val addressBar: EditText by lazy {
        findViewById(R.id.addressBar)
    }

    private val webView: WebView by lazy {
        findViewById(R.id.webView)
    }

    private val progressBar: ContentLoadingProgressBar by lazy {
        findViewById(R.id.progressBar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //초기 뷰 설정
        initViews()
        //뷰에 기능 추가
        bindViews()
    }

    //뷰에 기능 추가
    private fun bindViews() {

        //휴대폰 키보드 엔터키(Action) 리스너. v에는 해당 view가 반환, actionId에는 해당 actionId가 반환
        addressBar.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
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

        goBackButton.setOnClickListener {
            webView.goBack() //웹페이지 뒤로가기
        }

        goForwardButton.setOnClickListener {
            webView.goForward() //웹페이지 앞으로 가기
        }

        goHomeButton.setOnClickListener {
            webView.loadUrl(DEFAUT_URL) //홈으로 이동
        }

        //리프레쉬 레이아웃을 위로 드래그 했을때 발생하는 리스너
        refreshLayout.setOnRefreshListener {
            webView.reload()
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {

        webView.apply {
            //웹 사이트는 원래 휴대폰내장 기본웹어플레이케이션으로 구동하게 되어있는데 웹뷰로
            //웹사이트를 구동하게끔 client를 바꿔준다.
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            loadUrl(DEFAUT_URL) //인자로 전달된 페이지 로드
            settings.javaScriptEnabled = true //페이지 내부의 자바스크립트를 사용가능하게 한다.
        }
    }

    //웹브라우저의 고급기능과 같은 기능 들을 구현할때는 WebChromeClient를 상속받아서 구현한다.
    //그렇지 않을 경우 WebViewClient 만으로 구현한다.
    inner class WebChromeClient() : android.webkit.WebChromeClient() {

        //현재페이지 로딩상태를 숫자로 불러온다.(1st 해당웹뷰,2nd: 진행상태)
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            //현재 로딩정도를 컨텐트 프로그레스바에 대입
            progressBar.progress = newProgress

            Toast.makeText(this@MainActivity, "$newProgress", Toast.LENGTH_SHORT).show()
        }
    }

    //웹브라우저의 고급기능과 같은 기능 들을 구현할때는 WebChromeClient를 상속받아서 구현한다.
    //그렇지 않을 경우 WebViewClient 만으로 구현한다.
    inner class WebViewClient() : android.webkit.WebViewClient() {

        //페이지 로딩이 시작되었을 때
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d("TAG", "onPageStarted: ")
            //로딩 프로그레스 바를 보여준다.
            progressBar.show()

        }

        //페이지 로딩이 완료되었을 때(2nd url에는 완전한 full 주소가 인자로 넘어옴)
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d("TAG", "onPageFinished: ")
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
}
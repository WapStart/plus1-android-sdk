package ru.wapstart.plus1.sdk;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

public class ApplicationBrowser extends Activity {
	public static final String URL_EXTRA = "extra_url";
	private static final String LOGTAG = "ApplicationBrowser";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		setContentView(R.layout.mraid_browser);

		Intent intent = getIntent();
		initializeWebView(intent);
		initializeButtons(intent);
		enableCookies();
	}

	private void initializeWebView(Intent intent) {
		WebView webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setAllowFileAccess(false);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(intent.getStringExtra(URL_EXTRA));
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode, String description,
					String failingUrl) {

				Activity a = (Activity) view.getContext();
				Toast.makeText(a, "Browser error: " + description, Toast.LENGTH_SHORT).show();

				Log.e(
					LOGTAG,
					String.format(
						"Browser error %s (errorCode=%d) when loading url '%s'",
						description,
						errorCode,
						failingUrl
					)
				);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (Plus1Helper.isIntentUrl(url)) {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					try {
						view.getContext().startActivity(intent);
						ApplicationBrowser.this.finish();
					} catch (ActivityNotFoundException e) {
						if (Plus1Helper.isPlayMarketIntentUrl(url)) {
							String playUrl = "http://play.google.com/store/apps/" + uri.getHost() + "?" + uri.getQuery();
							Log.i(
								LOGTAG,
								String.format(
									"Could not open link '%s' because Google Play app is not installed, we will open the app store link: '%s'",
									url,
									playUrl
								)
							);
							view.loadUrl(playUrl);
						} else {
							Log.e(LOGTAG, "Could not handle intent with URI: " + url);
							return false;
						}
					}
				} else {
					view.loadUrl(url);
				}

				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				ImageButton forwardButton = (ImageButton) findViewById(R.id.browserForwardButton);
				forwardButton.setImageResource(R.drawable.unrightarrow);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

				ImageButton backButton = (ImageButton) findViewById(R.id.browserBackButton);
				int backImageResource = (view.canGoBack()) ?
						R.drawable.leftarrow : R.drawable.unleftarrow;
				backButton.setImageResource(backImageResource);

				ImageButton forwardButton = (ImageButton) findViewById(R.id.browserForwardButton);
				int fwdImageResource = (view.canGoForward()) ?
						R.drawable.rightarrow : R.drawable.unrightarrow;
				forwardButton.setImageResource(fwdImageResource);
			}
		});

		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				Activity a = (Activity) view.getContext();
				a.setTitle("Loading...");
				a.setProgress(progress * 100);
				if (progress == 100) a.setTitle(view.getUrl());
			}
		});
	}

	private void initializeButtons(Intent intent) {
		ImageButton backButton = (ImageButton) findViewById(R.id.browserBackButton);
		backButton.setBackgroundColor(Color.TRANSPARENT);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				WebView webView = (WebView) findViewById(R.id.webView);
				if (webView.canGoBack()) webView.goBack();
			}
		});

		ImageButton forwardButton = (ImageButton) findViewById(R.id.browserForwardButton);
		forwardButton.setBackgroundColor(Color.TRANSPARENT);
		forwardButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				WebView webView = (WebView) findViewById(R.id.webView);
				if (webView.canGoForward()) webView.goForward();
			}
		});

		ImageButton refreshButton = (ImageButton) findViewById(R.id.browserRefreshButton);
		refreshButton.setBackgroundColor(Color.TRANSPARENT);
		refreshButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				WebView webView = (WebView) findViewById(R.id.webView);
				webView.reload();
			}
		});

		ImageButton closeButton = (ImageButton) findViewById(R.id.browserCloseButton);
		closeButton.setBackgroundColor(Color.TRANSPARENT);
		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ApplicationBrowser.this.finish();
			}
		});
	}

	private void enableCookies() {
		CookieSyncManager.createInstance(this);
		CookieSyncManager.getInstance().startSync();
	}

	@Override
	protected void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
	}

	@Override
	protected void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().startSync();
	}
}

package ru.wapstart.plus1.bart;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.widget.ImageView;

import java.util.Random;
import java.util.Date;

import ru.wapstart.plus1.sdk.Plus1BannerView;
import ru.wapstart.plus1.sdk.Plus1BannerRequest;
import ru.wapstart.plus1.sdk.Plus1BannerAsker;

public class BartActivity extends Activity implements View.OnClickListener
{
	private MediaPlayer mp;
	private Plus1BannerAsker mAsker;
	private Plus1BannerView mBannerView;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		ImageView imageView = (ImageView) findViewById(R.id.background);
		Random random = new Random(new Date().getTime());

		imageView.setBackgroundColor(
			Color.rgb(
				random.nextInt(256),
				random.nextInt(256),
				random.nextInt(256))
		);

		imageView.setImageResource(R.drawable.bartsimpson);

		imageView.setOnClickListener(this);

		mBannerView =
			(Plus1BannerView) findViewById(R.id.plus1BannerView);

		mAsker =
			new Plus1BannerAsker(
				new Plus1BannerRequest()
					.setApplicationId(/* Place your WapStart Plus1 application id here */),
				mBannerView
					.enableAnimationFromTop()
					.enableCloseButton()
			)
			.setTimeout(10); // default value

		Log.d("BartActivity", "onCreate fired");
    }

	public void onClick(View view) {
		if (mp == null)
			this.mp = MediaPlayer.create(getApplicationContext(), R.raw.laugh);

		mp.start();
		mAsker.refreshBanner();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mAsker.onResume();
		Log.d("BartActivity", "onResume fired");
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mp != null) {
			mp.release();
			mp = null;
		}

		mAsker.onPause();
		Log.d("BartActivity", "onPause fired");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("BartActivity", "onKeyDown fired");
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mBannerView.canGoBack()) {
			mBannerView.goBack();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}

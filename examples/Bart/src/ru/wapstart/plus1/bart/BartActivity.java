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

import ru.wapstart.plus1.conversion.helloconversion.HelloConversionActivity;
import ru.wapstart.plus1.conversion.sdk.Plus1ConversionTracker;
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
		Log.d("BartActivity", "onCreate fired");

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
			.setRefreshDelay(10); // default value

		mBannerView
			.addListener(new Plus1BannerView.OnShowListener() {
				public void onShow(Plus1BannerView pbv) {
					Log.d("BartActivity", "OnShowListener was touched");
				}
			})
			.addListener(new Plus1BannerView.OnHideListener() {
				public void onHide(Plus1BannerView pbv) {
					Log.d("BartActivity", "OnHideListener was touched");
				}
			})
			.addListener(new Plus1BannerView.OnCloseButtonListener() {
				public void onCloseButton(Plus1BannerView pbv) {
					Log.d("BartActivity", "OnCloseButtonListener was touched");
				}
			})
			.addListener(new Plus1BannerView.OnImpressionListener() {
				public void onImpression(Plus1BannerView pbv) {
					Log.d("BartActivity", "OnImpressionListener was touched");
				}
			})
			.addListener(new Plus1BannerView.OnTrackClickListener() {
				public void onTrackClick(Plus1BannerView pbv) {
					Log.d("BartActivity", "OnTrackClickListener was touched");
				}
			});
    }

	public void onClick(View view) {
		if (mp == null)
			this.mp = MediaPlayer.create(getApplicationContext(), R.raw.laugh);

		mp.start();
		mAsker.refreshBanner();
	}

	@Override
	protected void onResume() {
		Log.d("BartActivity", "onResume fired");

		super.onResume();

		// Uncomment the block bellow if you want to use Plus1 WapStart Conversion SDK
		/**
		try {
			new Plus1ConversionTracker(this)
				.setTrackId(Place your WapStart Plus1 conversion track id here)
				.setCallbackUrl("wsp1hc://ru.wapstart.plus1.bart")
				.run();
		} catch (Plus1ConversionTracker.TrackIdNotDefinedException e) {
			Log.e(
				HelloConversionActivity.class.getSimpleName(),
				"You must define conversion track id"
			);
		} catch (Plus1ConversionTracker.CallbackUrlNotDefinedException e) {
			Log.e(
				HelloConversionActivity.class.getSimpleName(),
				"You must define callback url"
			);
		}
		*/

		mAsker.onResume();
	}

	@Override
	protected void onPause() {
		Log.d("BartActivity", "onPause fired");

		super.onPause();

		if (mp != null) {
			mp.release();
			mp = null;
		}

		mAsker.onPause();
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

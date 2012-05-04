package ru.wapstart.plus1.bart;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;
import java.util.Date;

import ru.wapstart.plus1.sdk.*;

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
					.setRotatorUrl("http://ro.zlex.plus1.oemtest.ru/")
					//.setRotatorUrl("http://ro.trunk.plus1.oemtest.ru/testmraid.php")
					//.setRotatorUrl("http://ro.trunk.plus1.oemtest.ru/testmraid_sz.php")
					//.setApplicationId(352),
					.setApplicationId(4550),
					//.setApplicationId(105261),
				mBannerView
					.setAutorefreshEnabled(true)
					.enableAnimationFromTop()
					.enableCloseButton()
			)
			.setTimeout(10);

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

		mAsker.onPause();
		Log.d("BartActivity", "onPause fired");
	}
}

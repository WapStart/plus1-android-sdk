package ru.wapstart.plus1.bart;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
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
    public void onCreate(Bundle savedInstanceState)
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
					//.setRotatorUrl("http://ro.trunk.plus1.oemtest.ru/")
					.setRotatorUrl("http://ro.trunk.plus1.oemtest.ru/testmraid.php")
					//.setApplicationId(352),
					.setApplicationId(105261),
				mBannerView
					.setAutorefreshEnabled(true)
					.enableAnimationFromTop()
					.enableCloseButton()
			)
			.setTimeout(10);
    }

	public void onClick(View view) {
		if (mp == null)
			this.mp = MediaPlayer.create(getApplicationContext(), R.raw.laugh);

		mp.start();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		mAsker.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mAsker.stop();
	}

    @Override
    protected void onDestroy() {
        mBannerView.destroy();
        super.onDestroy();
    }
}

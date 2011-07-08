package ru.wapstart.plus1.sdk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

final class Plus1ImageView extends View {
	private Drawable	image;
	private Movie		movie;
	private long 		movieStart;
	
	public Plus1ImageView(Context context) {
		super(context);
	}

	public Plus1ImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Plus1ImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setImage(Drawable drawable) {
		this.image	= drawable;
		this.movie	= null;

		this.movieStart = 0;
	}
	
	public void setMovie(Movie movie) {
		this.movie	= movie;
		this.image	= null;
		
		this.movieStart = 0;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (image != null) {
			int x = (getWidth() - image.getIntrinsicWidth()) / 2;
			int y = (getHeight() - image.getIntrinsicHeight()) / 2;
			
			image.setBounds(
				x, y, 
				x + image.getIntrinsicWidth(), y + image.getIntrinsicHeight()
			);
			
			image.draw(canvas);
		}
		
		if (movie != null) {
			long now = android.os.SystemClock.uptimeMillis();
			if (movieStart == 0)
				movieStart = now;
			
			int duration = movie.duration();
			if (duration == 0)
				duration = 1000;
			
			int relTime = (int)((now - movieStart) % duration);
			
			movie.setTime(relTime);
			movie.draw(
				canvas, 
				getWidth() - movie.width(),
				getHeight() - movie.height()
			);
			
			invalidate();
		}
	}
	
}

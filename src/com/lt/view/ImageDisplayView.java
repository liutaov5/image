package com.lt.view;

import com.example.imagedemo.R;
import com.lt.utils.ImageLoader;
import com.lt.utils.ImageLoader.Type;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class ImageDisplayView extends Activity {

	private ImageView mImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);   
		 //去掉Activity上面的状态栏
		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , 
				WindowManager.LayoutParams. FLAG_FULLSCREEN);  
		setContentView(R.layout.image);
		mImage = (ImageView) findViewById(R.id.image);
		Intent intent = getIntent();
		 Bitmap  bitmap=BitmapFactory.decodeFile(intent.getStringExtra("image"));
		 Drawable drawable=new BitmapDrawable(bitmap);
		 bitmap= drawableToBitmap(drawable);
		 //mImage.setImageDrawable(drawable);
		 mImage.setImageBitmap(bitmap);

		//ImageLoader.getInsatnce(3, Type.LIFO).loadImage(intent.getStringExtra("image"), mImage);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
}

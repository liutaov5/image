package com.lt.view;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

import com.example.imagedemo.R;
import com.lt.bean.ImageFloder;
import com.lt.utils.ImageAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;

public class ImagesDisplayView extends Activity {

	private GridView mGridImage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grid_image);
		mGridImage=(GridView)findViewById(R.id.grid_image);   
		Intent intent=getIntent();
		ImageFloder imageFloder=(ImageFloder)intent.getSerializableExtra("imageFloder");
		
		File imageDir = new File(imageFloder.getmDir());
		List<String> images = Arrays.asList(imageDir
				.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						if (filename.endsWith("jpg")
								|| filename.endsWith("jpeg")
								|| filename.endsWith("png")) {
							return true;
						}
						return false;
					}
				}));

		ImageAdapter adapter = new ImageAdapter(
				getApplicationContext(), images,
				R.layout.grid_image_item, imageDir.getAbsolutePath());
		mGridImage.setAdapter(adapter);
	}

	
}

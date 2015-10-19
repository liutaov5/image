package com.lt.utils;

import java.util.List;

import com.example.imagedemo.R;
import com.lt.utils.ImageLoader.Type;
import com.lt.view.ImageDisplayView;
import com.lt.view.ImagesDisplayView;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ImageAdapter extends CommonAdapter<String> {
	
	private String mDirPath;

	public ImageAdapter(Context mContext, List<String> mDatas,
			int mItemLayoutId, String mDirPath) {
		super(mContext, mDatas, mItemLayoutId);
		this.mDirPath = mDirPath;
	}

	@Override
	public void convert(ViewHolder holder, String item) {
		((ImageView)holder.getView(R.id.grid_item_image)).setImageResource(R.drawable.pictures_no);
		ImageLoader.getInsatnce(3, Type.LIFO).loadImage(mDirPath+"/"+item, (ImageView)holder.getView(R.id.grid_item_image));
		ImageView imageView=(ImageView)holder.getView(R.id.grid_item_image);
		imageView.setTag(mDirPath+"/"+item);
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(mContext, ImageDisplayView.class);
				intent.putExtra("image", v.getTag().toString());
				intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
		});
	}
	
	

}

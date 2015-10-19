package com.lt.view;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.example.imagedemo.R;
import com.lt.bean.ImageFloder;
import com.lt.utils.CommonAdapter;
import com.lt.utils.ImageLoader;
import com.lt.utils.ImageLoader.Type;
import com.lt.utils.ViewHolder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ListView mListDir;
	private ProgressDialog mProgressDialog;
	/**
	 * 存放所有图片文件夹信息
	 */
	private List<ImageFloder> mImageFloders = new ArrayList<ImageFloder>();
	/**
	 * 用来存放文件夹路径
	 */
	private HashSet<String> mDirPaths = new HashSet<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		getImages();
		initEvent();
	}

	private void init() {
		mListDir = (ListView) findViewById(R.id.list_dir);

	}
	/**
	 * 设置点击事件
	 */
	private void initEvent() {
		mListDir.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ImageFloder imageFloder = mImageFloders.get(position);
				Intent intent=new Intent(MainActivity.this, ImagesDisplayView.class);
				Bundle bundle=new Bundle();
				bundle.putSerializable("imageFloder", imageFloder);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			initListDir();
		}
	};
	/**
	 * 设置适配器
	 */
	private void initListDir() {
		CommonAdapter<ImageFloder> adapter = new CommonAdapter<ImageFloder>(
				getApplicationContext(), mImageFloders, R.layout.list_dir_item) {

			@Override
			public void convert(ViewHolder holder, ImageFloder item) {
				ImageLoader.getInsatnce(3, Type.LIFO).loadImage(
						item.getmFirstImagePath(),
						(ImageView) holder.getView(R.id.first_image));
				
				((TextView) holder.getView(R.id.floder_name)).setText(item
						.getmName());
				
				((TextView) holder.getView(R.id.image_count)).setText(item
						.getmCount() + "张");
			}
		};
		mListDir.setAdapter(adapter);
	}
	/**
	 * 扫描手机存储图片
	 */
	private void getImages() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(getApplicationContext(), "暂无外部存储",
					Toast.LENGTH_SHORT).show();
			return;
		}
		mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
		new Thread(new Runnable() {

			@Override
			public void run() {
				Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				;
				ContentResolver contentResolver = MainActivity.this
						.getContentResolver();
				Cursor cursor = contentResolver.query(uri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" },
						MediaStore.Images.Media.DATE_MODIFIED);
				while (cursor.moveToNext()) {
					String path = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					ImageFloder imageFloder = null;
					File parentFile = new File(path).getParentFile();
					if (parentFile == null) {
						continue;
					}
					String dirPath = parentFile.getAbsolutePath();
					if (mDirPaths.contains(dirPath)) {
						continue;
					} else {
						mDirPaths.add(dirPath);
						imageFloder = new ImageFloder();
						imageFloder.setmDir(dirPath);
						imageFloder.setmFirstImagePath(path);
					}
					//文件过滤
					int imageCount = parentFile.list(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							if (filename.endsWith("jpg")
									|| filename.endsWith("png")
									|| filename.endsWith("jpeg")) {
								return true;
							}
							return false;
						}
					}).length;
					imageFloder.setmCount(imageCount);
					mImageFloders.add(imageFloder);
				}
				mDirPaths = null;
				cursor.close();
				mHandler.sendEmptyMessage(1);
			}
		}).start();
	}

}

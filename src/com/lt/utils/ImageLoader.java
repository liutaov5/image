package com.lt.utils;


import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImageLoader {
	/**
	 * 缓存图片
	 */
	private LruCache<String, Bitmap> mLruCache;
	/**
	 * 运行在UI线程中更新UI
	 */
	private Handler mHandler;
	/**
	 * 轮询线程，用来执行图片加载任务
	 */
	private Thread mThread;
	private Handler mThreadHandler;
	/**
	 * 一个静态实例
	 */
	private static ImageLoader mInstance;
	/**
	 * 存储任务
	 */
	private LinkedList<Runnable> mTasks;

	private ExecutorService mThreadPool;

	private static final int THREAD_COUNT = 1;
	
	private volatile Semaphore mSemaphore=new Semaphore(0);
	
	private volatile Semaphore mThreadSemaphore;
	/**
	 * 先进先出和后进先出两种加载方式
	 * @author taoliu
	 *
	 */
	public enum Type {
		FIFO, LIFO;
	}

	private Type mType = Type.LIFO;

	private ImageLoader(int threadCount, Type type) {
		init(threadCount, type);
	}

	public static ImageLoader getInstance() {

		if (mInstance == null) {
			synchronized (ImageLoader.class) {
				if (mInstance == null) {
					mInstance = new ImageLoader(THREAD_COUNT, Type.LIFO);
				}
			}
		}
		return mInstance;
	}

	public static ImageLoader getInsatnce(int threadCount, Type type) {
		if (mInstance == null) {
			synchronized (ImageLoader.class) {
				if (mInstance == null) {
					mInstance = new ImageLoader(threadCount, type);
				}
			}
		}
		return mInstance;
	}
	/**
	 * 初始化数据并开辟线程执行图片加载任务
	 * @param 线程数量
	 * @param 执行方法
	 */
	private void init(int threadCount, Type type) {
		mThread = new Thread() {
			@Override
			public void run() {
				Looper.prepare();

				mThreadHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						mThreadPool.execute(getTask());
						try {
							mThreadSemaphore.acquire();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
				mSemaphore.release();
				Looper.loop();
			}
		};
		mThread.start();
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};

		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mTasks = new LinkedList<Runnable>();
		mThreadSemaphore=new Semaphore(threadCount);
		mType = type == null ? Type.LIFO : type;
	}
	/**
	 * 加载图片
	 * @param 图片路径
	 * @param imageView
	 */
	public void loadImage(final String path, final ImageView imageView) {
		
		imageView.setTag(path);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				ImageHolder holder=(ImageHolder)msg.obj;
				if(holder.imageView.getTag().toString().equals(holder.path)){
					holder.imageView.setImageBitmap(holder.bitmap);
				}
			}
		};

		Bitmap bitmap = getBitmpFromLruCache(path);
		if (bitmap != null) {
			ImageHolder holder = new ImageHolder();
			holder.bitmap = bitmap;
			holder.imageView = imageView;
			holder.path = path;
			Message message = Message.obtain();
			message.obj = holder;
			mHandler.sendMessage(message);
		} else {
			addTask(new Runnable() {
				@Override
				public void run() {
					ImageHolder holder = new ImageHolder();
					ImageSize imageSize = new ImageSize();
					imageSize = getImageViewSize(imageView);
					Bitmap bitmap = decodeSimpleBitmapFromResource(path,
							imageSize.width, imageSize.height);
					addBitmapToLruCache(path, bitmap);
					holder.bitmap = getBitmpFromLruCache(path);
					holder.imageView = imageView;
					holder.path = path;
					Message message=Message.obtain();
					message.obj=holder;
					mHandler.sendMessage(message);
					mThreadSemaphore.release();
				}
			});
		}
	}

	private synchronized void addTask(Runnable runnable) {
		if(mThreadHandler==null){
			try {
				mSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mTasks.add(runnable);
		mThreadHandler.sendEmptyMessage(1);
	}

	private synchronized Runnable getTask() {
		if (mType == Type.FIFO) {
			return mTasks.removeFirst();
		} else if (mType == Type.LIFO) {
			return mTasks.removeLast();
		}
		return null;
	}
	/**
	 * 获取imageview的宽高
	 * @param imageView
	 * @return
	 */
	private ImageSize getImageViewSize(ImageView imageView) {
		ImageSize imageSize = new ImageSize();
		final DisplayMetrics displayMetrics = imageView.getContext()
				.getResources().getDisplayMetrics();
		final LayoutParams params = imageView.getLayoutParams();
		int width = params.width == LayoutParams.WRAP_CONTENT ? 0
				: params.width;
		if (width <= 0) {
			width = params.width;
		}
		if (width <= 0) {
			width = getImageViewFieldValue(imageView, "mMaxWidth");
		}
		if (width <= 0) {
			width = displayMetrics.widthPixels;
		}

		int height = params.height == LayoutParams.WRAP_CONTENT ? 0
				: params.height;
		if (height <= 0) {
			height = params.height;
		}
		if (height <= 0) {
			height = getImageViewFieldValue(imageView, "mMaxHeight");
		}
		if (height <= 0) {
			height = displayMetrics.heightPixels;
		}

		imageSize.height = height;
		imageSize.width = width;
		return imageSize;
	}
	/**
	 * 计算图片缩放比例
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private int calculateInSimpleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		int width = options.outWidth;
		int height = options.outHeight;
		int InsimpleSize = 1;
		if (width > reqWidth && height > reqHeight) {
			int widthRadio = Math.round((float) width / (float) reqWidth);
			int heightRadio = Math.round((float) height / (float) reqHeight);
			InsimpleSize = Math.max(widthRadio, heightRadio);
		}
		return InsimpleSize;
	}

	private Bitmap decodeSimpleBitmapFromResource(String path, int reqWidth,
			int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		//设置为true获得图片的宽高
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSimpleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}
	/**
	 * 利用反射获得object的属性fieldName的值
	 * @param object
	 * @param fieldName
	 * @return
	 */
	private int getImageViewFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = (Integer) field.get(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	private Bitmap getBitmpFromLruCache(String key){
		return mLruCache.get(key);
	}
	/**
	 * 把图片添加到缓存中
	 * @param key
	 * @param bitmap
	 */
	private void addBitmapToLruCache(String key,Bitmap bitmap){
		if(getBitmpFromLruCache(key)==null){
			if(bitmap!=null){
				mLruCache.put(key, bitmap);
			}
		}
	}

	private class ImageHolder {
		Bitmap bitmap;
		ImageView imageView;
		String path;
	}

	private class ImageSize {
		int width;
		int height;
	}

}

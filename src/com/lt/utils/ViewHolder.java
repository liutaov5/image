package com.lt.utils;

import android.R.integer;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/**
 * 作为View的容器，用来缓存配合适配器
 * @author taoliu
 *
 */
public class ViewHolder {

	private SparseArray<View> mViews;
	private View mConvertView;
	private int mPosition;

	private ViewHolder(int position, Context context, ViewGroup parent,
			int layoutId) {
		this.mPosition = position;
		mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
				false);
		mConvertView.setTag(this);
	}

	public static ViewHolder get(Context context, ViewGroup parent,
			int position, int layoutId, View convertView) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder(position, context, parent, layoutId);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.mPosition = position;
		}
		return holder;
	}
	
	public View getConvertView(){
		return mConvertView;
	}
	/**
	 * 获取View控件
	 * @param 控件Id
	 * @return	控件
	 */
	public <T extends View> T getView(int viewId){
		View view=mViews.get(viewId);
		if(view==null){
			view =mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T)view;
	}
	
	public int getPosition(){
		return mPosition;
	}

}

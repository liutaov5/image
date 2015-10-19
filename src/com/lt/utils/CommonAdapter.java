package com.lt.utils;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class CommonAdapter<T> extends BaseAdapter {

	protected Context mContext;
	protected List<T> mDatas;
	protected LayoutInflater mInflater;
	protected final int mItemLayoutId;

	public CommonAdapter(Context mContext, List<T> mDatas, int mItemLayoutId) {
		super();
		this.mContext = mContext;
		this.mDatas = mDatas;
		this.mInflater = LayoutInflater.from(mContext);
		this.mItemLayoutId = mItemLayoutId;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size();
	}

	@Override
	public T getItem(int position) {
		// TODO Auto-generated method stub
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = getViewHolder(position, parent, convertView);
		convert(holder, getItem(position));
		return holder.getConvertView();
	}

	public abstract void convert(ViewHolder holder, T item);

	private ViewHolder getViewHolder(int position, ViewGroup parent,
			View convertView) {
		return ViewHolder.get(mContext, parent, position, mItemLayoutId,
				convertView);
	}

}

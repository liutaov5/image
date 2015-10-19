package com.lt.bean;

import java.io.Serializable;

public class ImageFloder implements Serializable{
	/**
	 * 文件夹目录
	 */
	private String mDir;
	/**
	 * 第一张图片的路径
	 */
	private String mFirstImagePath;
	/**
	 * 文件夹名称
	 */
	private String mName;
	/**
	 * 图片数量
	 */
	private int mCount;
	
	public String getmDir() {
		return mDir;
	}
	public void setmDir(String mDir) {
		this.mDir = mDir;
		int lastIndexOf=this.mDir.lastIndexOf("/");
		this.mName=this.mDir.substring(lastIndexOf);
	}
	public String getmFirstImagePath() {
		return mFirstImagePath;
	}
	public void setmFirstImagePath(String mFirstImagePath) {
		this.mFirstImagePath = mFirstImagePath;
	}
	public String getmName() {
		return mName;
	}
	public int getmCount() {
		return mCount;
	}
	public void setmCount(int mCount) {
		this.mCount = mCount;
	}
	

}

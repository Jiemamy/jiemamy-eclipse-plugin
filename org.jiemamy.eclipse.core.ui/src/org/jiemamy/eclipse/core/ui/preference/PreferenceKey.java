/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2008/07/28
 *
 * This file is part of Jiemamy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.jiemamy.eclipse.core.ui.preference;

/**
 * Preferenceのキーリスト。
 * 
 * @author daisuke
 */
public enum PreferenceKey {
	
	/** FK生成時に参照元テーブルに新規カラムをつくるかどうか */
	CREATE_COLUMNS_WITH_FK("Jiemamy.Connection.CreateColumnsWithFk", false),

	/** コネクションルータ */
	CONNECTION_ROUTER("Jiemamy.Connection.ConnectionRouter", ConnectionRouters.BENDPOINT.name()),

	/** データベース接続情報保持領域 */
	DATABASE_INFORMATONS("Jiemamy.DatabaseInformations"),

	/** Preference ID used to persist the palette location. */
	PALETTE_DOCK_LOCATION("DiagramEditorPalette.Location"),

	/** Preference ID used to persist the palette size. */
	PALETTE_SIZE("DiagramEditorPalette.Size"),

	/** Preference ID used to persist the flyout palette's state. */
	PALETTE_STATE("DiagramEditorPalette.State");
	
	/** 保存キー */
	private String key;
	
	/** デフォルト値 */
	private Object defaultValue;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param key 保存キー
	 */
	PreferenceKey(String key) {
		this(key, null);
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param key 保存キー
	 * @param defaultValue デフォルト値
	 */
	PreferenceKey(String key, Object defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * デフォルト値をbooleanとして取得する。
	 * 
	 * @return デフォルト値
	 */
	public boolean getDefaultBoolean() {
		return this.<Boolean> getDefault();
	}
	
	/**
	 * デフォルト値をdoubleとして取得する。
	 * 
	 * @return デフォルト値
	 */
	public double getDefaultDouble() {
		return this.<Double> getDefault();
	}
	
	/**
	 * デフォルト値をfloatとして取得する。
	 * 
	 * @return デフォルト値
	 */
	public float getDefaultFloat() {
		
		return this.<Float> getDefault();
	}
	
	/**
	 * デフォルト値をintとして取得する。
	 * 
	 * @return デフォルト値
	 */
	public int getDefaultInt() {
		
		return this.<Integer> getDefault();
	}
	
	/**
	 * デフォルト値をlongとして取得する。
	 * 
	 * @return デフォルト値
	 */
	public long getDefaultLong() {
		return this.<Long> getDefault();
	}
	
	/**
	 * デフォルト値をStringとして取得する。
	 * 
	 * @return デフォルト値
	 */
	public String getDefaultString() {
		return getDefault();
	}
	
	@Override
	public String toString() {
		return key;
	}
	
	/**
	 * デフォルト値を取得する。
	 * 
	 * @param <T> 設定値の型
	 * @return デフォルト値
	 */
	@SuppressWarnings("unchecked")
	private <T>T getDefault() {
		return (T) defaultValue;
	}
	
}

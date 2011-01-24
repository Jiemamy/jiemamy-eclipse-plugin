/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/16
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
package org.jiemamy.eclipse.core.ui.editor.dialog.context;

import java.util.Locale;

import org.eclipse.osgi.util.NLS;

/**
 * {@link JiemamyContextEditDialog}用メッセージリソースクラス。
 * 
 * @author daisuke
 */
class Messages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** Dialogのタイトル */
	public static String Dialog_Title;
	
	/** スキーマ名ラベル */
	public static String Label_SchemaName;
	
	/** DBシステム選択ラベル */
	public static String Label_RDBMS;
	
	/** ドメインタブ */
	public static String Tab_Domains;
	
	/** 開始スクリプトタブ */
	public static String Tab_BeginScript;
	
	/** 終了スクリプトタブ */
	public static String Tab_EndScript;
	
	/** 説明タブ */
	public static String Tab_Description;
	
	/** ドメイン編集コントロールグループタイトル */
	public static String Label_GroupTitle_Domain;
	
	/** ドメイン名ラベル */
	public static String Label_Domain_Name;
	
	/** データ型選択ラベル */
	public static String Label_Domain_DataType;
	
	/** サイズラベル */
	public static String Label_Domain_DataTypeSize;
	
	/** チェック制約ラベル */
	public static String Label_Domain_CheckConstraint;
	
	/** 非NULL制約ラベル */
	public static String Label_Domain_NotNullConstraint;
	
	/** 説明ラベル */
	public static String Label_Domain_Description;
	
	/** ドメイン一覧テーブル ドメイン名カラム */
	public static String Column_Domain_Name;
	
	/** ドメイン一覧テーブル データ型カラム */
	public static String Column_Domain_DataType;
	
	/** ドメイン一覧テーブル 非NULL制約カラム */
	public static String Column_Domain_NotNullConstraint;
	
	/** ドメイン一覧テーブル チェック制約カラム */
	public static String Column_Domain_CheckConstraint;
	
	private static final String BUNDLE_NAME = Messages.class.getName().toLowerCase(Locale.US);
	
	static {
		reloadMessages();
	}
	

	/**
	 * load message values from bundle file
	 */
	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
}

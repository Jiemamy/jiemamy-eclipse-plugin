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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table;

import java.util.Locale;

import org.eclipse.osgi.util.NLS;

/**
 * {@link TableEditDialog}用メッセージリソースクラス。
 * 
 * @author daisuke
 */
class Messages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** Dialogのタイトル */
	public static String Dialog_Title;
	
	/** テーブル名ラベル */
	public static String Label_Table_Name;
	
	/** テーブル名ラベル */
	public static String Label_Table_LogicalName;
	
	/** カラムタブ */
	public static String Tab_Table_Columns;
	
	/** 制約タブ */
	public static String Tab_Table_Constraints;
	
	/** インデックスタブ */
	public static String Tab_Table_Indexes;
	
	/** 開始スクリプトタブ */
	public static String Tab_Table_BeginScript;
	
	/** 終了スクリプトタブ */
	public static String Tab_Table_EndScript;
	
	/** 説明タブ */
	public static String Tab_Table_Description;
	
	/** カラム選択ダイアログ: タイトル */
	public static String ColumnSelectDialog_title;
	
	/** カラム選択ダイアログ: カラム名列 */
	public static String ColumnSelectDialog_columnName_column;
	
	/** カラム選択ダイアログ: データ型列 */
	public static String ColumnSelectDialog_dataType_column;
	
	// CHECKSTYLE:ON
	
	private static final String BUNDLE_NAME = Messages.class.getName().toLowerCase(Locale.getDefault());
	
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

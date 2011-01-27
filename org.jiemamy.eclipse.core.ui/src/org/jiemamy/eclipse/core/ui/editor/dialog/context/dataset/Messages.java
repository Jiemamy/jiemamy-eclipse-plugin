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
package org.jiemamy.eclipse.core.ui.editor.dialog.context.dataset;

import java.util.Locale;

import org.eclipse.osgi.util.NLS;

import org.jiemamy.eclipse.core.ui.editor.dialog.context.JiemamyContextEditDialog;

/**
 * {@link JiemamyContextEditDialog}用メッセージリソースクラス。
 * 
 * @author daisuke
 */
class Messages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** データセット編集ダイアログ タイトル */
	public static String DataSetEditDialog_title;
	
	/** データセット編集ダイアログ インポートボタン */
	public static String DataSetEditDialog_btn_import;
	
	/** データセット編集ダイアログ エクスポートボタン */
	public static String DataSetEditDialog_btn_export;
	
	/** データセット編集ダイアログ 注意ラベル */
	public static String DataSetEditDialog_label_notice;
	
	/** データセット編集ダイアログ タブメニュー 追加 */
	public static String DataSetEditDialog_tabMenu_add;
	
	/** データセット編集ダイアログ タブメニュー 削除 */
	public static String DataSetEditDialog_tabMenu_remove;
	
	/** データセット編集ダイアログ テーブル削除 確認 */
	public static String DataSetEditDialog_deleteTable_confirm;
	
	/** データセット編集ダイアログ CSVエクスポート タイトル */
	public static String DataSetEditDialog_export_title;
	
	/** データセット編集ダイアログ CSVエクスポート 完了(Windows) */
	public static String DataSetEditDialog_export_success_windows;
	
	/** データセット編集ダイアログ CSVエクスポート 完了 */
	public static String DataSetEditDialog_export_success;
	
	/** データセット編集ダイアログ CSVエクスポート ファイルオープンに失敗 */
	public static String DataSetEditDialog_export_openFailed;
	
	/** データセット編集ダイアログ CSVインポート タイトル */
	public static String DataSetEditDialog_import_title;
	
	/** データセット編集ダイアログ CSVインポート 確認メッセージ */
	public static String DataSetEditDialog_import_confirm;
	
	/** データセット編集ダイアログ CSVインポート 完了 */
	public static String DataSetEditDialog_import_success;
	
	/** データセット編集ダイアログ 説明 */
	public static String DataSetEditDialog_description;
	
	// CHECKSTYLE:ON
	
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

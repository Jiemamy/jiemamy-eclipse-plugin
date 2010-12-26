/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/03/07
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
package org.jiemamy.eclipse.action;

import java.util.Locale;

import org.eclipse.osgi.util.NLS;

/**
 * アクションメッセージリソースクラス。
 * 
 * @author Keisuke.K
 */
class Messages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** 名称: 自動レイアウトアクション */
	public static String AutoLayoutAction_name;
	
	/** 名称: 色の変更 */
	public static String ChangeNodeBgcolorAction_name;
	
	/** 名称: サイズをデフォルトに戻す */
	public static String FitNodeConstraintAction_name;
	
	/** 名称: プロパティ */
	public static String PropertyAction_name;
	
	/** 名称: 画像として保存 */
	public static String SaveDiagramImageAction_name;
	
	/** 画像保存ウィザードタイトル */
	public static String GraphicWizard_title;
	
	/** 画像保存ウィザード説明 */
	public static String GraphicWizard_description;
	
	/** 画像保存ウィザード: ファイル名 */
	public static String GraphicWizard_fileName_label;
	
	/** 画像保存ウィザード: ファイルフォーマット */
	public static String GraphicWizard_fileFormat_label;
	
	/** ファイルフォーマット説明: JPEG */
	public static String FileFormat_jpg_description;
	
	/** ファイルフォーマット説明: BMP */
	public static String FileFormat_bmp_description;
	
	/** ファイルフォーマット説明: BMP (RLE圧縮) */
	public static String FileFormat_bmpRLE_description;
	
	/** ファイルフォーマット説明: ICO */
	public static String FileFormat_ico_description;
	
	// CHECKSTYLE:ON
	
	private static final String BUNDLE_NAME = Messages.class.getName().toLowerCase(Locale.US);
	
	static {
		reloadMessages();
	}
	

	/**
	 * load message values from bundle file
	 */
	private static void reloadMessages() {
		initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
}

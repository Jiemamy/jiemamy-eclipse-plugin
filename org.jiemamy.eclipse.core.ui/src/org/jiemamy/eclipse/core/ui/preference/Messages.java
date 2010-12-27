/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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

import org.eclipse.osgi.util.NLS;

/**
 * {@link org.jiemamy.eclipse.core.ui.preference}パッケージ用メッセージリソースクラス。
 * 
 * @author daisuke
 */
public class Messages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** グループタイトル：コネクション */
	public static String Group_Connection;
	
	/** 項目名：外部キー作成時に、参照元テーブルに参照先PKと同名のカラムを作るかどうか */
	public static String Connection_CreateColumnWithFk;
	
	/** 項目名：コネクションルータ */
	public static String Connection_Router;
	
	/** Bendpoint Connection Router */
	public static String Bendpoint_Connection_Router;
	
	/** Shortest Path Connection Router */
	public static String Shortest_Path_Connection_Router;
	
	/** Manhattan Connection Router */
	public static String Manhattan_Connection_Router;
	
	// CHECKSTYLE:ON
	
	private static final String BUNDLE_NAME = "org.jiemamy.eclipse.core.ui.preference.messages";
	
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

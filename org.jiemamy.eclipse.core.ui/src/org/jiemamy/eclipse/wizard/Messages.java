/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/08/03
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
package org.jiemamy.eclipse.wizard;

import java.util.Locale;

import org.eclipse.osgi.util.NLS;

/**
 * Wizardメッセージリソースクラス。
 * 
 * @author daisuke
 */
public class Messages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** データファイル新規作成Wizardのタイトル */
	public static String Wizard_Title;
	
	/** データファイル新規作成Wizardのメッセージ */
	public static String Wizard_Message;
	
	/** 拡張子にjer以外が指定された場合のエラーメッセージ */
	public static String File_Extension_Error;
	
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

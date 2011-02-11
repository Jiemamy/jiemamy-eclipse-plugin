/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/04/18
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
package org.jiemamy.eclipse.core.ui;

import org.eclipse.osgi.util.NLS;

/**
 * 共通メッセージリソースクラス
 * 
 * @author Keisuke.K
 */
public class CommonMessages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** 共通: ファイルが見つからない */
	public static String Common_fileNotFound;
	
	/** 共通: ファイルが読めない */
	public static String Common_fileNotReadable;
	
	/** 共通: ファイルの書き込みに失敗 */
	public static String Common_fileWriteFailed;
	
	/** 共通: 上書き確認 */
	public static String Common_fileOverwrite;
	
	// CHECKSTYLE:ON
	
	private static final String BUNDLE_NAME = "org.jiemamy.eclipse.core.ui.common_messages";
	
	static {
		reloadMessages();
	}
	

	/**
	 * load message values from bundle file
	 */
	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, CommonMessages.class);
	}
	
}

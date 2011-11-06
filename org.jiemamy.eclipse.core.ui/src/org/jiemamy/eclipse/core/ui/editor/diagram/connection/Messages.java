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
package org.jiemamy.eclipse.core.ui.editor.diagram.connection;

import java.util.Locale;

import org.eclipse.osgi.util.NLS;

/**
 * {@link ForeignKeyEditDialog}用メッセージリソースクラス。
 * 
 * @author daisuke
 */
class Messages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** Dialogのタイトル */
	public static String Dialog_Title;
	
	/** 外部キー名ラベル */
	public static String Label_ForeignKey_Name;
	
	/** 外部キー名ラベル */
	public static String Label_ForeignKey_LogicalName;
	
	/** canExecute() ログ01 */
	public static String CreateConnectionCommand_log_canExecute_01;
	
	/** canExecute() ログ02 */
	public static String CreateConnectionCommand_log_canExecute_02;
	
	/** canExecute() ログ03 */
	public static String CreateConnectionCommand_log_canExecute_03;
	
	/** canExecute() ログ04 */
	public static String CreateConnectionCommand_log_canExecute_04;
	
	/** canExecute() ログ05 */
	public static String CreateConnectionCommand_log_canExecute_05;
	
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

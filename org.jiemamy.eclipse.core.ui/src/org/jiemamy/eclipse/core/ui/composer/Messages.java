/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/04/20
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
package org.jiemamy.eclipse.core.ui.composer;

import java.util.Locale;

import org.eclipse.osgi.util.NLS;

/**
 * Composer パッケージのメッセージリソースクラス。
 * 
 * @author Keisuke.K
 */
class Messages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** DbImporterWizard のタイトル */
	public static String DbImportWizard_title;
	
	/** DbImporterWizardPage タイトル */
	public static String DbImportWizardPage_title;
	
	/** DbImporterWizardPage ラベル DB種別 */
	public static String DbImportWizardPage_label_dbType;
	
	/** DbImporterWizardPage 接続テストボタン */
	public static String DbImportWizardPage_btn_connectionTest;
	
	/** DbImporterWizardPage 再設定ボタン */
	public static String DbImportWizardPage_btn_reconfigure;
	
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

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
package org.jiemamy.eclipse.editor;

import org.eclipse.osgi.util.NLS;

/**
 * Preferenceメッセージリソースクラス。
 * @author daisuke
 */
class Messages extends NLS {
	
	// CHECKSTYLE:OFF
	
	/** 物理モデル：属性/型レベル */
	public static String Physical_AttrAndType;
	
	/** 物理モデル：属性レベル */
	public static String Physical_Attribute;
	
	/** 物理モデル：識別子レベル */
	public static String Physical_Key;
	
	/** 物理モデル：エンティティレベル */
	public static String Physical_Entity;
	
	/** 論理モデル：属性/型レベル */
	public static String Logical_AttrAndType;
	
	/** 論理モデル：属性レベル */
	public static String Logical_Attribute;
	
	/** 論理モデル：識別子レベル */
	public static String Logical_Key;
	
	/** 論理モデル：エンティティレベル */
	public static String Logical_Entity;
	
	// CHECKSTYLE:ON
	
	private static final String BUNDLE_NAME = "org.jiemamy.eclipse.editor.messages";
	
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

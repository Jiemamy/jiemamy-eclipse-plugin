/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2009/02/24
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
package org.jiemamy.eclipse.core.ui.editor.diagram;

import org.apache.commons.lang.Validate;

/**
 * {@link TableEditorConfig}のデフォルト実装。
 * 
 * @author daisuke
 */
public class DefaultTableEditorConfig implements TableEditorConfig {
	
	private final String editorTitle;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param editorTitle エディタのタイトル
	 */
	public DefaultTableEditorConfig(String editorTitle) {
		Validate.notNull(editorTitle);
		this.editorTitle = editorTitle;
		
	}
	
	public String getAddLabel() {
		return "追加(&A)"; // RESOURCE
	}
	
	public String getEditorTitle() {
		return editorTitle;
	}
	
	public String getInsertLabel() {
		return "挿入(&I)"; // RESOURCE
	}
	
	public String getRemoveLabel() {
		return "削除(&R)"; // RESOURCE
	}
	
	public boolean isFreeOrder() {
		return true;
	}
	
}

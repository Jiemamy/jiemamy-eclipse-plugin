/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/02/17
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
package org.jiemamy.eclipse.core.ui.editor.dialog;

import org.eclipse.swt.custom.TableEditor;

/**
 * {@link TableEditor}の設定インターフェイス。
 * 
 * @author daisuke
 */
public interface TableEditorConfig {
	
	/**
	 * 追加ボタンに表示するラベルテキストを取得する。
	 * 
	 * @return  追加ボタンに表示するラベルテキスト
	 */
	String getAddLabel();
	
	/**
	 * 編集エリアのタイトルを取得する。
	 * 
	 * @return 編集エリアのタイトル
	 */
	String getEditorTitle();
	
	/**
	 * 挿入ボタンに表示するラベルテキストを取得する。
	 * 
	 * @return  挿入ボタンに表示するラベルテキスト
	 */
	String getInsertLabel();
	
	/**
	 * 削除ボタンに表示するラベルテキストを取得する。
	 * 
	 * @return removeLabel 削除ボタンに表示するラベルテキスト
	 */
	String getRemoveLabel();
	
}

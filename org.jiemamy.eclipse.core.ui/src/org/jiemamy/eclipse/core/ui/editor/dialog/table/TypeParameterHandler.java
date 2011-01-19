/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/03/19
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
package org.jiemamy.eclipse.core.ui.editor.dialog.table;

import org.eclipse.swt.widgets.Composite;

import org.jiemamy.eclipse.core.ui.editor.dialog.EditListener;
import org.jiemamy.model.column.ColumnModel;

/**
 * 型オプションのUIをコントロールするインターフェイス。
 * 
 * TODO v0.2由来のよくわからんインターフェイスなので、リファクタリングが必要。
 * 
 * @author daisuke
 */
public interface TypeParameterHandler {
	
	/**
	 * UIを構築する。
	 * 
	 * @param columnModel データ型を設定されるモデル
	 * @param composite オプションコントロール描画対象の親
	 * @param editListener コントロールの操作を検知するリスナ
	 */
	void createControl(ColumnModel columnModel, Composite composite, EditListener editListener);
	
	/**
	 * コントロールを無効にする。
	 */
	void disable();
	
	/**
	 * コントロールを有効にする。
	 */
	void enable();
	
	/**
	 * アダプタからコントロールに値を格納する。
	 */
	void setValue();
	
	/**
	 * コントロールからアダプタに値を格納する。
	 */
	void writeBackToAdapter();
	
}

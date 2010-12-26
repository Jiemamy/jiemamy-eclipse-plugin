/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.editor.dialog;

import org.eclipse.swt.widgets.Composite;

import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.dbo.DomainModel;

/**
 * 型オプションのUIをコントロールするインターフェイス。
 * 
 * @author daisuke
 */
public interface TypeOptionHandler {
	
	/**
	 * UIを構築する。
	 * 
	 * @param holder データ型を設定されるモデル（{@link ColumnModel}または{@link DomainModel}）
	 * @param composite オプションコントロール描画対象の親
	 * @param editListener コントロールの操作を検知するリスナ
	 */
	void createControl(DataTypeHolder<? extends DataType> holder, Composite composite, EditListener editListener);
	
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
	 * 
	 * @param adapter 対象アダプタ
	 */
	void setValue(Class<?> adapter);
	
	/**
	 * コントロールからアダプタに値を格納する。
	 */
	void writeBackToAdapter();
	
}

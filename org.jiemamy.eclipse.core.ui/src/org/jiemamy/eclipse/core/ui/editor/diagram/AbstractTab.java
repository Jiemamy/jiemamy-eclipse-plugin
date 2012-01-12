/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram;

import org.apache.commons.lang.Validate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * タブエリアの抽象クラス。
 * 
 * <p>{@link TabItem}を継承して作りたいところだが、継承が許されていないため、このような形となった。</p>
 * 
 * @author daisuke
 */
public abstract class AbstractTab extends Composite {
	
	private final TabItem tabItem;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param tabTitle タブのタイトル文字列
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public AbstractTab(TabFolder parentTabFolder, int style, String tabTitle) {
		super(parentTabFolder, style);
		Validate.notNull(tabTitle);
		
		tabItem = new TabItem(parentTabFolder, SWT.NONE);
		tabItem.setText(tabTitle);
	}
	
	/**
	 * タブ要素を取得する。
	 * 
	 * @return タブ要素
	 */
	public TabItem getTabItem() {
		return tabItem;
	}
	
	/**
	 * タブ内コントロールの入力が終了しているかどうか（OKを有効にして良いかどうか）を調べる。
	 * 
	 * @return 入力が終了していれば（OKを有効にして良ければ）{@code true}
	 */
	public abstract boolean isTabComplete();
	
	/**
	 * ダイアログにてOKが押下された時の処理を行う。
	 * 
	 * <p>サブクラスにて、必要に応じてオーバーライドする。</p>
	 */
	public void okPressed() {
	}
}

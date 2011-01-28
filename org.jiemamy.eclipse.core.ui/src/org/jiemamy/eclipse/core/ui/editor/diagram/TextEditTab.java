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
package org.jiemamy.eclipse.core.ui.editor.diagram;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

/**
 * テキスト編集タブ。
 * 
 * @author daisuke
 */
public class TextEditTab extends AbstractTab {
	
	private Text text;
	
	private final boolean permitEmpty;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param tabTitle タブのタイトル
	 * @param strText 編集するテキスト
	 */
	public TextEditTab(TabFolder parentTabFolder, String tabTitle, String strText) {
		this(parentTabFolder, tabTitle, strText, true);
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param tabTitle タブのタイトル
	 * @param strText 編集するテキスト
	 * @param permitEmpty 空入力を許すかどうか
	 */
	public TextEditTab(TabFolder parentTabFolder, String tabTitle, String strText, boolean permitEmpty) {
		super(parentTabFolder, SWT.NONE, tabTitle);
		this.permitEmpty = permitEmpty;
		
		text = new Text(parentTabFolder, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.setText(strText);
		
		getTabItem().setControl(text);
	}
	
	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
	}
	
	/**
	 * 子として作られたTextウィジェットを取得する。
	 * 
	 * @return 子として作られたTextウィジェット
	 */
	public Text getTextWidget() {
		return text;
	}
	
	@Override
	public boolean isTabComplete() {
		return permitEmpty || StringUtils.isEmpty(text.getText()) == false;
	}
	
	@Override
	public void removeKeyListener(KeyListener listener) {
		text.removeKeyListener(listener);
	}
}

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
package org.jiemamy.eclipse.core.ui.utils;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

/**
 * {@link Text}コンポーネントに対するフォーカス時に、コンポーネント内の文字列を選択状態にするアダプタ。
 * 
 * @author daisuke
 */
public class TextSelectionAdapter implements FocusListener {
	
	private Text text;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param text 対象となるテキスト入力コンポーネント
	 */
	public TextSelectionAdapter(Text text) {
		this.text = text;
	}
	
	public void focusGained(FocusEvent e) {
		text.selectAll();
	}
	
	public void focusLost(FocusEvent e) {
		// nothing to do
	}
}

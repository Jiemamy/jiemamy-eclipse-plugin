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
package org.jiemamy.eclipse.editor.tools;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * multi-lineテキスト用セルエディタ。
 * 
 * <p>編集中に、CTRL+Enterで改行が入力できる。</p>
 * 
 * @author daisuke
 */
public class MultiLineTextCellEditor extends TextCellEditor {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param parent 親コンポーネント
	 */
	public MultiLineTextCellEditor(Composite parent) {
		super(parent, SWT.MULTI);
	}
	
	@Override
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.character == '\r') { // Return key
			if (text != null && text.isDisposed() == false && (text.getStyle() & SWT.MULTI) != 0) {
				if ((keyEvent.stateMask & SWT.CTRL) == 0) {
					fireApplyEditorValue();
					deactivate();
				}
			}
			return;
		}
		super.keyReleaseOccured(keyEvent);
	}
	
}

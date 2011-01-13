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
package org.jiemamy.eclipse.core.ui.editor.dialog;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;

/**
 * {@link EditListener}の骨格実装クラス。
 * 
 * @author daisuke
 */
public abstract class AbstractEditListener implements EditListener {
	
	public void keyPressed(KeyEvent e) {
	}
	
	public void keyReleased(KeyEvent e) {
		process(e);
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
	}
	
	public void widgetSelected(SelectionEvent e) {
		process(e);
	}
	
	/**
	 * コントロールの操作時に実行する処理を記述するメソッド。
	 * 
	 * @param e {@link KeyEvent}または{@link SelectionEvent}
	 */
	protected abstract void process(TypedEvent e);
	
}

/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2009/04/06
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

import org.eclipse.swt.widgets.Control;

/**
 * SWTを扱うユーティリティクラス。
 * 
 * @author daisuke
 */
public class SwtUtil {
	
	/**
	 * SWTコントロールが有効かどうかを調べる。
	 * 
	 * @param control SWTコントロール
	 * @return 生きている場合は{@code true}、そうでない場合は{@code false}
	 */
	public static boolean isAlive(Control control) {
		return control != null && control.isDisposed() == false;
	}
	
	/**
	 * オプションコントロールが生きていれば、enableの値を設定する。
	 * 
	 * <p>死んでいる（存在しない or 破棄済み）場合は何もしない。</p>
	 * 
	 * @param control 対象コントロール
	 * @param enabled enableの値
	 */
	public static void setEnabledIfAlive(Control control, boolean enabled) {
		if (isAlive(control)) {
			control.setEnabled(enabled);
		}
	}
	
	private SwtUtil() {
	}
}

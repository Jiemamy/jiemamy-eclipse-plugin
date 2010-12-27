/*
 * Copyright 2007-2010 Jiemamy Project and the Others.
 * Created on 2010/12/27
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
package org.jiemamy.eclipse.utils;

import org.apache.commons.lang.StringUtils;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public final class JiemamyPropertyUtil {
	
	/**
	 * モデルからWidgetへの値転送時の、{@code null}に対する処理を行う。
	 * 
	 * @param value モデル保持値
	 * @return 入力が{@code null}の場合は空文字列、そうでない場合はそのまま出力する
	 */
	public static String careNull(String value) {
		return StringUtils.defaultIfEmpty(value, "");
	}
	
	/**
	 * Widgetからモデルへの値転送時の、{@code null}に対する処理を行う。
	 * 
	 * @param value 入力値
	 * @param nullable {@code null}が許されるモデルフィールドであるかどうか
	 * @return 入力が空（{@code null}または空文字）であり、nullableである場合は{@code null}、そうでない場合はそのまま出力する
	 */
	public static String careNull(String value, boolean nullable) {
		if (nullable) {
			return StringUtils.isEmpty(value) ? null : value;
		}
		return value;
	}
	
	private JiemamyPropertyUtil() {
	}
}

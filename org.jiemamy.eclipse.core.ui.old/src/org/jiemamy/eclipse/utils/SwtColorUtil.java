/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/07/30
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

import org.eclipse.swt.graphics.Color;

/**
 * {@link Color}に対するユーティリティクラス。
 * 
 * @author daisuke
 */
public class SwtColorUtil {
	
	private static final int MAX_COLOR_VALUE = 255;
	

	/**
	 * 色の明度を調べる。
	 * 
	 * @param color 調査対象の色
	 * @return 明度が低い場合はtrue
	 */
	public static boolean isDarkColor(Color color) {
		int brightness = color.getRed() + color.getGreen() + color.getBlue();
		return brightness < (MAX_COLOR_VALUE * 3) / 2;
	}
	
	private SwtColorUtil() {
	}
}

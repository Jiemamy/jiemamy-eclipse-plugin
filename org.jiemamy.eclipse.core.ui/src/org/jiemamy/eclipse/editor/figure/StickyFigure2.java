/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/08/03
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
package org.jiemamy.eclipse.editor.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.graphics.Color;

import org.jiemamy.eclipse.utils.SwtColorUtil;

/**
 * 付箋Figure2（候補）。
 * 
 * @author daisuke
 */
public class StickyFigure2 extends Label {
	
	/** デフォルト背景色 */
	private static final Color DEFAULT = new Color(null, 255, 230, 230);
	

	/**
	 * インスタンスを生成する。
	 */
	public StickyFigure2() {
		setLayoutManager(new BorderLayout());
		setOpaque(true);
		setBorder(new LineBorder());
	}
	
	/**
	 * ラベルを取得する。
	 * 
	 * <p>StickyFigureとインターフェイスを揃える為の…。</p>
	 * 
	 * @return ラベル
	 */
	public Label getContentsLabel() {
		return this;
	}
	
	/**
	 * 背景色を設定する。
	 * 
	 * @param bgColor 背景色
	 */
	public void setBgColor(Color bgColor) {
		setBackgroundColor(bgColor == null ? DEFAULT : bgColor);
		if (SwtColorUtil.isDarkColor(getBackgroundColor())) {
			setForegroundColor(ColorConstants.white);
		} else {
			setForegroundColor(ColorConstants.black);
		}
	}
	
	/**
	 * 付箋の内容文を設定する。
	 * 
	 * @param contents 内容文
	 */
	public void setContents(String contents) {
		setText(contents);
	}
}

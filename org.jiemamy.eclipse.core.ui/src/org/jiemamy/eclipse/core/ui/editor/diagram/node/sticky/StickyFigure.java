/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.sticky;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import org.jiemamy.eclipse.core.ui.utils.SwtColorUtil;
import org.jiemamy.model.JmStickyNode;

/**
 * {@link JmStickyNode}用Figure（ビュー）。
 * 
 * @author daisuke
 */
public class StickyFigure extends RoundedRectangle {
	
	/** デフォルト背景色 */
	private static final Color DEFAULT = new Color(null, 255, 230, 230);
	
	/** 内容文用Figure */
	private Label contentsLabel;
	
	
	/**
	 * インスタンスを生成する。
	 */
	public StickyFigure() {
		setLayoutManager(new BorderLayout());
		contentsLabel = new Label();
		setBorder(new MarginBorder(5));
		setCornerDimensions(new Dimension(16, 16));
		add(contentsLabel, BorderLayout.CENTER);
	}
	
	/**
	 * 内容文用Figureを取得する。
	 * 
	 * @return 内容文用Figure
	 */
	public Label getContentsLabel() {
		return contentsLabel;
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
		contentsLabel.setText(contents);
	}
	
	@Override
	protected void outlineShape(Graphics graphics) {
		super.outlineShape(graphics);
		Rectangle f = Rectangle.SINGLETON;
		Rectangle r = getBounds();
		f.x = r.x + lineWidth / 2;
		f.y = r.y + lineWidth / 2;
		f.width = 16;
		f.height = 16;
		
		// THINK シェイプの装飾
//		graphics.drawLine(new Point(f.x + f.width / 2, f.y + f.height), new Point(f.x + 50, f.y));
//		graphics.drawArc(f, -90, 270);
	}
}

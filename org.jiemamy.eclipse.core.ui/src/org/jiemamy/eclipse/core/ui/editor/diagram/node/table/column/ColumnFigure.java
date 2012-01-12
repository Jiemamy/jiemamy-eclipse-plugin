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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Rectangle;

import org.jiemamy.model.column.JmColumn;

/**
 * {@link JmColumn}用Figure（ビュー）。
 * 
 * @author daisuke
 */
public class ColumnFigure extends Label {
	
	/** 水平マージン */
	private static final int HORIZONTAL_MARGIN = 5;
	
	/** 垂直マージン */
	private static final int VERTICAL_MARGIN = 2;
	
	/** アンダーラインを引くかどうか */
	private boolean underline;
	
	
	/**
	 * インスタンスを生成する。
	 */
	public ColumnFigure() {
		setBorder(new MarginBorder(VERTICAL_MARGIN, HORIZONTAL_MARGIN, VERTICAL_MARGIN, HORIZONTAL_MARGIN));
	}
	
	/**
	 * アンダーラインを引くかどうかを設定する。
	 * 
	 * @param underline アンダーラインを引くかどうか
	 */
	public void setUnderline(boolean underline) {
		this.underline = underline;
	}
	
	@Override
	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		
		if (underline && getText().length() != 0) {
			Rectangle bounds = getBounds();
			
			int x1 = bounds.x + 1 + HORIZONTAL_MARGIN;
			int x2 = bounds.x + bounds.width - 2 - HORIZONTAL_MARGIN;
			int y = bounds.y + bounds.height - 2;
			
			graphics.drawLine(x1, y, x2, y);
		}
	}
}

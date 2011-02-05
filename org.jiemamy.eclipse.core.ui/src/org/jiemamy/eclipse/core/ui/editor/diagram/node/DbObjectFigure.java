/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.graphics.Color;

import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column.CompartmentFigure;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column.CompartmentFigureBorder;
import org.jiemamy.eclipse.core.ui.utils.SwtColorUtil;
import org.jiemamy.model.DbObject;

/**
 * {@link DbObject}のFigure（ビュー）。
 * 
 * @author daisuke
 */
public abstract class DbObjectFigure extends Figure {
	
	/** {@link DbObject}名表示部分Figure */
	private Label dbObjectNameLabel;
	
	/** カラム表示部分Figure */
	private CompartmentFigure columnFigure;
	
	/** デフォルト背景色 */
	private Color defaultColor;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param defaultColor デフォルト背景色
	 */
	public DbObjectFigure(Color defaultColor) {
		dbObjectNameLabel = new Label();
		dbObjectNameLabel.setBorder(new MarginBorder(2, 2, 0, 2));
		
		columnFigure = new CompartmentFigure();
		columnFigure.setBorder(new CompartmentFigureBorder());
		
		this.defaultColor = defaultColor;
	}
	
	/**
	 * カラム表示部分Figureを取得する。
	 * 
	 * @return カラム表示部分Figure
	 */
	public CompartmentFigure getColumnFigure() {
		return columnFigure;
	}
	
	/**
	 * デフォルト背景色を取得する。
	 * 
	 * @return デフォルト背景色
	 */
	public Color getDefaultColor() {
		return defaultColor;
	}
	
	/**
	 * {@link DbObject}名表示部分Figureを取得する。
	 * 
	 * @return {@link DbObject}名表示部分Figure
	 */
	public Label getDbObjectNameLabel() {
		return dbObjectNameLabel;
	}
	
	/**
	 * 背景色を設定する。
	 * 
	 * @param bgColor 背景色. {@code null}の場合、デフォルトの色を設定する
	 */
	public void setBgColor(Color bgColor) {
		setBackgroundColor(bgColor == null ? defaultColor : bgColor);
		if (SwtColorUtil.isDarkColor(getBackgroundColor())) {
			dbObjectNameLabel.setForegroundColor(ColorConstants.white);
			columnFigure.setForegroundColor(ColorConstants.white);
		} else {
			dbObjectNameLabel.setForegroundColor(ColorConstants.black);
			columnFigure.setForegroundColor(ColorConstants.black);
		}
	}
	
	/**
	 * DbObject名を設定する。
	 * 
	 * @param dbObjectName DbObject名
	 */
	public void setDbObjectName(String dbObjectName) {
		dbObjectNameLabel.setText(dbObjectName);
	}
	
}

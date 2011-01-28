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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;

import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.DatabaseObjectFigure;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column.ColumnFigure;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column.CompartmentFigure;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column.CompartmentFigureBorder;
import org.jiemamy.eclipse.core.ui.utils.SwtColorUtil;
import org.jiemamy.model.table.TableModel;

/**
 * {@link TableModel}の{@link IFigure}（ビュー）。
 * 
 * @author daisuke
 */
public class TableFigure extends DatabaseObjectFigure {
	
	private ColumnLayoutFigure columnFigure = new ColumnLayoutFigure();
	
	private CompartmentFigure columnNameFigure = new CompartmentFigure();
	
	private CompartmentFigure columnTypeFigure = new CompartmentFigure();
	

	/**
	 * インスタンスを生成する。
	 */
	public TableFigure() {
		super(ColorConstants.tooltipBackground);
		
		Label entityNameLabel = getEntityNameLabel();
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		entityNameLabel.setIcon(ir.get(Images.LABEL_TABLE));
		
		setLayoutManager(new ToolbarLayout());
		setBorder(new LineBorder(ColorConstants.black, 1));
		setBackgroundColor(getDefaultColor());
		setOpaque(true);
		
		add(entityNameLabel);
		add(columnFigure);
		
		columnFigure.add(columnNameFigure);
		columnFigure.add(columnTypeFigure);
	}
	
	/**
	 * フィギュアを追加する。
	 * 
	 * @param nameFigure カラム名
	 * @param typeFigure 型
	 */
	public void add(ColumnFigure nameFigure, ColumnFigure typeFigure) {
		columnNameFigure.add(nameFigure);
		columnTypeFigure.add(typeFigure);
	}
	
	@Override
	public void remove(IFigure figure) {
		if (figure instanceof ColumnFigure) {
			columnNameFigure.remove(figure);
			columnTypeFigure.remove(figure);
		} else {
			super.remove(figure);
		}
	}
	
	/**
	 * カラムをクリアする。
	 */
	public void removeAllColumns() {
		columnNameFigure.removeAll();
		columnTypeFigure.removeAll();
	}
	
	/**
	 * 背景色を設定する。
	 * 
	 * <p>背景色に合わせて、文字色も調整する。</p>
	 * 
	 * @param bgColor 背景色
	 */
	@Override
	public void setBgColor(Color bgColor) {
		super.setBgColor(bgColor);
		
		if (SwtColorUtil.isDarkColor(getBackgroundColor())) {
			columnNameFigure.setForegroundColor(ColorConstants.white);
			columnTypeFigure.setForegroundColor(ColorConstants.white);
		} else {
			columnNameFigure.setForegroundColor(ColorConstants.black);
			columnTypeFigure.setForegroundColor(ColorConstants.black);
		}
	}
	

	private static class ColumnLayoutFigure extends Figure {
		
		/**
		 * インスタンスを生成する。
		 */
		public ColumnLayoutFigure() {
			ToolbarLayout layout = new ToolbarLayout(true);
			layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
			layout.setStretchMinorAxis(false);
			layout.setSpacing(2);
			setLayoutManager(layout);
			setBorder(new CompartmentFigureBorder());
		}
	}
}

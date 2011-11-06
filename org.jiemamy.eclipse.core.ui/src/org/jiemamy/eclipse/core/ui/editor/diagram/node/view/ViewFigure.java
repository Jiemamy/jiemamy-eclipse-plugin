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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.view;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;

import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.DbObjectFigure;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column.ColumnFigure;
import org.jiemamy.eclipse.core.ui.utils.SwtColorUtil;
import org.jiemamy.model.view.JmView;

/**
 * {@link JmView}の{@link IFigure}（ビュー）。
 * 
 * @author daisuke
 */
public class ViewFigure extends DbObjectFigure {
	
	private static final Color DEFAULT = new Color(null, 240, 250, 255);
	
	
	/**
	 * インスタンスを生成する。
	 */
	public ViewFigure() {
		super(DEFAULT);
		
		Label dbObjectNameLabel = getDbObjectNameLabel();
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		dbObjectNameLabel.setIcon(ir.get(Images.LABEL_VIEW));
		
		setLayoutManager(new ToolbarLayout());
		setBorder(new LineBorder(ColorConstants.black, 1));
		setBackgroundColor(getDefaultColor());
		setOpaque(true);
		
		add(dbObjectNameLabel);
		add(getColumnFigure());
	}
	
	@Override
	public void add(IFigure figure, Object constraint, int index) {
		if (figure instanceof ColumnFigure) {
			getColumnFigure().add(figure);
		} else {
			super.add(figure, constraint, index);
		}
	}
	
	@Override
	public void remove(IFigure figure) {
		if (figure instanceof ColumnFigure) {
			getColumnFigure().remove(figure);
		} else {
			super.remove(figure);
		}
	}
	
	/**
	 * 全てのFigureをクリアする。
	 */
	public void removeAllColumns() {
		getColumnFigure().removeAll();
	}
	
	/**
	 * 背景色を設定する。
	 * 
	 * <p>背景色に合わせて、文字色も調整する。</p>
	 * 
	 * @param bgColor 背景色. {@code null}の場合、デフォルトの色を設定する
	 */
	@Override
	public void setBgColor(Color bgColor) {
		super.setBgColor(bgColor);
		
		if (SwtColorUtil.isDarkColor(getBackgroundColor())) {
			setForegroundColor(ColorConstants.white);
		} else {
			setForegroundColor(ColorConstants.black);
		}
	}
}

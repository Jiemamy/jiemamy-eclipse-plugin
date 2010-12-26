/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/02/17
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
package org.jiemamy.eclipse.editor.tools;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;

/**
 * ノードに対するセルエディタロケータ。
 * 
 * @author daisuke
 */
public class NodeCellEditorLocator implements CellEditorLocator {
	
	private IFigure figure;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param figure 対象フィギュア
	 */
	public NodeCellEditorLocator(IFigure figure) {
		this.figure = figure;
	}
	
	public void relocate(CellEditor celleditor) {
		Text text = (Text) celleditor.getControl();
		/* Point pref = */text.computeSize(-1, -1);
		Rectangle rect = figure.getBounds().getCopy();
		figure.translateToAbsolute(rect);
		text.setBounds(rect.x, rect.y, rect.width, rect.height);
	}
}

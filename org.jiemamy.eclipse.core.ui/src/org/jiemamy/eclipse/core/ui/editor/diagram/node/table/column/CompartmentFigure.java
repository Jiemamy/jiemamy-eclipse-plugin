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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ToolbarLayout;

import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.TableFigure;

/**
 * {@link TableFigure}に包含されるFigure（ビュー）。
 * 
 * @author daisuke
 */
public class CompartmentFigure extends Figure {
	
	/**
	 * インスタンスを生成する。
	 */
	public CompartmentFigure() {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		layout.setStretchMinorAxis(false);
		layout.setSpacing(2);
		setLayoutManager(layout);
	}
}

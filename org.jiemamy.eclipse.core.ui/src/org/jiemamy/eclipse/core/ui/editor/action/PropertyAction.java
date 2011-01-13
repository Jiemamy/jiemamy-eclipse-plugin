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
package org.jiemamy.eclipse.core.ui.editor.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;

import org.jiemamy.eclipse.core.ui.editor.editpart.EditDialogSupport;

/**
 * Propertiesアクション。
 * 
 * @author daisuke
 */
public class PropertyAction extends AbstractJiemamyAction {
	
	/**
	 * インスタンスを生成する。
	 * @param viewer ビューア
	 */
	public PropertyAction(GraphicalViewer viewer) {
		super(Messages.PropertyAction_name, viewer);
	}
	
	@Override
	public void run() {
		// TODO 現状、その時フォーカスされているFigureのEditPartを取得している。
		// 「右クリックしたFigure」のEditPartにできないか？
		EditPart ep = getViewer().getFocusEditPart();
		if (ep instanceof EditDialogSupport) {
			EditDialogSupport editDialogSupport = (EditDialogSupport) ep;
			editDialogSupport.openEditDialog();
		}
	}
}

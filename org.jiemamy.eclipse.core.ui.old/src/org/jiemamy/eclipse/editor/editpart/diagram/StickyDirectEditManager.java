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
package org.jiemamy.eclipse.editor.editpart.diagram;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.CellEditor;

/**
 * 付箋のダイレクト編集マネージャ。
 * 
 * @author daisuke
 */
public class StickyDirectEditManager extends DirectEditManager {
	
	private StickyModel sticky;
	

	/**
	 * コンストラクタ。
	 * 
	 * @param source ダイレクト編集リクエストの発生元 edit part
	 * @param editorType セルエディタの種類
	 * @param locator セルエディタロケータ
	 */
	public StickyDirectEditManager(GraphicalEditPart source, Class<? extends CellEditor> editorType,
			CellEditorLocator locator) {
		super(source, editorType, locator);
		sticky = (StickyModel) source.getModel();
	}
	
	@Override
	protected void initCellEditor() {
		// CellEditorを表示する前に、現在モデルに設定されているテキストをCellEditorの初期値として設定する
		getCellEditor().setValue(sticky.getContents());
	}
}

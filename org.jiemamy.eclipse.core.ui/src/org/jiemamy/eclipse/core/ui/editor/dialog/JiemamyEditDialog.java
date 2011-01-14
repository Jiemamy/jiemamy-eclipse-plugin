/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/24
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
package org.jiemamy.eclipse.core.ui.editor.dialog;

import org.eclipse.swt.widgets.Shell;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DatabaseObjectModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DefaultNodeModel;

/**
 * Jiemamyのモデル編集ダイアログ抽象クラス。
 * 
 * @param <T> 編集対象Coreモデルの型
 * @author daisuke
 */
public abstract class JiemamyEditDialog<T extends DatabaseObjectModel> extends JiemamyEditDialog0<T> {
	
	private DefaultNodeModel nodeModel;
	
	private DefaultDiagramModel diagramModel;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentShell 親シェル
	 * @param context コンテキスト
	 * @param targetCoreModel 編集対象モデルの型
	 * @param type 編集対象モデルの型
	 * @throws IllegalArgumentException 引数targetModel, typeに{@code null}を与えた場合
	 */
	protected JiemamyEditDialog(Shell parentShell, JiemamyContext context, T targetCoreModel, Class<?> type,
			int diagramIndex) {
		super(parentShell, context, targetCoreModel, type);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(diagramIndex);
		nodeModel = (DefaultNodeModel) diagramModel.getNodeFor(targetCoreModel.toReference());
	}
	
	protected DefaultDiagramModel getDiagramModel() {
		return diagramModel;
	}
	
	protected DefaultNodeModel getNodeModel() {
		return nodeModel;
	}
}

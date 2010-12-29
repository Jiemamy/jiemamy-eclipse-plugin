/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.command;

import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.NodeModel;

/**
 * ノード削除GEFコマンド。
 * 
 * @author daisuke
 */
public class DeleteNodeCommand extends Command {
	
	private final JiemamyContext context;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	
	/** 削除されるノード */
	private NodeModel nodeModel;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param nodeModel 削除されるノード
	 */
	public DeleteNodeCommand(JiemamyContext context, int diagramIndex, NodeModel nodeModel) {
		this.context = context;
		this.diagramIndex = diagramIndex;
		this.nodeModel = nodeModel;
	}
	
	@Override
	public void execute() {
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(diagramIndex);
		diagramModel.delete(nodeModel.toReference());
		facet.store(diagramModel);
	}
	
	@Override
	public void undo() {
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(diagramIndex);
		diagramModel.store(nodeModel);
		facet.store(diagramModel);
	}
	
}

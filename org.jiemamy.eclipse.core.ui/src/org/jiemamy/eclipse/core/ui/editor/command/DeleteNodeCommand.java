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
package org.jiemamy.eclipse.core.ui.editor.command;

import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.NodeModel;

/**
 * ノード削除GEFコマンド。
 * 
 * @author daisuke
 */
public class DeleteNodeCommand extends Command {
	
	private final DiagramFacet diagramFacet;
	
	private final DefaultDiagramModel diagramModel;
	
	/** 削除されるノード */
	private final NodeModel nodeModel;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param diagramFacet facet
	 * @param diagramModel ダイアグラム
	 * @param nodeModel 削除されるノード
	 */
	public DeleteNodeCommand(DiagramFacet diagramFacet, DefaultDiagramModel diagramModel, NodeModel nodeModel) {
		this.diagramFacet = diagramFacet;
		this.diagramModel = diagramModel;
		this.nodeModel = nodeModel;
	}
	
	@Override
	public void execute() {
		diagramModel.delete(nodeModel.toReference());
		diagramFacet.store(diagramModel);
	}
	
	@Override
	public void undo() {
		diagramModel.store(nodeModel);
		diagramFacet.store(diagramModel);
	}
}

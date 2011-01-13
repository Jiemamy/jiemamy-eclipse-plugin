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
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DefaultNodeModel;
import org.jiemamy.model.geometory.JmColor;

/**
 * ノードの背景色変更GEFコマンド。
 * 
 * @author daisuke
 */
public class ChangeNodeColorCommand extends Command {
	
	private final DiagramFacet diagramFacet;
	
	private final DefaultDiagramModel diagramModel;
	
	private final EntityRef<? extends DefaultNodeModel> nodeRef;
	
	private final JmColor newColor;
	
	private final JmColor oldColor;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param diagramFacet ファセット
	 * @param diagramModel ダイアグラム
	 * @param nodeRef 変更対象のエンティティ
	 * @param newColor newColor
	 */
	public ChangeNodeColorCommand(DiagramFacet diagramFacet, DefaultDiagramModel diagramModel,
			EntityRef<? extends DefaultNodeModel> nodeRef, JmColor newColor) {
		this.diagramFacet = diagramFacet;
		this.diagramModel = diagramModel;
		this.nodeRef = nodeRef;
		this.newColor = newColor;
		DefaultNodeModel node = diagramModel.resolve(nodeRef);
		oldColor = node.getColor();
	}
	
	@Override
	public void execute() {
		DefaultNodeModel node = diagramModel.resolve(nodeRef);
		
		node.setColor(newColor);
		diagramModel.store(node);
		diagramFacet.store(diagramModel);
	}
	
	@Override
	public void undo() {
		DefaultNodeModel node = diagramModel.resolve(nodeRef);
		
		node.setColor(oldColor);
		diagramModel.store(node);
		diagramFacet.store(diagramModel);
	}
}

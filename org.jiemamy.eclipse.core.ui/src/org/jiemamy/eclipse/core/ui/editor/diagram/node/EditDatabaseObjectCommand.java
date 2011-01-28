/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2011/01/26
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DatabaseObjectModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.NodeModel;

/**
 * {@link DatabaseObjectModel}を編集するコマンド。
 * 
 * @version $Id$
 * @author daisuke
 */
public class EditDatabaseObjectCommand extends Command {
	
	private final JiemamyContext context;
	
	private final DatabaseObjectModel dom;
	
	private final DatabaseObjectModel oldDom;
	
	private final NodeModel nodeModel;
	
	private final NodeModel oldNodeModel;
	
	private final int diagramIndex;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context コンテキスト
	 * @param dom 編集対象{@link DatabaseObjectModel}
	 * @param nodeModel ノード
	 * @param diagramIndex
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public EditDatabaseObjectCommand(JiemamyContext context, DatabaseObjectModel dom, NodeModel nodeModel,
			int diagramIndex) {
		Validate.notNull(context);
		Validate.notNull(dom);
		Validate.notNull(nodeModel);
		
		this.context = context;
		this.dom = dom;
		this.nodeModel = nodeModel;
		this.diagramIndex = diagramIndex;
		oldNodeModel = context.resolve(nodeModel.toReference());
		oldDom = context.resolve(dom.toReference());
	}
	
	@Override
	public void execute() {
		context.store(dom);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(diagramIndex);
		diagramModel.store(nodeModel);
		facet.store(diagramModel);
	}
	
	@Override
	public void undo() {
		context.store(oldDom);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(diagramIndex);
		diagramModel.store(oldNodeModel);
		facet.store(diagramModel);
	}
}

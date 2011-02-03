/*
 * Copyright 2007-2010 Jiemamy Project and the Others.
 * Created on 2010/12/28
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

import java.util.UUID;

import org.apache.commons.lang.Validate;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.NodeCreation;
import org.jiemamy.model.DefaultDatabaseObjectNodeModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DefaultNodeModel;
import org.jiemamy.model.geometory.JmRectangle;
import org.jiemamy.model.view.DefaultViewModel;
import org.jiemamy.utils.NamingUtil;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class ViewCreation implements NodeCreation {
	
	private final DefaultViewModel view;
	
	private final DefaultNodeModel node;
	

	/**
	 * インスタンスを生成する。
	 */
	public ViewCreation() {
		view = new DefaultViewModel(UUID.randomUUID());
		node = new DefaultDatabaseObjectNodeModel(UUID.randomUUID(), view.toReference());
	}
	
	public void execute(JiemamyContext context, DefaultDiagramModel diagramModel) {
		Validate.notNull(context);
		Validate.notNull(diagramModel);
		
		NamingUtil.autoName(view, context);
		context.store(view);
		diagramModel.store(node);
		context.getFacet(DiagramFacet.class).store(diagramModel);
	}
	
	public void setBoundary(JmRectangle boundary) {
		Validate.notNull(boundary);
		node.setBoundary(boundary);
	}
	
	public void undo(JiemamyContext context, DefaultDiagramModel diagramModel) {
		Validate.notNull(context);
		Validate.notNull(diagramModel);
		
		diagramModel.deleteNode(node.toReference());
		context.getFacet(DiagramFacet.class).store(diagramModel);
		context.deleteDatabaseObject(view.toReference());
	}
}

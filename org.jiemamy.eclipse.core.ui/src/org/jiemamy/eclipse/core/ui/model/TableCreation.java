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
package org.jiemamy.eclipse.core.ui.model;

import java.util.UUID;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DefaultDatabaseObjectNodeModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DefaultNodeModel;
import org.jiemamy.model.geometory.JmRectangle;
import org.jiemamy.model.table.DefaultTableModel;
import org.jiemamy.utils.NamingUtil;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class TableCreation implements NodeCreation {
	
	private final DefaultTableModel table;
	
	private final DefaultNodeModel node;
	

	/**
	 * インスタンスを生成する。
	 */
	public TableCreation() {
		table = new DefaultTableModel(UUID.randomUUID());
		node = new DefaultDatabaseObjectNodeModel(UUID.randomUUID(), table.toReference());
	}
	
	public void execute(JiemamyContext context, DefaultDiagramModel diagramModel) {
		NamingUtil.autoName(table, context);
		context.store(table);
		diagramModel.store(node);
		context.getFacet(DiagramFacet.class).store(diagramModel);
	}
	
	public void setBoundary(JmRectangle boundary) {
		node.setBoundary(boundary);
	}
	
	public void undo(JiemamyContext context, DefaultDiagramModel diagramModel) {
		diagramModel.deleteNode(node.toReference());
		context.getFacet(DiagramFacet.class).store(diagramModel);
		context.deleteDatabaseObject(table.toReference());
	}
}

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
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.model.DefaultConnectionModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.constraint.DefaultForeignKeyConstraintModel;
import org.jiemamy.model.table.DefaultTableModel;
import org.jiemamy.utils.ForeignKeyFactory;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class ForeignKeyCreation implements Creation {
	
	private final DefaultForeignKeyConstraintModel foreignKey;
	
	private final DefaultConnectionModel connection;
	
	private DefaultTableModel sourceTable;
	
	private DefaultTableModel targetTable;
	

	/**
	 * インスタンスを生成する。
	 */
	public ForeignKeyCreation() {
		foreignKey = new DefaultForeignKeyConstraintModel(UUID.randomUUID());
		connection = new DefaultConnectionModel(UUID.randomUUID(), foreignKey.toReference());
	}
	
	public void execute(JiemamyContext context, DefaultDiagramModel diagramModel) {
		ForeignKeyFactory.setup(foreignKey, context, sourceTable, targetTable);
		
		sourceTable.store(foreignKey);
		context.store(sourceTable);
		
		diagramModel.store(connection);
		context.getFacet(DiagramFacet.class).store(diagramModel);
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @param sourceRef 起点ノードの参照
	 */
	public void setSource(EntityRef<? extends NodeModel> sourceRef) {
		connection.setSource(sourceRef);
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @param sourceTable
	 */
	public void setSourceTable(DefaultTableModel sourceTable) {
		this.sourceTable = sourceTable;
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @param targetRef 終点ノードの参照
	 */
	public void setTarget(EntityRef<? extends NodeModel> targetRef) {
		connection.setTarget(targetRef);
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @param targetTable
	 */
	public void setTargetTable(DefaultTableModel targetTable) {
		this.targetTable = targetTable;
	}
	
	public void undo(JiemamyContext context, DefaultDiagramModel diagramModel) {
		diagramModel.deleteConnection(connection.toReference());
		context.getFacet(DiagramFacet.class).store(diagramModel);
		
		sourceTable.deleteConstraint(foreignKey.toReference());
		context.store(sourceTable);
	}
}

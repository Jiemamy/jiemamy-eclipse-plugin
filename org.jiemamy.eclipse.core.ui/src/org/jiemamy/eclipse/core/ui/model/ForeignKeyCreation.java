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
import org.jiemamy.model.table.TableModel;
import org.jiemamy.utils.ForeignKeyFactory;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class ForeignKeyCreation implements Creation {
	
	private DefaultTableModel sourceTable;
	
	private TableModel targetTable;
	
	private EntityRef<? extends NodeModel> sourceRef;
	
	private EntityRef<? extends NodeModel> targetRef;
	

	public void execute(JiemamyContext context, DefaultDiagramModel diagramModel) {
		DefaultForeignKeyConstraintModel fk = ForeignKeyFactory.create(context, sourceTable, targetTable);
		DefaultConnectionModel connection = new DefaultConnectionModel(UUID.randomUUID(), fk.toReference());
		connection.setSource(sourceRef);
		connection.setTarget(targetRef);
		
		sourceTable.store(fk);
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
		this.sourceRef = sourceRef;
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
		this.targetRef = targetRef;
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @param targetTable
	 */
	public void setTargetTable(TableModel targetTable) {
		this.targetTable = targetTable;
	}
}

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
package org.jiemamy.eclipse.core.ui.editor.diagram.connection;

import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.constraint.DefaultForeignKeyConstraintModel;
import org.jiemamy.model.table.DefaultTableModel;

/**
 * コネクション削除GEFコマンド。
 * 
 * @author daisuke
 */
public class DeleteConnectionCommand extends Command {
	
	private JiemamyContext context;
	
	private ConnectionModel connection;
	
	private DefaultTableModel tableModel;
	
	private DefaultForeignKeyConstraintModel foreignKey;
	
	private DefaultDiagramModel diagmramModel;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context コンテスキースト
	 * @param connection 削除対象のコネクション
	 */
	public DeleteConnectionCommand(JiemamyContext context, ConnectionModel connection) {
		this.context = context;
		this.connection = connection;
	}
	
	@Override
	public void execute() {
		foreignKey = (DefaultForeignKeyConstraintModel) context.resolve(connection.getCoreModelRef());
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		diagmramModel = (DefaultDiagramModel) facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		diagmramModel.deleteConnection(connection.toReference());
		facet.store(diagmramModel);
		
		tableModel = (DefaultTableModel) foreignKey.findDeclaringTable(context.getTables());
		tableModel.deleteConstraint(foreignKey.toReference());
		context.store(tableModel);
	}
	
	@Override
	public void undo() {
		tableModel.store(foreignKey);
		context.store(tableModel);
		
		diagmramModel.store(connection);
		context.getFacet(DiagramFacet.class).store(diagmramModel);
	}
}

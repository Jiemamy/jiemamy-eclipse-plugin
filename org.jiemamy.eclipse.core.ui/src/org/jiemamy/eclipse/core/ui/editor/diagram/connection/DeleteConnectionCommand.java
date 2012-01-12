/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
import org.jiemamy.model.JmConnection;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.constraint.SimpleJmForeignKeyConstraint;
import org.jiemamy.model.table.SimpleJmTable;

/**
 * コネクション削除GEFコマンド。
 * 
 * @author daisuke
 */
public class DeleteConnectionCommand extends Command {
	
	private JiemamyContext context;
	
	private JmConnection connection;
	
	private SimpleJmTable table;
	
	private SimpleJmForeignKeyConstraint foreignKey;
	
	private SimpleJmDiagram diagmram;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param context コンテスキースト
	 * @param connection 削除対象のコネクション
	 */
	public DeleteConnectionCommand(JiemamyContext context, JmConnection connection) {
		this.context = context;
		this.connection = connection;
	}
	
	@Override
	public void execute() {
		foreignKey = (SimpleJmForeignKeyConstraint) context.resolve(connection.getCoreModelRef());
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		diagmram = (SimpleJmDiagram) facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		diagmram.deleteConnection(connection.toReference());
		facet.store(diagmram);
		
		table = (SimpleJmTable) foreignKey.findDeclaringTable(context.getTables());
		table.deleteConstraint(foreignKey.toReference());
		context.store(table);
	}
	
	@Override
	public void undo() {
		table.store(foreignKey);
		context.store(table);
		
		diagmram.store(connection);
		context.getFacet(DiagramFacet.class).store(diagmram);
	}
}

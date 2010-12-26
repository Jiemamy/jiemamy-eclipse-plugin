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
package org.jiemamy.eclipse.editor.command;

import org.eclipse.gef.commands.Command;

import org.jiemamy.JiemamyContext;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.attribute.constraint.ForeignKeyConstraintModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.transaction.SavePoint;

/**
 * コネクション削除GEFコマンド。
 * 
 * @author daisuke
 */
public class DeleteConnectionCommand extends Command {
	
	private JiemamyContext rootModel;
	
	private ConnectionModel connection;
	
	private JiemamyViewFacade jiemamyFacade;
	
	private SavePoint save;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel ルートモデル
	 * @param connection 削除対象のコネクション
	 */
	public DeleteConnectionCommand(JiemamyContext rootModel, ConnectionModel connection) {
		this.rootModel = rootModel;
		this.connection = connection;
		
		jiemamyFacade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
	}
	
	@Override
	public void execute() {
		ForeignKeyConstraintModel foreignKey = connection.unwrap();
		TableModel definedTable = foreignKey.findDeclaringTable();
		
		save = jiemamyFacade.save();
		jiemamyFacade.removeAttribute(definedTable, foreignKey);
	}
	
	@Override
	public void undo() {
		jiemamyFacade.rollback(save);
		rootModel.normalize();
	}
}

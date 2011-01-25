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
package org.jiemamy.eclipse.core.ui.editor.command;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;

import org.jiemamy.JiemamyContext;
import org.jiemamy.model.constraint.ForeignKeyConstraintModel;
import org.jiemamy.model.table.DefaultTableModel;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class EditForeignKeyCommand extends Command {
	
	private final JiemamyContext context;
	
	private final ForeignKeyConstraintModel foreignKey;
	
	private final DefaultTableModel tableModel;
	
	private final DefaultTableModel oldTableModel;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context
	 * @param foreignKey
	 */
	public EditForeignKeyCommand(JiemamyContext context, ForeignKeyConstraintModel foreignKey) {
		Validate.notNull(context);
		Validate.notNull(foreignKey);
		this.context = context;
		this.foreignKey = foreignKey;
		tableModel = (DefaultTableModel) DefaultTableModel.findDeclaringTable(context.getTables(), foreignKey);
		oldTableModel = context.resolve(tableModel.toReference());
		
		tableModel.store(foreignKey);
	}
	
	@Override
	public void execute() {
		context.store(tableModel);
	}
	
	@Override
	public void undo() {
		context.store(oldTableModel);
	}
}

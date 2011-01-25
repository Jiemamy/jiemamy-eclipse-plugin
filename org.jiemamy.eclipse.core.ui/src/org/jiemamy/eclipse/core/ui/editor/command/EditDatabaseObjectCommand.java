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
import org.jiemamy.model.DatabaseObjectModel;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class EditDatabaseObjectCommand extends Command {
	
	private final JiemamyContext context;
	
	private final DatabaseObjectModel dom;
	
	private final DatabaseObjectModel old;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context
	 * @param dom
	 */
	public EditDatabaseObjectCommand(JiemamyContext context, DatabaseObjectModel dom) {
		Validate.notNull(context);
		Validate.notNull(dom);
		
		this.context = context;
		this.dom = dom;
		old = context.resolve(dom.toReference());
	}
	
	@Override
	public void execute() {
		context.store(dom);
	}
	
	@Override
	public void undo() {
		context.store(old);
	}
}

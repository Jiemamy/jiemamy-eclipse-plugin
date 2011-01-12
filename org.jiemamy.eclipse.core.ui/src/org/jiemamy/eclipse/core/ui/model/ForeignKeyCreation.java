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

import org.apache.commons.lang.Validate;

import org.jiemamy.JiemamyContext;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.constraint.ForeignKeyConstraintModel;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class ForeignKeyCreation implements Creation {
	
	private final ForeignKeyConstraintModel fk;
	
	private final ConnectionModel connection;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param fk 作成する外部キー
	 * @param connection 作成するコネクション
	 */
	public ForeignKeyCreation(ForeignKeyConstraintModel fk, ConnectionModel connection) {
		Validate.notNull(fk);
		Validate.notNull(connection);
		this.fk = fk;
		this.connection = connection;
	}
	
	public void execute(JiemamyContext context, DefaultDiagramModel diagramModel) {
		// TODO Auto-generated method stub
		
	}
	
	public ForeignKeyConstraintModel getCoreElement() {
		return fk;
	}
	
	public ConnectionModel getDiagramElement() {
		return connection;
	}
	
}

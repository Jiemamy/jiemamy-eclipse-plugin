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
package org.jiemamy.eclipse.core.ui.editor.diagram.node;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.diagram.connection.CreateConnectionCommand;
import org.jiemamy.eclipse.core.ui.editor.diagram.connection.ForeignKeyCreation;
import org.jiemamy.model.JmNode;

/**
 * GraphicalNodeののEditPolicy。
 * 
 * @author daisuke
 */
public class JmGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy {
	
	/**
	 * {@inheritDoc}
	 * 
	 * memo: 多分、コネクションを引き終わる時（終点ノードをクリックした時）に呼ばれる。
	 */
	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		CreateConnectionCommand command = (CreateConnectionCommand) request.getStartCommand();
		command.setTarget((JmNode) getHost().getModel());
		return command;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * memo: 多分、コネクションを引き始める時（起点ノードをクリックした時）に呼ばれる。
	 */
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		ForeignKeyCreation creation = (ForeignKeyCreation) request.getNewObject();
		CreateConnectionCommand command =
				new CreateConnectionCommand(getJiemamyContext(), TODO.DIAGRAM_INDEX, creation);
		command.setSource((JmNode) getHost().getModel());
		command.setFigureSize(getHostFigure().getSize());
		request.setStartCommand(command);
		
		return command;
	}
	
	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		return null;
	}
	
	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		return null;
	}
	
	private JiemamyContext getJiemamyContext() {
		return (JiemamyContext) getHost().getRoot().getContents().getModel();
	}
}

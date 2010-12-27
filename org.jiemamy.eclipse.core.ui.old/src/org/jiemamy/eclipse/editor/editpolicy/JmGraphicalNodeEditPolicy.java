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
package org.jiemamy.eclipse.editor.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import org.jiemamy.JiemamyContext;
import org.jiemamy.JiemamyEntity;
import org.jiemamy.eclipse.Migration;
import org.jiemamy.eclipse.editor.command.CreateConnectionCommand;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.NodeModel;

/**
 * GraphicalNodeののEditPolicy。
 * 
 * @author daisuke
 */
public class JmGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy {
	
	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		CreateConnectionCommand command = (CreateConnectionCommand) request.getStartCommand();
		command.setTarget((NodeModel) getHost().getModel());
		return command;
	}
	
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		JiemamyEntity model = (JiemamyEntity) request.getNewObject();
		JiemamyContext rootModel = (JiemamyContext) getHost().getRoot().getContents().getModel();
		ConnectionModel connection = model.getAdapter(ConnectionModel.class);
		CreateConnectionCommand command = new CreateConnectionCommand(rootModel, Migration.DIAGRAM_INDEX, connection);
		command.setSource((NodeModel) getHost().getModel());
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
}

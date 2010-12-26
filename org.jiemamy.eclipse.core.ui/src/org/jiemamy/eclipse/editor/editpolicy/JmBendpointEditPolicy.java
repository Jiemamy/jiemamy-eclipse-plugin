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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.BendpointEditPolicy;
import org.eclipse.gef.requests.BendpointRequest;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.command.CreateBendpointCommand;
import org.jiemamy.eclipse.editor.command.DeleteBendpointCommand;
import org.jiemamy.eclipse.editor.command.MoveBendpointCommand;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.attribute.constraint.ForeignKeyConstraintModel;

/**
 * BendpointのEditPolicy。
 * 
 * @author daisuke
 */
public class JmBendpointEditPolicy extends BendpointEditPolicy {
	
	@Override
	protected Command getCreateBendpointCommand(BendpointRequest request) {
		Point point = request.getLocation();
		getConnection().translateToRelative(point);
		
		ConnectionModel connection = (ConnectionModel) getHost().getModel();
		ForeignKeyConstraintModel foreignKey = connection.unwrap();
		JiemamyContext rootModel = foreignKey.getJiemamy().getFactory().getJiemamyContext();
		return new CreateBendpointCommand(rootModel, Migration.DIAGRAM_INDEX, connection, point, request.getIndex());
		
	}
	
	@Override
	protected Command getDeleteBendpointCommand(BendpointRequest request) {
		ConnectionModel connection = (ConnectionModel) getHost().getModel();
		ForeignKeyConstraintModel foreignKey = connection.unwrap();
		JiemamyContext rootModel = foreignKey.getJiemamy().getFactory().getJiemamyContext();
		return new DeleteBendpointCommand(rootModel, Migration.DIAGRAM_INDEX, connection, request.getIndex());
	}
	
	@Override
	protected Command getMoveBendpointCommand(BendpointRequest request) {
		JiemamyContext rootModel = (JiemamyContext) getHost().getRoot().getContents().getModel();
		Point location = request.getLocation();
		getConnection().translateToRelative(location);
		
		ConnectionModel connection = (ConnectionModel) getHost().getModel();
		return new MoveBendpointCommand(rootModel, Migration.DIAGRAM_INDEX, connection, request.getIndex(), location);
	}
}

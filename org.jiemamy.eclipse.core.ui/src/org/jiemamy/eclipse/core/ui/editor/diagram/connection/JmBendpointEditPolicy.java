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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.BendpointEditPolicy;
import org.eclipse.gef.requests.BendpointRequest;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.model.SimpleJmConnection;

/**
 * Bendpoint の {@link EditPolicy}。
 * 
 * @author daisuke
 */
public final class JmBendpointEditPolicy extends BendpointEditPolicy {
	
	@Override
	protected Command getCreateBendpointCommand(BendpointRequest request) {
		Point point = request.getLocation();
		getConnection().translateToRelative(point);
		
		SimpleJmConnection connection = (SimpleJmConnection) getHost().getModel();
		
		return new CreateBendpointCommand(getJiemamyContext(), TODO.DIAGRAM_INDEX, connection, point,
				request.getIndex());
		
	}
	
	@Override
	protected Command getDeleteBendpointCommand(BendpointRequest request) {
		SimpleJmConnection connection = (SimpleJmConnection) getHost().getModel();
		return new DeleteBendpointCommand(getJiemamyContext(), TODO.DIAGRAM_INDEX, connection, request.getIndex());
	}
	
	@Override
	protected Command getMoveBendpointCommand(BendpointRequest request) {
		Point location = request.getLocation();
		getConnection().translateToRelative(location);
		
		SimpleJmConnection connection = (SimpleJmConnection) getHost().getModel();
		return new MoveBendpointCommand(getJiemamyContext(), TODO.DIAGRAM_INDEX, connection, request.getIndex(),
				location);
	}
	
	private JiemamyContext getJiemamyContext() {
		return (JiemamyContext) getHost().getRoot().getContents().getModel();
	}
}

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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import org.jiemamy.JiemamyContext;
import org.jiemamy.JiemamyEntity;
import org.jiemamy.eclipse.Migration;
import org.jiemamy.eclipse.editor.command.ChangeNodeConstraintCommand;
import org.jiemamy.eclipse.editor.command.CreateNodeCommand;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.dbo.DatabaseObjectModel;

/**
 * グラフィックXYレイアウトのEditPolicy。
 * 
 * @author daisuke
 */
public class JmLayoutEditPolicy extends XYLayoutEditPolicy {
	
	/** {@link StickyModel}が作られた時、はじめに設定されている値 */
	private static final String DEFAULT_STICKY_CONTENTS = "memo";
	

	@Override
	protected Command createAddCommand(EditPart child, Object constraint) {
		return null;
	}
	
	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		JiemamyContext rootModel = (JiemamyContext) getHost().getModel();
		NodeModel nodeAdapter = (NodeModel) child.getModel();
		EditPartViewer viewer = child.getViewer();
		Rectangle rectangle = (Rectangle) constraint;
		return new ChangeNodeConstraintCommand(rootModel, Migration.DIAGRAM_INDEX, nodeAdapter, rectangle, viewer);
	}
	
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		JiemamyEntity model = (JiemamyEntity) request.getNewObject();
		JiemamyContext rootModel = (JiemamyContext) getHost().getModel();
		
		NodeModel node = null;
		if (model instanceof DatabaseObjectModel) {
			DatabaseObjectModel entityModel = (DatabaseObjectModel) model;
			EntityUtil.autoDenominate(entityModel, rootModel);
			node = entityModel.getAdapter(NodeModel.class);
		} else if (model instanceof StickyModel) {
			StickyModel stickyModel = (StickyModel) model;
			stickyModel.setContents(DEFAULT_STICKY_CONTENTS);
			node = stickyModel;
		}
		
		return new CreateNodeCommand(rootModel, Migration.DIAGRAM_INDEX, node, (Rectangle) getConstraintFor(request));
	}
	
	@Override
	protected Command getDeleteDependantCommand(Request request) {
		return null;
	}
}

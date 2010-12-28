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
package org.jiemamy.eclipse.core.ui.editor.editpolicy;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.command.ChangeNodeConstraintCommand;
import org.jiemamy.eclipse.core.ui.editor.command.CreateNodeCommand;
import org.jiemamy.eclipse.core.ui.model.CoreNodePair;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.DefaultNodeModel;
import org.jiemamy.model.StickyNodeModel;

/**
 * Jiemamy用 {@link XYLayoutEditPolicy}実装クラス。
 * 
 * @author daisuke
 */
public class JmXYLayoutEditPolicy extends XYLayoutEditPolicy {
	
	/** {@link StickyNodeModel}が作られた時、はじめに設定されている値 */
	private static final String DEFAULT_STICKY_CONTENTS = "memo";
	
	private static Logger logger = LoggerFactory.getLogger(JmXYLayoutEditPolicy.class);
	

	@Override
	protected Command createAddCommand(EditPart child, Object constraint) {
		logger.debug("createAddCommand returns null.");
		return null;
	}
	
	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		JiemamyContext context = (JiemamyContext) getHost().getModel();
		DefaultNodeModel nodeModel = (DefaultNodeModel) child.getModel();
		EditPartViewer viewer = child.getViewer();
		Rectangle rectangle = (Rectangle) constraint;
		return new ChangeNodeConstraintCommand(context, TODO.DIAGRAM_INDEX, nodeModel, rectangle, viewer);
	}
	
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		CoreNodePair model = (CoreNodePair) request.getNewObject();
		JiemamyContext context = (JiemamyContext) getHost().getModel();
		
		if (model.getDiagramElement() instanceof StickyNodeModel) {
			((StickyNodeModel) model.getDiagramElement()).setContents(DEFAULT_STICKY_CONTENTS);
		}
		
		Rectangle rect = (Rectangle) getConstraintFor(request);
		((DefaultNodeModel) model.getDiagramElement()).setBoundary(ConvertUtil.convert(rect));
		
		return new CreateNodeCommand(context, TODO.DIAGRAM_INDEX, model);
	}
	
	@Override
	protected Command getDeleteDependantCommand(Request request) {
		return null;
	}
}

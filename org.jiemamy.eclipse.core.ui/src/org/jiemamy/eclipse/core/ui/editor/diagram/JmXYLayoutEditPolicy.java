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
package org.jiemamy.eclipse.core.ui.editor.diagram;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.ChangeNodeConstraintCommand;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.CreateNodeCommand;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.NodeCreation;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.SimpleJmNode;

/**
 * Jiemamy用 {@link XYLayoutEditPolicy}実装クラス。
 * 
 * @author daisuke
 */
public class JmXYLayoutEditPolicy extends XYLayoutEditPolicy {
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>既存のノードの位置サイズが変更された時に呼ばれ、編集コマンドを返す。</p>
	 */
	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		JiemamyContext context = (JiemamyContext) getHost().getModel();
		SimpleJmNode node = (SimpleJmNode) child.getModel();
		EditPartViewer viewer = child.getViewer();
		Rectangle rectangle = (Rectangle) constraint;
		return new ChangeNodeConstraintCommand(context, TODO.DIAGRAM_INDEX, node, rectangle, viewer);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>ノードが新規に作成された時に呼ばれ、編集コマンドを返す。</p>
	 */
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		NodeCreation creation = (NodeCreation) request.getNewObject();
		JiemamyContext context = (JiemamyContext) getHost().getModel();
		
		Rectangle rect = (Rectangle) getConstraintFor(request);
		creation.setBoundary(ConvertUtil.convert(rect));
		
		return new CreateNodeCommand(context, TODO.DIAGRAM_INDEX, creation);
	}
	
	@Override
	protected Command getDeleteDependantCommand(Request request) {
		return null;
	}
}

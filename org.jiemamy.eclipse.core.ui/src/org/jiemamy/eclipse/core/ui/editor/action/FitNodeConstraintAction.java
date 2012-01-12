/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.action;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.ChangeNodeConstraintCommand;
import org.jiemamy.model.SimpleJmNode;
import org.jiemamy.model.geometory.JmRectangle;

/**
 * ノードのサイズをフィット（デフォルトサイズに変更）させるアクション。
 * 
 * @author daisuke
 */
public class FitNodeConstraintAction extends AbstractJiemamyAction {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param viewer ビューア
	 */
	public FitNodeConstraintAction(GraphicalViewer viewer) {
		super(Messages.FitNodeConstraintAction_name, viewer);
	}
	
	@Override
	public void run() {
		JiemamyContext context = (JiemamyContext) getViewer().getContents().getModel();
		Object model = getViewer().getFocusEditPart().getModel();
		
		if (model instanceof SimpleJmNode) {
			SimpleJmNode node = (SimpleJmNode) model;
			CommandStack stack = getViewer().getEditDomain().getCommandStack();
			
			JmRectangle boundary = node.getBoundary();
			JmRectangle newBoundary = new JmRectangle(boundary.x, boundary.y, -1, -1);
			
			Command command =
					new ChangeNodeConstraintCommand(context, TODO.DIAGRAM_INDEX, node, newBoundary, getViewer());
			
			stack.execute(command);
		}
	}
}

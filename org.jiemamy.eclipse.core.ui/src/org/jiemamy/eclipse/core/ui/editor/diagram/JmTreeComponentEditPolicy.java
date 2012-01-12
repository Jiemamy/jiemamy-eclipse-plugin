/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2011/03/05
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

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.JiemamyEditor;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.DeleteNodeCommand;
import org.jiemamy.eclipse.core.ui.utils.EditorUtil;
import org.jiemamy.model.DbObject;
import org.jiemamy.model.JmNode;

/**
 * アウトラインツリー用のEditPolicy。
 * 
 * @version $Id$
 * @author daisuke
 */
public class JmTreeComponentEditPolicy extends ComponentEditPolicy {
	
	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		if (getHost().getModel() instanceof DbObject) {
			// THINK ActiveEditorは必ずJiemamyEditorか？
			JiemamyEditor editor = (JiemamyEditor) EditorUtil.getActiveEditor();
			JiemamyContext context = editor.getJiemamyContext();
			JmNode node = (JmNode) getHost().getModel();
			return new DeleteNodeCommand(context, TODO.DIAGRAM_INDEX, node);
//		} else if (getHost().getModel() instanceof AbstractRelationModel) {
//			DeleteRelationCommand command = new DeleteRelationCommand();
//			command.setRelationModel(getHost().getModel());
//			
//			return command;
		}
		return null;
	}
}

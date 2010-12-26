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
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.JiemamyEditor;
import org.jiemamy.eclipse.editor.command.DeleteNodeCommand;
import org.jiemamy.eclipse.utils.EditorUtil;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.dbo.DatabaseObjectModel;

/**
 * アウトラインツリー用のEditPolicy。
 * 
 * @author daisuke
 */
public class JmTreeComponentEditPolicy extends ComponentEditPolicy {
	
	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		if (getHost().getModel() instanceof DatabaseObjectModel) {
			// THINK ActiveEditorは必ずDiagramEditorか？
			JiemamyEditor editor = (JiemamyEditor) EditorUtil.getActiveEditor();
			JiemamyContext rootModel = editor.getJiemamyContext();
			NodeModel nodeAdapter = (NodeModel) getHost().getModel();
			DeleteNodeCommand command = new DeleteNodeCommand(rootModel, Migration.DIAGRAM_INDEX, nodeAdapter);
			
			return command;
//		} else if (getHost().getModel() instanceof AbstractRelationModel) {
//			DeleteRelationCommand command = new DeleteRelationCommand();
//			command.setRelationModel(getHost().getModel());
//			
//			return command;
		}
		return null;
	}
}

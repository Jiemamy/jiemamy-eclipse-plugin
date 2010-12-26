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
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.DisplayStatus;
import org.jiemamy.eclipse.editor.command.DirectEditNodeCommand;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.NodeModel;

/**
 * エンティティ名のダイレクト編集ポリシー。
 * 
 * @author daisuke
 */
public class JmDirectEditPolicy extends DirectEditPolicy {
	
	@Override
	protected Command getDirectEditCommand(DirectEditRequest request) {
		JiemamyContext rootModel = (JiemamyContext) getHost().getRoot().getContents().getModel();
		NodeModel nodeAdapter = (NodeModel) getHost().getModel();
		
		DiagramFacet diagramPresentations = rootModel.getAdapter(DiagramFacet.class);
		DiagramModel presentation = diagramPresentations.get(Migration.DIAGRAM_INDEX);
		DisplayStatus displayStatus = DisplayStatus.find(presentation);
		
		DirectEditNodeCommand command = new DirectEditNodeCommand(rootModel, displayStatus, nodeAdapter);
		command.setNewValue((String) request.getCellEditor().getValue());
		
		return command;
	}
	
	@Override
	protected void showCurrentEditValue(DirectEditRequest request) {
		// nothing to do
	}
}

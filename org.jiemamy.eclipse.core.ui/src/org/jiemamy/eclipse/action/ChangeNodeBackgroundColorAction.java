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
package org.jiemamy.eclipse.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.command.ChangeNodeColorCommand;
import org.jiemamy.eclipse.editor.editpart.diagram.AbstractJmNodeEditPart;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.NodeModel;

/**
 * ノード背景色変更アクション。
 * 
 * @author daisuke
 */
public class ChangeNodeBackgroundColorAction extends AbstractJiemamyAction {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param viewer ビューア
	 */
	public ChangeNodeBackgroundColorAction(GraphicalViewer viewer) {
		super(Messages.ChangeNodeBgcolorAction_name, viewer);
	}
	
	@Override
	public void run() {
		JiemamyContext rootModel = (JiemamyContext) getViewer().getContents().getModel();
		List<AbstractJmNodeEditPart> editParts = new ArrayList<AbstractJmNodeEditPart>();
		for (Object ep : getViewer().getSelectedEditParts()) {
			if (ep instanceof AbstractJmNodeEditPart) {
				editParts.add((AbstractJmNodeEditPart) ep);
			}
		}
		
		if (editParts.isEmpty() == false) {
			ColorDialog colorDialog = new ColorDialog(getViewer().getControl().getShell(), SWT.NULL);
			RGB rgb = colorDialog.open();
			if (rgb != null) {
				for (AbstractJmNodeEditPart editPart : editParts) {
					NodeModel nodeAdapter = editPart.getModel();
					CommandStack stack = getViewer().getEditDomain().getCommandStack();
					Command command =
							new ChangeNodeColorCommand(rootModel, Migration.DIAGRAM_INDEX, nodeAdapter,
									ConvertUtil.convert(rgb));
					
					stack.execute(command);
				}
			}
		}
	}
}

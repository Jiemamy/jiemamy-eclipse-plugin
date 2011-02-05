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
package org.jiemamy.eclipse.core.ui.editor.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.AbstractJmNodeEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.ChangeNodeColorCommand;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.SimpleJmNode;
import org.jiemamy.model.geometory.JmColor;

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
		JiemamyContext context = (JiemamyContext) getViewer().getContents().getModel();
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
				DiagramFacet facet = context.getFacet(DiagramFacet.class);
				JmColor newColor = ConvertUtil.convert(rgb);
				CommandStack stack = getViewer().getEditDomain().getCommandStack();
				
				for (AbstractJmNodeEditPart editPart : editParts) {
					SimpleJmNode nodeModel = (SimpleJmNode) editPart.getModel();
					EntityRef<? extends SimpleJmNode> ref = nodeModel.toReference();
					SimpleJmDiagram diagramModel =
							(SimpleJmDiagram) facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
					Command command = new ChangeNodeColorCommand(facet, diagramModel, ref, newColor);
					
					stack.execute(command);
				}
			}
		}
	}
}

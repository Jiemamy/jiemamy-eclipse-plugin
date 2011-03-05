/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.view;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractDbObjectTreeEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.JmTreeComponentEditPolicy;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.EditDbObjectCommand;
import org.jiemamy.model.view.JmView;
import org.jiemamy.model.view.SimpleJmView;
import org.jiemamy.utils.LogMarker;

/**
 * {@link JmView}に対するTree用EditPart。
 * 
 * @version $Id$
 * @author daisuke
 */
public class JmViewTreeEditPart extends AbstractDbObjectTreeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(JmViewTreeEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param view コントロール対象のビュー
	 */
	public JmViewTreeEditPart(JmView view) {
		setModel(view);
	}
	
	@Override
	public JmView getModel() {
		return (JmView) super.getModel();
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext context = (JiemamyContext) getParent().getModel();
		SimpleJmView view = (SimpleJmView) getModel();
		
		Shell shell = getViewer().getControl().getShell();
		ViewEditDialog dialog = new ViewEditDialog(shell, context, view, null); // THINK nodeはnullでよいか？
		
		if (dialog.open() == Dialog.OK) {
			Command command = new EditDbObjectCommand(context, view, null, TODO.DIAGRAM_INDEX);
			GraphicalViewer viewer = (GraphicalViewer) getViewer();
			viewer.getEditDomain().getCommandStack().execute(command);
		}
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof JmView) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new JmTreeComponentEditPolicy());
	}
	
	@Override
	protected void refreshVisuals() {
		SimpleJmView model = (SimpleJmView) getModel();
		
		// ツリー・アイテムのテキストとしてモデルのテキストを設定
		setWidgetText(StringUtils.defaultString(model.getName()));
		
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		setWidgetImage(ir.get(Images.ICON_VIEW));
	}
}

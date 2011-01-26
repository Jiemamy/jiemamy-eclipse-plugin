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
package org.jiemamy.eclipse.core.ui.editor.editpart.diagram;

import org.apache.commons.lang.StringUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.command.EditDatabaseObjectCommand;
import org.jiemamy.eclipse.core.ui.editor.dialog.view.ViewEditDialog;
import org.jiemamy.eclipse.core.ui.editor.figure.ViewFigure;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.DatabaseObjectNodeModel;
import org.jiemamy.model.DefaultDatabaseObjectNodeModel;
import org.jiemamy.model.geometory.JmColor;
import org.jiemamy.model.view.DefaultViewModel;
import org.jiemamy.model.view.ViewModel;
import org.jiemamy.utils.LogMarker;

/**
 * {@link ViewModel}に対するDiagram用{@link EditPart}（コントローラ）。
 * 
 * @author daisuke
 */
public class ViewEditPart extends AbstractJmNodeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(ViewEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param nodeModel コントロール対象のノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ViewEditPart(DefaultDatabaseObjectNodeModel nodeModel) {
		super(nodeModel);
	}
	
	@Override
	public DefaultDatabaseObjectNodeModel getModel() {
		return (DefaultDatabaseObjectNodeModel) super.getModel();
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext context = (JiemamyContext) getParent().getModel();
		DefaultDatabaseObjectNodeModel nodeModel = getModel();
		DefaultViewModel viewModel = (DefaultViewModel) context.resolve(nodeModel.getCoreModelRef());
		
		Shell shell = getViewer().getControl().getShell();
		ViewEditDialog dialog = new ViewEditDialog(shell, context, viewModel, nodeModel);
		
		if (dialog.open() == Dialog.OK) {
			Command command = new EditDatabaseObjectCommand(context, viewModel, nodeModel, TODO.DIAGRAM_INDEX);
			GraphicalViewer viewer = (GraphicalViewer) getViewer();
			viewer.getEditDomain().getCommandStack().execute(command);
		}
	}
	
	@Override
	public void refresh() {
		logger.debug(LogMarker.LIFECYCLE, "refresh");
		super.refresh();
	}
	
	@Override
	protected IFigure createFigure() {
		logger.debug(LogMarker.LIFECYCLE, "createFigure");
		JiemamyContext context = (JiemamyContext) getParent().getModel();
		ViewFigure figure = new ViewFigure();
		DatabaseObjectNodeModel node = getModel();
		
		ViewModel viewModel = (ViewModel) context.resolve(node.getCoreModelRef());
		String description = viewModel.getDescription();
		
		if (StringUtils.isEmpty(description) == false) {
			Panel tooltip = new Panel();
			tooltip.setLayoutManager(new StackLayout());
			tooltip.setBackgroundColor(ColorConstants.tooltipBackground);
			tooltip.add(new Label(description));
			
			figure.setToolTip(tooltip);
		}
		
		updateFigure(figure);
		return figure;
	}
	
	@Override
	protected void updateFigure(IFigure figure) {
		logger.debug(LogMarker.LIFECYCLE, "updateFigure");
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		DatabaseObjectNodeModel node = getModel();
		ViewModel viewModel = (ViewModel) context.resolve(node.getCoreModelRef());
		ViewFigure viewFigure = (ViewFigure) figure;
		
		viewFigure.setDatabaseObjectName(viewModel.getName());
		
		JmColor color = node.getColor();
		viewFigure.setBgColor(ConvertUtil.convert(color));
		
		viewFigure.removeAllColumns();
		
		// TODO カラム部の表示
	}
}

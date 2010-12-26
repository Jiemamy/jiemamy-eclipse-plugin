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
package org.jiemamy.eclipse.editor.editpart.diagram;

import org.apache.commons.lang.StringUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.DisplayPlace;
import org.jiemamy.eclipse.editor.command.DialogEditCommand;
import org.jiemamy.eclipse.editor.dialog.view.ViewEditDialog;
import org.jiemamy.eclipse.editor.figure.ViewFigure;
import org.jiemamy.eclipse.editor.utils.LabelStringUtil;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.dbo.ViewModel;
import org.jiemamy.model.geometory.JmColor;
import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;

/**
 * {@link ViewModel}に対するDiagram用EditPart（コントローラ）。
 * 
 * @author daisuke
 */
public class ViewEditPart extends AbstractEntityNodeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(ViewEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param nodeAdapter コントロール対象のノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ViewEditPart(NodeModel nodeAdapter) {
		super(nodeAdapter);
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext rootModel = (JiemamyContext) getParent().getModel();
		NodeModel node = getModel();
		ViewModel viewModel = (ViewModel) node.unwrap();
		
		// 編集前のスナップショットを保存
		JiemamyViewFacade facade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
		SavePoint beforeEditSavePoint = facade.save();
		
		Shell shell = getViewer().getControl().getShell();
		ViewEditDialog dialog = new ViewEditDialog(shell, viewModel, Migration.DIAGRAM_INDEX, facade);
		
		if (dialog.open() == Window.OK) {
			// 編集後のスナップショットを保存
			SavePoint afterEditSavePoint = facade.save();
			
			Command command = new DialogEditCommand(facade, beforeEditSavePoint, afterEditSavePoint);
			GraphicalViewer viewer = (GraphicalViewer) getViewer();
			viewer.getEditDomain().getCommandStack().execute(command);
		} else {
			// 編集前にロールバック
			facade.rollback(beforeEditSavePoint);
		}
	}
	
	@Override
	protected IFigure createFigure() {
		ViewFigure figure = new ViewFigure();
		NodeModel node = getModel();
		ViewModel viewModel = (ViewModel) node.unwrap();
		String definition = viewModel.getDefinition();
		
		if (StringUtils.isEmpty(definition) == false) {
			Panel tooltip = new Panel();
			tooltip.setLayoutManager(new StackLayout());
			tooltip.setBackgroundColor(ColorConstants.tooltipBackground);
			tooltip.add(new Label(definition));
			
			figure.setToolTip(tooltip);
		}
		
		updateFigure(figure);
		return figure;
	}
	
	@Override
	protected void updateFigure(IFigure figure) {
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		NodeModel node = getModel();
		ViewModel viewModel = (ViewModel) node.unwrap();
		ViewFigure viewFigure = (ViewFigure) figure;
		
		String labelString = LabelStringUtil.getString(rootModel, viewModel, DisplayPlace.FIGURE);
		DiagramFacet diagramPresentations = rootModel.getAdapter(DiagramFacet.class);
		DiagramModel presentation = diagramPresentations.get(Migration.DIAGRAM_INDEX);
		NodeProfile nodeProfile = presentation.getNodeProfiles().get(node);
		
		viewFigure.setEntityName(labelString);
		
		if (nodeProfile == null) {
			viewFigure.setBgColor(null);
		} else {
			JmColor color = nodeProfile.getColor();
			viewFigure.setBgColor(ConvertUtil.convert(color));
		}
		
		viewFigure.removeAllColumns();
		
		// TODO カラム部の表示
	}
}

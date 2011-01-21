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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.dialog.sticky.StickyEditDialog;
import org.jiemamy.eclipse.core.ui.editor.figure.StickyFigure;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.DatabaseObjectModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.StickyNodeModel;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.utils.LogMarker;

/**
 * ビューモデルに対するDiagram用EditPart（コントローラ）。
 * 
 * @author daisuke
 */
public class StickyEditPart extends AbstractJmNodeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(StickyEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param stickyModel コントロール対象の付箋モデル
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public StickyEditPart(StickyNodeModel stickyModel) {
		super(stickyModel);
	}
	
	@Override
	public void commandExecuted(StoredEvent<DatabaseObjectModel> command) {
		refresh();
	}
	
	@Override
	public StickyNodeModel getModel() {
		return (StickyNodeModel) super.getModel();
	}
	
	public void openEditDialog() {
		JiemamyContext context = (JiemamyContext) getParent().getModel();
		StickyNodeModel stickyModel = getModel();
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog: {}", stickyModel);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		
		Shell shell = getViewer().getControl().getShell();
		StickyEditDialog dialog = new StickyEditDialog(shell, context, stickyModel, diagramModel);
		
		if (dialog.open() == Dialog.OK) {
//			Command command = new DialogEditCommand(facade, beforeEditSavePoint, afterEditSavePoint);
//			GraphicalViewer viewer = (GraphicalViewer) getViewer();
//			viewer.getEditDomain().getCommandStack().execute(command);
			diagramModel.store(stickyModel);
			facet.store(diagramModel);
		}
	}
	
//	@Override
//	protected DirectEditManager getDirectEditManager() {
//		StickyFigure figure = (StickyFigure) getFigure();
//		CellEditorLocator locator = new NodeCellEditorLocator(figure.getContentsLabel());
//		return new StickyDirectEditManager(this, MultiLineTextCellEditor.class, locator);
//	}
	
	@Override
	protected IFigure createFigure() {
		StickyFigure figure = new StickyFigure();
		String contents = getModel().getContents();
		
		if (contents.length() > 0) {
			Panel tooltip = new Panel();
			tooltip.setLayoutManager(new StackLayout());
			tooltip.setBackgroundColor(ColorConstants.tooltipBackground);
			tooltip.add(new Label(contents));
			
			figure.setToolTip(tooltip);
		}
		
		updateFigure(figure);
		return figure;
	}
	
	/**
	 * {@link StickyFigure}のアップデートを行う。
	 * 
	 * @param figure アップデート対象のフィギュア
	 */
	@Override
	protected void updateFigure(IFigure figure) {
		logger.debug(LogMarker.LIFECYCLE, "updateFigure");
		
		StickyNodeModel stickyModel = getModel();
		StickyFigure stickyFigure = (StickyFigure) figure;
		
		stickyFigure.setContents(stickyModel.getContents());
		stickyFigure.setBgColor(ConvertUtil.convert(stickyModel.getColor()));
	}
}

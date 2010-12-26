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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.command.DialogEditCommand;
import org.jiemamy.eclipse.editor.dialog.sticky.StickyEditDialog;
import org.jiemamy.eclipse.editor.figure.StickyFigure;
import org.jiemamy.eclipse.editor.tools.MultiLineTextCellEditor;
import org.jiemamy.eclipse.editor.tools.NodeCellEditorLocator;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.geometory.JmColor;
import org.jiemamy.model.geometory.JmRectangle;
import org.jiemamy.transaction.SavePoint;
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
	public StickyEditPart(StickyModel stickyModel) {
		super(stickyModel);
	}
	
	public JiemamyElement getTargetModel() {
		StickyModel stickyModel = (StickyModel) getModel();
		return stickyModel;
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext rootModel = (JiemamyContext) getParent().getModel();
		StickyModel stickyModel = (StickyModel) getModel();
		
		// 編集前のスナップショットを保存
		JiemamyViewFacade facade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
		SavePoint beforeEditSavePoint = facade.save();
		
		Shell shell = getViewer().getControl().getShell();
		StickyEditDialog dialog = new StickyEditDialog(shell, stickyModel, Migration.DIAGRAM_INDEX, facade);
		
		if (dialog.open() == Dialog.OK) {
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
		StickyFigure figure = new StickyFigure();
		String contents = ((StickyModel) getModel()).getContents();
		
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
	
	@Override
	protected DirectEditManager getDirectEditManager() {
		StickyFigure figure = (StickyFigure) getFigure();
		CellEditorLocator locator = new NodeCellEditorLocator(figure.getContentsLabel());
		return new StickyDirectEditManager(this, MultiLineTextCellEditor.class, locator);
	}
	
	@Override
	protected void refreshVisuals() {
		GraphicalEditPart editPart = (GraphicalEditPart) getParent();
		if (editPart == null) {
			// モデルが削除された場合にeditPart=nullとなる。その時は描画処理は行わない。
			return;
		}
		JiemamyContext rootModel = (JiemamyContext) editPart.getModel();
		StickyModel stickyModel = (StickyModel) getModel();
		
		DiagramFacet diagramPresentations = rootModel.getAdapter(DiagramFacet.class);
		DiagramModel presentation = diagramPresentations.get(Migration.DIAGRAM_INDEX);
		NodeProfile nodeProfile = presentation.getNodeProfiles().get(stickyModel);
		if (nodeProfile == null) {
			// 表示しない
		} else {
			JmRectangle boundary = nodeProfile.getBoundary();
			editPart.setLayoutConstraint(this, getFigure(), ConvertUtil.convert(boundary));
		}
		
		updateFigure(getFigure());
	}
	
	/**
	 * StickyFigureのアップデートを行う。
	 * 
	 * @param figure アップデート対象のフィギュア
	 */
	@Override
	protected void updateFigure(IFigure figure) {
		JiemamyContext rootModel = (JiemamyContext) getParent().getModel();
		StickyModel stickyModel = (StickyModel) getModel();
		StickyFigure stickyFigure = (StickyFigure) figure;
		
		DiagramFacet diagramPresentations = rootModel.getAdapter(DiagramFacet.class);
		DiagramModel presentation = diagramPresentations.get(Migration.DIAGRAM_INDEX);
		NodeProfile nodeProfile = presentation.getNodeProfiles().get(stickyModel);
		if (nodeProfile == null) {
			return;
		}
		JmColor color = presentation.getNodeProfiles().get(stickyModel).getColor();
		stickyFigure.setContents(stickyModel.getContents());
		stickyFigure.setBgColor(ConvertUtil.convert(color));
	}
}

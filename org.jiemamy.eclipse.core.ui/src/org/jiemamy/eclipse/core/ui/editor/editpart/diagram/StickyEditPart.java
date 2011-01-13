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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.Entity;
import org.jiemamy.eclipse.core.ui.editor.figure.StickyFigure;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
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
	public void commandExecuted(StoredEvent<?> command) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public StickyNodeModel getModel() {
		return (StickyNodeModel) super.getModel();
	}
	
	@Override
	public Entity getTargetModel() {
		StickyNodeModel stickyModel = getModel();
		return stickyModel;
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext rootModel = (JiemamyContext) getParent().getModel();
		StickyNodeModel stickyModel = getModel();
		
//		// 編集前のスナップショットを保存
//		JiemamyViewFacade facade = rootModel.newFacade(JiemamyViewFacade.class);
//		SavePoint beforeEditSavePoint = facade.save();
//		
//		Shell shell = getViewer().getControl().getShell();
//		StickyEditDialog dialog = new StickyEditDialog(shell, stickyModel, Migration.DIAGRAM_INDEX, facade);
//		
//		if (dialog.open() == Dialog.OK) {
//			// 編集後のスナップショットを保存
//			SavePoint afterEditSavePoint = facade.save();
//			
//			Command command = new DialogEditCommand(facade, beforeEditSavePoint, afterEditSavePoint);
//			GraphicalViewer viewer = (GraphicalViewer) getViewer();
//			viewer.getEditDomain().getCommandStack().execute(command);
//		} else {
//			// 編集前にロールバック
//			facade.rollback(beforeEditSavePoint);
//		}
	}
	
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
	
//	@Override
//	protected DirectEditManager getDirectEditManager() {
//		StickyFigure figure = (StickyFigure) getFigure();
//		CellEditorLocator locator = new NodeCellEditorLocator(figure.getContentsLabel());
//		return new StickyDirectEditManager(this, MultiLineTextCellEditor.class, locator);
//	}
	
	/**
	 * StickyFigureのアップデートを行う。
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

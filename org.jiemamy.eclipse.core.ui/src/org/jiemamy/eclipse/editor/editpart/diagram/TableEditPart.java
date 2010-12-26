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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.DisplayPlace;
import org.jiemamy.eclipse.editor.command.DialogEditCommand;
import org.jiemamy.eclipse.editor.dialog.table.TableEditDialog;
import org.jiemamy.eclipse.editor.figure.ColumnFigure;
import org.jiemamy.eclipse.editor.figure.TableFigure;
import org.jiemamy.eclipse.editor.utils.LabelStringUtil;
import org.jiemamy.eclipse.ui.JiemamyEditDialog;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.Level;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.model.geometory.JmColor;
import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;

/**
 * テーブルモデルに対するDiagram用EditPart（コントローラ）。
 * @author daisuke
 */
public class TableEditPart extends AbstractEntityNodeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param nodeAdapter コントロール対象のノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public TableEditPart(NodeModel nodeAdapter) {
		super(nodeAdapter);
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext rootModel = (JiemamyContext) getParent().getModel();
		NodeModel node = getModel();
		TableModel tableModel = (TableModel) node.unwrap();
		
		// 編集前のスナップショットを保存
		JiemamyViewFacade facade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
		SavePoint beforeEditSavePoint = facade.save();
		
		Shell shell = getViewer().getControl().getShell();
		JiemamyEditDialog<TableModel> dialog = new TableEditDialog(shell, tableModel, Migration.DIAGRAM_INDEX, facade);
		
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
	public void refresh() {
		logger.debug(LogMarker.LIFECYCLE, "refresh");
		super.refresh();
	}
	
	@Override
	protected IFigure createFigure() {
		logger.debug(LogMarker.LIFECYCLE, "createFigure");
		TableFigure figure = new TableFigure();
		NodeModel node = getModel();
		String description = ((TableModel) node.unwrap()).getDescription();
		
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
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		NodeModel node = getModel();
		TableModel tableModel = (TableModel) node.unwrap();
		TableFigure tableFigure = (TableFigure) figure;
		
		String labelString = LabelStringUtil.getString(rootModel, tableModel, DisplayPlace.FIGURE);
		
		DiagramFacet diagramPresentations = rootModel.getAdapter(DiagramFacet.class);
		DiagramModel presentation = diagramPresentations.get(Migration.DIAGRAM_INDEX);
		NodeProfile nodeProfile = presentation.getNodeProfiles().get(node);
		
		tableFigure.setTableName(labelString);
		
		if (nodeProfile == null) {
			tableFigure.setBgColor(null);
		} else {
			JmColor color = nodeProfile.getColor();
			tableFigure.setBgColor(ConvertUtil.convert(color));
		}
		
		tableFigure.removeAllColumns();
		
		for (ColumnModel columnModel : tableModel.findColumns()) {
			ColumnFigure[] columnFigure = createColumnFigure(columnModel);
			tableFigure.add(columnFigure[0], columnFigure[1]);
		}
	}
	
	private ColumnFigure[] createColumnFigure(ColumnModel columnModel) {
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		
		DiagramFacet diagramPresentations = rootModel.getAdapter(DiagramFacet.class);
		DiagramModel presentation = diagramPresentations.get(Migration.DIAGRAM_INDEX);
		
		if (presentation.getLevel() == Level.ENTITY) {
			return new ColumnFigure[0];
		}
		
		ColumnFigure nameLabel = new ColumnFigure();
		ColumnFigure typeLabel = new ColumnFigure();
		
		nameLabel.setText(LabelStringUtil.getString(rootModel, columnModel, DisplayPlace.FIGURE));
		typeLabel.setText(LabelStringUtil.getString(rootModel, columnModel.getDataType(), DisplayPlace.FIGURE));
		
		if (columnModel.checkPrimaryKeyColumn()) {
			nameLabel.setUnderline(true);
			typeLabel.setUnderline(true);
		}
		
		return new ColumnFigure[] {
			nameLabel,
			typeLabel
		};
	}
}

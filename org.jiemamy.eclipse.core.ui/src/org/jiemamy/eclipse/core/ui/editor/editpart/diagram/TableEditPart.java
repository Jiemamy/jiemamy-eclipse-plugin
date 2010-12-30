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
package org.jiemamy.eclipse.core.ui.editor.editpart.diagram;

import org.apache.commons.lang.StringUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.gef.EditPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.DisplayPlace;
import org.jiemamy.eclipse.core.ui.editor.figure.ColumnFigure;
import org.jiemamy.eclipse.core.ui.editor.figure.TableFigure;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.eclipse.core.ui.utils.LabelStringUtil;
import org.jiemamy.model.DefaultNodeModel;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.Level;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.model.geometory.JmColor;
import org.jiemamy.utils.LogMarker;

/**
 * {@link TableModel}に対するDiagram用{@link EditPart}（コントローラ）。
 * 
 * @author daisuke
 */
public class TableEditPart extends AbstractJmNodeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditPart.class);
	

//	@Override
//	protected DirectEditManager createDirectEditManager() {
//		EntityFigure figure = (EntityFigure) getFigure();
//		CellEditorLocator locator = new NodeCellEditorLocator(figure.getEntityNameLabel());
//		return new EntityDirectEditManager(this, TextCellEditor.class, locator);
//	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param nodeModel コントロール対象のノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public TableEditPart(DefaultNodeModel nodeModel) {
		super(nodeModel);
	}
	
	@Override
	public void commandExecuted(org.jiemamy.transaction.Command command) {
		refresh();
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext context = (JiemamyContext) getParent().getModel();
		NodeModel node = getModel();
		TableModel tableModel = (TableModel) context.resolve(node.getCoreModelRef());
		
//		// 編集前のスナップショットを保存
//		JiemamyViewFacade facade = context.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
//		SavePoint beforeEditSavePoint = facade.save();
//		
//		Shell shell = getViewer().getControl().getShell();
//		JiemamyEditDialog<TableModel> dialog = new TableEditDialog(shell, tableModel, Migration.DIAGRAM_INDEX, facade);
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
	public void refresh() {
		logger.debug(LogMarker.LIFECYCLE, "refresh");
		super.refresh();
	}
	
	@Override
	protected IFigure createFigure() {
		logger.debug(LogMarker.LIFECYCLE, "createFigure");
		JiemamyContext context = (JiemamyContext) getParent().getModel();
		TableFigure figure = new TableFigure();
		NodeModel node = getModel();
		
		TableModel tableModel = (TableModel) context.resolve(node.getCoreModelRef());
		String description = tableModel.getDescription();
		
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
		NodeModel node = getModel();
		TableModel tableModel = (TableModel) context.resolve(node.getCoreModelRef());
		TableFigure tableFigure = (TableFigure) figure;
		
		String labelString = LabelStringUtil.getString(context, tableModel, DisplayPlace.FIGURE);
		DiagramFacet diagramPresentations = context.getFacet(DiagramFacet.class);
		
		tableFigure.setDatabaseObjectName(labelString);
		
		JmColor color = node.getColor();
		tableFigure.setBgColor(ConvertUtil.convert(color));
		
		tableFigure.removeAllColumns();
		
		for (ColumnModel columnModel : tableModel.getColumns()) {
			ColumnFigure[] columnFigure = createColumnFigure(columnModel);
			tableFigure.add(columnFigure[0], columnFigure[1]);
		}
	}
	
	private ColumnFigure[] createColumnFigure(ColumnModel columnModel) {
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		
		DiagramFacet diagramPresentations = context.getFacet(DiagramFacet.class);
		DiagramModel presentation = diagramPresentations.getDiagrams().get(TODO.DIAGRAM_INDEX);
		
		if (presentation.getLevel() == Level.ENTITY) {
			return new ColumnFigure[0];
		}
		
		ColumnFigure nameLabel = new ColumnFigure();
		ColumnFigure typeLabel = new ColumnFigure();
		
		nameLabel.setText(LabelStringUtil.getString(context, columnModel, DisplayPlace.FIGURE));
		typeLabel.setText(LabelStringUtil.getString(context, columnModel.getDataType(), DisplayPlace.FIGURE));
		
		NodeModel node = getModel();
		TableModel tableModel = (TableModel) context.resolve(node.getCoreModelRef());
		
		if (tableModel.getPrimaryKey().getKeyColumns().contains(columnModel.toReference())) {
			nameLabel.setUnderline(true);
			typeLabel.setUnderline(true);
		}
		
		return new ColumnFigure[] {
			nameLabel,
			typeLabel
		};
	}
}
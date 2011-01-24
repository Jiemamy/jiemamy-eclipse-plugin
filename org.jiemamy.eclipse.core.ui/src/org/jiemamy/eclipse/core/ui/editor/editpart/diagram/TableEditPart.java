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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.DisplayPlace;
import org.jiemamy.eclipse.core.ui.editor.dialog.table.TableEditDialog;
import org.jiemamy.eclipse.core.ui.editor.figure.ColumnFigure;
import org.jiemamy.eclipse.core.ui.editor.figure.TableFigure;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.eclipse.core.ui.utils.LabelStringUtil;
import org.jiemamy.model.DatabaseObjectNodeModel;
import org.jiemamy.model.DefaultDatabaseObjectNodeModel;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.Level;
import org.jiemamy.model.column.ColumnModel;
import org.jiemamy.model.geometory.JmColor;
import org.jiemamy.model.table.DefaultTableModel;
import org.jiemamy.model.table.TableModel;
import org.jiemamy.utils.LogMarker;

/**
 * {@link TableModel}に対するDiagram用{@link EditPart}（コントローラ）。
 * 
 * @author daisuke
 */
public class TableEditPart extends AbstractJmNodeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param nodeModel コントロール対象のノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public TableEditPart(DefaultDatabaseObjectNodeModel nodeModel) {
		super(nodeModel);
	}
	
//	@Override
//	protected DirectEditManager createDirectEditManager() {
//		EntityFigure figure = (EntityFigure) getFigure();
//		CellEditorLocator locator = new NodeCellEditorLocator(figure.getEntityNameLabel());
//		return new EntityDirectEditManager(this, TextCellEditor.class, locator);
//	}
	
	@Override
	public DatabaseObjectNodeModel getModel() {
		return (DatabaseObjectNodeModel) super.getModel();
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext context = (JiemamyContext) getParent().getModel();
		DatabaseObjectNodeModel node = getModel();
		DefaultTableModel tableModel = (DefaultTableModel) context.resolve(node.getCoreModelRef());
		
//		// 編集前のスナップショットを保存
//		JiemamyViewFacade facade = context.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
//		SavePoint beforeEditSavePoint = facade.save();
		
		Shell shell = getViewer().getControl().getShell();
		TableEditDialog dialog = new TableEditDialog(shell, context, tableModel, TODO.DIAGRAM_INDEX);
		
		if (dialog.open() == Dialog.OK) {
//			// 編集後のスナップショットを保存
//			SavePoint afterEditSavePoint = facade.save();
//			
//			Command command = new DialogEditCommand(facade, beforeEditSavePoint, afterEditSavePoint);
//			GraphicalViewer viewer = (GraphicalViewer) getViewer();
//			viewer.getEditDomain().getCommandStack().execute(command);
			context.store(tableModel);
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
		TableFigure figure = new TableFigure();
		DatabaseObjectNodeModel node = getModel();
		
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
		
		DatabaseObjectNodeModel node = getModel();
		DefaultTableModel tableModel = (DefaultTableModel) context.resolve(node.getCoreModelRef());
		TableFigure tableFigure = (TableFigure) figure;
		
		tableFigure.setDatabaseObjectName(tableModel.getName());
		
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
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DiagramModel diagramModel = facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		DefaultTableModel tableModel = (DefaultTableModel) context.resolve(getModel().getCoreModelRef());
		
		if (diagramModel.getLevel() == Level.ENTITY) {
			return new ColumnFigure[0];
		}
		
		ColumnFigure nameLabel = new ColumnFigure();
		ColumnFigure typeLabel = new ColumnFigure();
		
		nameLabel.setText(columnModel.getName());
		try {
			typeLabel.setText(LabelStringUtil.toString(context.findDialect(), columnModel.getDataType(),
					DisplayPlace.FIGURE));
		} catch (ClassNotFoundException e) {
			logger.error("lost dialect", e);
			typeLabel.setText(columnModel.getDataType().getTypeReference().getTypeName());
		}
		
		if (tableModel.isPrimaryKeyColumn(columnModel.toReference())) {
			nameLabel.setUnderline(true);
			typeLabel.setUnderline(true);
		}
		
		return new ColumnFigure[] {
			nameLabel,
			typeLabel
		};
	}
}

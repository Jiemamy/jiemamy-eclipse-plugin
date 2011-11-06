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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table;

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

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.AbstractJmNodeEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.EditDbObjectCommand;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column.ColumnFigure;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.eclipse.core.ui.utils.LabelStringUtil;
import org.jiemamy.model.DbObjectNode;
import org.jiemamy.model.JmDiagram;
import org.jiemamy.model.Level;
import org.jiemamy.model.SimpleDbObjectNode;
import org.jiemamy.model.column.JmColumn;
import org.jiemamy.model.geometory.JmColor;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.model.table.SimpleJmTable;
import org.jiemamy.utils.LogMarker;

/**
 * {@link JmTable}に対するDiagram用{@link EditPart}（コントローラ）。
 * 
 * @author daisuke
 */
public class TableEditPart extends AbstractJmNodeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditPart.class);
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param node コントロール対象のノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public TableEditPart(SimpleDbObjectNode node) {
		super(node);
	}
	
//	@Override
//	protected DirectEditManager createDirectEditManager() {
//		DbObjectFigure figure = (DbObjectFigure) getFigure();
//		CellEditorLocator locator = new NodeCellEditorLocator(figure.getDbObjectNameLabel());
//		return new DbObjectDirectEditManager(this, TextCellEditor.class, locator);
//	}
	
	@Override
	public SimpleDbObjectNode getModel() {
		return (SimpleDbObjectNode) super.getModel();
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext context = (JiemamyContext) getParent().getModel();
		SimpleDbObjectNode node = getModel();
		SimpleJmTable table = (SimpleJmTable) context.resolve(node.getCoreModelRef());
		
		Shell shell = getViewer().getControl().getShell();
		TableEditDialog dialog = new TableEditDialog(shell, context, table, node);
		
		if (dialog.open() == Dialog.OK) {
			Command command = new EditDbObjectCommand(context, table, node, TODO.DIAGRAM_INDEX);
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
		TableFigure figure = new TableFigure();
		DbObjectNode node = getModel();
		
		JmTable table = (JmTable) context.resolve(node.getCoreModelRef());
		String description = table.getDescription();
		
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
		
		DbObjectNode node = getModel();
		SimpleJmTable table = (SimpleJmTable) context.resolve(node.getCoreModelRef());
		TableFigure tableFigure = (TableFigure) figure;
		
		tableFigure.setDbObjectName(table.getName());
		
		JmColor color = node.getColor();
		tableFigure.setBgColor(ConvertUtil.convert(color));
		
		tableFigure.removeAllColumns();
		
		for (JmColumn column : table.getColumns()) {
			ColumnFigure[] columnFigure = createColumnFigure(column);
			tableFigure.add(columnFigure[0], columnFigure[1]);
		}
	}
	
	private ColumnFigure[] createColumnFigure(JmColumn column) {
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		JmDiagram diagram = facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		SimpleJmTable table = (SimpleJmTable) context.resolve(getModel().getCoreModelRef());
		
		if (diagram.getLevel() == Level.ENTITY) {
			return new ColumnFigure[0];
		}
		
		ColumnFigure nameLabel = new ColumnFigure();
		ColumnFigure typeLabel = new ColumnFigure();
		
		nameLabel.setText(column.getName());
		try {
			typeLabel.setText(LabelStringUtil.toString(context.findDialect(), column.getDataType()));
		} catch (ClassNotFoundException e) {
			logger.error("lost dialect", e);
			typeLabel.setText(column.getDataType().getRawTypeDescriptor().getTypeName());
		}
		
		if (table.isPrimaryKeyColumn(column.toReference())) {
			nameLabel.setUnderline(true);
			typeLabel.setUnderline(true);
		}
		
		return new ColumnFigure[] {
			nameLabel,
			typeLabel
		};
	}
}

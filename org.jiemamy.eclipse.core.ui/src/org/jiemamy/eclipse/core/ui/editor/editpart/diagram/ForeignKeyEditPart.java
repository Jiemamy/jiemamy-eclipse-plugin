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

import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.Entity;
import org.jiemamy.eclipse.core.ui.editor.DisplayPlace;
import org.jiemamy.eclipse.core.ui.editor.dialog.foreignkey.ForeignKeyEditDialog;
import org.jiemamy.eclipse.core.ui.editor.editpart.EditDialogSupport;
import org.jiemamy.eclipse.core.ui.utils.LabelStringUtil;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.constraint.DefaultForeignKeyConstraintModel;
import org.jiemamy.model.constraint.ForeignKeyConstraintModel;
import org.jiemamy.model.table.DefaultTableModel;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.utils.LogMarker;

/**
 * 外部キーモデルに対するDiagram用EditPart（コントローラ）。
 * 
 * @author daisuke
 */
public class ForeignKeyEditPart extends AbstractJmConnectionEditPart implements EditDialogSupport {
	
	private static Logger logger = LoggerFactory.getLogger(ForeignKeyEditPart.class);
	
	private Label label;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param connectionAdapter コントロール対象のコネクション
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ForeignKeyEditPart(ConnectionModel connectionAdapter) {
		super(connectionAdapter);
		logger.debug(LogMarker.LIFECYCLE, "construct");
	}
	
	public void commandExecuted(StoredEvent<ForeignKeyConstraintModel> command) {
		// TODO Auto-generated method stub
		
	}
	
	public Entity getTargetModel() {
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		ConnectionModel connection = getModel();
		ForeignKeyConstraintModel foreignKey = context.resolve(connection.getCoreModelRef());
		return foreignKey;
	}
	
	public void openEditDialog() {
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		ConnectionModel connection = getModel();
		DefaultForeignKeyConstraintModel foreignKey =
				(DefaultForeignKeyConstraintModel) context.resolve(connection.getCoreModelRef());
		
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog: {}", foreignKey);
		
		ForeignKeyEditDialog dialog =
				new ForeignKeyEditDialog(getViewer().getControl().getShell(), context, foreignKey);
		
		if (dialog.open() == Dialog.OK) {
			DefaultTableModel tableModel =
					(DefaultTableModel) DefaultTableModel.findDeclaringTable(context.getTables(), foreignKey);
			tableModel.store(foreignKey);
			context.store(tableModel);
//			Command command = new DialogEditCommand(facade, beforeEditSavePoint, afterEditSavePoint);
//			GraphicalViewer viewer = (GraphicalViewer) getViewer();
//			viewer.getEditDomain().getCommandStack().execute(command);
		}
	}
	
	@Override
	public void performRequest(Request req) {
		logger.info(LogMarker.LIFECYCLE, "Incoming GEF Request: " + req.getType());
		if (req.getType().equals(RequestConstants.REQ_OPEN)) {
			openEditDialog();
			return;
		}
		super.performRequest(req);
	}
	
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();
		updateLabel();
	}
	
	@Override
	protected IFigure createFigure() {
		PolylineConnection connection = new PolylineConnection();
		connection.setTargetDecoration(new PolylineDecoration());
		
		label = new Label();
		label.setLabelAlignment(PositionConstants.CENTER);
		label.setOpaque(true);
		label.setBackgroundColor(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		updateLabel();
		connection.add(label, new ConnectionLocator(connection, ConnectionLocator.MIDDLE));
		
		return connection;
	}
	
	/**
	 * ラベルを更新する。
	 */
	private void updateLabel() {
		if (getParent() == null) {
			return;
		}
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		ConnectionModel connection = getModel();
		ForeignKeyConstraintModel foreignKey = context.resolve(connection.getCoreModelRef());
		
		String labelString = LabelStringUtil.getString(context, foreignKey, DisplayPlace.FIGURE);
		label.setText(labelString);
	}
}

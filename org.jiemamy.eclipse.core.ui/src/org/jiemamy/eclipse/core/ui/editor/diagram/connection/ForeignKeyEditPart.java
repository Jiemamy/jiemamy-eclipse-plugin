/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.connection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityNotFoundException;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.core.ui.editor.diagram.EditDialogSupport;
import org.jiemamy.model.JmConnection;
import org.jiemamy.model.ModelConsistencyException;
import org.jiemamy.model.column.JmColumn;
import org.jiemamy.model.constraint.JmForeignKeyConstraint;
import org.jiemamy.model.constraint.SimpleJmForeignKeyConstraint;
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
	public ForeignKeyEditPart(JmConnection connectionAdapter) {
		super(connectionAdapter);
		logger.debug(LogMarker.LIFECYCLE, "construct");
	}
	
	public void openEditDialog() {
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		JmConnection connection = getModel();
		SimpleJmForeignKeyConstraint foreignKey =
				(SimpleJmForeignKeyConstraint) context.resolve(connection.getCoreModelRef());
		
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog: {}", foreignKey);
		
		Shell shell = getViewer().getControl().getShell();
		try {
			ForeignKeyEditDialog dialog = new ForeignKeyEditDialog(shell, context, foreignKey);
			
			if (dialog.open() == Dialog.OK) {
				Command command = new EditForeignKeyCommand(context, foreignKey);
				GraphicalViewer viewer = (GraphicalViewer) getViewer();
				viewer.getEditDomain().getCommandStack().execute(command);
			}
		} catch (ModelConsistencyException e) {
			MessageDialog.openError(shell, "ModelConsistencyException", e.getMessage());
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
	
	private String toString(JiemamyContext context, JmForeignKeyConstraint foreignKey) {
		StringBuilder sb = new StringBuilder();
		
		if (StringUtils.isEmpty(foreignKey.getName()) == false) {
			sb.append(foreignKey.getName()).append("\n");
		}
		
		int size = Math.max(foreignKey.getReferenceColumns().size(), foreignKey.getKeyColumns().size());
		for (int i = 0; i < size; i++) {
			if (i != 0) {
				sb.append("\n");
			}
			if (foreignKey.getKeyColumns().size() > i) {
				EntityRef<? extends JmColumn> keyColumnRef = foreignKey.getKeyColumns().get(i);
				try {
					JmColumn keyColumn = context.resolve(keyColumnRef);
					sb.append(keyColumn.getName());
				} catch (EntityNotFoundException e) {
					sb.append("UNKNOWN");
				}
			} else {
				sb.append("UNKNOWN");
			}
			sb.append(" -> ");
			if (foreignKey.getReferenceColumns().size() > i) {
				EntityRef<? extends JmColumn> referenceColumnRef = foreignKey.getReferenceColumns().get(i);
				try {
					JmColumn referenceColumn = context.resolve(referenceColumnRef);
					sb.append(referenceColumn.getName());
				} catch (EntityNotFoundException e) {
					sb.append("UNKNOWN");
				}
			} else {
				sb.append("UNKNOWN");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * ラベルを更新する。
	 */
	private void updateLabel() {
		if (getParent() == null) {
			return;
		}
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		JmConnection connection = getModel();
		JmForeignKeyConstraint foreignKey = context.resolve(connection.getCoreModelRef());
		
		String labelString = toString(context, foreignKey);
		label.setText(labelString);
	}
}

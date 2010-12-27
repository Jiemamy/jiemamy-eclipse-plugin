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
package org.jiemamy.eclipse.editor.editpart.outlinetree;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.JiemamyEntity;
import org.jiemamy.eclipse.Images;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.Migration;
import org.jiemamy.eclipse.editor.DisplayPlace;
import org.jiemamy.eclipse.editor.command.DialogEditCommand;
import org.jiemamy.eclipse.editor.dialog.table.TableEditDialog;
import org.jiemamy.eclipse.editor.editpolicy.JmTreeComponentEditPolicy;
import org.jiemamy.eclipse.editor.utils.LabelStringUtil;
import org.jiemamy.eclipse.ui.JiemamyEditDialog;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;

/**
 * {@link TableModel}に対するTree用EditPart（コントローラ）。
 * @author daisuke
 */
public class TableTreeEditPart extends AbstractEntityTreeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(TableTreeEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param tableModel コントロール対象のテーブル
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public TableTreeEditPart(TableModel tableModel) {
		Validate.notNull(tableModel);
		setModel(tableModel);
	}
	
	public void commandExecuted(org.jiemamy.transaction.Command arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public TableModel getModel() {
		return (TableModel) super.getModel();
	}
	
	public JiemamyEntity getTargetModel() {
		TableModel model = getModel();
		return model;
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext rootModel = (JiemamyContext) getParent().getModel();
		TableModel tableModel = getModel();
		
		// 編集前のスナップショットを保存
		JiemamyViewFacade facade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
		SavePoint beforeEditSavePoint = facade.save();
		
		Shell shell = getViewer().getControl().getShell();
		JiemamyEditDialog<TableModel> dialog = new TableEditDialog(shell, tableModel, Migration.DIAGRAM_INDEX, facade);
		
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
	public void setModel(Object model) {
		if (model instanceof TableModel) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new JmTreeComponentEditPolicy());
	}
	
	@Override
	protected List<ColumnModel> getModelChildren() {
		// ここで返された子モデルがツリーの子アイテムになる
		TableModel tableModel = getModel();
		return tableModel.getColumns();
	}
	
	@Override
	protected void refreshVisuals() {
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		TableModel model = getModel();
		// ツリー・アイテムのテキストとしてモデルのテキストを設定
		setWidgetText(LabelStringUtil.getString(rootModel, model, DisplayPlace.OUTLINE_TREE));
		
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		setWidgetImage(ir.get(Images.ICON_TABLE));
	}
}

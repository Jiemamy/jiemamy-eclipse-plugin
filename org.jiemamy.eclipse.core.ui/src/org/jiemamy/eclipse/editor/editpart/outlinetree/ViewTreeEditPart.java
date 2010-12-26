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
import org.jiemamy.eclipse.editor.DisplayPlace;
import org.jiemamy.eclipse.editor.command.DialogEditCommand;
import org.jiemamy.eclipse.editor.dialog.view.ViewEditDialog;
import org.jiemamy.eclipse.editor.editpolicy.JmTreeComponentEditPolicy;
import org.jiemamy.eclipse.editor.utils.LabelStringUtil;
import org.jiemamy.model.dbo.ViewModel;
import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;

/**
 * ViewModelに対するTree用EditPart
 * 
 * @author daisuke
 */
public class ViewTreeEditPart extends AbstractEntityTreeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(ViewTreeEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param viewModel コントロール対象のビュー
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ViewTreeEditPart(ViewModel viewModel) {
		Validate.notNull(viewModel);
		setModel(viewModel);
	}
	
	@Override
	public ViewModel getModel() {
		return (ViewModel) super.getModel();
	}
	
	public JiemamyEntity getTargetModel() {
		ViewModel model = getModel();
		return model;
	}
	
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		
		JiemamyContext rootModel = (JiemamyContext) getParent().getModel();
		ViewModel viewModel = getModel();
		
		// 編集前のスナップショットを保存
		JiemamyViewFacade facade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
		SavePoint beforeEditSavePoint = facade.save();
		
		Shell shell = getViewer().getControl().getShell();
		ViewEditDialog dialog = new ViewEditDialog(shell, viewModel, Migration.DIAGRAM_INDEX, facade);
		
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
		if (model instanceof ViewModel) {
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
	protected void refreshVisuals() {
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		ViewModel model = getModel();
		// ツリー・アイテムのテキストとしてモデルのテキストを設定
		setWidgetText(LabelStringUtil.getString(rootModel, model, DisplayPlace.OUTLINE_TREE));
		
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		setWidgetImage(ir.get(Images.ICON_VIEW));
	}
	
	public void commandExecuted(org.jiemamy.transaction.Command arg0) {
		// TODO Auto-generated method stub
		
	}
}

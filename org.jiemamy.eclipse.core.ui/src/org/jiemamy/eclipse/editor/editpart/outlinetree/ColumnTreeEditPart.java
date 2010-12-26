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
import org.eclipse.jface.resource.ImageRegistry;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.Images;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.editor.DisplayPlace;
import org.jiemamy.eclipse.editor.editpolicy.JmTreeComponentEditPolicy;
import org.jiemamy.eclipse.editor.utils.LabelStringUtil;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.transaction.Command;

/**
 * DefinitionModelに対するTree用EditPart
 * @author daisuke
 */
public class ColumnTreeEditPart extends AbstractModelTreeEditPart {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param attributeModel コントロール対象の属性
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ColumnTreeEditPart(ColumnModel attributeModel) {
		Validate.notNull(attributeModel);
		setModel(attributeModel);
	}
	
	public void commandExecuted(Command arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ColumnModel getModel() {
		return (ColumnModel) super.getModel();
	}
	
	public ColumnModel getTargetModel() {
		ColumnModel model = getModel();
		return model;
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof ColumnModel) {
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
		ColumnModel model = getModel();
		// ツリー・アイテムのテキストとしてモデルのテキストを設定
		setWidgetText(LabelStringUtil.getString(rootModel, model, DisplayPlace.OUTLINE_TREE));
		
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		setWidgetImage(ir.get(Images.ICON_COLUMN));
	}
}

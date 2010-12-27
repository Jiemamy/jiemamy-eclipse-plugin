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
import org.eclipse.jface.resource.ImageRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyEntity;
import org.jiemamy.eclipse.Images;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.editor.ModelContainer;
import org.jiemamy.transaction.Command;

/**
 * {@link ModelContainer}に対するTree用EditPart（コントローラ）。
 * @author daisuke
 */
public class ModelContainerEditPart extends AbstractModelTreeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(ModelContainerEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param modelContainer コントローラが管理するモデル
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ModelContainerEditPart(ModelContainer modelContainer) {
		Validate.notNull(modelContainer);
		setModel(modelContainer);
	}
	
	@Override
	public ModelContainer getModel() {
		return (ModelContainer) super.getModel();
	}
	
	public JiemamyEntity getTargetModel() {
		ModelContainer container = getModel();
		return container;
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof ModelContainer) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	protected List<Object> getModelChildren() {
		// ここで返された子モデルがツリーの子アイテムになる
		ModelContainer container = getModel();
		return container.getChildren();
	}
	
	@Override
	protected void refreshVisuals() {
		ModelContainer modelContainer = getModel();
		
		// ツリー・アイテムのテキストとしてモデルのテキストを設定
		setWidgetText(modelContainer.getName());
		
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		if (modelContainer.getContainerId().equals(RootTreeEditPart.CONTAINER_TABLE)) {
			setWidgetImage(ir.get(Images.ICON_TABLE));
		} else if (modelContainer.getContainerId().equals(RootTreeEditPart.CONTAINER_VIEW)) {
			setWidgetImage(ir.get(Images.ICON_VIEW));
		} else if (modelContainer.getContainerId().equals(RootTreeEditPart.CONTAINER_DOMAIN)) {
			setWidgetImage(ir.get(Images.ICON_DOMAIN));
		} else {
			logger.warn("unknown container ID: " + modelContainer.getContainerId());
			setWidgetImage(null);
		}
	}
	
	public void commandExecuted(Command arg0) {
		// TODO Auto-generated method stub
		
	}
}

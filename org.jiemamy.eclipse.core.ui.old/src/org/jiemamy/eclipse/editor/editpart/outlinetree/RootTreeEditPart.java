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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.JiemamyEntity;
import org.jiemamy.eclipse.editor.ModelContainer;
import org.jiemamy.model.dbo.DatabaseObjectModel;
import org.jiemamy.model.dbo.DomainModel;
import org.jiemamy.model.dbo.TableModel;
import org.jiemamy.model.dbo.ViewModel;
import org.jiemamy.transaction.Command;
import org.jiemamy.utils.LogMarker;
import org.jiemamy.utils.collection.CollectionsUtil;

/**
 * JiemamyContextに対するTree用EditPart
 * 
 * @author daisuke
 */
public class RootTreeEditPart extends AbstractModelTreeEditPart {
	
	/** ドメインのコンテナID */
	public static final String CONTAINER_DOMAIN = "org.jiemamy.eclipse.ui.container.domain";
	
	/** テーブルのコンテナID */
	public static final String CONTAINER_TABLE = "org.jiemamy.eclipse.ui.container.table";
	
	/** ビューのコンテナID */
	public static final String CONTAINER_VIEW = "org.jiemamy.eclipse.ui.container.view";
	
	private static Logger logger = LoggerFactory.getLogger(RootTreeEditPart.class);
	
	private List<ModelContainer> children = CollectionsUtil.newArrayList();
	
	private ModelContainer domainContainer;
	
	private ModelContainer tableContainer;
	
	private ModelContainer viewContainer;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel コントロール対象の{@link JiemamyContext}
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public RootTreeEditPart(JiemamyContext rootModel) {
		Validate.notNull(rootModel);
		setModel(rootModel);
	}
	
	@Override
	public void activate() {
		logger.trace(LogMarker.LIFECYCLE, "activate");
		super.activate();
	}
	
	@Override
	public void deactivate() {
		logger.trace(LogMarker.LIFECYCLE, "deactivate");
		super.deactivate();
	}
	
	@Override
	public JiemamyContext getModel() {
		return (JiemamyContext) super.getModel();
	}
	
	public JiemamyEntity getTargetModel() {
		JiemamyContext rootModel = getModel();
		return rootModel;
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof JiemamyContext) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException();
		}
		
		// このタイミングでいいのか…？
		JiemamyContext context = getModel();
		
		domainContainer = new ModelContainer(CONTAINER_DOMAIN, "ドメイン", context); // RESOURCE
		tableContainer = new ModelContainer(CONTAINER_TABLE, "テーブル", context); // RESOURCE
		viewContainer = new ModelContainer(CONTAINER_VIEW, "ビュー", context); // RESOURCE
		
		children.add(tableContainer);
		children.add(viewContainer);
		children.add(domainContainer);
	}
	
	@Override
	protected List<? extends JiemamyEntity> getModelChildren() {
		logger.trace(LogMarker.LIFECYCLE, "getModelChildren");
		JiemamyContext rootModel = getModel();
		
		domainContainer.getChildren().clear();
		tableContainer.getChildren().clear();
		viewContainer.getChildren().clear();
		
		for (DatabaseObjectModel entityModel : rootModel.getDatabaseObjects()) {
			if (entityModel instanceof TableModel) {
				tableContainer.getChildren().add(entityModel);
			} else if (entityModel instanceof ViewModel) {
				viewContainer.getChildren().add(entityModel);
			} else if (entityModel instanceof DomainModel) {
				domainContainer.getChildren().add(entityModel);
			} else {
				logger.warn("unknown entity: " + entityModel.getClass().getName());
			}
		}
		// ここで返された子モデルがツリーの子アイテムになる
		return children;
	}
	
	public void commandExecuted(Command arg0) {
		// TODO Auto-generated method stub
		
	}
}

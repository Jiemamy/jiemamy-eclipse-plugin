/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2011/03/05
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
package org.jiemamy.eclipse.core.ui.editor.diagram;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DbObject;
import org.jiemamy.serializer.DbObjectComparator;
import org.jiemamy.utils.LogMarker;

/**
 * RootModelに対するTree用EditPart
 * 
 * @version $Id$
 * @author daisuke
 */
public class JiemamyContextTreeEditPart extends AbstractModelTreeEditPart {
	
	/** ドメインのコンテナID */
	public static final String CONTAINER_DOMAIN = "org.jiemamy.eclipse.ui.container.domain";
	
	/** テーブルのコンテナID */
	public static final String CONTAINER_TABLE = "org.jiemamy.eclipse.ui.container.table";
	
	/** ビューのコンテナID */
	public static final String CONTAINER_VIEW = "org.jiemamy.eclipse.ui.container.view";
	
	private static Logger logger = LoggerFactory.getLogger(JiemamyContextTreeEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context コントロール対象の{@link JiemamyContext}
	 */
	public JiemamyContextTreeEditPart(JiemamyContext context) {
		setModel(context);
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
	
	@Override
	public void setModel(Object model) {
		if (model instanceof JiemamyContext) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException();
		}
		
		// このタイミングでいいのか…？
		JiemamyContext context = getModel();
		
//		domainContainer = new ModelContainer(CONTAINER_DOMAIN, "ドメイン", context); // RESOURCE
//		tableContainer = new ModelContainer(CONTAINER_TABLE, "テーブル", context); // RESOURCE
//		viewContainer = new ModelContainer(CONTAINER_VIEW, "ビュー", context); // RESOURCE
//		
//		children.add(tableContainer);
//		children.add(viewContainer);
//		children.add(domainContainer);
	}
	
	@Override
	protected List<?> getModelChildren() {
		logger.trace(LogMarker.LIFECYCLE, "getModelChildren");
		JiemamyContext context = getModel();
		
		Set<DbObject> dbObjects = context.getDbObjects();
		List<DbObject> children = Lists.newArrayList(dbObjects);
		Collections.sort(children, DbObjectComparator.INSTANCE);
		
		// ここで返された子モデルがツリーの子アイテムになる
		return children;
	}
}

/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table.column;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageRegistry;

import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractModelTreeEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.JmTreeComponentEditPolicy;
import org.jiemamy.model.column.JmColumn;

/**
 * {@link JmColumn}に対するTree用EditPart。
 * 
 * @version $Id$
 * @author daisuke
 */
public class ColumnTreeEditPart extends AbstractModelTreeEditPart {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param column コントロール対象の属性
	 */
	public ColumnTreeEditPart(JmColumn column) {
		setModel(column);
	}
	
	@Override
	public JmColumn getModel() {
		return (JmColumn) super.getModel();
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof JmColumn) {
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
		JmColumn model = getModel();
		// ツリー・アイテムのテキストとしてモデルのテキストを設定
		setWidgetText(StringUtils.defaultString(model.getName()));
		
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		setWidgetImage(ir.get(Images.ICON_COLUMN));
	}
}

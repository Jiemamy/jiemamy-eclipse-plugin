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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table.constraint;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractModelTreeEditPart;
import org.jiemamy.eclipse.core.ui.editor.diagram.JmTreeComponentEditPolicy;
import org.jiemamy.model.constraint.JmCheckConstraint;
import org.jiemamy.model.constraint.JmConstraint;
import org.jiemamy.model.constraint.JmForeignKeyConstraint;
import org.jiemamy.model.constraint.JmNotNullConstraint;
import org.jiemamy.model.constraint.JmPrimaryKeyConstraint;

/**
 * {@link JmConstraint}に対するTree用EditPart。
 * 
 * @version $Id$
 * @author daisuke
 */
public class ConstraintTreeEditPart extends AbstractModelTreeEditPart {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param constraint コントロール対象の属性
	 */
	public ConstraintTreeEditPart(JmConstraint constraint) {
		setModel(constraint);
	}
	
	@Override
	public JmConstraint getModel() {
		return (JmConstraint) super.getModel();
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof JmConstraint) {
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
		JmConstraint model = getModel();
		// ツリー・アイテムのテキストとしてモデルのテキストを設定
		
		String header = "";
		if (model instanceof JmNotNullConstraint) {
			header = "NN: ";
		}
		if (model instanceof JmCheckConstraint) {
			header = "CC: ";
		}
		setWidgetText(header + StringUtils.defaultString(model.getName()));
		
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		Image icon = null;
		if (model instanceof JmForeignKeyConstraint) {
			icon = ir.get(Images.ICON_FK);
		} else if (model instanceof JmPrimaryKeyConstraint) {
			icon = ir.get(Images.ICON_PK);
		}
		setWidgetImage(icon);
	}
}

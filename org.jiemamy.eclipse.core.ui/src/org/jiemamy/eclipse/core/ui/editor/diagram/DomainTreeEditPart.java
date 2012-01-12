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
package org.jiemamy.eclipse.core.ui.editor.diagram;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageRegistry;

import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.model.domain.JmDomain;

/**
 * DefinitionModelに対するTree用EditPart。
 * 
 * @version $Id$
 * @author daisuke
 */
public class DomainTreeEditPart extends AbstractModelTreeEditPart {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param domain コントロール対象のドメイン
	 */
	public DomainTreeEditPart(JmDomain domain) {
		setModel(domain);
	}
	
	@Override
	public JmDomain getModel() {
		return (JmDomain) super.getModel();
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof JmDomain) {
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
		JmDomain model = getModel();
		
		// ツリー・アイテムのテキストとしてモデルのテキストを設定
		setWidgetText(StringUtils.defaultString(model.getName()));
		
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		setWidgetImage(ir.get(Images.ICON_DOMAIN));
	}
}

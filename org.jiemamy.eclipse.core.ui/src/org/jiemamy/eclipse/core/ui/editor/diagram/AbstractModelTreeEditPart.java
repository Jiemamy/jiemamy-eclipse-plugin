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

import org.eclipse.gef.editparts.AbstractTreeEditPart;

import org.jiemamy.JiemamyContext;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.transaction.StoredEventListener;

/**
 * モデルに対するTree用EditPart（コントローラ）の抽象クラス。
 * 
 * @version $Id$
 * @author daisuke
 */
public abstract class AbstractModelTreeEditPart extends AbstractTreeEditPart implements StoredEventListener {
	
	@Override
	public void activate() {
		super.activate();
		
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		context.getEventBroker().addListener(this);
	}
	
	@Override
	public void deactivate() {
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		context.getEventBroker().addListener(this);
		
		super.deactivate();
	}
	
	public void handleStoredEvent(StoredEvent<?> event) {
//		refreshChildren();
		// comment out for bug fix ECL-116
	}
}

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

import org.eclipse.gef.editparts.AbstractTreeEditPart;

import org.jiemamy.JiemamyContext;
import org.jiemamy.transaction.CommandListener;

/**
 * モデルに対するTree用EditPart（コントローラ）の抽象クラス。
 * @author daisuke
 */
public abstract class AbstractModelTreeEditPart extends AbstractTreeEditPart implements CommandListener {
	
	@Override
	public void activate() {
		super.activate();
		
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		rootModel.getJiemamy().getEventBroker().addListener(this);
	}
	
	public void commandExecuted(Command command) {
		try {
			getRoot();
			refresh();
		} catch (NullPointerException e) {
			// HACK getRoot() で NPE が出るケースを避ける方法が分からない。
			// NPEが出る場合は、refreshChildren() にも失敗する為、何もせずメソッドを終了する。
		}
	}
	
	@Override
	public void deactivate() {
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		rootModel.getJiemamy().getEventBroker().removeListener(this);
		
		super.deactivate();
	}
}

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
package org.jiemamy.eclipse.action;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.Action;

/**
 * Jiemamyで使用するアクションの抽象クラス。
 * 
 * @author daisuke
 */
public abstract class AbstractJiemamyAction extends Action {
	
	/** ビュアー */
	private GraphicalViewer viewer;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param name アクション名
	 * @param viewer ビュアー
	 */
	public AbstractJiemamyAction(String name, GraphicalViewer viewer) {
		super(name);
		this.viewer = viewer;
	}
	
	/**
	 * ビュアーを取得する。
	 * 
	 * @return ビュアー
	 */
	protected GraphicalViewer getViewer() {
		return viewer;
	}
}

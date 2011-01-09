/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2011/01/09
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
package org.jiemamy.eclipse.core.ui.model;

import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DefaultDatabaseObjectModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DefaultNodeModel;
import org.jiemamy.model.StickyNodeModel;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class StickyCreation extends NodeCreation {
	
	/** {@link StickyNodeModel}が作られた時、はじめに設定されている値 */
	private static final String DEFAULT_STICKY_CONTENTS = "memo";
	
	private final StickyNodeModel stickyNodeModel;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param stickyNodeModel
	 */
	public StickyCreation(StickyNodeModel stickyNodeModel) {
		this.stickyNodeModel = stickyNodeModel;
	}
	
	@Override
	public void execute(JiemamyContext context, DefaultDiagramModel diagramModel) {
		stickyNodeModel.setContents(DEFAULT_STICKY_CONTENTS);
		diagramModel.store(stickyNodeModel);
	}
	
	@Override
	public DefaultDatabaseObjectModel getCoreElement() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public DefaultNodeModel getDiagramElement() {
		return stickyNodeModel;
	}
}

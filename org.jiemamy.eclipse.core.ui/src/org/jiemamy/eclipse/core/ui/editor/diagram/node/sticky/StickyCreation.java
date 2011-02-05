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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.sticky;

import java.util.UUID;

import org.apache.commons.lang.Validate;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.NodeCreation;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.JmStickyNode;
import org.jiemamy.model.geometory.JmRectangle;

/**
 * {@link JmStickyNode}の生成を表す実装クラス。
 * 
 * @version $Id$
 * @author daisuke
 */
public class StickyCreation implements NodeCreation {
	
	/** {@link JmStickyNode}が作られた時、はじめに設定されている値 */
	private static final String DEFAULT_STICKY_CONTENTS = "memo";
	
	private final JmStickyNode stickyJmNode;
	

	/**
	 * インスタンスを生成する。
	 */
	public StickyCreation() {
		stickyJmNode = new JmStickyNode(UUID.randomUUID());
		stickyJmNode.setContents(DEFAULT_STICKY_CONTENTS);
	}
	
	public void execute(JiemamyContext context, SimpleJmDiagram diagram) {
		Validate.notNull(context);
		Validate.notNull(diagram);
		
		diagram.store(stickyJmNode);
		context.getFacet(DiagramFacet.class).store(diagram);
	}
	
	public void setBoundary(JmRectangle boundary) {
		Validate.notNull(boundary);
		stickyJmNode.setBoundary(boundary);
	}
	
	public void undo(JiemamyContext context, SimpleJmDiagram diagram) {
		Validate.notNull(context);
		Validate.notNull(diagram);
		
		diagram.deleteNode(stickyJmNode.toReference());
		context.getFacet(DiagramFacet.class).store(diagram);
	}
}

/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2010/12/28
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table;

import org.apache.commons.lang.Validate;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.NodeCreation;
import org.jiemamy.model.SimpleDbObjectNode;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.SimpleJmNode;
import org.jiemamy.model.geometory.JmRectangle;
import org.jiemamy.model.table.SimpleJmTable;
import org.jiemamy.utils.NamingUtil;

/**
 * {@link SimpleJmTable}とその{@link SimpleJmNode}の生成を表す実装クラス。
 * 
 * @version $Id$
 * @author daisuke
 */
public class TableCreation implements NodeCreation {
	
	private final SimpleJmTable table;
	
	private final SimpleJmNode node;
	
	
	/**
	 * インスタンスを生成する。
	 */
	public TableCreation() {
		table = new SimpleJmTable();
		node = new SimpleDbObjectNode(table.toReference());
	}
	
	public void execute(JiemamyContext context, SimpleJmDiagram diagram) {
		Validate.notNull(context);
		Validate.notNull(diagram);
		
		NamingUtil.autoName(table, context);
		context.store(table);
		diagram.store(node);
		context.getFacet(DiagramFacet.class).store(diagram);
	}
	
	public void setBoundary(JmRectangle boundary) {
		Validate.notNull(boundary);
		node.setBoundary(boundary);
	}
	
	public void undo(JiemamyContext context, SimpleJmDiagram diagram) {
		Validate.notNull(context);
		Validate.notNull(diagram);
		
		diagram.deleteNode(node.toReference());
		context.getFacet(DiagramFacet.class).store(diagram);
		context.deleteDbObject(table.toReference());
	}
}

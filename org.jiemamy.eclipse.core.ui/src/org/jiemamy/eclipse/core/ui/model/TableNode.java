/*
 * Copyright 2007-2010 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.model;

import org.apache.commons.lang.Validate;

import org.jiemamy.model.NodeModel;
import org.jiemamy.model.dbo.TableModel;

/**
 * TODO for daisuke
 * 
 * @version $Id$
 * @author daisuke
 */
public class TableNode implements CoreNodePair {
	
	private final TableModel table;
	
	private final NodeModel node;
	

	public TableNode(TableModel table, NodeModel node) {
		Validate.notNull(table);
		Validate.notNull(node);
		this.table = table;
		this.node = node;
	}
	
	public TableModel getCoreElement() {
		return table;
	}
	
	public NodeModel getDiagramElement() {
		return node;
	}
}

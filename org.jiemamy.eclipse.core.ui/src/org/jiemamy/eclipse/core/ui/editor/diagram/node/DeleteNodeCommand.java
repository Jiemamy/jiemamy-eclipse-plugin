/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.model.DbObject;
import org.jiemamy.model.DbObjectNode;
import org.jiemamy.model.JmConnection;
import org.jiemamy.model.JmNode;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.constraint.JmForeignKeyConstraint;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.model.table.SimpleJmTable;

/**
 * ノード削除GEFコマンド。
 * 
 * @author daisuke
 */
public class DeleteNodeCommand extends Command {
	
	/** 削除元 {@link JiemamyContext} */
	private final JiemamyContext context;
	
	/** 削除されるノード */
	private final JmNode node;
	
	private DbObject deletedCore;
	
	private final int diagramIndex;
	
	Collection<JmConnection> connections;
	
	Collection<Entry> outerForeingKeys;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param context {@link JiemamyContext}
	 * @param diagramIndex ダイアグラムindex
	 * @param node 削除されるノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 * @throws IllegalArgumentException 引数{@code context}が {@link DiagramFacet} を持っていない場合
	 */
	public DeleteNodeCommand(JiemamyContext context, int diagramIndex, JmNode node) {
		Validate.notNull(context);
		Validate.notNull(node);
		Validate.isTrue(context.hasFacet(DiagramFacet.class));
		
		this.context = context;
		this.diagramIndex = diagramIndex;
		this.node = node;
	}
	
	@Override
	public void execute() {
		DiagramFacet diagramFacet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagram = (SimpleJmDiagram) diagramFacet.getDiagrams().get(diagramIndex);
		
		Set<JmTable> tables = context.getTables();
		
		// ノードにつられて削除されるコネクション
		connections = Sets.newHashSet();
		// （主ノードではないノードに属する）削除される外部キー
		outerForeingKeys = Sets.newHashSet();
		for (JmConnection connection : diagram.getSourceConnectionsFor(node.toReference())) {
			connections.add(connection);
		}
		for (JmConnection connection : diagram.getTargetConnectionsFor(node.toReference())) {
			connections.add(connection);
			
			JmForeignKeyConstraint fk = context.resolve(connection.getCoreModelRef());
			JmTable table = fk.findDeclaringTable(tables);
			if (table instanceof SimpleJmTable) {
				SimpleJmTable t = (SimpleJmTable) table;
				outerForeingKeys.add(new Entry(t, fk));
			}
		}
		
		for (JmConnection connection : connections) {
			diagram.deleteConnection(connection.toReference());
		}
		diagramFacet.store(diagram);
		
		diagram.deleteNode(node.toReference());
		diagramFacet.store(diagram);
		
		if (node instanceof DbObjectNode) {
			DbObjectNode dbObjectNode = (DbObjectNode) node;
			EntityRef<? extends DbObject> coreRef = dbObjectNode.getCoreModelRef();
			deletedCore = context.resolve(coreRef);
			context.deleteDbObject(coreRef);
			
			for (Entry e : outerForeingKeys) {
				e.table.deleteConstraint(e.fk.toReference());
				context.store(e.table);
			}
		}
	}
	
	@Override
	public void undo() {
		DiagramFacet diagramFacet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagram = (SimpleJmDiagram) diagramFacet.getDiagrams().get(diagramIndex);
		
		context.store(deletedCore);
		
		for (Entry e : outerForeingKeys) {
			e.table.store(e.fk);
			context.store(e.table);
		}
		
		diagram.store(node);
		
		for (JmConnection connection : connections) {
			diagram.store(connection);
		}
		
		diagramFacet.store(diagram);
	}
	
	
	private static class Entry {
		
		SimpleJmTable table;
		
		JmForeignKeyConstraint fk;
		
		
		Entry(SimpleJmTable table, JmForeignKeyConstraint fk) {
			this.table = table;
			this.fk = fk;
		}
	}
}

/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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

import com.google.common.collect.ImmutableSet;
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
	
	private final DiagramFacet diagramFacet;
	
	private final SimpleJmDiagram diagramModel;
	
	/** 削除されるノード */
	private final JmNode nodeModel;
	
	/** 削除されるコネクション */
	private final Collection<JmConnection> connectionModels;
	
	/** （主ノードではないノードに属する）削除される外部キー */
	private final Collection<Entry> outerForeingKeys;
	
	private final JiemamyContext context;
	
	private DbObject deletedCore;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context {@link JiemamyContext}
	 * @param diagramIndex ダイアグラムindex
	 * @param nodeModel 削除されるノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 * @throws IllegalArgumentException 引数{@code context}が {@link DiagramFacet} を持っていない場合
	 */
	public DeleteNodeCommand(JiemamyContext context, int diagramIndex, JmNode nodeModel) {
		Validate.notNull(context);
		Validate.notNull(nodeModel);
		Validate.isTrue(context.hasFacet(DiagramFacet.class));
		
		this.context = context;
		this.nodeModel = nodeModel;
		diagramFacet = context.getFacet(DiagramFacet.class);
		diagramModel = (SimpleJmDiagram) diagramFacet.getDiagrams().get(diagramIndex);
		
		Set<JmTable> tables = context.getTables();
		
		Collection<JmConnection> connectionModels = Sets.newHashSet();
		Collection<Entry> outerForeingKeys = Sets.newHashSet();
		for (JmConnection connectionModel : diagramModel.getSourceConnectionsFor(nodeModel.toReference())) {
			connectionModels.add(connectionModel);
		}
		for (JmConnection connectionModel : diagramModel.getTargetConnectionsFor(nodeModel.toReference())) {
			connectionModels.add(connectionModel);
			
			JmForeignKeyConstraint fk = context.resolve(connectionModel.getCoreModelRef());
			JmTable table = fk.findDeclaringTable(tables);
			if (table instanceof SimpleJmTable) {
				SimpleJmTable t = (SimpleJmTable) table;
				outerForeingKeys.add(new Entry(t, fk));
			}
		}
		this.connectionModels = ImmutableSet.copyOf(connectionModels);
		this.outerForeingKeys = ImmutableSet.copyOf(outerForeingKeys);
	}
	
	@Override
	public void execute() {
		for (JmConnection connectionModel : connectionModels) {
			diagramModel.deleteConnection(connectionModel.toReference());
		}
		diagramFacet.store(diagramModel);
		
		diagramModel.deleteNode(nodeModel.toReference());
		diagramFacet.store(diagramModel);
		
		if (nodeModel instanceof DbObjectNode) {
			DbObjectNode databaseObjectJmNode = (DbObjectNode) nodeModel;
			EntityRef<? extends DbObject> coreRef = databaseObjectJmNode.getCoreModelRef();
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
		context.store(deletedCore);
		
		for (Entry e : outerForeingKeys) {
			e.table.store(e.fk);
			context.store(e.table);
		}
		
		diagramModel.store(nodeModel);
		
		for (JmConnection connectionModel : connectionModels) {
			diagramModel.store(connectionModel);
		}
		
		diagramFacet.store(diagramModel);
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

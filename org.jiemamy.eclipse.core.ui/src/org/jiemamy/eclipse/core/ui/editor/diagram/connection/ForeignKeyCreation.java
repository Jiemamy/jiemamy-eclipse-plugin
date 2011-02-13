/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.connection;

import java.util.UUID;

import org.apache.commons.lang.Validate;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.core.ui.editor.diagram.Creation;
import org.jiemamy.model.JmNode;
import org.jiemamy.model.SimpleJmConnection;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.constraint.SimpleJmForeignKeyConstraint;
import org.jiemamy.model.table.SimpleJmTable;
import org.jiemamy.utils.ForeignKeyFactory;

/**
 * 外部キーとそのコネクションの生成を表すクラス。
 * 
 * @version $Id$
 * @author daisuke
 */
public class ForeignKeyCreation implements Creation {
	
	private final SimpleJmForeignKeyConstraint foreignKey;
	
	private final SimpleJmConnection connection;
	
	private SimpleJmTable sourceTable;
	
	private SimpleJmTable targetTable;
	

	/**
	 * インスタンスを生成する。
	 */
	public ForeignKeyCreation() {
		foreignKey = new SimpleJmForeignKeyConstraint();
		connection = new SimpleJmConnection(UUID.randomUUID(), foreignKey.toReference());
	}
	
	public void execute(JiemamyContext context, SimpleJmDiagram diagram) {
		Validate.notNull(context);
		Validate.notNull(diagram);
		
		ForeignKeyFactory.setup(foreignKey, context, sourceTable, targetTable);
		
		sourceTable.store(foreignKey);
		context.store(sourceTable);
		
		diagram.store(connection);
		context.getFacet(DiagramFacet.class).store(diagram);
	}
	
	/**
	 * 起点ノードを設定する。
	 * 
	 * @param sourceRef 起点ノードの参照
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public void setSource(EntityRef<? extends JmNode> sourceRef) {
		Validate.notNull(sourceRef);
		connection.setSource(sourceRef);
	}
	
	/**
	 * 外部キーを作成する対象のテーブルを設定する。
	 * 
	 * @param sourceTable 外部キーを作成する対象のテーブル
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public void setSourceTable(SimpleJmTable sourceTable) {
		Validate.notNull(sourceTable);
		this.sourceTable = sourceTable;
	}
	
	/**
	 * 終点ノードを設定する。
	 * 
	 * @param targetRef 終点ノードの参照
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public void setTarget(EntityRef<? extends JmNode> targetRef) {
		Validate.notNull(targetRef);
		connection.setTarget(targetRef);
	}
	
	/**
	 * 外部キーが参照するテーブルを設定する。
	 * 
	 * @param targetTable 外部キーが参照するテーブル
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public void setTargetTable(SimpleJmTable targetTable) {
		Validate.notNull(targetTable);
		this.targetTable = targetTable;
	}
	
	public void undo(JiemamyContext context, SimpleJmDiagram diagram) {
		Validate.notNull(context);
		Validate.notNull(diagram);
		
		diagram.deleteConnection(connection.toReference());
		context.getFacet(DiagramFacet.class).store(diagram);
		
		sourceTable.deleteConstraint(foreignKey.toReference());
		context.store(sourceTable);
	}
}

/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2011/01/26
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

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DbObject;
import org.jiemamy.model.JmNode;
import org.jiemamy.model.SimpleJmDiagram;

/**
 * {@link DbObject}を編集するコマンド。
 * 
 * @version $Id$
 * @author daisuke
 */
public class EditDbObjectCommand extends Command {
	
	private final JiemamyContext context;
	
	private final DbObject dbObject;
	
	private final DbObject oldDbObject;
	
	private final JmNode node;
	
	private final JmNode oldNode;
	
	private final int diagramIndex;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context コンテキスト
	 * @param dbObject 編集対象{@link DbObject}
	 * @param node ノード
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public EditDbObjectCommand(JiemamyContext context, DbObject dbObject, JmNode node, int diagramIndex) {
		Validate.notNull(context);
		Validate.notNull(dbObject);
		Validate.notNull(node);
		
		this.context = context;
		this.dbObject = dbObject;
		this.node = node;
		this.diagramIndex = diagramIndex;
		oldNode = context.resolve(node.toReference());
		oldDbObject = context.resolve(dbObject.toReference());
	}
	
	@Override
	public void execute() {
		context.store(dbObject);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagram = (SimpleJmDiagram) facet.getDiagrams().get(diagramIndex);
		diagram.store(node);
		facet.store(diagram);
	}
	
	@Override
	public void undo() {
		context.store(oldDbObject);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagram = (SimpleJmDiagram) facet.getDiagrams().get(diagramIndex);
		diagram.store(oldNode);
		facet.store(diagram);
	}
}

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
package org.jiemamy.eclipse.core.ui.editor.diagram.connection;

import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.SimpleJmConnection;
import org.jiemamy.model.SimpleJmDiagram;

/**
 * ベンドポイント削除GEFコマンド。
 * 
 * @author daisuke
 */
public class DeleteBendpointCommand extends Command {
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	
	/** 削除元のコネクション */
	private SimpleJmConnection connection;
	
	/** source側からtarget側に向かって数えたベンドポイントのインデックス */
	private int bendpointIndex;
	
	private final JiemamyContext context;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param context ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param connection ベンドポイント削除対象のコネクション
	 * @param bendpointIndex source側からtarget側に向かって数えたベンドポイントのインデックス
	 */
	public DeleteBendpointCommand(JiemamyContext context, int diagramIndex, SimpleJmConnection connection,
			int bendpointIndex) {
		this.context = context;
		this.diagramIndex = diagramIndex;
		this.connection = connection;
		this.bendpointIndex = bendpointIndex;
	}
	
	@Override
	public void execute() {
		connection.breachEncapsulationOfBendpoints().remove(bendpointIndex);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagram = (SimpleJmDiagram) facet.getDiagrams().get(diagramIndex);
		diagram.store(connection);
		facet.store(diagram);
	}
	
	@Override
	public void undo() {
		// TODO undo
//		jiemamyFacade.rollback(save);
	}
}

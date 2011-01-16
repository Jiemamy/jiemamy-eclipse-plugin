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
package org.jiemamy.eclipse.core.ui.editor.command;

import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.DefaultConnectionModel;
import org.jiemamy.model.DefaultDiagramModel;

/**
 * ベンドポイント削除GEFコマンド。
 * 
 * @author daisuke
 */
public class DeleteBendpointCommand extends Command {
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	
	/** 削除元のコネクション */
	private DefaultConnectionModel connectionModel;
	
	/** source側からtarget側に向かって数えたベンドポイントのインデックス */
	private int bendpointIndex;
	
	private final JiemamyContext context;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param connectionModel ベンドポイント削除対象のコネクション
	 * @param bendpointIndex source側からtarget側に向かって数えたベンドポイントのインデックス
	 */
	public DeleteBendpointCommand(JiemamyContext context, int diagramIndex, DefaultConnectionModel connectionModel,
			int bendpointIndex) {
		this.context = context;
		this.diagramIndex = diagramIndex;
		this.connectionModel = connectionModel;
		this.bendpointIndex = bendpointIndex;
	}
	
	@Override
	public void execute() {
		connectionModel.breachEncapsulationOfBendpoints().remove(bendpointIndex);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(diagramIndex);
		diagramModel.store(connectionModel);
		facet.store(diagramModel);
	}
	
	@Override
	public void undo() {
//		jiemamyFacade.rollback(save);
	}
}

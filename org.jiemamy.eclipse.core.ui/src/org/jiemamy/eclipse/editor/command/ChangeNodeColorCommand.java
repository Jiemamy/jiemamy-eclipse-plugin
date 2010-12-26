/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.editor.command;

import org.eclipse.gef.commands.Command;

import org.jiemamy.JiemamyContext;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.geometory.JmColor;
import org.jiemamy.transaction.SavePoint;

/**
 * ノードの背景色変更GEFコマンド。
 * 
 * @author daisuke
 */
public class ChangeNodeColorCommand extends Command {
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	
	private NodeModel nodeAdapter;
	
	private JmColor newColor;
	
	private JiemamyViewFacade jiemamyFacade;
	
	private SavePoint save;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param nodeAdapter 変更対象のエンティティ
	 * @param newColor newColor
	 */
	public ChangeNodeColorCommand(JiemamyContext rootModel, int diagramIndex, NodeModel nodeAdapter, JmColor newColor) {
		this.diagramIndex = diagramIndex;
		this.nodeAdapter = nodeAdapter;
		this.newColor = newColor;
		
		jiemamyFacade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
	}
	
	@Override
	public void execute() {
		save = jiemamyFacade.save();
		jiemamyFacade.changeNodeColor(diagramIndex, nodeAdapter, newColor);
	}
	
	@Override
	public void undo() {
		jiemamyFacade.rollback(save);
	}
}

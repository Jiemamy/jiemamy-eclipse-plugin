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

import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.utils.LogMarker;

/**
 * ノード作成GEFコマンド。
 * 
 * @author daisuke
 */
public class CreateNodeCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(CreateNodeCommand.class);
	
	private final JiemamyContext context;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private final int diagramIndex;
	
	private NodeCreation creation;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context 作成ノードの親モデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param creation 作成するノード
	 */
	public CreateNodeCommand(JiemamyContext context, int diagramIndex, NodeCreation creation) {
		this.context = context;
		this.diagramIndex = diagramIndex;
		this.creation = creation;
	}
	
	@Override
	public void execute() {
		logger.debug(LogMarker.LIFECYCLE, "execute");
		
		SimpleJmDiagram diagram =
				(SimpleJmDiagram) context.getFacet(DiagramFacet.class).getDiagrams().get(diagramIndex);
		creation.execute(context, diagram);
	}
	
	@Override
	public void undo() {
		logger.debug(LogMarker.LIFECYCLE, "undo");
		SimpleJmDiagram diagram =
				(SimpleJmDiagram) context.getFacet(DiagramFacet.class).getDiagrams().get(diagramIndex);
		creation.undo(context, diagram);
	}
}

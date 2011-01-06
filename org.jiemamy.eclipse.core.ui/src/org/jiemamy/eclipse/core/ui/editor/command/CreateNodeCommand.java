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
package org.jiemamy.eclipse.core.ui.editor.command;

import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.model.CoreNodePair;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.dbo.DefaultDatabaseObjectModel;
import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;
import org.jiemamy.utils.NamingUtil;

/**
 * ノード作成GEFコマンド。
 * 
 * @author daisuke
 */
public class CreateNodeCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(CreateNodeCommand.class);
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private final int diagramIndex;
	
	private CoreNodePair model;
	
	private SavePoint savePoint;
	
	private final JiemamyContext context;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context 作成ノードの親モデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param model 作成するノード
	 */
	public CreateNodeCommand(JiemamyContext context, int diagramIndex, CoreNodePair model) {
		this.context = context;
		this.diagramIndex = diagramIndex;
		this.model = model;
	}
	
	@Override
	public void execute() {
		logger.debug(LogMarker.LIFECYCLE, "execute");
//		savePoint = jiemamyFacade.save();
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(diagramIndex);
		NodeModel nodeModel = model.getDiagramElement();
		DefaultDatabaseObjectModel coreModel = model.getCoreElement();
		NamingUtil.autoName(coreModel, context);
		context.store(coreModel);
		diagramModel.store(nodeModel);
		facet.store(diagramModel);
		
//		jiemamyFacade.addNode(diagramIndex, nodeAdapter, ConvertUtil.convert(rectangle));
	}
	
	@Override
	public void undo() {
		logger.debug(LogMarker.LIFECYCLE, "undo");
//		jiemamyFacade.rollback(savePoint);
	}
}

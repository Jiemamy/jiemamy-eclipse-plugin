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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.NodeModel;
import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;

/**
 * ノード作成GEFコマンド。
 * 
 * @author daisuke
 */
public class CreateNodeCommand extends Command {
	
	private static Logger logger = LoggerFactory.getLogger(CreateNodeCommand.class);
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private final int diagramIndex;
	
	private NodeModel nodeAdapter;
	
	private Rectangle rectangle;
	
	private JiemamyViewFacade jiemamyFacade;
	
	private SavePoint savePoint;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel 作成ノードの親モデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param nodeAdapter 作成するノード
	 * @param rectangle 作成する位置サイズ
	 */
	public CreateNodeCommand(JiemamyContext rootModel, int diagramIndex, NodeModel nodeAdapter, Rectangle rectangle) {
		this.diagramIndex = diagramIndex;
		this.nodeAdapter = nodeAdapter;
		this.rectangle = rectangle;
		
		jiemamyFacade = rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class);
		logger.debug(LogMarker.LIFECYCLE, "construct");
	}
	
	@Override
	public void execute() {
		logger.debug(LogMarker.LIFECYCLE, "execute");
		savePoint = jiemamyFacade.save();
		jiemamyFacade.addNode(diagramIndex, nodeAdapter, ConvertUtil.convert(rectangle));
	}
	
	@Override
	public void undo() {
		logger.debug(LogMarker.LIFECYCLE, "undo");
		jiemamyFacade.rollback(savePoint);
	}
}

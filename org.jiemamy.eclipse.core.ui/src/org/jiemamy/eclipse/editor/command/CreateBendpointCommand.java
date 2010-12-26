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

import org.eclipse.draw2d.geometry.Point;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.geometory.JmPoint;
import org.jiemamy.transaction.SavePoint;

/**
 * ベンドポイント追加GEFコマンド。
 * 
 * @author daisuke
 */
public class CreateBendpointCommand extends AbstractMovePositionCommand {
	
	private ConnectionModel connectionAdapter;
	
	private Point location;
	
	/** source側からtarget側に向かって数えたベンドポイントのインデックス */
	private int bendpointIndex;
	
	private SavePoint save;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param connectionAdapter ベンドポイント追加対象のrelation
	 * @param location ベンドポイントの座標
	 * @param bendpointIndex source側からtarget側に向かって数えたベンドポイントのインデックス
	 */
	public CreateBendpointCommand(JiemamyContext rootModel, int diagramIndex, ConnectionModel connectionAdapter,
			Point location, int bendpointIndex) {
		super(diagramIndex, rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class));
		this.diagramIndex = diagramIndex;
		this.connectionAdapter = connectionAdapter;
		this.location = location;
		this.bendpointIndex = bendpointIndex;
		
		// 移動量の計算
		int shiftX = location.x < 0 ? Math.abs(location.x) : 0;
		int shiftY = location.y < 0 ? Math.abs(location.y) : 0;
		setShift(new JmPoint(shiftX, shiftY));
	}
	
	@Override
	public void execute() {
		JiemamyViewFacade jiemamyFacade = getJiemamyFacade();
		save = jiemamyFacade.save();
		jiemamyFacade.addBendpoint(diagramIndex, connectionAdapter, bendpointIndex, ConvertUtil.convert(location));
		shiftPosition(false);
	}
	
	@Override
	public void undo() {
		JiemamyViewFacade jiemamyFacade = getJiemamyFacade();
		jiemamyFacade.rollback(save);
	}
	
}

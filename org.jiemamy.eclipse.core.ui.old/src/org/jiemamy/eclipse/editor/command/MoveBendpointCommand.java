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
 * ベンドポイント移動GEFコマンド。
 * 
 * @author daisuke
 */
public class MoveBendpointCommand extends AbstractMovePositionCommand {
	
	private ConnectionModel connectionAdapter;
	
	/** ベンドポイントの移動先座標 */
	private Point newLocation;
	
	/** source側からtarget側に向かって数えたベンドポイントのインデックス */
	private int bendpointIndex;
	
	private SavePoint savePoint;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param connectionAdapter 操作対象のコネクション
	 * @param newLocation 新しい座標
	 * @param bendpointIndex source側からtarget側に向かって数えたベンドポイントのインデックス
	 */
	public MoveBendpointCommand(JiemamyContext rootModel, int diagramIndex, ConnectionModel connectionAdapter,
			int bendpointIndex, Point newLocation) {
		super(diagramIndex, rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class));
		this.diagramIndex = diagramIndex;
		this.connectionAdapter = connectionAdapter;
		this.bendpointIndex = bendpointIndex;
		this.newLocation = newLocation;
		
		int shiftX = newLocation.x < 0 ? Math.abs(newLocation.x) : 0;
		int shiftY = newLocation.y < 0 ? Math.abs(newLocation.y) : 0;
		setShift(new JmPoint(shiftX, shiftY));
	}
	
	@Override
	public void execute() {
		JiemamyViewFacade jiemamyFacade = getJiemamyFacade();
		savePoint = jiemamyFacade.save();
		jiemamyFacade.moveBendpoint(diagramIndex, connectionAdapter, bendpointIndex, ConvertUtil.convert(newLocation));
		shiftPosition(false);
	}
	
	@Override
	public void undo() {
		JiemamyViewFacade jiemamyFacade = getJiemamyFacade();
		jiemamyFacade.rollback(savePoint);
	}
}

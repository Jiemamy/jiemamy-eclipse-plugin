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

import java.util.List;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.JiemamyEditor;
import org.jiemamy.eclipse.utils.EditorUtil;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.geometory.JmPoint;
import org.jiemamy.model.geometory.JmRectangle;

/**
 * ノードを移動させるGEFコマンドの抽象クラス。
 * 
 * @author daisuke
 */
public abstract class AbstractMovePositionCommand extends Command {
	
	private JmPoint shift;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	
	private final JiemamyViewFacade jiemamyFacade;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param jiemamyFacade モデル操作を実行するファサード
	 */
	public AbstractMovePositionCommand(int diagramIndex, JiemamyViewFacade jiemamyFacade) {
		this.diagramIndex = diagramIndex;
		this.jiemamyFacade = jiemamyFacade;
	}
	
	/**
	 * shiftを取得する。
	 * 
	 * @return shift
	 */
	public JmPoint getShift() {
		return shift;
	}
	
	/**
	 * shiftを設定する。
	 * 
	 * @param shift shift
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public void setShift(JmPoint shift) {
		Validate.notNull(shift);
		this.shift = shift;
	}
	
	/**
	 * ファサードを取得する。
	 * 
	 * @return ファサード
	 */
	protected JiemamyViewFacade getJiemamyFacade() {
		return jiemamyFacade;
	}
	
	/**
	 * ダイアグラムを全体的に移動させる。
	 * 
	 * @param negative 正方向に移動させる場合は{@code true}、負方向の場合は{@code false}
	 */
	protected void shiftPosition(boolean negative) {
		JiemamyEditor editor = (JiemamyEditor) EditorUtil.getActiveEditor();
		JiemamyContext rootModel = editor.getJiemamyContext();
		
		DiagramFacet diagramPresentations = rootModel.getAdapter(DiagramFacet.class);
		DiagramModel presentation = diagramPresentations.get(diagramIndex);
		for (NodeModel node : presentation.getNodeProfiles().keySet()) {
			// エンティティの移動
			NodeProfile nodeProfile = presentation.getNodeProfiles().get(node);
			JmRectangle old = nodeProfile.getBoundary();
			JmRectangle newBoundary;
			if (negative) {
				newBoundary = new JmRectangle(old.x - shift.x, old.y - shift.y, old.width, old.height);
			} else {
				newBoundary = new JmRectangle(old.x + shift.x, old.y + shift.y, old.width, old.height);
			}
			jiemamyFacade.changeModelProperty(nodeProfile, NodeProfileProperty.boundary, newBoundary);
			
			// ベンドポイントの移動
			for (ConnectionModel connection : node.getSourceConnections()) {
				ConnectionProfile connectionProfile = presentation.getConnectionProfiles().get(connection);
				
				List<JmPoint> bendpoints = connectionProfile.getBendpoints();
				for (JmPoint bendpoint : bendpoints) {
					JmPoint newLocation;
					if (negative) {
						newLocation = new JmPoint(bendpoint.x - shift.x, bendpoint.y - shift.y);
					} else {
						newLocation = new JmPoint(bendpoint.x + shift.x, bendpoint.y + shift.y);
					}
					
					jiemamyFacade.moveBendpoint(diagramIndex, connection, bendpoints.indexOf(bendpoint), newLocation);
				}
			}
		}
	}
}

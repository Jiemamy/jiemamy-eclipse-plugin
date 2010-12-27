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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.editor.JiemamyEditor;
import org.jiemamy.eclipse.core.ui.utils.EditorUtil;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.DefaultConnectionModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DefaultNodeModel;
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
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 */
	public AbstractMovePositionCommand(int diagramIndex) {
		this.diagramIndex = diagramIndex;
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
	 * ダイアグラムを全体的に移動させる。
	 * 
	 * @param negative 正方向に移動させる場合は{@code true}、負方向の場合は{@code false}
	 */
	protected void shiftPosition(boolean negative) {
		JiemamyEditor editor = (JiemamyEditor) EditorUtil.getActiveEditor();
		JiemamyContext context = editor.getJiemamyContext();
		
		DiagramFacet diagramFacet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) diagramFacet.getDiagrams().get(diagramIndex);
		for (NodeModel node : diagramModel.getNodes()) {
			// ノードの移動
			JmRectangle old = node.getBoundary();
			JmRectangle newBoundary;
			if (negative) {
				newBoundary = new JmRectangle(old.x - shift.x, old.y - shift.y, old.width, old.height);
			} else {
				newBoundary = new JmRectangle(old.x + shift.x, old.y + shift.y, old.width, old.height);
			}
			((DefaultNodeModel) node).setBoundary(newBoundary);
			
			// ベンドポイントの移動
			Iterator<? extends ConnectionModel> itr = node.getSourceConnections().iterator();
			for (int i = 0; itr.hasNext(); i++) {
				ConnectionModel connection = itr.next();
				List<JmPoint> bendpoints = connection.getBendpoints();
				for (JmPoint bendpoint : bendpoints) {
					JmPoint newLocation;
					if (negative) {
						newLocation = new JmPoint(bendpoint.x - shift.x, bendpoint.y - shift.y);
					} else {
						newLocation = new JmPoint(bendpoint.x + shift.x, bendpoint.y + shift.y);
					}
					((DefaultConnectionModel) connection).breachEncapsulationOfBendpoints().set(i, newLocation);
//					jiemamyFacade.moveBendpoint(diagramIndex, connection, bendpoints.indexOf(bendpoint), newLocation);
				}
			}
			
			diagramModel.store(node);
		}
		diagramFacet.store(diagramModel);
	}
}

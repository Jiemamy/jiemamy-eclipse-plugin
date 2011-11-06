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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.eclipse.gef.commands.Command;

import org.jiemamy.model.JmConnection;
import org.jiemamy.model.JmNode;
import org.jiemamy.model.SimpleJmConnection;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.SimpleJmNode;
import org.jiemamy.model.geometory.JmPoint;
import org.jiemamy.model.geometory.JmRectangle;

/**
 * ノードを移動させるGEFコマンドの抽象クラス。
 * 
 * @author daisuke
 */
public abstract class AbstractMovePositionCommand extends Command {
	
	private JmPoint shift;
	
	
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
	 * @param diagram 操作対象ダイアグラム
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	protected void shiftPosition(boolean negative, SimpleJmDiagram diagram) {
		Validate.notNull(diagram);
		for (JmNode node : diagram.getNodes()) {
			if (node instanceof SimpleJmNode == false) {
				continue;
			}
			SimpleJmNode simpleNode = (SimpleJmNode) node;
			
			// ノードの移動
			JmRectangle old = simpleNode.getBoundary();
			JmRectangle newBoundary;
			if (negative) {
				newBoundary = new JmRectangle(old.x - shift.x, old.y - shift.y, old.width, old.height);
			} else {
				newBoundary = new JmRectangle(old.x + shift.x, old.y + shift.y, old.width, old.height);
			}
			simpleNode.setBoundary(newBoundary);
			
			// ベンドポイントの移動
			Collection<? extends JmConnection> sourceConnections =
					diagram.getSourceConnectionsFor(simpleNode.toReference());
			for (JmConnection connection : sourceConnections) {
				if (connection instanceof SimpleJmConnection == false) {
					continue;
				}
				SimpleJmConnection simpleConnection = (SimpleJmConnection) connection;
				List<JmPoint> bendpoints = simpleConnection.getBendpoints();
				for (int bendpointIndex = 0; bendpointIndex < simpleConnection.getBendpoints().size(); bendpointIndex++) {
					JmPoint bendpoint = bendpoints.get(bendpointIndex);
					JmPoint newLocation;
					if (negative) {
						newLocation = new JmPoint(bendpoint.x - shift.x, bendpoint.y - shift.y);
					} else {
						newLocation = new JmPoint(bendpoint.x + shift.x, bendpoint.y + shift.y);
					}
					simpleConnection.breachEncapsulationOfBendpoints().set(bendpointIndex, newLocation);
				}
				diagram.store(simpleConnection);
			}
			diagram.store(simpleNode);
		}
	}
}

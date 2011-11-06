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
package org.jiemamy.eclipse.core.ui.editor.diagram.connection;

import org.apache.commons.lang.Validate;
import org.eclipse.draw2d.geometry.Point;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.editor.diagram.node.AbstractMovePositionCommand;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.SimpleJmConnection;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.geometory.JmPoint;

/**
 * ベンドポイント移動GEFコマンド。
 * 
 * @author daisuke
 */
public class MoveBendpointCommand extends AbstractMovePositionCommand {
	
	private SimpleJmConnection connection;
	
	/** ベンドポイントの移動先座標 */
	private Point newLocation;
	
	/** source側からtarget側に向かって数えたベンドポイントのインデックス */
	private int bendpointIndex;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private int diagramIndex;
	
	private final JiemamyContext context;
	
	private final JmPoint oldLocation;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param context ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param connection 操作対象のコネクション
	 * @param newLocation 新しい座標
	 * @param bendpointIndex source側からtarget側に向かって数えたベンドポイントのインデックス
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public MoveBendpointCommand(JiemamyContext context, int diagramIndex, SimpleJmConnection connection,
			int bendpointIndex, Point newLocation) {
		Validate.notNull(context);
		Validate.notNull(connection);
		Validate.notNull(newLocation);
		
		this.context = context;
		this.diagramIndex = diagramIndex;
		this.connection = connection;
		this.bendpointIndex = bendpointIndex;
		this.newLocation = newLocation;
		oldLocation = connection.getBendpoints().get(bendpointIndex);
		
		int shiftX = newLocation.x < 0 ? Math.abs(newLocation.x) : 0;
		int shiftY = newLocation.y < 0 ? Math.abs(newLocation.y) : 0;
		setShift(new JmPoint(shiftX, shiftY));
	}
	
	@Override
	public void execute() {
		connection.breachEncapsulationOfBendpoints().set(bendpointIndex, ConvertUtil.convert(newLocation));
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagram = (SimpleJmDiagram) facet.getDiagrams().get(diagramIndex);
		diagram.store(connection);
		shiftPosition(false, diagram);
		facet.store(diagram);
	}
	
	@Override
	public void undo() {
		connection.breachEncapsulationOfBendpoints().set(bendpointIndex, oldLocation);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagram = (SimpleJmDiagram) facet.getDiagrams().get(diagramIndex);
		diagram.store(connection);
		shiftPosition(true, diagram);
		facet.store(diagram);
	}
}

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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.geometory.JmPoint;
import org.jiemamy.model.geometory.JmPointUtil;
import org.jiemamy.model.geometory.JmRectangle;
import org.jiemamy.transaction.SavePoint;
import org.jiemamy.utils.LogMarker;

/**
 * ノードの位置・サイズ変更GEFコマンド。
 * 
 * @author daisuke
 */
public class ChangeNodeConstraintCommand extends AbstractMovePositionCommand {
	
	private static Logger logger = LoggerFactory.getLogger(ChangeNodeConstraintCommand.class);
	
	private final JiemamyContext rootModel;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private final int diagramIndex;
	
	private final NodeModel nodeAdapter;
	
	private final JmRectangle boundary;
	
	private final JmRectangle oldBoundary;
	
	private final EditPartViewer viewer;
	
	private SavePoint save;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param nodeAdapter 操作対象ノード
	 * @param boundary 新しい位置サイズ
	 * @param viewer ビューア
	 */
	public ChangeNodeConstraintCommand(JiemamyContext rootModel, int diagramIndex, NodeModel nodeAdapter,
			JmRectangle boundary, EditPartViewer viewer) {
		super(diagramIndex, rootModel.getJiemamy().getFactory().newFacade(JiemamyViewFacade.class));
		this.rootModel = rootModel;
		this.diagramIndex = diagramIndex;
		this.nodeAdapter = nodeAdapter;
		DiagramFacet diagramPresentations = rootModel.getFacet(DiagramFacet.class);
		DiagramModel presentationModel = diagramPresentations.getDiagrams().get(diagramIndex);
		oldBoundary = presentationModel.getNodeProfiles().get(nodeAdapter).getBoundary();
		this.boundary = boundary;
		this.viewer = viewer;
		
		// 移動量の計算
		int shiftX = boundary.x < 0 ? Math.abs(boundary.x) : 0;
		int shiftY = boundary.y < 0 ? Math.abs(boundary.y) : 0;
		setShift(new JmPoint(shiftX, shiftY));
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param rootModel ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param nodeAdapter 操作対象ノード
	 * @param rectangle 新しい位置サイズ
	 * @param viewer ビューア
	 */
	public ChangeNodeConstraintCommand(JiemamyContext rootModel, int diagramIndex, NodeModel nodeAdapter,
			Rectangle rectangle, EditPartViewer viewer) {
		this(rootModel, diagramIndex, nodeAdapter, ConvertUtil.convert(rectangle), viewer);
	}
	
	@Override
	public void execute() {
		logger.debug(LogMarker.LIFECYCLE, "execute");
		
		JiemamyViewFacade jiemamyFacade = getJiemamyFacade();
		save = jiemamyFacade.save();
		// 本体の移動
		jiemamyFacade.changeNodeBoundary(diagramIndex, nodeAdapter, boundary);
		
		// ベンドポイントの移動
		shiftBendpoints(false);
		
		// 負領域に移動した際、全体を移動させ、すべて正領域に
		shiftPosition(false);
	}
	
	@Override
	public void undo() {
		JiemamyViewFacade jiemamyFacade = getJiemamyFacade();
		jiemamyFacade.rollback(save);
	}
	
	private void shiftBendpoints(boolean positive) {
		JmPoint delta = JmPointUtil.delta(oldBoundary, boundary);
		
		// 選択されたモデルのリストを得る
		List<Object> selectedModels = new ArrayList<Object>();
		for (Object obj : viewer.getSelectedEditParts()) {
			EditPart ep = (EditPart) obj;
			selectedModels.add(ep.getModel());
		}
		
		// ベンドポイントも同時に移動させる（必要なもののみ）
		for (ConnectionModel connection : nodeAdapter.getSourceConnections()) {
			if (selectedModels.contains(connection.getSource()) && selectedModels.contains(connection.getTarget())) {
				JiemamyViewFacade jiemamyFacade = getJiemamyFacade();
				DiagramFacet diagramPresentations = rootModel.getFacet(DiagramFacet.class);
				DiagramModel presentationModel = diagramPresentations.getDiagrams().get(diagramIndex);
				List<JmPoint> bendpoints = presentationModel.getConnectionProfiles().get(connection).getBendpoints();
				for (JmPoint bendpoint : bendpoints) {
					int bendpointIndex = bendpoints.indexOf(bendpoint);
					JmPoint newLocation;
					if (positive) {
						newLocation = JmPointUtil.shiftPositive(bendpoint, delta);
					} else {
						newLocation = JmPointUtil.shiftNegative(bendpoint, delta);
					}
					jiemamyFacade.moveBendpoint(diagramIndex, connection, bendpointIndex, newLocation);
				}
			}
		}
	}
}

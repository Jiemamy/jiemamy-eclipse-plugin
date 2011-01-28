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

import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.Entity;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.DefaultConnectionModel;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DefaultNodeModel;
import org.jiemamy.model.geometory.JmPoint;
import org.jiemamy.model.geometory.JmPointUtil;
import org.jiemamy.model.geometory.JmRectangle;
import org.jiemamy.utils.LogMarker;

/**
 * ノードの位置・サイズ変更GEFコマンド。
 * 
 * @author daisuke
 */
public class ChangeNodeConstraintCommand extends AbstractMovePositionCommand {
	
	private static Logger logger = LoggerFactory.getLogger(ChangeNodeConstraintCommand.class);
	
	private final JiemamyContext context;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private final int diagramIndex;
	
	private final DefaultNodeModel nodeModel;
	
	private final JmRectangle boundary;
	
	private final JmRectangle oldBoundary;
	
	private final EditPartViewer viewer;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param nodeModel 操作対象ノード
	 * @param boundary 新しい位置サイズ
	 * @param viewer ビューア
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ChangeNodeConstraintCommand(JiemamyContext context, int diagramIndex, DefaultNodeModel nodeModel,
			JmRectangle boundary, EditPartViewer viewer) {
		Validate.notNull(context);
		Validate.notNull(nodeModel);
		Validate.notNull(boundary);
		Validate.notNull(viewer);
		
		this.context = context;
		this.diagramIndex = diagramIndex;
		this.nodeModel = nodeModel;
		oldBoundary = nodeModel.getBoundary();
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
	 * @param context ルートモデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param nodeModel 操作対象ノード
	 * @param rectangle 新しい位置サイズ
	 * @param viewer ビューア
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ChangeNodeConstraintCommand(JiemamyContext context, int diagramIndex, DefaultNodeModel nodeModel,
			Rectangle rectangle, EditPartViewer viewer) {
		this(context, diagramIndex, nodeModel, ConvertUtil.convert(rectangle), viewer);
	}
	
	@Override
	public void execute() {
		logger.debug(LogMarker.LIFECYCLE, "execute");
		nodeModel.setBoundary(boundary);
		
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(diagramIndex);
		diagramModel.store(nodeModel);
		
		// ベンドポイントの移動
		shiftBendpoints(false, diagramModel);
		
		// 負領域に移動した際、全体を移動させ、すべて正領域に
		shiftPosition(false, diagramModel);
		
		facet.store(diagramModel);
	}
	
	@Override
	public void undo() {
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		DefaultDiagramModel diagramModel = (DefaultDiagramModel) facet.getDiagrams().get(diagramIndex);
		
		nodeModel.setBoundary(oldBoundary);
		
		// ベンドポイントの移動
		shiftBendpoints(true, diagramModel);
		
		diagramModel.store(nodeModel);
		facet.store(diagramModel);
	}
	
	private void shiftBendpoints(boolean positive, DefaultDiagramModel diagramModel) {
		JmPoint delta = JmPointUtil.delta(oldBoundary, boundary);
		
		// 選択しているノードに対応するモデルのリストを得る
		Collection<EntityRef<? extends Entity>> selectedModels = Lists.newArrayList();
		for (Object obj : viewer.getSelectedEditParts()) {
			EditPart ep = (EditPart) obj;
			Entity model = (Entity) ep.getModel();
			selectedModels.add(model.toReference());
		}
		
		// ベンドポイントも同時に移動させる（必要なもののみ）
		for (ConnectionModel connection : diagramModel.getSourceConnectionsFor(nodeModel.toReference())) {
			if (selectedModels.contains(connection.getSource()) && selectedModels.contains(connection.getTarget())) {
				List<JmPoint> bendpoints = connection.getBendpoints();
				for (int i = 0; i < bendpoints.size(); i++) {
					JmPoint bendpoint = bendpoints.get(i);
					JmPoint newLocation;
					if (positive) {
						newLocation = JmPointUtil.shiftPositive(bendpoint, delta);
					} else {
						newLocation = JmPointUtil.shiftNegative(bendpoint, delta);
					}
					((DefaultConnectionModel) connection).breachEncapsulationOfBendpoints().set(i, newLocation);
				}
				diagramModel.store(connection);
			}
		}
	}
}

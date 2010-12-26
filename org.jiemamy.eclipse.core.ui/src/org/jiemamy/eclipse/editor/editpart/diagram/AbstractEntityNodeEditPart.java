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
package org.jiemamy.eclipse.editor.editpart.diagram;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.figure.EntityFigure;
import org.jiemamy.eclipse.editor.tools.NodeCellEditorLocator;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.geometory.JmRectangle;
import org.jiemamy.utils.LogMarker;

/**
 * {@link DatabaseObjectModel}のNodeに対するDiagram用EditPart（コントローラ）の抽象クラス。
 * 
 * @author daisuke
 */
public abstract class AbstractEntityNodeEditPart extends AbstractJmNodeEditPart {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractEntityNodeEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param nodeAdapter コントロール対象のノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public AbstractEntityNodeEditPart(NodeModel nodeAdapter) {
		super(nodeAdapter);
	}
	
	public JiemamyElement getTargetModel() {
		NodeModel node = getModel();
		DatabaseObjectModel entityModel = node.unwrap();
		return entityModel;
	}
	
	@Override
	protected DirectEditManager getDirectEditManager() {
		EntityFigure figure = (EntityFigure) getFigure();
		CellEditorLocator locator = new NodeCellEditorLocator(figure.getEntityNameLabel());
		return new EntityDirectEditManager(this, TextCellEditor.class, locator);
	}
	
	@Override
	protected void refreshVisuals() {
		logger.debug(LogMarker.LIFECYCLE, "refreshVisuals");
		super.refreshVisuals();
		GraphicalEditPart editPart = (GraphicalEditPart) getParent();
		if (editPart == null) {
			// モデルが削除された場合にeditPart=nullとなる。その時は描画処理は行わない。
			return;
		}
		
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		DiagramFacet diagramPresentations = rootModel.getAdapter(DiagramFacet.class);
		DiagramModel presentation = diagramPresentations.get(Migration.DIAGRAM_INDEX);
		NodeModel node = getModel();
		NodeProfile nodeProfile = presentation.getNodeProfiles().get(node);
		if (nodeProfile == null) {
			// 表示しない
		} else {
			JmRectangle boundary = nodeProfile.getBoundary();
			editPart.setLayoutConstraint(this, getFigure(), ConvertUtil.convert(boundary));
		}
		updateFigure(getFigure());
	}
}

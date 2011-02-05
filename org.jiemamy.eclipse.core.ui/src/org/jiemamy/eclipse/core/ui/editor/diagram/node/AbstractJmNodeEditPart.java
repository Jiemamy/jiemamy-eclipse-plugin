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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DirectEditManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.diagram.EditDialogSupport;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.DbObject;
import org.jiemamy.model.JmConnection;
import org.jiemamy.model.JmNode;
import org.jiemamy.model.SimpleDbObjectNode;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.SimpleJmNode;
import org.jiemamy.model.geometory.JmRectangle;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.transaction.StoredEventListener;
import org.jiemamy.utils.LogMarker;

/**
 * {@link JmNode}に対するDiagram用EditPart（コントローラ）の抽象クラス。
 * 
 * @author daisuke
 */
public abstract class AbstractJmNodeEditPart extends AbstractGraphicalEditPart implements EditDialogSupport,
		NodeEditPart, StoredEventListener {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractJmNodeEditPart.class);
	
	private DirectEditManager directManager = null;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param nodeModel コントロール対象のノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public AbstractJmNodeEditPart(JmNode nodeModel) {
		Validate.notNull(nodeModel);
		setModel(nodeModel);
	}
	
	@Override
	public void activate() {
		super.activate();
		getJiemamyContext().getEventBroker().addListener(this);
		logger.trace(LogMarker.LIFECYCLE, "activated");
	}
	
	public void commandExecuted(StoredEvent<?> event) {
		// THINK どのメソッドを呼ばなければならないのか精査
		refresh();
	}
	
	@Override
	public void deactivate() {
		getJiemamyContext().getEventBroker().removeListener(this);
		super.deactivate();
		logger.trace(LogMarker.LIFECYCLE, "deactivated");
	}
	
//	@Override
//	// Java1.4対応APIのため、Classに型パラメータをつけることができない
//	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
//		logger.debug(key.getName());
//		JmNode node = getModel();
//		
//		JiemamyContext context = getJiemamyContext();
//		
//		if (node.getCoreModelRef() != null) {
//			DbObject entityModel = context.resolve(node.getCoreModelRef());
//			if (entityModel.hasAdapter(key)) {
//				return entityModel.getAdapter(key);
//			}
//		}
//		return super.getAdapter(key);
//	}
	
	@Override
	public JmNode getModel() {
		return (JmNode) super.getModel();
	}
	
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}
	
	public ConnectionAnchor getSourceConnectionAnchor(Request connection) {
		return new ChopboxAnchor(getFigure());
	}
	
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}
	
	public ConnectionAnchor getTargetConnectionAnchor(Request connection) {
		return new ChopboxAnchor(getFigure());
	}
	
	@Override
	public void performRequest(Request req) {
		logger.info(LogMarker.LIFECYCLE, "Incoming GEF Request: " + req.getType());
		// Requestがモデル・プロパティの直接編集を要求するものかどうか
		if (req.getType().equals(RequestConstants.REQ_DIRECT_EDIT)) {
			performDirectEdit();
			return;
		} else if (req.getType().equals(RequestConstants.REQ_OPEN)) {
			openEditDialog();
			return;
		}
		super.performRequest(req);
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof SimpleJmNode) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * {@link DirectEditManager} （直接編集マネージャ）を取得する。
	 * 
	 * @return {@link DirectEditManager}
	 */
	protected DirectEditManager createDirectEditManager() {
		return null;
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new JmComponentEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new JmGraphicalNodeEditPolicy());
		// TODO direct-edit
//		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new JmDirectEditPolicy());
	}
	
	/**
	 * コンテキストを取得する。
	 * 
	 * @return {@link JiemamyContext}
	 */
	protected JiemamyContext getJiemamyContext() {
		return (JiemamyContext) getRoot().getContents().getModel();
	}
	
	@Override
	protected List<JmConnection> getModelSourceConnections() {
		if (getParent() == null) {
			return Collections.emptyList();
		}
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagramModel = (SimpleJmDiagram) facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		
		if (diagramModel.contains(getModel().toReference()) == false) {
			return Collections.emptyList();
		}
		
		List<JmConnection> result = Lists.newArrayList(diagramModel.getSourceConnectionsFor(getModel().toReference()));
		
		// 以下ログのためのロジック
		JmNode model = getModel();
		if (model instanceof SimpleDbObjectNode) {
			SimpleDbObjectNode databaseObjectJmNode = (SimpleDbObjectNode) model;
			DbObject core = context.resolve(databaseObjectJmNode.getCoreModelRef());
			logger.debug(core + " sourceConnections = " + result);
		} else {
			logger.debug(model + " sourceConnections = " + result);
		}
		return result;
	}
	
	@Override
	protected List<JmConnection> getModelTargetConnections() {
		if (getParent() == null) {
			return Collections.emptyList();
		}
		JiemamyContext context = (JiemamyContext) getRoot().getContents().getModel();
		DiagramFacet facet = context.getFacet(DiagramFacet.class);
		SimpleJmDiagram diagramModel = (SimpleJmDiagram) facet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		
		if (diagramModel.contains(getModel().toReference()) == false) {
			return Collections.emptyList();
		}
		
		List<JmConnection> result = Lists.newArrayList(diagramModel.getTargetConnectionsFor(getModel().toReference()));
		
		// 以下ログのためのロジック
		JmNode model = getModel();
		if (model instanceof SimpleDbObjectNode) {
			SimpleDbObjectNode databaseObjectJmNode = (SimpleDbObjectNode) model;
			DbObject core = context.resolve(databaseObjectJmNode.getCoreModelRef());
			logger.debug(core + " sourceConnections = " + result);
		} else {
			logger.debug(model + " sourceConnections = " + result);
		}
		return result;
	}
	
	@Override
	protected void refreshVisuals() {
		logger.debug(LogMarker.LIFECYCLE, "refreshVisuals");
		super.refreshVisuals();
		GraphicalEditPart editPart = (GraphicalEditPart) getParent();
		if (editPart == null) {
			// モデルが削除された場合にeditPart==nullとなる。その時は描画処理は行わない。
			return;
		}
		
		JmNode node = getModel();
		JmRectangle boundary = node.getBoundary();
		editPart.setLayoutConstraint(this, getFigure(), ConvertUtil.convert(boundary));
		updateFigure(getFigure());
	}
	
	/**
	 * ビュー（Figure）を更新する。
	 * 
	 * @param figure 更新するFigure
	 */
	protected abstract void updateFigure(IFigure figure);
	
	private void performDirectEdit() {
		if (directManager == null) {
			directManager = createDirectEditManager();
		}
		if (directManager == null) {
			return;
		}
		// セル・エディタの表示
		directManager.show();
	}
	
}

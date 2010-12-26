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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DirectEditManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.Migration;
import org.jiemamy.eclipse.editor.editpart.EditDialogSupport;
import org.jiemamy.eclipse.editor.editpolicy.JmComponentEditPolicy;
import org.jiemamy.eclipse.editor.editpolicy.JmDirectEditPolicy;
import org.jiemamy.eclipse.editor.editpolicy.JmGraphicalNodeEditPolicy;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.DefaultNodeModel;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.dbo.DatabaseObjectModel;
import org.jiemamy.transaction.CommandListener;
import org.jiemamy.utils.LogMarker;
import org.jiemamy.utils.collection.CollectionsUtil;

/**
 * {@link NodeModel}に対するDiagram用EditPart（コントローラ）の抽象クラス。
 * @author daisuke
 */
public abstract class AbstractJmNodeEditPart extends AbstractGraphicalEditPart implements EditDialogSupport,
		NodeEditPart, CommandListener {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractJmNodeEditPart.class);
	
	private DirectEditManager directManager = null;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param nodeAdapter コントロール対象のノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public AbstractJmNodeEditPart(NodeModel nodeAdapter) {
		Validate.notNull(nodeAdapter);
		setModel(nodeAdapter);
	}
	
	@Override
	public void activate() {
		super.activate();
		
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		rootModel.getEventBroker().addListener(this);
		logger.debug("activate");
	}
	
	public void commandExecuted(Command command) {
		// THINK どのメソッドを呼ばなければならないのか精査
		refresh();
		refreshVisuals();
	}
	
	@Override
	public void deactivate() {
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		rootModel.getEventBroker().removeListener(this);
		
		super.deactivate();
		logger.debug("deactivate");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	// Java1.4対応APIのため、Classに型パラメータをつけることができない
	public Object getAdapter(Class key) {
		logger.debug(key.getName());
		NodeModel nodeAdapter = getModel();
		if (nodeAdapter.unwrap() != null) {
			DatabaseObjectModel entityModel = nodeAdapter.unwrap();
			if (entityModel.hasAdapter(key)) {
				return entityModel.getAdapter(key);
			}
		}
		return super.getAdapter(key);
	}
	
	@Override
	public DefaultNodeModel getModel() {
		return (DefaultNodeModel) super.getModel();
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
		if (model instanceof DefaultNodeModel) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new JmComponentEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new JmGraphicalNodeEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new JmDirectEditPolicy());
	}
	
	/**
	 * DirectEditManager（直接編集マネージャ）を取得する。
	 * @return DirectEditManager
	 */
	protected abstract DirectEditManager getDirectEditManager();
	
	@Override
	protected List<ConnectionModel> getModelSourceConnections() {
		if (getParent() == null) {
			return Collections.emptyList();
		}
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		DiagramFacet diagramPresentations = rootModel.getFacet(DiagramFacet.class);
		DiagramModel diagramPresentationModel = diagramPresentations.getDiagrams().get(Migration.DIAGRAM_INDEX);
		
		List<ConnectionModel> result = CollectionsUtil.newArrayList();
		Collection<? extends ConnectionModel> connections = getModel().getSourceConnections();
		for (ConnectionModel connectionAdapter : connections) {
			// FIXME
//			if (connectionProfiles.containsKey(connectionAdapter)) {
//				result.add(connectionAdapter);
//			}
		}
		logger.debug(getModel() + " sourceConnections = " + result);
		return result;
	}
	
	@Override
	protected List<ConnectionModel> getModelTargetConnections() {
		if (getParent() == null) {
			return Collections.emptyList();
		}
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		DiagramFacet diagramPresentations = rootModel.getFacet(DiagramFacet.class);
		DiagramModel diagramPresentationModel = diagramPresentations.getDiagrams().get(Migration.DIAGRAM_INDEX);
		
		List<ConnectionModel> result = CollectionsUtil.newArrayList();
		Collection<? extends ConnectionModel> connections = getModel().getTargetConnections();
		for (ConnectionModel connectionAdapter : connections) {
			// FIXME
//			if (connectionProfiles.containsKey(connectionAdapter)) {
//				result.add(connectionAdapter);
//			}
		}
		logger.debug(getModel() + " targetConnections = " + result);
		return result;
	}
	
	/**
	 * ビュー（Figure）を更新する。
	 * 
	 * @param figure 更新するFigure
	 */
	protected abstract void updateFigure(IFigure figure);
	
	private void performDirectEdit() {
		if (directManager == null) {
			directManager = getDirectEditManager();
		}
		// セル・エディタの表示
		directManager.show();
	}
	
}

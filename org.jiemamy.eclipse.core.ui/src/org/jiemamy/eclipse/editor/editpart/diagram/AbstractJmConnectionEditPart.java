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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.Migration;
import org.jiemamy.eclipse.editor.editpolicy.JmBendpointEditPolicy;
import org.jiemamy.eclipse.editor.editpolicy.JmConnectionEditPolicy;
import org.jiemamy.model.ConnectionModel;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.geometory.JmPoint;
import org.jiemamy.transaction.CommandListener;

/**
 * Relationに対するDiagram用EditPart（コントローラ）の抽象クラス。
 * 
 * @author daisuke
 */
public abstract class AbstractJmConnectionEditPart extends AbstractConnectionEditPart implements CommandListener {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractJmConnectionEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param connectionAdapter コントロール対象のコネクション
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public AbstractJmConnectionEditPart(ConnectionModel connectionAdapter) {
		Validate.notNull(connectionAdapter);
		setModel(connectionAdapter);
	}
	
	@Override
	public void activate() {
		super.activate();
		ConnectionModel model = getModel();
		model.unwrap().getJiemamy().getEventBroker().addListener(this);
		logger.debug("activate");
	}
	
	public void commandExecuted(Command command) {
		refreshVisuals();
	}
	
	@Override
	public void deactivate() {
		ConnectionModel model = getModel();
		model.unwrap().getJiemamy().getEventBroker().removeListener(this);
		super.deactivate();
		logger.debug("deactivate");
	}
	
	@Override
	public ConnectionModel getModel() {
		return (ConnectionModel) super.getModel();
	}
	
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();
		refreshBendpoints();
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof ConnectionModel) {
			super.setModel(model);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new JmConnectionEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new JmBendpointEditPolicy());
	}
	
	private void refreshBendpoints() {
		if (getParent() == null) {
			return;
		}
		JiemamyContext rootModel = (JiemamyContext) getRoot().getContents().getModel();
		DiagramFacet diagramFacet = rootModel.getFacet(DiagramFacet.class);
		DiagramModel diagramModel = diagramFacet.getDiagrams().get(Migration.DIAGRAM_INDEX);
		ConnectionModel connection = getModel();
		
		if (connection == null) {
			return;
		}
		List<JmPoint> bendpoints = connection.getBendpoints();
//		if (connection.containsKey(connection) == false) {
//			bendpoints.clear();
//		}
		List<AbsoluteBendpoint> constraint = new ArrayList<AbsoluteBendpoint>(bendpoints.size());
		
		for (JmPoint bendpoint : bendpoints) {
			constraint.add(new AbsoluteBendpoint(new Point(bendpoint.x, bendpoint.y)));
		}
		
		getConnectionFigure().setRoutingConstraint(constraint);
	}
}

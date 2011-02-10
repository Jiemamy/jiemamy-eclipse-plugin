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

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.model.JmConnection;
import org.jiemamy.model.geometory.JmPoint;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.transaction.StoredEventListener;

/**
 * Relationに対するDiagram用EditPart（コントローラ）の抽象クラス。
 * 
 * @author daisuke
 */
public abstract class AbstractJmConnectionEditPart extends AbstractConnectionEditPart implements StoredEventListener {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractJmConnectionEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param connectionAdapter コントロール対象のコネクション
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public AbstractJmConnectionEditPart(JmConnection connectionAdapter) {
		Validate.notNull(connectionAdapter);
		setModel(connectionAdapter);
	}
	
	@Override
	public void activate() {
		super.activate();
		getJiemamyContext().getEventBroker().addListener(this);
		logger.trace("activated");
	}
	
	@Override
	public void deactivate() {
		getJiemamyContext().getEventBroker().removeListener(this);
		super.deactivate();
		logger.trace("deactivated");
	}
	
	@Override
	public JmConnection getModel() {
		return (JmConnection) super.getModel();
	}
	
	public void handleStoredEvent(StoredEvent<?> event) {
		// TODO ↓適当です
		refresh();
	}
	
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();
		refreshBendpoints();
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof JmConnection) {
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
	
	/**
	 * コンテキストを取得する。
	 * 
	 * @return {@link JiemamyContext}
	 */
	protected JiemamyContext getJiemamyContext() {
		return (JiemamyContext) getRoot().getContents().getModel();
	}
	
	private void refreshBendpoints() {
		if (getParent() == null) {
			// モデルが削除された場合にeditPart==nullとなる。その時は描画処理は行わない。
			return;
		}
		
		JmConnection connection = getModel();
		if (connection == null) {
			return;
		}
		
		List<JmPoint> bendpoints = connection.getBendpoints();
		List<AbsoluteBendpoint> constraint = Lists.newArrayListWithCapacity(bendpoints.size());
		for (JmPoint bendpoint : bendpoints) {
			constraint.add(new AbsoluteBendpoint(new Point(bendpoint.x, bendpoint.y)));
		}
		
		getConnectionFigure().setRoutingConstraint(constraint);
	}
}

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
package org.jiemamy.eclipse.core.ui.editor.editpart.diagram;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.editor.dialog.context.JiemamyContextEditDialog;
import org.jiemamy.eclipse.core.ui.editor.editpart.EditDialogSupport;
import org.jiemamy.eclipse.core.ui.editor.editpolicy.JmXYLayoutEditPolicy;
import org.jiemamy.eclipse.core.ui.preference.JiemamyPreference;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.model.NodeModel;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.transaction.StoredEventListener;
import org.jiemamy.utils.LogMarker;

/**
 * {@link JiemamyContext}に対するDiagram用{@link EditPart}。
 * 
 * @author daisuke
 */
public class JiemamyContextEditPart extends AbstractGraphicalEditPart implements EditDialogSupport,
		IPropertyChangeListener, StoredEventListener {
	
	private static Logger logger = LoggerFactory.getLogger(JiemamyContextEditPart.class);
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param context コントロール対象の{@link JiemamyContext}
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public JiemamyContextEditPart(JiemamyContext context) {
		Validate.notNull(context);
		setModel(context);
	}
	
	@Override
	public void activate() {
		super.activate();
		
		IPreferenceStore ps = JiemamyUIPlugin.getDefault().getPreferenceStore();
		ps.addPropertyChangeListener(this);
		
		JiemamyContext context = getModel();
		context.getEventBroker().addListener(this);
		
		logger.trace(LogMarker.LIFECYCLE, "activated");
	}
	
	public void commandExecuted(StoredEvent command) {
		refresh();
//		JiemamyValidatorUtil.validate(getResource(), (JiemamyContext) getModel());
	}
	
	@Override
	public void deactivate() {
		JiemamyContext context = getModel();
		context.getEventBroker().removeListener(this);
		
		IPreferenceStore ps = JiemamyUIPlugin.getDefault().getPreferenceStore();
		ps.removePropertyChangeListener(this);
		
		super.deactivate();
		logger.trace(LogMarker.LIFECYCLE, "deactivated");
	}
	
	@Override
	public JiemamyContext getModel() {
		return (JiemamyContext) super.getModel();
	}
	
	/**
	 * @see EditDialogSupport#openEditDialog()
	 */
	public void openEditDialog() {
		logger.debug(LogMarker.LIFECYCLE, "openEditDialog");
		JiemamyContext context = getModel();
		
//		// 編集前のスナップショットを保存
//		JiemamyFacade facade = context.newFacade(JiemamyViewFacade.class);
//		SavePoint beforeEditSavePoint = facade.save();
//		
		Shell shell = getViewer().getControl().getShell();
		JiemamyContextEditDialog dialog = new JiemamyContextEditDialog(shell, context);
//		
		if (dialog.open() == Dialog.OK) {
//			// 編集後のスナップショットを保存
//			SavePoint afterEditSavePoint = facade.save();
//			
//			org.eclipse.gef.commands.Command command =
//					new DialogEditCommand(facade, beforeEditSavePoint, afterEditSavePoint);
//			GraphicalViewer viewer = (GraphicalViewer) getViewer();
//			viewer.getEditDomain().getCommandStack().execute(command);
		} else {
//			// 編集前にロールバック
//			facade.rollback(beforeEditSavePoint);
		}
	}
	
	@Override
	public void performRequest(Request req) {
		logger.info(LogMarker.LIFECYCLE, "Incoming GEF Request: " + req.getType());
		super.performRequest(req);
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
		setConnectionRouter(getFigure());
	}
	
	@Override
	public void setModel(Object model) {
		if (model instanceof JiemamyContext == false) {
			throw new IllegalArgumentException();
		}
		super.setModel(model);
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new JmXYLayoutEditPolicy());
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>この実装では、モデル{@link JiemamyContext}に対応するビュー（{@link IFigure}）を
	 * 新しく生成する。対応するビューは {@link Layer} 型であり、XY座標平面上に子要素を配置（{@link XYLayout}）する。
	 * また、{@link JiemamyPreference}に応じて、{@link LayerConstants#CONNECTION_LAYER}に
	 * {@link ConnectionRouter}（コネクションをどのように引き回すか、を表す戦略）を設定する。</p>
	 */
	@Override
	protected IFigure createFigure() {
		Layer figure = new Layer();
		figure.setLayoutManager(new XYLayout());
		
		setConnectionRouter(figure);
		
		return figure;
	}
	
	@Override
	protected List<NodeModel> getModelChildren() {
		JiemamyContext context = getModel();
		DiagramFacet diagramFacet = context.getFacet(DiagramFacet.class);
		DiagramModel diagramModel = diagramFacet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		Collection<? extends NodeModel> nodes = diagramModel.getNodes();
		return Lists.newArrayList(nodes);
	}
	
	private void setConnectionRouter(IFigure figure) {
		JiemamyPreference ps = JiemamyUIPlugin.getPreference();
		ConnectionRouter router = ps.getConnectionRouter().getRouter(figure);
		ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		connLayer.setConnectionRouter(router);
	}
	
}

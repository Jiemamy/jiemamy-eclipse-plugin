/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/07/29
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
package org.jiemamy.eclipse.core.ui.editor;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.AlignmentRetargetAction;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.VersionedIdentifier;
import org.osgi.framework.Bundle;
import org.seasar.eclipse.common.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.TODO;
import org.jiemamy.eclipse.core.ui.utils.EditorUtil;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DiagramModel;
import org.jiemamy.utils.LogMarker;

/**
 * エディタのコントリビュータ。
 * 
 * @author daisuke
 */
public class JmContributor extends ActionBarContributor {
	
	private static Logger logger = LoggerFactory.getLogger(JmContributor.class);
	
	private static final String JIEMAMY_UPDATE_SITE_URL = "http://eclipse.jiemamy.org/release/";
	
	private Combo cmbDisplayStatus;
	

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		// コピペ関連アクションの追加
		toolBarManager.add(getActionRegistry().getAction(ActionFactory.DELETE.getId()));
		toolBarManager.add(getActionRegistry().getAction(ActionFactory.UNDO.getId()));
		toolBarManager.add(getActionRegistry().getAction(ActionFactory.REDO.getId()));
		
		toolBarManager.add(new Separator());
		
		// 水平方向の整列アクションの追加
		toolBarManager.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_LEFT));
		toolBarManager.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_CENTER));
		toolBarManager.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_RIGHT));
		
		toolBarManager.add(new Separator());
		
		// 垂直方向の整列アクションの追加
		toolBarManager.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_TOP));
		toolBarManager.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_MIDDLE));
		toolBarManager.add(getActionRegistry().getAction(GEFActionConstants.ALIGN_BOTTOM));
		
		toolBarManager.add(new Separator());
		
		// ズーム関連アクションの追加
		toolBarManager.add(getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
		toolBarManager.add(getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));
		toolBarManager.add(new ZoomComboContributionItem(getPage()));
		
		toolBarManager.add(new Separator());
		
		// 表示モード切替コンボの追加
		toolBarManager.add(new JiemamyDiagramEditorContribution());
	}
	
	/**
	 * 表示モード設定コンボに、現在の表示モードを設定する。
	 * 
	 * <p>Thanks to Naokiさん</p>
	 * 
	 * @param rootModel 現在編集中のルートモデル
	 */
	public void selectCombo(JiemamyContext rootModel) {
		if (cmbDisplayStatus == null || cmbDisplayStatus.isDisposed()) {
			logger.error("combo is null or disposed");
			return;
		}
		
		DiagramFacet diagramFacet = rootModel.getFacet(DiagramFacet.class);
		DiagramModel presentation = diagramFacet.getDiagrams().get(TODO.DIAGRAM_INDEX);
		for (DisplayStatus displayStatus : DisplayStatus.values()) {
			if (presentation.getMode() == displayStatus.getMode()
					&& presentation.getLevel() == displayStatus.getLevel()) {
				cmbDisplayStatus.select(displayStatus.ordinal());
				return;
			}
		}
		logger.error("matched DisplayStatus is not found");
		cmbDisplayStatus.select(0);
	}
	
	@Override
	protected void buildActions() {
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		addRetargetAction(new DeleteRetargetAction());
		
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
		
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.CENTER));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.RIGHT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.MIDDLE));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.BOTTOM));
	}
	
	@Override
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
	}
	

	private class JiemamyDiagramEditorContribution extends ControlContribution {
		
		/**
		 * インスタンスを生成する。
		 */
		private JiemamyDiagramEditorContribution() {
			super("org.jiemamy.eclipse.displayMode");
		}
		
		/**
		 * 更新サイトをチェックして、アップデートアラートを表示する。
		 * 
		 * @param parent 親コンポーネント
		 */
		@SuppressWarnings({ // CHECKSTYLE IGNORE THIS LINE
			"deprecation",
			"unused"
		})
		// そのうち使う予定…
		public void checkUpdate(Composite parent) {
			try {
				ISite rs = SiteManager.getSite(new URL(JIEMAMY_UPDATE_SITE_URL), new NullProgressMonitor());
				IFeatureReference[] frs = rs.getFeatureReferences();
				VersionedIdentifier frsVi = frs[frs.length - 1].getVersionedIdentifier();
				org.eclipse.core.runtime.PluginVersionIdentifier latestVer = frsVi.getVersion();
				
				Bundle bundle = JiemamyUIPlugin.getDefault().getBundle();
				String version = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
				org.eclipse.core.runtime.PluginVersionIdentifier currentVer =
						new org.eclipse.core.runtime.PluginVersionIdentifier(version);
				
				if (latestVer.isGreaterThan(currentVer)) {
					Label label = new Label(parent, SWT.NONE);
					label.setText("New version is available."); // RESOURCE
				}
			} catch (CoreException e) {
				LogUtil.log(JiemamyUIPlugin.getDefault(), "Network is not connected.");
			} catch (MalformedURLException e) {
				ExceptionHandler.handleException(e);
			}
		}
		
		@Override
		protected Control createControl(Composite parent) {
			cmbDisplayStatus = new Combo(parent, SWT.READ_ONLY);
			for (DisplayStatus mode : DisplayStatus.values()) {
				cmbDisplayStatus.add(mode.getLabel());
			}
			
			cmbDisplayStatus.addSelectionListener(new ComboSelectionListener());
			
			IEditorPart editor = EditorUtil.getActiveEditor();
			if (editor instanceof JiemamyEditor) {
				JiemamyContext rootModel = ((JiemamyEditor) editor).getJiemamyContext();
				if (rootModel != null) {
					selectCombo(rootModel);
				}
			}
			
//			checkUpdate(parent);
			
			return cmbDisplayStatus;
		}
		

		private class ComboSelectionListener extends SelectionAdapter {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug(LogMarker.LIFECYCLE, "DisplayStatus changed");
				int index = cmbDisplayStatus.getSelectionIndex();
				if (index == -1) {
					return;
				}
				IEditorPart editor = EditorUtil.getActiveEditor();
				if ((editor instanceof JiemamyEditor) == false) {
					return;
				}
				JiemamyContext context = ((JiemamyEditor) editor).getJiemamyContext();
				DiagramFacet diagramFacet = context.getFacet(DiagramFacet.class);
				DefaultDiagramModel presentation =
						(DefaultDiagramModel) diagramFacet.getDiagrams().get(TODO.DIAGRAM_INDEX);
				for (DisplayStatus displayStatus : DisplayStatus.values()) {
					if (displayStatus.ordinal() == index) {
						presentation.setMode(displayStatus.getMode());
						presentation.setLevel(displayStatus.getLevel());
						break;
					}
				}
			}
		}
		
	}
	
}

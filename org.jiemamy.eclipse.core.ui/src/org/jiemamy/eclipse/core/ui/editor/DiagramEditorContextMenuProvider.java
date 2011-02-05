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
package org.jiemamy.eclipse.core.ui.editor;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;

import org.jiemamy.composer.ExportConfig;
import org.jiemamy.composer.Exporter;
import org.jiemamy.composer.ImportConfig;
import org.jiemamy.composer.Importer;
import org.jiemamy.eclipse.JiemamyCorePlugin;
import org.jiemamy.eclipse.core.ui.composer.ExporterWizard;
import org.jiemamy.eclipse.core.ui.composer.ImporterWizard;
import org.jiemamy.eclipse.core.ui.editor.action.AutoLayoutAction;
import org.jiemamy.eclipse.core.ui.editor.action.ChangeNodeBackgroundColorAction;
import org.jiemamy.eclipse.core.ui.editor.action.ExportAction;
import org.jiemamy.eclipse.core.ui.editor.action.FitNodeConstraintAction;
import org.jiemamy.eclipse.core.ui.editor.action.ImportAction;
import org.jiemamy.eclipse.core.ui.editor.action.PropertyAction;
import org.jiemamy.eclipse.core.ui.editor.action.SaveDiagramImageAction;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.eclipse.extension.ExtensionResolver;

/**
 * {@link JiemamyDiagramEditor}用のコンテキストメニュー（右クリックメニュー）を提供するプロバイダ実装クラス。
 * 
 * @author daisuke
 */
public class DiagramEditorContextMenuProvider extends ContextMenuProvider {
	
	private final JiemamyDiagramEditor editorPart;
	
	/** The editor's action registry. */
	private final ActionRegistry actionRegistry;
	

	/**
	 * Instantiate a new menu context provider for the specified EditPartViewer and
	 * ActionRegistry.
	 * 
	 * @param viewer the editor's graphical viewer
	 * @param editor the editor
	 * @param registry the editor's action registry
	 */
	public DiagramEditorContextMenuProvider(EditPartViewer viewer, JiemamyDiagramEditor editor, ActionRegistry registry) {
		super(viewer);
		if (registry == null) {
			throw new IllegalArgumentException();
		}
		editorPart = editor;
		actionRegistry = registry;
	}
	
	/**
	 * Called when the context menu is about to show. Actions, whose state is enabled,
	 * will appear in the context menu.
	 * 
	 * @see org.eclipse.gef.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void buildContextMenu(IMenuManager menu) {
		// Add standard action groups to the menu
		GEFActionConstants.addStandardActionGroups(menu);
		
		IAction action;
		GraphicalViewer viewer = (GraphicalViewer) getViewer();
		
		// Add actions to the menu
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO, // target group id
				getAction(ActionFactory.UNDO.getId())); // action to add
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO, getAction(ActionFactory.REDO.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT, getAction(ActionFactory.DELETE.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, getAction(GEFActionConstants.ZOOM_IN));
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, getAction(GEFActionConstants.ZOOM_OUT));
		
		// TODO DirectEditはContextMenuで機能していない。修正せよ。
		action = getAction(GEFActionConstants.DIRECT_EDIT);
		if (action.isEnabled()) {
			menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
		}
		
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new ChangeNodeBackgroundColorAction(viewer));
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new SaveDiagramImageAction(viewer));
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new AutoLayoutAction(viewer));
		
		buildImporterMenu(menu, viewer);
		buildExporterMenu(menu, viewer);
		
		menu.add(new Separator());
		menu.add(new PropertyAction(viewer));
		
		// Alignment Actions
		MenuManager alignMenu = new MenuManager("位置調整(&O)"); // RESOURCE
		action = getAction(GEFActionConstants.ALIGN_LEFT);
		action.setEnabled(true);
		if (action.isEnabled()) {
			alignMenu.add(action);
		}
		action = getAction(GEFActionConstants.ALIGN_CENTER);
		action.setEnabled(true);
		if (action.isEnabled()) {
			alignMenu.add(action);
		}
		action = getAction(GEFActionConstants.ALIGN_RIGHT);
		action.setEnabled(true);
		if (action.isEnabled()) {
			alignMenu.add(action);
		}
		alignMenu.add(new Separator());
		action = getAction(GEFActionConstants.ALIGN_TOP);
		action.setEnabled(true);
		if (action.isEnabled()) {
			alignMenu.add(action);
		}
		action = getAction(GEFActionConstants.ALIGN_MIDDLE);
		action.setEnabled(true);
		if (action.isEnabled()) {
			alignMenu.add(action);
		}
		action = getAction(GEFActionConstants.ALIGN_BOTTOM);
		action.setEnabled(true);
		if (action.isEnabled()) {
			alignMenu.add(action);
		}
		if (alignMenu.isEmpty() == false) {
			menu.appendToGroup(GEFActionConstants.GROUP_REST, alignMenu);
		}
		
		// Match width Actions
		// TODO nodeでないときはdisableにする。 hint: getViewer().getSelectedEditParts()を使う？
		MenuManager matchSizeMenu = new MenuManager("サイズ調整(&Z)"); // RESOURCE
		matchSizeMenu.add(new FitNodeConstraintAction(viewer));
		
		action = getAction(GEFActionConstants.MATCH_HEIGHT);
		action.setEnabled(true);
		if (action.isEnabled()) {
			matchSizeMenu.add(action);
		}
		action = getAction(GEFActionConstants.MATCH_WIDTH);
		action.setEnabled(true);
		if (action.isEnabled()) {
			matchSizeMenu.add(action);
		}
		if (matchSizeMenu.isEmpty() == false) {
			menu.appendToGroup(GEFActionConstants.GROUP_REST, matchSizeMenu);
		}
	}
	
	private void buildExporterMenu(IMenuManager menu, GraphicalViewer viewer) {
		ExtensionResolver<Exporter<ExportConfig>> exporterResolver = JiemamyCorePlugin.getExporterResolver();
		Map<String, IConfigurationElement> configurationElements = exporterResolver.getExtensionConfigurationElements();
		if (configurationElements.size() > 0) {
			MenuManager exportMenu = new MenuManager("エクスポート(&E)"); // RESOURCE
			for (IConfigurationElement exporterElement : configurationElements.values()) {
				registerExporterToMenu(viewer, exportMenu, exporterElement);
			}
			menu.add(exportMenu);
		}
	}
	
	private void buildImporterMenu(IMenuManager menu, GraphicalViewer viewer) {
		ExtensionResolver<Importer<ImportConfig>> importerResolver = JiemamyCorePlugin.getImporterResolver();
		Map<String, IConfigurationElement> configurationElements = importerResolver.getExtensionConfigurationElements();
		if (configurationElements.size() > 0) {
			MenuManager importMenu = new MenuManager("インポート(&I)"); // RESOURCE
			for (IConfigurationElement importerElement : configurationElements.values()) {
				registerImporterToMenu(viewer, importMenu, importerElement);
			}
			menu.add(importMenu);
		}
	}
	
	private IAction getAction(String actionId) {
		return getActionRegistry().getAction(actionId);
	}
	
	private ActionRegistry getActionRegistry() {
		return actionRegistry;
	}
	
	private void registerExporterToMenu(GraphicalViewer viewer, MenuManager menu, IConfigurationElement element) {
		try {
			@SuppressWarnings("unchecked")
			Exporter<ExportConfig> exporter = (Exporter<ExportConfig>) element.createExecutableExtension("class");
			@SuppressWarnings("unchecked")
			ExporterWizard<Exporter<ExportConfig>, ExportConfig> wizard =
					(ExporterWizard<Exporter<ExportConfig>, ExportConfig>) element.createExecutableExtension("wizard");
			menu.add(new ExportAction(exporter, wizard, viewer, editorPart));
		} catch (ClassCastException e) {
			ExceptionHandler.handleException(e);
		} catch (CoreException e) {
			ExceptionHandler.handleException(e);
		}
	}
	
	private void registerImporterToMenu(GraphicalViewer viewer, MenuManager menu, IConfigurationElement element) {
		try {
			@SuppressWarnings("unchecked")
			Importer<ImportConfig> importer = (Importer<ImportConfig>) element.createExecutableExtension("class");
			@SuppressWarnings("unchecked")
			ImporterWizard<Importer<ImportConfig>, ImportConfig> wizard =
					(ImporterWizard<Importer<ImportConfig>, ImportConfig>) element.createExecutableExtension("wizard");
			menu.add(new ImportAction(importer, wizard, viewer, editorPart));
		} catch (ClassCastException e) {
			ExceptionHandler.handleException(e);
		} catch (CoreException e) {
			ExceptionHandler.handleException(e);
		}
	}
}

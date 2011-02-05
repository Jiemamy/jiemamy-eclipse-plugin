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
package org.jiemamy.eclipse.core.ui.editor.action;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.composer.ImportConfig;
import org.jiemamy.composer.ImportException;
import org.jiemamy.composer.Importer;
import org.jiemamy.eclipse.core.ui.composer.ImporterWizard;
import org.jiemamy.eclipse.core.ui.editor.JiemamyEditor;
import org.jiemamy.eclipse.core.ui.editor.diagram.JiemamyContextEditPart;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.utils.LogMarker;

/**
 * UI付きimportアクションクラス。
 * 
 * @author daisuke
 */
public class ImportAction extends AbstractJiemamyAction {
	
	private static Logger logger = LoggerFactory.getLogger(ImportAction.class);
	
	private final Importer<ImportConfig> importer;
	
	private final ImporterWizard<Importer<ImportConfig>, ImportConfig> wizard;
	
	private final JiemamyEditor editor;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param importer このアクションで実行されるインポータ
	 * @param wizard 実行前にパラメータを設定するためのウィザード
	 * @param viewer ビューア
	 * @param editor エディタ
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ImportAction(Importer<ImportConfig> importer, ImporterWizard<Importer<ImportConfig>, ImportConfig> wizard,
			GraphicalViewer viewer, JiemamyEditor editor) {
		super(importer == null ? null : importer.getName(), viewer);
		
		Validate.notNull(importer);
		Validate.notNull(wizard);
		Validate.notNull(viewer);
		Validate.notNull(editor);
		
		this.importer = importer;
		this.wizard = wizard;
		this.editor = editor;
		
		assert importer != null;
		logger.debug(LogMarker.LIFECYCLE, "instanciated " + importer.getName());
	}
	
	@Override
	public void run() {
		logger.debug(LogMarker.LIFECYCLE, "run " + importer.getName());
		GraphicalViewer viewer = getViewer();
		JiemamyContextEditPart contextEditPart = (JiemamyContextEditPart) viewer.getContents();
		JiemamyContext context = contextEditPart.getModel();
		IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
		wizard.setInput(input);
		
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		List<Object> selectedModels = Lists.newArrayList();
		for (Object selectedObject : selection.toList()) {
			if (selectedObject instanceof EditPart) {
				EditPart editPart = (EditPart) selectedObject;
				selectedModels.add(editPart.getModel());
			}
		}
		
		Shell shell = viewer.getControl().getShell();
		try {
			// パラメータを取得
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() != Window.OK) {
				logger.debug(LogMarker.LIFECYCLE, "canceled " + importer.getName());
				return;
			}
			ImportConfig config = wizard.getConfig();
			if (config != null) {
				// 実行
				boolean success = importer.importModel(context, config);
				
				if (success) {
					contextEditPart.refresh();
					new AutoLayoutAction(viewer).run();
					MessageDialog.openInformation(shell, "import succeeded", "インポートが正常に完了しました。"); // RESOURCE
				} else {
					MessageDialog.openWarning(shell, "import aborted", "インポートは行われませんでした。"); // RESOURCE
				}
			}
		} catch (ImportException e) {
			ExceptionHandler.handleException(e.getCause());
		} finally {
			// リフレッシュ
			try {
				ResourcesPlugin.getWorkspace().getRoot()
					.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (CoreException e) {
				ExceptionHandler.handleException(e);
			}
			
		}
	}
}

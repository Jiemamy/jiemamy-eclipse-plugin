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

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.SystemUtils;
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
import org.jiemamy.composer.ExportConfig;
import org.jiemamy.composer.ExportException;
import org.jiemamy.composer.Exporter;
import org.jiemamy.composer.FileExportConfig;
import org.jiemamy.eclipse.core.ui.composer.ExporterWizard;
import org.jiemamy.eclipse.core.ui.editor.JiemamyEditor;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.utils.LogMarker;

/**
 * UI付きexportアクションクラス。
 * 
 * @author daisuke
 */
public class ExportAction extends AbstractJiemamyAction {
	
	private static Logger logger = LoggerFactory.getLogger(ExportAction.class);
	
	private final Exporter<ExportConfig> exporter;
	
	private final ExporterWizard<Exporter<ExportConfig>, ExportConfig> wizard;
	
	private final JiemamyEditor editor;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param exporter このアクションで実行されるエクスポータ
	 * @param wizard 実行前にパラメータを設定するためのウィザード
	 * @param viewer ビューア
	 * @param editor エディタ
	 * @throws IllegalArgumentException 引数exporterに{@code null}を与えた場合
	 * @throws IllegalArgumentException その他引数に{@code null}を与えた場合
	 */
	@SuppressWarnings("null")
	public ExportAction(Exporter<ExportConfig> exporter, ExporterWizard<Exporter<ExportConfig>, ExportConfig> wizard,
			GraphicalViewer viewer, JiemamyEditor editor) {
		super(exporter == null ? null : exporter.getName(), viewer);
		
		Validate.notNull(exporter);
		Validate.notNull(wizard);
		Validate.notNull(viewer);
		Validate.notNull(editor);
		
		this.exporter = exporter;
		this.wizard = wizard;
		this.editor = editor;
		
		assert exporter != null;
		logger.debug(LogMarker.LIFECYCLE, "instanciated " + exporter.getName());
	}
	
	@Override
	public void run() {
		logger.debug(LogMarker.LIFECYCLE, "run " + exporter.getName());
		JiemamyContext context = (JiemamyContext) getViewer().getContents().getModel();
		IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
		wizard.setInput(input);
		
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		List<Object> selectedModels = Lists.newArrayList();
		for (Object selectedObject : selection.toList()) {
			if (selectedObject instanceof EditPart) {
				EditPart editPart = (EditPart) selectedObject;
				selectedModels.add(editPart.getModel());
			}
		}
		
		Shell shell = getViewer().getControl().getShell();
		try {
			// パラメータを取得
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() != Window.OK) {
				logger.debug(LogMarker.LIFECYCLE, "canceled " + exporter.getName());
				return;
			}
			ExportConfig config = wizard.getConfig();
			
			// 実行
			boolean success = exporter.exportModel(context, config);
			
			if (success) {
				if (SystemUtils.IS_OS_WINDOWS && config instanceof FileExportConfig) {
					FileExportConfig fileExportConfig = (FileExportConfig) config;
					boolean result = MessageDialog.openQuestion(null, "Success", "エクスポートが完了しました。ファイルを開きますか？"); // RESOURCE
					if (result) {
						try {
							Runtime.getRuntime().exec(
									"cmd /c \"" + fileExportConfig.getOutputFile().getAbsolutePath() + "\"");
						} catch (IOException e) {
							MessageDialog.openError(shell, "Failed", "ファイルが開けませんでした。"); // RESOURCE
						}
					}
				} else {
					MessageDialog.openInformation(shell, "export succeeded", "エクスポートが正常に完了しました。"); // RESOURCE
				}
			} else {
				MessageDialog.openWarning(shell, "export aborted", "エクスポートは行われませんでした。"); // RESOURCE
			}
		} catch (ExportException e) {
			MessageDialog.openError(shell, "export error", e.getMessage());
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

/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/25
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
package org.jiemamy.eclipse.core.ui.composer;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import org.jiemamy.JiemamyContext;
import org.jiemamy.composer.exporter.SimpleSqlExportConfig;
import org.jiemamy.composer.exporter.SqlExportConfig;
import org.jiemamy.composer.exporter.SqlExporter;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.JiemamyEditor;
import org.jiemamy.eclipse.core.ui.utils.EditorUtil;
import org.jiemamy.eclipse.core.ui.utils.FileSelectWizardPage;
import org.jiemamy.model.dataset.JmDataSet;

/**
 * {@link SqlExporter}の設定取得ウィザード。
 * 
 * @author daisuke
 */
public class SqlExporterWizard extends Wizard implements ExporterWizard<SqlExporter, SqlExportConfig>, IWorkbenchWizard {
	
	private FileSelectWizardPage page1;
	
	private SqlExporterWizardPage page2;
	
	private SimpleSqlExportConfig config;
	

	/**
	 * Creates a wizard for exporting SQL to the local file system.
	 */
	public SqlExporterWizard() {
		IDialogSettings workbenchSettings = JiemamyUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("ExportWizard"); // $NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("ExportWizard"); // $NON-NLS-1$
		}
		setDialogSettings(section);
	}
	
	@Override
	public void addPages() {
		IEditorPart activeEditor = EditorUtil.getActiveEditor();
		List<String> dataSetNames = Lists.newArrayList();
		if (activeEditor instanceof JiemamyEditor) {
			JiemamyEditor jiemamyEditor = (JiemamyEditor) activeEditor;
			JiemamyContext context = jiemamyEditor.getJiemamyContext();
			List<JmDataSet> dataSets = context.getDataSets();
			for (JmDataSet dataSet : dataSets) {
				dataSetNames.add(dataSet.getName());
			}
		}
		super.addPages();
		
		// RESOURCE
		page1 = new FileSelectWizardPage("出力ファイル選択", "出力ファイル選択", (ImageDescriptor) null, new String[] {
			"SQLファイル(.sql)",
			"すべて"
		}, new String[] {
			"*.sql",
			"*.*"
		});
		addPage(page1);
		
		page2 = new SqlExporterWizardPage(dataSetNames);
		addPage(page2);
	}
	
	public SqlExportConfig getConfig() {
		return config;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		setWindowTitle("SQLにエクスポート"); // RESOURCE
//		setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportdir_wiz.png"));
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public boolean performFinish() {
		config = new SimpleSqlExportConfig();
		config.setOutputFile(new File(page1.getPath()));
		config.setOverwrite(page1.getOverwrite());
		config.setDataSetIndex(page2.getDataSetIndex());
		config.setEmitDropStatements(page2.getEmitDropStatements());
		config.setEmitCreateSchema(page2.getEmitCreateSchema());
		return true;
	}
	
	public void setInput(IFileEditorInput input) {
		// nothing to do
	}
	
}

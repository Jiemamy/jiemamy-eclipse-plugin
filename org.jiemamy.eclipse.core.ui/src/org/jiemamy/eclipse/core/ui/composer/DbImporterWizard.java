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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import org.jiemamy.composer.importer.DbImportConfig;
import org.jiemamy.composer.importer.DbImporter;
import org.jiemamy.composer.importer.SimpleDbImportConfig;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;

/**
 * {@link DbImporter}の設定取得ウィザード。
 * 
 * @author daisuke
 */
public class DbImporterWizard extends Wizard implements ImporterWizard<DbImporter, DbImportConfig>,
		IWorkbenchWizard {
	
	private DbImporterWizardPage page;
	
	private SimpleDbImportConfig config;
	
	private IDialogSettings settings;
	

	/**
	 * Creates a wizard for exporting SQL to the local file system.
	 */
	public DbImporterWizard() {
		IDialogSettings workbenchSettings = JiemamyUIPlugin.getDefault().getDialogSettings();
		settings = workbenchSettings.getSection("ImportWizard");
		if (settings == null) {
			settings = workbenchSettings.addNewSection("ImportWizard"); // $NON-NLS-1$
		}
		setDialogSettings(settings);
	}
	
	@Override
	public void addPages() {
		super.addPages();
		page = new DbImporterWizardPage(settings);
		addPage(page);
	}
	
	public DbImportConfig getConfig() {
		return config;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		setWindowTitle(Messages.DbImportWizard_title);
//		setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportdir_wiz.png"));
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public boolean performFinish() {
		config = new SimpleDbImportConfig();
		
		config.setDriverJarPaths(page.getDriverJarPaths());
		config.setDriverClassName(page.getDriverClassName());
		
		config.setUri(page.getUri());
		config.setUsername(page.getUsername());
		config.setPassword(page.getPassword());
		
		config.setDialect(page.getDialect());
		config.setSchema(page.getSchema());
		
		config.setImportDataSet(page.isImportDataSet());
		
		return true;
	}
	
	public void setInput(IFileEditorInput input) {
		// nothing to do
	}
	
}

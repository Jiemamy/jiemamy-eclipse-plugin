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

import org.jiemamy.composer.importer.DatabaseImportConfig;
import org.jiemamy.composer.importer.DatabaseImporter;
import org.jiemamy.composer.importer.DefaultDatabaseImportConfig;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;

/**
 * {@link DatabaseImporter}の設定取得ウィザード。
 * 
 * @author daisuke
 */
public class DatabaseImportWizard extends Wizard implements ImporterWizard<DatabaseImporter, DatabaseImportConfig>,
		IWorkbenchWizard {
	
	private DatabaseImportWizardPage page;
	
	private DefaultDatabaseImportConfig config;
	
	private IDialogSettings settings;
	

	/**
	 * Creates a wizard for exporting SQL to the local file system.
	 */
	public DatabaseImportWizard() {
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
		page = new DatabaseImportWizardPage(settings);
		addPage(page);
	}
	
	public DatabaseImportConfig getConfig() {
		return config;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		setWindowTitle(Messages.DatabaseImportWizard_title);
//		setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportdir_wiz.png"));
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public boolean performFinish() {
		config = new DefaultDatabaseImportConfig();
		
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

/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.SimpleJmMetadata;
import org.jiemamy.SqlFacet;
import org.jiemamy.dialect.GenericDialect;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.serializer.JiemamySerializer;

/**
 * 新規ダイアグラムファイル作成ウィザード。
 * 
 * @author daisuke
 */
public final class NewDiagramWizard extends Wizard implements INewWizard {
	
	private NewDiagramWizardPage page;
	
	private IWorkbench workbench;
	
	private IStructuredSelection selection;
	
	
	/**
	 * Instantiates a new new diagram wizard.
	 */
	public NewDiagramWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle("New Jiemamy Database Diagram"); // RESOURCE
		
		IDialogSettings workbenchSettings = JiemamyUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("NewDiagramWizard"); // $NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("NewDiagramWizard"); // $NON-NLS-1$
		}
		setDialogSettings(section);
	}
	
	@Override
	public void addPages() {
		page = new NewDiagramWizardPage(selection);
		addPage(page);
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;
	}
	
	@Override
	public boolean performFinish() {
		try {
			if (page.getFileExtension() == null) {
				page.setFileExtension(JiemamySerializer.FILE_EXT);
			}
			IFile file = page.createNewFile();
			if (file == null) {
				return false;
			}
			
			try {
				IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), file, true);
			} catch (PartInitException e) {
				ExceptionHandler.handleException(e);
				return false;
			}
		} catch (Exception e) {
			ExceptionHandler.handleException(e);
		}
		
		return true;
	}
	
	
	/**
	 * 新規ダイアグラムファイル作成ウィザードのページ。
	 * 
	 * @author daisuke
	 */
	private static class NewDiagramWizardPage extends WizardNewFileCreationPage {
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param selection 現在選択されているリソースの選択状態
		 */
		public NewDiagramWizardPage(IStructuredSelection selection) {
			super("pageName", selection);
			setTitle(Messages.Wizard_Title);
			setMessage(Messages.Wizard_Message);
		}
		
		@Override
		protected void createLinkTarget() {
			// nothing to do
		}
		
		@Override
		protected InputStream getInitialContents() {
			JiemamyContext context = new JiemamyContext(DiagramFacet.PROVIDER, SqlFacet.PROVIDER);
			SimpleJmMetadata metadata = new SimpleJmMetadata();
			metadata.setDialectClassName(GenericDialect.class.getName());
			context.setMetadata(metadata);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				JiemamyContext.findSerializer().serialize(context, out);
			} catch (Exception e) {
				ExceptionHandler.handleException(e);
			} finally {
				IOUtils.closeQuietly(out);
			}
			return new ByteArrayInputStream(out.toByteArray());
		}
		
		@Override
		protected boolean validatePage() {
			if (getFileExtension() == null) {
				setFileExtension(JiemamySerializer.FILE_EXT);
			}
			if (getFileExtension().equals(JiemamySerializer.FILE_EXT) == false) {
				return false;
			}
			return super.validatePage();
		}
	}
}

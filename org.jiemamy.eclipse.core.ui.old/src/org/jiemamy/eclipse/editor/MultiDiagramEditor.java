/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/07/30
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
package org.jiemamy.eclipse.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.SqlFacet;
import org.jiemamy.eclipse.utils.ExceptionHandler;
import org.jiemamy.model.DefaultDiagramModel;
import org.jiemamy.model.DiagramModel;

/**
 * マルチページ構成のダイアグラムエディタクラス。
 * @author daisuke
 */
public class MultiDiagramEditor extends MultiPageEditorPart implements IResourceChangeListener, JiemamyEditor {
	
	private static Logger logger = LoggerFactory.getLogger(MultiDiagramEditor.class);
	
	private List<DiagramEditor> editors = Lists.newArrayList();
	
	private JiemamyContext context;
	

	/**
	 * インスタンスを生成する。
	 */
	public MultiDiagramEditor() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		try {
			out = new ByteArrayOutputStream();
			JiemamyContext.findSerializer().serialize(context, out);
			
			in = new ByteArrayInputStream(out.toByteArray());
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.setContents(in, true, true, monitor);
			
			for (DiagramEditor editor : editors) {
				editor.doSave(monitor);
			}
		} catch (Exception e) {
			ExceptionHandler.handleException(e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
	
	@Override
	public void doSaveAs() {
		Shell shell = getSite().getWorkbenchWindow().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();
		
		IPath path = dialog.getResult();
		if (path == null) {
			return;
		}
		
		// try to save the editor's contents under a different file name
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		try {
			new ProgressMonitorDialog(shell).run(false, // don't fork
					false, // not cancelable
					new WorkspaceModifyOperation() { // run this operation
					
						@Override
						public void execute(IProgressMonitor monitor) {
							ByteArrayOutputStream out = null;
							ByteArrayInputStream in = null;
							try {
								out = new ByteArrayOutputStream();
								JiemamyContext.findSerializer().serialize(context, out);
								
								in = new ByteArrayInputStream(out.toByteArray());
								file.create(in, true, monitor);
							} catch (Exception e) {
								ExceptionHandler.handleException(e);
							} finally {
								IOUtils.closeQuietly(in);
								IOUtils.closeQuietly(out);
							}
						}
					});
			
			setInput(new FileEditorInput(file));
			for (DiagramEditor editor : editors) {
				editor.doSaveAs();
			}
		} catch (InterruptedException e) {
			// should not happen, since the monitor dialog is not cancelable
			ExceptionHandler.handleException(e);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handleException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IEditorPart getActiveEditor() {
		return super.getActiveEditor();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getActivePage() {
		return super.getActivePage();
	}
	
	/**
	 * {@link JiemamyContext}を取得する。
	 * 
	 * @return エディタのルートモデル
	 */
	public JiemamyContext getJiemamyContext() {
		return context;
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		
		context = new JiemamyContext(DiagramFacet.PROVIDER, SqlFacet.PROVIDER);
		
		// 最上位モデルの設定
		IFile file = ((IFileEditorInput) input).getFile();
		try {
			context = JiemamyContext.findSerializer().deserialize(file.getContents());
//			context.normalize();
//			rootModel.setDisplayMode(DatabaseModel.MODE_PHYSICAL_ATTRTYPE);
		} catch (SerializationException e) {
			ExceptionHandler.handleException(e, "Data file is broken.");
		} catch (Exception e) {
			ExceptionHandler.handleException(e);
		} finally {
			DiagramFacet diagramFacet = context.getFacet(DiagramFacet.class);
			if (diagramFacet.getDiagrams().size() == 0) {
				DefaultDiagramModel presentationModel = new DefaultDiagramModel(UUID.randomUUID());
				presentationModel.setName("default");
				diagramFacet.store(presentationModel);
			}
		}
		setPartName(input.getName());
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	public void resourceChanged(IResourceChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		IEditorActionBarContributor contributor = getEditorSite().getActionBarContributor();
		if (contributor != null) {
			((JmContributor) contributor).selectCombo(context);
		} else {
			logger.warn("contributor is null.");
		}
		// Thanks to Naokiさん
	}
	
	@Override
	protected void createPages() {
		for (DiagramModel presentation : context.getFacet(DiagramFacet.class).getDiagrams()) {
			// 各タブを生成
			try {
				DiagramEditor editor = new DiagramEditor(context, editors.size());
				int tabIndex = addPage(editor, getEditorInput());
				editor.setTabIndex(tabIndex);
				setPageText(tabIndex, presentation.getName());
				editors.add(editor);
			} catch (PartInitException e) {
				ExceptionHandler.handleException(e);
			}
		}
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		
		// タブにファイル名をセット
		setPartName(input.getName());
	}
}

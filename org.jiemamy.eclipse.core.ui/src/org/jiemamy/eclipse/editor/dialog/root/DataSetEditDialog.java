/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/03/17
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
package org.jiemamy.eclipse.editor.dialog.root;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.CommonMessages;
import org.jiemamy.eclipse.ui.JiemamyEditDialog;
import org.jiemamy.eclipse.utils.ExceptionHandler;
import org.jiemamy.model.dataset.DataSetModel;
import org.jiemamy.model.dbo.TableModel;

/**
 * {@link JiemamyContext}設定ダイアログクラス。
 * 
 * @author daisuke
 */
public class DataSetEditDialog extends JiemamyEditDialog<DataSetModel> {
	
	private static final Point DEFAULT_SIZE = new Point((int) (400 * 1.618), 400);
	
	private static Logger logger = LoggerFactory.getLogger(DataSetEditDialog.class);
	
	/** CSVインポートボタン */
	private Button btnImport;
	
	/** CSVエクスポートボタン */
	private Button btnExport;
	
	private TabFolder tabFolder;
	
	private final JiemamyFacade jiemamyFacade;
	
	/** 前回import/exportしたファイル名 */
	private String filename;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param shell 親シェルオブジェクト
	 * @param dataSetModel 編集対象{@link JiemamyContext}
	 * @param jiemamyFacade 操作に用いるファサード
	 * @throws IllegalArgumentException 引数rootModel, jiemamyFacadeに{@code null}を与えた場合
	 */
	public DataSetEditDialog(Shell shell, DataSetModel dataSetModel, JiemamyFacade jiemamyFacade) {
		super(shell, dataSetModel, DataSetModel.class);
		
		Validate.notNull(dataSetModel);
		Validate.notNull(jiemamyFacade);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.jiemamyFacade = jiemamyFacade;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final DataSetModel dataSetModel = getTargetModel();
		getShell().setText(Messages.DataSetEditDialog_title);
		
		ReferenceResolver referenceResolver = dataSetModel.getJiemamy().getReferenceResolver();
		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gd);
		final Menu menu = new Menu(tabFolder);
		tabFolder.setMenu(menu);
		menu.addMenuListener(new TabMenuListener(dataSetModel, menu));
		
		Set<TableRef> tableRefs = dataSetModel.getRecords().keySet();
		for (TableRef tableRef : tableRefs) {
			TableModel tableModel = referenceResolver.resolve(tableRef);
			if (tableModel == null) {
				logger.warn("");
				continue;
			}
			addTab(new DataSetEditDialogTableTab(tabFolder, SWT.NONE, dataSetModel, tableModel, jiemamyFacade));
		}
		
		Composite cmpButtons = new Composite(composite, SWT.NULL);
		cmpButtons.setLayout(new RowLayout());
		cmpButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnImport = new Button(cmpButtons, SWT.PUSH);
		btnImport.setText(Messages.DataSetEditDialog_btn_import);
		btnImport.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				importFromCsv();
			}
			
		});
		
		btnExport = new Button(cmpButtons, SWT.PUSH);
		btnExport.setText(Messages.DataSetEditDialog_btn_export);
		btnExport.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportToCsv();
			}
		});
		
		Label label = new Label(cmpButtons, SWT.NONE);
		label.setText(Messages.DataSetEditDialog_label_notice);
		// TODO レコード追加・削除機能
		
		return composite;
	}
	
	@Override
	protected Point getDefaultSize() {
		return DEFAULT_SIZE;
	}
	
	@Override
	protected boolean performOk() {
		return true;
	}
	
	private void exportToCsv() {
		FileDialog dialog = new FileDialog(btnExport.getShell(), SWT.SAVE);
		dialog.setText(Messages.DataSetEditDialog_export_title);
		dialog.setFileName(filename);
		filename = dialog.open();
		
		if (filename == null) {
			return;
		}
		File csv = new File(filename);
		
		if (csv.exists()) {
			boolean result =
					MessageDialog.openQuestion(getShell(), Messages.DataSetEditDialog_export_title, NLS.bind(
							CommonMessages.Common_fileOverwrite, csv.getPath()));
			if (result == false) {
				return;
			}
			
			if (csv.canWrite() == false) {
				MessageDialog.openError(getShell(), Messages.DataSetEditDialog_export_title, NLS.bind(
						CommonMessages.Common_fileWriteFailed, csv.getPath()));
				return;
			}
		}
		
		DataSetModel dataSetModel = getTargetModel();
		TabItem item = tabFolder.getItem(tabFolder.getSelectionIndex());
		TableModel tableModel = (TableModel) item.getData();
		
		OutputStream out = null;
		try {
			out = new FileOutputStream(csv);
			DataSetUtil.exportToCsv(dataSetModel, tableModel, out);
		} catch (IOException e) {
			ExceptionHandler.handleException(e);
		} finally {
			IOUtils.closeQuietly(out);
		}
		
		if (SystemUtils.IS_OS_WINDOWS) {
			boolean result =
					MessageDialog.openQuestion(getShell(), Messages.DataSetEditDialog_export_title,
							Messages.DataSetEditDialog_export_success_windows);
			if (result) {
				try {
					Runtime.getRuntime().exec("cmd /c \"" + csv.getAbsolutePath() + "\"");
				} catch (IOException e) {
					MessageDialog.openError(getShell(), Messages.DataSetEditDialog_export_title,
							Messages.DataSetEditDialog_export_openFailed);
				}
			}
		} else {
			MessageDialog.openInformation(getShell(), Messages.DataSetEditDialog_export_title,
					Messages.DataSetEditDialog_export_success);
		}
	}
	
	private void importFromCsv() {
		FileDialog dialog = new FileDialog(btnImport.getShell(), SWT.OPEN);
		dialog.setText(Messages.DataSetEditDialog_import_title);
		dialog.setFileName(filename);
		filename = dialog.open();
		
		if (filename == null) {
			return;
		}
		
		File csv = new File(filename);
		if (csv.exists() == false) {
			MessageDialog.openError(getShell(), Messages.DataSetEditDialog_import_title, NLS.bind(
					CommonMessages.Common_fileNotFound, csv.getPath()));
			return;
		}
		
		if (csv.canRead() == false) {
			MessageDialog.openError(getShell(), Messages.DataSetEditDialog_import_title, NLS.bind(
					CommonMessages.Common_fileNotReadable, csv.getPath()));
			return;
		}
		
		boolean result =
				MessageDialog.openQuestion(getShell(), Messages.DataSetEditDialog_import_title,
						Messages.DataSetEditDialog_import_confirm);
		if (result == false) {
			return;
		}
		
		DataSetModel dataSetModel = getTargetModel();
		TabItem item = tabFolder.getItem(tabFolder.getSelectionIndex());
		TableModel tableModel = (TableModel) item.getData();
		
		try {
			DataSetUtil.importFromCsv(dataSetModel, tableModel, new FileInputStream(csv));
		} catch (FileNotFoundException e) {
			MessageDialog.openError(getShell(), Messages.DataSetEditDialog_import_title, NLS.bind(
					CommonMessages.Common_fileNotFound, csv.getPath()));
			ExceptionHandler.handleException(e);
		} catch (IOException e) {
			ExceptionHandler.handleException(e);
		}
		MessageDialog.openInformation(getShell(), Messages.DataSetEditDialog_import_title,
				Messages.DataSetEditDialog_import_success);
		
		item.notifyListeners(DataSetEditDialogTableTab.RECORD_CHANGED, new Event());
	}
	

	/**
	 * タブを追加・削除するメニューを表示するリスナ。
	 * 
	 * TODO 同じテーブルに対するタブを複数作られてしまう心配は？
	 *  
	 * @author daisuke
	 */
	private final class TabMenuListener extends MenuAdapter {
		
		private final DataSetModel dataSetModel;
		
		private final Menu menu;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param dataSetModel データセット
		 * @param menu コンテキストメニュー
		 */
		private TabMenuListener(DataSetModel dataSetModel, Menu menu) {
			this.dataSetModel = dataSetModel;
			this.menu = menu;
		}
		
		@Override
		public void menuShown(MenuEvent evt) {
			for (MenuItem item : menu.getItems()) {
				item.dispose();
			}
			
			MenuItem addTab = new MenuItem(menu, SWT.PUSH);
			addTab.setText(Messages.DataSetEditDialog_tabMenu_add);
			addTab.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent evt) {
					JiemamyFactory factory = dataSetModel.getJiemamy().getFactory();
					JiemamyContext rootModel = factory.getJiemamyContext();
					Collection<TableModel> tables = rootModel.findEntities(TableModel.class);
					List<TableModel> list = IteratorUtils.toList(tables.iterator());
					TableSelectDialog dialog = new TableSelectDialog(getShell(), list);
					dialog.open();
					TableModel tableModel = dialog.getResult();
					jiemamyFacade.addRecords(dataSetModel, tableModel);
					addTab(new DataSetEditDialogTableTab(tabFolder, SWT.NONE, dataSetModel, tableModel, jiemamyFacade));
				}
			});
			
			MenuItem removeTab = new MenuItem(menu, SWT.PUSH);
			removeTab.setText(Messages.DataSetEditDialog_tabMenu_remove);
			removeTab.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent evt) {
					TabItem item = tabFolder.getItem(tabFolder.getSelectionIndex());
					TableModel tableModel = (TableModel) item.getData();
					boolean result =
							MessageDialog.openQuestion(getShell(), Messages.DataSetEditDialog_title, NLS.bind(
									Messages.DataSetEditDialog_deleteTable_confirm, tableModel.getName()));
					if (result == false) {
						return;
					}
					
					jiemamyFacade.removeRecords(dataSetModel, tableModel);
					item.dispose();
				}
			});
		}
	}
}

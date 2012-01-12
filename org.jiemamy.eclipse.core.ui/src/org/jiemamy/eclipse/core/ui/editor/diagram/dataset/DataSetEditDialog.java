/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityNotFoundException;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.core.ui.CommonMessages;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractTab;
import org.jiemamy.eclipse.core.ui.editor.diagram.JiemamyEditDialog0;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.model.column.JmColumn;
import org.jiemamy.model.dataset.JmDataSet;
import org.jiemamy.model.dataset.JmRecord;
import org.jiemamy.model.dataset.SimpleJmDataSet;
import org.jiemamy.model.dataset.SimpleJmRecord;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.model.table.SimpleJmTable;
import org.jiemamy.script.ScriptString;
import org.jiemamy.utils.DataSetUtil;

/**
 * {@link JiemamyContext}設定ダイアログクラス。
 * 
 * @author daisuke
 */
public class DataSetEditDialog extends JiemamyEditDialog0<SimpleJmDataSet> {
	
	private static final Point DEFAULT_SIZE = new Point((int) (400 * 1.618), 400);
	
	private static Logger logger = LoggerFactory.getLogger(DataSetEditDialog.class);
	
	/** CSVインポートボタン */
	private Button btnImport;
	
	/** CSVエクスポートボタン */
	private Button btnExport;
	
	private TabFolder tabFolder;
	
	/** 前回import/exportしたファイル名 */
	private String filename;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param shell 親シェルオブジェクト
	 * @param context {@link JiemamyContext}
	 * @param dataSet 編集対象{@link JiemamyContext}
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public DataSetEditDialog(Shell shell, JiemamyContext context, SimpleJmDataSet dataSet) {
		super(shell, context, dataSet, SimpleJmDataSet.class);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final SimpleJmDataSet dataSet = getTargetCoreModel();
		getShell().setText(Messages.DataSetEditDialog_title);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gd);
		
		addTab(new DescriptionTab(tabFolder, SWT.NONE, "Description"));
		
		final Menu menu = new Menu(tabFolder);
		tabFolder.setMenu(menu);
		menu.addMenuListener(new TabMenuListener(dataSet, menu));
		
		JiemamyContext context = getContext();
		Set<EntityRef<? extends JmTable>> tableRefs = dataSet.getRecords().keySet();
		for (EntityRef<? extends JmTable> tableRef : tableRefs) {
			try {
				JmTable table = context.resolve(tableRef);
				addTab(new DataSetEditDialogTableTab(tabFolder, SWT.NONE, dataSet, table));
			} catch (EntityNotFoundException e) {
				logger.warn("table unresolvable");
				continue;
			}
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
		JiemamyContext context = getContext();
		SimpleJmDataSet dataSet = getTargetCoreModel();
		
		List<AbstractTab> tabs = getTabs();
		for (AbstractTab t : tabs) {
			if (t instanceof DataSetEditDialogTableTab == false) {
				continue;
			}
			DataSetEditDialogTableTab tab = (DataSetEditDialogTableTab) t;
			SimpleJmTable table = (SimpleJmTable) tab.getTabItem().getData();
			
			Table swtTable = tab.getSwtTable();
			List<EntityRef<? extends JmColumn>> columns = Lists.newArrayList();
			for (JmColumn column : table.getColumns()) {
				columns.add(column.toReference());
			}
			
			TableItem[] items = swtTable.getItems();
			List<JmRecord> records = Lists.newArrayList();
			for (TableItem item : items) {
				Map<EntityRef<? extends JmColumn>, ScriptString> map = Maps.newHashMap();
				for (int i = 0; i < columns.size(); i++) {
					EntityRef<? extends JmColumn> columnRef = columns.get(i);
					String text = item.getText(i);
					map.put(columnRef, new ScriptString(text));
				}
				records.add(new SimpleJmRecord(map));
			}
			dataSet.putRecord(table.toReference(), records);
		}
		
		context.store(dataSet);
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
					MessageDialog.openQuestion(getShell(), Messages.DataSetEditDialog_export_title,
							NLS.bind(CommonMessages.Common_fileOverwrite, csv.getPath()));
			if (result == false) {
				return;
			}
			
			if (csv.canWrite() == false) {
				MessageDialog.openError(getShell(), Messages.DataSetEditDialog_export_title,
						NLS.bind(CommonMessages.Common_fileWriteFailed, csv.getPath()));
				return;
			}
		}
		
		JmDataSet dataSet = getTargetCoreModel();
		int tabIndex = tabFolder.getSelectionIndex();
		if (tabIndex <= 0) {
			MessageDialog.openInformation(getShell(), "テーブル未選択", "エクスポートするテーブルのタブを選択してください。"); // RESOURCE
			return;
		}
		
		TabItem item = tabFolder.getItem(tabIndex);
		JmTable table = (JmTable) item.getData();
		
		OutputStream out = null;
		try {
			out = new FileOutputStream(csv);
			DataSetUtil.exportToCsv(dataSet, table, out);
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
			MessageDialog.openError(getShell(), Messages.DataSetEditDialog_import_title,
					NLS.bind(CommonMessages.Common_fileNotFound, csv.getPath()));
			return;
		}
		
		if (csv.canRead() == false) {
			MessageDialog.openError(getShell(), Messages.DataSetEditDialog_import_title,
					NLS.bind(CommonMessages.Common_fileNotReadable, csv.getPath()));
			return;
		}
		
		boolean result =
				MessageDialog.openQuestion(getShell(), Messages.DataSetEditDialog_import_title,
						Messages.DataSetEditDialog_import_confirm);
		if (result == false) {
			return;
		}
		
		int tabIndex = tabFolder.getSelectionIndex();
		if (tabIndex <= 0) {
			MessageDialog.openInformation(getShell(), "テーブル未選択", "エクスポートするテーブルのタブを選択してください。"); // RESOURCE
			return;
		}
		TabItem item = tabFolder.getItem(tabIndex);
		JmTable table = (JmTable) item.getData();
		
		SimpleJmDataSet dataSet = getTargetCoreModel();
		
		try {
			DataSetUtil.importFromCsv(dataSet, table, new FileInputStream(csv));
		} catch (FileNotFoundException e) {
			MessageDialog.openError(getShell(), Messages.DataSetEditDialog_import_title,
					NLS.bind(CommonMessages.Common_fileNotFound, csv.getPath()));
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
	 * @author daisuke
	 */
	private final class TabMenuListener extends MenuAdapter {
		
		private final SimpleJmDataSet dataSet;
		
		private final Menu menu;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param dataSet データセット
		 * @param menu コンテキストメニュー
		 */
		private TabMenuListener(SimpleJmDataSet dataSet, Menu menu) {
			this.dataSet = dataSet;
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
					// TODO 同じテーブルに対するタブを複数作られてしまう心配は？
					Collection<JmTable> tables = getContext().getTables();
					List<JmTable> list = Lists.newArrayList(tables);
					TableSelectDialog dialog = new TableSelectDialog(getShell(), list);
					dialog.open();
					JmTable table = dialog.getResult();
					if (table != null) {
						dataSet.putRecord(table.toReference(), new ArrayList<JmRecord>());
						addTab(new DataSetEditDialogTableTab(tabFolder, SWT.NONE, dataSet, table));
					}
				}
			});
			
			MenuItem removeTab = new MenuItem(menu, SWT.PUSH);
			removeTab.setText(Messages.DataSetEditDialog_tabMenu_remove);
			removeTab.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent evt) {
					TabItem item = tabFolder.getItem(tabFolder.getSelectionIndex());
					JmTable table = (JmTable) item.getData();
					if (table == null) {
						return;
					}
					String message = NLS.bind(Messages.DataSetEditDialog_deleteTable_confirm, table.getName());
					boolean result = MessageDialog.openQuestion(getShell(), Messages.DataSetEditDialog_title, message);
					if (result == false) {
						return;
					}
					
					dataSet.removeRecord(table.toReference());
					item.dispose();
				}
			});
		}
	}
}

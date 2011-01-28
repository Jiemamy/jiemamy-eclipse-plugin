/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/18
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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.jiemamy.model.table.TableModel;

/**
 * テーブルを選択するダイアログ。
 * 
 * @author daisuke
 */
public class TableSelectDialog extends Dialog {
	
	private static final int COL_WIDTH_STATUS = 40;
	
	private static final int COL_WIDTH_NAME = 100;
	
	private int selectIndex = -1;
	
	private List<TableModel> tables;
	
	private Table tblColumns;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param shell シェルオブジェクト
	 * @param tables テーブルのリスト
	 */
	public TableSelectDialog(Shell shell, List<TableModel> tables) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		this.tables = tables;
	}
	
	/**
	* 選択結果を取得します。
	* 
	* @return 選択されたカラム
	*/
	public TableModel getResult() {
		if (selectIndex >= 0 && selectIndex < tables.size()) {
			return tables.get(selectIndex);
		}
		return null;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("テーブル選択"); // RESOURCE
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tblColumns = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		tblColumns.setLayoutData(new GridData(GridData.FILL_BOTH));
		tblColumns.setHeaderVisible(true);
		
		TableColumn colStatus = new TableColumn(tblColumns, SWT.LEFT);
		colStatus.setText("");
		colStatus.setWidth(COL_WIDTH_STATUS);
		
		TableColumn colName = new TableColumn(tblColumns, SWT.LEFT);
		colName.setText("テーブル名");
		colName.setWidth(COL_WIDTH_NAME);
		
		refreshTable();
		
		tblColumns.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectIndex = tblColumns.getSelectionIndex();
			}
		});
		
		return composite;
	}
	
	/**
	 * Tableをリフレッシュします。
	 */
	protected void refreshTable() {
		tblColumns.removeAll();
		for (TableModel model : tables) {
			TableItem item = new TableItem(tblColumns, SWT.NULL);
			updateColumnTableItem(item, model);
		}
	}
	
	private void updateColumnTableItem(TableItem item, TableModel tableModel) {
		item.setText(0, "");
		item.setText(1, tableModel.getName());
	}
}

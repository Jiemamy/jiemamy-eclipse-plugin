/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.editor.dialog.table;

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

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.editor.DisplayPlace;
import org.jiemamy.eclipse.editor.utils.LabelStringUtil;
import org.jiemamy.model.attribute.ColumnModel;

/**
 * カラムを選択するダイアログ。インデックスカラム選択用。
 * 
 * @author daisuke
 */
public class ColumnSelectDialog extends Dialog {
	
	private static final int COL_WIDTH_STATUS = 40;
	
	private static final int COL_WIDTH_NAME = 100;
	
	private static final int COL_WIDTH_TYPE = 150;
	
	private int selectIndex = -1;
	
	private List<ColumnModel> columns;
	
	private Table tblColumns;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param shell シェルオブジェクト
	 * @param columns 候補カラムのリスト
	 */
	public ColumnSelectDialog(Shell shell, List<ColumnModel> columns) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		this.columns = columns;
	}
	
	/**
	* 選択結果を取得します。
	* 
	* @return 選択されたカラム
	*/
	public ColumnModel getResult() {
		if (selectIndex >= 0 && selectIndex < columns.size()) {
			return columns.get(selectIndex);
		}
		return null;
	}
	
//	@Override
//	protected void constrainShellSize() {
//		Shell shell = getShell();
//		shell.pack();
//		shell.setSize(shell.getSize().x, 400);
//	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.ColumnSelectDialog_title);
		
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
		colName.setText(Messages.ColumnSelectDialog_columnName_column);
		colName.setWidth(COL_WIDTH_NAME);
		
		TableColumn colType = new TableColumn(tblColumns, SWT.LEFT);
		colType.setText(Messages.ColumnSelectDialog_dataType_column);
		colType.setWidth(COL_WIDTH_TYPE);
		
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
		for (ColumnModel model : columns) {
			TableItem item = new TableItem(tblColumns, SWT.NULL);
			updateColumnTableItem(item, model);
		}
	}
	
	private void updateColumnTableItem(TableItem item, ColumnModel columnModel) {
		JiemamyContext rootModel = columnModel.getJiemamy().getFactory().getJiemamyContext();
		item.setText(0, "");
		item.setText(1, LabelStringUtil.getString(rootModel, columnModel, DisplayPlace.TABLE));
		item.setText(2, LabelStringUtil.getString(rootModel, columnModel.getDataType(), DisplayPlace.TABLE));
	}
}

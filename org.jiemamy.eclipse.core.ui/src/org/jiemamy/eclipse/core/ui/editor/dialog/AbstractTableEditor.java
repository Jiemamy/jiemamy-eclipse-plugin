/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/16
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
package org.jiemamy.eclipse.core.ui.editor.dialog;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * テーブルを用いたエディタコンポーネントクラス。
 * 
 * <p>図のように、表・ボタン・編集コントロールからなるコンポーネント</p>
 * 
 * <p>This component looks like this:<br/>
 * <img src="http://img.f.hatena.ne.jp/images/fotolife/d/daisuke-m/20090217/20090217224121.png">
 * </p>
 * 
 * @author daisuke
 */
public abstract class AbstractTableEditor extends Composite {
	
	private final TableViewer tableViewer;
	
	/** 追加ボタン */
	private Button btnAdd;
	
	/** 挿入ボタン */
	private Button btnInsert;
	
	/** 削除ボタン */
	private Button btnRemove;
	
	/** 上ボタン */
	private Button btnMoveUp;
	
	/** 下ボタン */
	private Button btnMoveDown;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parent 親コンポーネント
	 * @param style SWTスタイル値
	 * @param config 設定オブジェクト
	 */
	public AbstractTableEditor(Composite parent, int style, TableEditorConfig config) {
		super(parent, style);
		
		// 親エリア
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// 子エリア１ - テーブル
		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		
		// 子エリア２ - ボタン
		Composite cmpButtons = new Composite(this, SWT.NULL);
		cmpButtons.setLayout(new RowLayout());
		cmpButtons.setLayoutData(new GridData());
		
		if (config.getAddLabel() != null) {
			btnAdd = new Button(cmpButtons, SWT.PUSH);
			btnAdd.setText(config.getAddLabel());
		}
		if (config.getInsertLabel() != null) {
			btnInsert = new Button(cmpButtons, SWT.PUSH);
			btnInsert.setText(config.getInsertLabel());
		}
		if (config.getRemoveLabel() != null) {
			btnRemove = new Button(cmpButtons, SWT.PUSH);
			btnRemove.setText(config.getRemoveLabel());
		}
		
		// hookメソッドのコール
		createOptionalEditButtons(cmpButtons);
		
		btnMoveUp = new Button(cmpButtons, SWT.ARROW | SWT.UP);
		btnMoveDown = new Button(cmpButtons, SWT.ARROW | SWT.DOWN);
		
		// 子エリア３ - 詳細エディタ
		Group grpEditor = new Group(this, SWT.NULL);
		grpEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpEditor.setLayout(new GridLayout(1, false));
		grpEditor.setText(config.getEditorTitle());
		
		createEditorControls(grpEditor);
		
		assert tableViewer != null;
	}
	
	/**
	 * 各種設定を行う。
	 */
	public void configure() {
		// 各種hookメソッドのcall
		configureTable(tableViewer.getTable());
		configureTableViewer(tableViewer);
		createTableColumns(tableViewer.getTable());
		configureCellEditor();
		configureEditButtons();
		configureEditorControls();
		
		refreshTable();
	}
	
	/**
	 * 編集コントロールを無効にする。
	 */
	public void disableEditControls() {
		disableEditButtons();
		disableEditorControls();
	}
	
	/**
	 * 編集コントロールを有効にする。
	 * 
	 * @param index 選択されたindex
	 */
	public void enableEditControls(int index) {
		enableEditButtons(index);
		enableEditorControls(index);
	}
	
	/**
	 * 追加ボタンを取得する。
	 * 
	 * @return 追加ボタン
	 */
	public Button getBtnAdd() {
		return btnAdd;
	}
	
	/**
	 * tableViewerを取得する。
	 * 
	 * @return tableViewer
	 */
	public final TableViewer getTableViewer() {
		return tableViewer;
	}
	
	/**
	 * Tableをリフレッシュする。
	 */
	public final void refreshTable() {
		tableViewer.refresh();
	}
	
	/**
	 * カラムをリフレッシュする。
	 */
	public void refreshTableColumns() {
		removeTableColumns(tableViewer.getTable());
		createTableColumns(tableViewer.getTable());
		refreshTable();
	}
	
	/**
	 * CellEditorの設定を行う。
	 */
	protected void configureCellEditor() {
		// nothing to do
	}
	
	/**
	 * 編集ボタンの設定を行う。
	 */
	protected void configureEditButtons() {
		if (btnAdd != null) {
			btnAdd.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					performAddItem();
				}
			});
		}
		
		if (btnInsert != null) {
			btnInsert.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					performInsertItem();
				}
			});
		}
		
		if (btnRemove != null) {
			btnRemove.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					performRemoveItem();
				}
			});
		}
		
		if (btnMoveUp != null) {
			btnMoveUp.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					performMoveUpItem();
				}
			});
		}
		
		if (btnMoveDown != null) {
			btnMoveDown.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					performMoveDownItem();
				}
			});
		}
	}
	
	/**
	 * 詳細編集コントロールの設定を行う。
	 */
	protected void configureEditorControls() {
		// nothing to do
	}
	
	/**
	 * テーブルコンポーネントの設定を行う。
	 * 
	 * @param table 設定対象のテーブルコンポーネント
	 */
	protected void configureTable(final Table table) {
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = table.getSelectionIndex();
				if (index >= 0) {
					enableEditControls(index);
				} else {
					disableEditControls();
				}
			}
		});
	}
	
	/**
	 * TableViewerの設定を行う。
	 * 
	 * @param tableViewer 設定対象テーブルビューア
	 */
	protected abstract void configureTableViewer(TableViewer tableViewer);
	
	/**
	 * 詳細編集コントロールを生成する。
	 * 
	 * @param parent 親
	 */
	protected abstract void createEditorControls(Composite parent);
	
	/**
	 * 編集ボタン領域にオプションのコントロールを生成する。 デフォルトでは何も行わない。
	 * 
	 * @param cmpButtons ボタンを配置する親コンポーネント
	 */
	protected void createOptionalEditButtons(Composite cmpButtons) {
		// noting to do
	}
	
	/**
	 * テーブルコンポーネントにカラムコンポーネントを生成する。
	 * 
	 * @param table カラムコンポーネント設定対象のテーブルコンポーネント
	 */
	protected abstract void createTableColumns(Table table);
	
	/**
	 * 編集ボタンを無効にする。 編集ボタン領域のオプションのコントロールについては関知しない。
	 */
	protected void disableEditButtons() {
		if (btnInsert != null) {
			btnInsert.setEnabled(false);
		}
		if (btnRemove != null) {
			btnRemove.setEnabled(false);
		}
		btnMoveUp.setEnabled(false);
		btnMoveDown.setEnabled(false);
	}
	
	/**
	 * 詳細編集コントロールを無効化する。
	 */
	protected void disableEditorControls() {
		// nothing to do
	}
	
	/**
	 * 編集ボタンを有効にする。 編集ボタン領域にオプションのコントロールについては関知しない。
	 * 
	 * @param index 選択されたindex
	 */
	protected void enableEditButtons(int index) {
		if (btnInsert != null) {
			btnInsert.setEnabled(true);
		}
		if (btnRemove != null) {
			btnRemove.setEnabled(true);
		}
		btnMoveUp.setEnabled(true);
		btnMoveDown.setEnabled(true);
		
		// 選択が一番上だった場合
		if (index <= 0) {
			btnMoveUp.setEnabled(false);
		}
		// 選択が一番下だった場合
		if (index >= tableViewer.getTable().getItemCount() - 1) {
			btnMoveDown.setEnabled(false);
		}
	}
	
	/**
	 * 詳細編集コントロールを有効化する。
	 * 
	 * @param index 選択された要素のテーブル内での位置インデックス
	 */
	protected void enableEditorControls(int index) {
		// nothing to do
	}
	
	/**
	 * 追加ボタンが押された時の処理を行う。
	 * 
	 * @return 追加されたモデル
	 */
	protected abstract Object performAddItem();
	
	/**
	 * 挿入ボタンが押された時の処理を行う。
	 * 
	 * @return 追加されたモデル
	 */
	protected abstract Object performInsertItem();
	
	/**
	 * 下ボタンが押された時の処理を行う。
	 */
	protected abstract void performMoveDownItem();
	
	/**
	 * 上ボタンが押された時の処理を行う。
	 */
	protected abstract void performMoveUpItem();
	
	/**
	 * 削除ボタンが押された時の処理を行う。
	 * 
	 * @return 削除されたモデル
	 */
	protected abstract Object performRemoveItem();
	
	/**
	 * テーブルコンポーネントからカラムコンポーネントを全て削除する。
	 * @param table 対象のテーブルコンポーネント
	 */
	private void removeTableColumns(Table table) {
		for (TableColumn col : table.getColumns()) {
			col.dispose();
		}
	}
}

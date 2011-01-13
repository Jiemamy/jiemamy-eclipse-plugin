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
package org.jiemamy.eclipse.core.ui.editor.dialog.table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import org.jiemamy.JiemamyContext;
import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.dialog.AbstractTab;
import org.jiemamy.eclipse.core.ui.editor.dialog.JiemamyEditDialog;
import org.jiemamy.eclipse.core.ui.editor.dialog.TextEditTab;
import org.jiemamy.eclipse.core.ui.editor.dialog.TextSelectionAdapter;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.model.DatabaseObjectModel;
import org.jiemamy.model.column.ColumnModel;
import org.jiemamy.model.table.DefaultTableModel;
import org.jiemamy.model.table.TableModel;

/**
 * テーブルの詳細編集ダイアログクラス。
 * 
 * @author daisuke
 */
public class TableEditDialog extends JiemamyEditDialog<DefaultTableModel> {
	
	private static final Point DEFAULT_SIZE = new Point(700, 500);
	
	// 共通
	private Text txtName;
	
	private Text txtLogicalName;
	
	private TextEditTab tabBeginScript;
	
	private TextEditTab tabEndScript;
	
	private TextEditTab tabDescription;
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private final int diagramIndex;
	

	/**
	 * コンストラクタ。
	 * 
	 * @param parentShell 親シェルオブジェクト
	 * @param context コンテキスト
	 * @param tableModel 編集対象モデル
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public TableEditDialog(Shell parentShell, JiemamyContext context, DefaultTableModel tableModel, int diagramIndex) {
		super(parentShell, context, tableModel, TableModel.class);
		
		Validate.notNull(tableModel);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.diagramIndex = diagramIndex;
	}
	
	@Override
	protected boolean canExecuteOk() {
		boolean nameOk = StringUtils.isEmpty(txtName.getText()) == false;
		return nameOk && super.canExecuteOk();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final DefaultTableModel tableModel = getTargetCoreModel();
		getShell().setText("テーブル情報編集"); // RESOURCE
		
		// ---- A. 最上段名称欄
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(7, false));
		
		// ---- A-1. テーブル名
		Label label = new Label(composite, SWT.NONE);
		label.setText("テーブル名(&N)"); // RESOURCE
		
		txtName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		txtName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtName.setText(tableModel.getName());
		txtName.addFocusListener(new TextSelectionAdapter(txtName));
		txtName.addKeyListener(editListener);
		
		// ---- A-2. 論理名
		label = new Label(composite, SWT.NONE);
		label.setText("論理名(&L)"); // RESOURCE
		
		txtLogicalName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		txtLogicalName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtLogicalName.setText(StringUtils.defaultString(tableModel.getLogicalName()));
		txtLogicalName.addFocusListener(new TextSelectionAdapter(txtLogicalName));
		txtLogicalName.addKeyListener(editListener);
		
		// ---- A-3. 色
		ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
		
		Button btnColor = new Button(composite, SWT.PUSH);
		btnColor.setImage(ir.get(Images.ICON_COLOR_PALETTE));
		btnColor.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				ColorDialog colorDialog = new ColorDialog(getShell(), SWT.NULL);
				RGB rgb = colorDialog.open();
				if (rgb != null) {
					getNodeModel().setColor(ConvertUtil.convert(rgb));
				}
			}
		});
		
		Button btnDefaultColor = new Button(composite, SWT.PUSH);
		btnDefaultColor.setText("default color"); // RESOURCE
		btnDefaultColor.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				getNodeModel().setColor(null);
			}
		});
		
		final Button btnDisable = new Button(composite, SWT.CHECK);
		btnDisable.setText("無効(&G)"); // RESOURCE
		btnDisable.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				// FIXME
//				if (tableModel.hasAdapter(Disablable.class) == false) {
//					JiemamyFactory factory = tableModel.getJiemamy().getFactory();
//					tableModel.registerAdapter(factory.newAdapter(Disablable.class));
//				}
//				tableModel.getAdapter(Disablable.class).setDisabled(btnDisable.getSelection());
			}
			
		});
//		if (tableModel.hasAdapter(Disablable.class)
//				&& Boolean.TRUE.equals(tableModel.getAdapter(Disablable.class).isDisabled())) {
//			btnDisable.setSelection(true);
//		}
		
		// ---- B. タブ
		TabFolder tabFolder = new TabFolder(composite, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 7;
		tabFolder.setLayoutData(gd);
		
		createTabs(tableModel, tabFolder);
		
		return composite;
	}
	
	@Override
	protected Point getDefaultSize() {
		return DEFAULT_SIZE;
	}
	
	@Override
	protected boolean performOk() {
		if (StringUtils.isEmpty(txtName.getText())) {
			return false;
		}
		
		DefaultTableModel tableModel = getTargetCoreModel();
		Set<DatabaseObjectModel> entities = getContext().getDatabaseObjects();
		Set<String> entityNames = new HashSet<String>(entities.size());
		for (DatabaseObjectModel entityModel : entities) {
			if (entityModel.equals(tableModel) == false) {
				entityNames.add(entityModel.getName());
			}
		}
		
		List<ColumnModel> columns = tableModel.getColumns();
		Set<String> columnNames = new HashSet<String>(columns.size());
		for (ColumnModel columnModel : columns) {
			columnNames.add(columnModel.getName());
		}
		
		if (entityNames.contains(txtName.getText())) {
			// RESOURCE
			boolean entityCheckOk = MessageDialog.openQuestion(getParentShell(), "Confirm", "エンティティ名が重複しますが、よろしいですか？");
			if (entityCheckOk == false) {
				return false;
			}
		}
		
		if (columnNames.size() != columns.size()) {
			// RESOURCE
			boolean columnCheckOk = MessageDialog.openQuestion(getParentShell(), "Confirm", "カラム名が重複しますが、よろしいですか？");
			if (columnCheckOk == false) {
				return false;
			}
		}
		
		String name = txtName.getText();
		tableModel.setName(name);
		
		String logicalName = txtLogicalName.getText();
		tableModel.setLogicalName(logicalName);
		
//		String beginScript = StringUtils.defaultString(tabBeginScript.getTextWidget().getText());
//		jiemamyFacade.changeModelProperty(tableModel, EntityProperty.beginScript, beginScript);
//		
//		String endScript = StringUtils.defaultString(tabEndScript.getTextWidget().getText());
//		jiemamyFacade.changeModelProperty(tableModel, EntityProperty.endScript, endScript);
		
		String description = StringUtils.defaultString(tabDescription.getTextWidget().getText());
		tableModel.setDescription(description);
		
		return true;
	}
	
	private void createTabs(final DefaultTableModel tableModel, TabFolder tabFolder) {
		// ---- B-1. カラム
		AbstractTab tabColumn = new TableEditDialogColumnTab(tabFolder, SWT.NULL, getContext(), tableModel);
		tabColumn.addKeyListener(editListener);
		addTab(tabColumn);
		
//		// ---- B-2. キー制約
//		AbstractTab tabLocalKey = new TableEditDialogLocalKeyTab(tabFolder, SWT.NULL, tableModel, jiemamyFacade);
//		tabLocalKey.addKeyListener(editListener);
//		addTab(tabLocalKey);
//		
//		// ---- B-3. チェック制約
//		AbstractTab tabCheck = new TableEditDialogCheckTab(tabFolder, SWT.NULL, tableModel, jiemamyFacade);
//		tabCheck.addKeyListener(editListener);
//		addTab(tabCheck);
//		
//		// ---- B-4. インデックス
//		AbstractTab tabIndex = new TableEditDialogIndexTab(tabFolder, SWT.NULL, tableModel, jiemamyFacade);
//		tabIndex.addKeyListener(editListener);
//		addTab(tabIndex);
//		
//		// ---- B-6. BeginScript
//		String beginScript = StringUtils.defaultString(tableModel.getBeginScript());
//		tabBeginScript = new TextEditTab(tabFolder, Messages.Tab_Table_BeginScript, beginScript);
//		addTab(tabBeginScript);
//		
//		// ---- B-7. EndScript
//		String endScript = StringUtils.defaultString(tableModel.getEndScript());
//		tabEndScript = new TextEditTab(tabFolder, Messages.Tab_Table_EndScript, endScript);
//		addTab(tabEndScript);
		
		// ---- B-8. Description
		String description = StringUtils.defaultString(tableModel.getDescription());
		tabDescription = new TextEditTab(tabFolder, Messages.Tab_Table_Description, description);
		addTab(tabDescription);
		
		createAdditionalTabs(tabFolder);
	}
}

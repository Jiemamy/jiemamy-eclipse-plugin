/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.commons.lang.StringUtils;
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
import org.jiemamy.SqlFacet;
import org.jiemamy.dddbase.EntityNotFoundException;
import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractTab;
import org.jiemamy.eclipse.core.ui.editor.diagram.JiemamyEditDialog;
import org.jiemamy.eclipse.core.ui.editor.diagram.TextEditTab;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.model.DbObject;
import org.jiemamy.model.SimpleDbObjectNode;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.model.SimpleJmNode;
import org.jiemamy.model.column.ColumnParameterKey;
import org.jiemamy.model.column.JmColumn;
import org.jiemamy.model.constraint.JmKeyConstraint;
import org.jiemamy.model.script.Position;
import org.jiemamy.model.script.SimpleJmAroundScript;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.model.table.SimpleJmTable;

/**
 * {@link SimpleJmTable}の詳細編集ダイアログクラス。
 * 
 * @author daisuke
 */
public class TableEditDialog extends JiemamyEditDialog<SimpleJmTable> {
	
	private static final Point DEFAULT_SIZE = new Point(700, 500);
	
	// 共通
	/** テーブル名コンポーネント */
	private Text txtName;
	
	/** テーブル論理名コンポーネント */
	private Text txtLogicalName;
	
	/** 開始スクリプトタブ */
	private TextEditTab tabBeginScript;
	
	/** 終了スクリプトタブ */
	private TextEditTab tabEndScript;
	
	/** 説明タブ */
	private TextEditTab tabDescription;
	
	
	/**
	 * コンストラクタ。
	 * 
	 * @param parentShell 親シェルオブジェクト
	 * @param context コンテキスト
	 * @param table 編集対象モデル
	 * @param node {@link SimpleJmDiagram}
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public TableEditDialog(Shell parentShell, JiemamyContext context, SimpleJmTable table, SimpleDbObjectNode node) {
		super(parentShell, context, table, SimpleJmTable.class, node);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	@Override
	protected boolean canExecuteOk() {
		boolean nameOk = StringUtils.isEmpty(txtName.getText()) == false;
		return nameOk && super.canExecuteOk();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final SimpleJmTable table = getTargetCoreModel();
		getShell().setText("テーブル情報編集"); // RESOURCE
		
		// ---- A. 最上段名称欄
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(7, false));
		
		// ---- A-1. テーブル名
		Label label = new Label(composite, SWT.NONE);
		label.setText("テーブル名(&N)"); // RESOURCE
		
		txtName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		txtName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtName.setText(StringUtils.defaultString(table.getName()));
		txtName.addFocusListener(new TextSelectionAdapter(txtName));
		txtName.addKeyListener(editListener);
		
		// ---- A-2. 論理名
		label = new Label(composite, SWT.NONE);
		label.setText("論理名(&L)"); // RESOURCE
		
		txtLogicalName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		txtLogicalName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtLogicalName.setText(StringUtils.defaultString(table.getLogicalName()));
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
					SimpleJmNode node = getJmNode();
					node.setColor(ConvertUtil.convert(rgb));
				}
			}
		});
		
		Button btnSimpleColor = new Button(composite, SWT.PUSH);
		btnSimpleColor.setText("default color"); // RESOURCE
		btnSimpleColor.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				SimpleJmNode node = getJmNode();
				node.setColor(null);
			}
		});
		
		final Button btnDisable = new Button(composite, SWT.CHECK);
		btnDisable.setText("無効(&G)"); // RESOURCE
		btnDisable.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				Boolean disabled = table.getParam(ColumnParameterKey.DISABLED);
				if (btnDisable.getSelection() == false) {
					if (disabled != null && disabled) {
						table.removeParam(ColumnParameterKey.DISABLED);
					}
				} else {
					if (disabled == null || disabled == false) {
						table.putParam(ColumnParameterKey.DISABLED, true);
					}
				}
			}
			
		});
		Boolean disabled = table.getParam(ColumnParameterKey.DISABLED);
		if (disabled != null && disabled) {
			btnDisable.setSelection(true);
		}
		
		// ---- B. タブ
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 7;
		tabFolder.setLayoutData(gd);
		
		createTabs(table, tabFolder);
		
		return composite;
	}
	
	@Override
	protected Point getDefaultSize() {
		return DEFAULT_SIZE;
	}
	
	@Override
	protected boolean performOk() {
		SimpleJmTable table = getTargetCoreModel();
		
		if (confirmConsistency(table) == false) {
			return false;
		}
		
		String name = txtName.getText();
		table.setName(name);
		
		String logicalName = txtLogicalName.getText();
		table.setLogicalName(logicalName);
		
		SqlFacet facet = getContext().getFacet(SqlFacet.class);
		String beginScript = StringUtils.defaultString(tabBeginScript.getTextWidget().getText());
		String endScript = StringUtils.defaultString(tabEndScript.getTextWidget().getText());
		
		SimpleJmAroundScript aroundScript = (SimpleJmAroundScript) facet.getAroundScriptFor(table.toReference());
		if (aroundScript == null) {
			aroundScript = new SimpleJmAroundScript();
			aroundScript.setCoreModelRef(table.toReference());
		}
		
		if (StringUtils.isEmpty(beginScript) == false || StringUtils.isEmpty(endScript) == false) {
			aroundScript.setScript(Position.BEGIN, beginScript);
			aroundScript.setScript(Position.END, endScript);
			facet.store(aroundScript);
		} else {
			try {
				facet.deleteScript(aroundScript.toReference());
			} catch (EntityNotFoundException e) {
				// ignore
			}
		}
		
		String description = StringUtils.defaultString(tabDescription.getTextWidget().getText());
		table.setDescription(description);
		
		Set<JmKeyConstraint> constraints = table.getConstraints(JmKeyConstraint.class);
		for (JmKeyConstraint keyConstraint : constraints) {
			if (keyConstraint.getKeyColumns().isEmpty()) {
				table.deleteConstraint(keyConstraint.toReference());
			}
		}
		
		return true;
	}
	
	/**
	 * 整合性をチェックし、ダイアログでユーザに確認する。
	 * 
	 * @param table テーブル
	 * @return 警告を無視する場合は{@code true}、そうでない場合は{@code false}
	 */
	private boolean confirmConsistency(JmTable table) {
		boolean result = true;
		
		Set<DbObject> doms = getContext().getDbObjects();
		Set<String> domNames = Sets.newHashSetWithExpectedSize(doms.size());
		for (DbObject dom : doms) {
			if (dom.equals(table) == false) {
				domNames.add(dom.getName());
			}
		}
		
		if (domNames.contains(txtName.getText())) {
			// RESOURCE
			boolean entityCheckOk =
					MessageDialog.openQuestion(getParentShell(), "Confirm", "DbObject名が重複しますが、よろしいですか？");
			if (entityCheckOk == false) {
				result = false;
			}
		}
		
		List<JmColumn> columns = table.getColumns();
		Set<String> columnNames = Sets.newHashSetWithExpectedSize(columns.size());
		for (JmColumn column : columns) {
			columnNames.add(column.getName());
		}
		
		if (columnNames.size() != columns.size()) {
			// RESOURCE
			boolean columnCheckOk = MessageDialog.openQuestion(getParentShell(), "Confirm", "カラム名が重複しますが、よろしいですか？");
			if (columnCheckOk == false) {
				result = false;
			}
		}
		return result;
	}
	
	private void createTabs(final SimpleJmTable table, TabFolder tabFolder) {
		// ---- B-1. カラム
		AbstractTab tabColumn = new TableEditDialogColumnTab(tabFolder, SWT.NULL, getContext(), table);
		tabColumn.addKeyListener(editListener);
		addTab(tabColumn);
		
		// ---- B-2. 制約
		AbstractTab tabConstraint = new TableEditDialogConstraintTab(tabFolder, SWT.NULL, getContext(), table);
		tabConstraint.addKeyListener(editListener);
		addTab(tabConstraint);
		
//		// ---- B-3. インデックス
//		AbstractTab tabIndex = new TableEditDialogIndexTab(tabFolder, SWT.NULL, getContext(), table);
//		tabIndex.addKeyListener(editListener);
//		addTab(tabIndex);
		
		String beginScript = "";
		String endScript = "";
		SqlFacet facet = getContext().getFacet(SqlFacet.class);
		SimpleJmAroundScript aroundScript = (SimpleJmAroundScript) facet.getAroundScriptFor(table.toReference());
		if (aroundScript != null) {
			beginScript = StringUtils.defaultString(aroundScript.getScript(Position.BEGIN));
			endScript = StringUtils.defaultString(aroundScript.getScript(Position.END));
		}
		
		// ---- B-4. BeginScript
		tabBeginScript = new TextEditTab(tabFolder, Messages.Tab_Table_BeginScript, beginScript);
		addTab(tabBeginScript);
		
		// ---- B-5. EndScript
		tabEndScript = new TextEditTab(tabFolder, Messages.Tab_Table_EndScript, endScript);
		addTab(tabEndScript);
		
		// ---- B-6. Description
		String description = StringUtils.defaultString(table.getDescription());
		tabDescription = new TextEditTab(tabFolder, Messages.Tab_Table_Description, description);
		addTab(tabDescription);
		
		createAdditionalTabs(tabFolder);
	}
}

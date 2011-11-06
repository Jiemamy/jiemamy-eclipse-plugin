/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/17
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.view;

import org.apache.commons.lang.StringUtils;
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
import org.jiemamy.eclipse.core.ui.editor.diagram.JiemamyEditDialog;
import org.jiemamy.eclipse.core.ui.editor.diagram.TextEditTab;
import org.jiemamy.eclipse.core.ui.utils.ConvertUtil;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.model.SimpleDbObjectNode;
import org.jiemamy.model.SimpleJmNode;
import org.jiemamy.model.column.ColumnParameterKey;
import org.jiemamy.model.script.Position;
import org.jiemamy.model.script.SimpleJmAroundScript;
import org.jiemamy.model.view.SimpleJmView;

/**
 * {@link SimpleJmView}の詳細編集ダイアログクラス。
 * 
 * @author daisuke
 */
public class ViewEditDialog extends JiemamyEditDialog<SimpleJmView> {
	
	private static final Point DEFAULT_SIZE = new Point((int) (370 * 1.618), 370);
	
	/** ビュー名コンポーネント */
	private Text txtName;
	
	/** ビュー論理名コンポーネント */
	private Text txtLogicalName;
	
	/** ビュー定義タブ */
	private TextEditTab tabDefinition;
	
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
	 * @param view 編集対象モデル
	 * @param node ノード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ViewEditDialog(Shell parentShell, JiemamyContext context, SimpleJmView view, SimpleDbObjectNode node) {
		super(parentShell, context, view, SimpleJmView.class, node);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	@Override
	protected boolean canExecuteOk() {
		boolean definitionOk = StringUtils.isEmpty(tabDefinition.getTextWidget().getText()) == false;
		boolean nameOk = StringUtils.isEmpty(txtName.getText()) == false;
		return definitionOk && nameOk && super.canExecuteOk();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final SimpleJmView view = getTargetCoreModel();
		getShell().setText(Messages.Dialog_Title);
		
		// ---- A. 最上段名称欄
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(7, false));
		
		// ---- A-1. ビュー名
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.Label_View_Name);
		
		txtName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		txtName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtName.setText(StringUtils.defaultString(view.getName()));
		txtName.addFocusListener(new TextSelectionAdapter(txtName));
		txtName.addKeyListener(editListener);
		
		// ---- A-2. 論理名
		label = new Label(composite, SWT.NONE);
		label.setText(Messages.Label_View_LogicalName);
		
		txtLogicalName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		txtLogicalName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtLogicalName.setText(StringUtils.defaultString(view.getLogicalName()));
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
				Boolean disabled = view.getParam(ColumnParameterKey.DISABLED);
				if (btnDisable.getSelection() == false) {
					if (disabled != null && disabled) {
						view.removeParam(ColumnParameterKey.DISABLED);
					}
				} else {
					if (disabled == null || disabled == false) {
						view.putParam(ColumnParameterKey.DISABLED, true);
					}
				}
			}
			
		});
		Boolean disabled = view.getParam(ColumnParameterKey.DISABLED);
		if (disabled != null && disabled) {
			btnDisable.setSelection(true);
		}
		
		// ---- A-4. ラベル
		label = new Label(composite, SWT.NULL);
		label.setText(Messages.Message);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 6;
		label.setLayoutData(gd);
		
		// ---- B. タブ
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 7;
		tabFolder.setLayoutData(gd);
		
		createTabs(view, tabFolder);
		
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
		
		SimpleJmView view = getTargetCoreModel();
		
		String name = txtName.getText();
		view.setName(name);
		
		String logicalName = txtLogicalName.getText();
		view.setLogicalName(logicalName);
		
		String definition = tabDefinition.getTextWidget().getText();
		view.setDefinition(definition);
		
		SqlFacet facet = getContext().getFacet(SqlFacet.class);
		SimpleJmAroundScript aroundScript;
		String beginScript = StringUtils.defaultString(tabBeginScript.getTextWidget().getText());
		String endScript = StringUtils.defaultString(tabEndScript.getTextWidget().getText());
		
		aroundScript = (SimpleJmAroundScript) facet.getAroundScriptFor(view.toReference());
		if (aroundScript == null) {
			aroundScript = new SimpleJmAroundScript();
			aroundScript.setCoreModelRef(view.toReference());
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
		view.setDescription(description);
		
		return true;
	}
	
	private void createTabs(final SimpleJmView view, TabFolder tabFolder) {
		// ---- B-1. Definition
		String definition = StringUtils.defaultString(view.getDefinition());
		tabDefinition = new TextEditTab(tabFolder, Messages.Tab_View_Definition, definition);
		tabDefinition.addKeyListener(editListener);
		addTab(tabDefinition);
		
		String beginScript = "";
		String endScript = "";
		SqlFacet facet = getContext().getFacet(SqlFacet.class);
		SimpleJmAroundScript aroundScript = (SimpleJmAroundScript) facet.getAroundScriptFor(view.toReference());
		if (aroundScript != null) {
			beginScript = StringUtils.defaultString(aroundScript.getScript(Position.BEGIN));
			endScript = StringUtils.defaultString(aroundScript.getScript(Position.END));
		}
		
		// ---- B-2. BeginScript
		tabBeginScript = new TextEditTab(tabFolder, Messages.Tab_View_BeginScript, beginScript);
		addTab(tabBeginScript);
		
		// ---- B-3. EndScript
		tabEndScript = new TextEditTab(tabFolder, Messages.Tab_View_EndScript, endScript);
		addTab(tabEndScript);
		
		// ---- B-4. Description
		String description = StringUtils.defaultString(view.getDescription());
		tabDescription = new TextEditTab(tabFolder, Messages.Tab_View_Description, description);
		tabDefinition.addKeyListener(editListener);
		addTab(tabDescription);
		
		createAdditionalTabs(tabFolder);
	}
}

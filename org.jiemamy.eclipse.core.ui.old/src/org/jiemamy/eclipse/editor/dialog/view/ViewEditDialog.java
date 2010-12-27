/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.editor.dialog.view;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
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
import org.jiemamy.eclipse.Images;
import org.jiemamy.eclipse.JiemamyUIPlugin;
import org.jiemamy.eclipse.ui.JiemamyEditDialog;
import org.jiemamy.eclipse.ui.helper.TextSelectionAdapter;
import org.jiemamy.eclipse.ui.tab.TextEditTab;
import org.jiemamy.eclipse.utils.ConvertUtil;
import org.jiemamy.model.NodeModel;
import org.jiemamy.model.dbo.ViewModel;

/**
 * View設定ダイアログクラス。
 * 
 * @author daisuke
 */
public class ViewEditDialog extends JiemamyEditDialog<ViewModel> {
	
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
	
	/** ダイアグラムエディタのインデックス（エディタ内のタブインデックス） */
	private final int diagramIndex;
	
	private final JiemamyViewFacade jiemamyFacade;
	

	/**
	 * コンストラクタ。
	 * 
	 * @param shell 親シェルオブジェクト
	 * @param viewModel 編集対象ビュー
	 * @param diagramIndex ダイアグラムエディタのインデックス（エディタ内のタブインデックス）
	 * @param jiemamyFacade モデル操作に用いるファサード
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ViewEditDialog(Shell shell, JiemamyContext context, ViewModel viewModel, int diagramIndex,
			JiemamyViewFacade jiemamyFacade) {
		super(shell, context, viewModel, ViewModel.class);
		
		Validate.notNull(viewModel);
		Validate.notNull(jiemamyFacade);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.diagramIndex = diagramIndex;
		this.jiemamyFacade = jiemamyFacade;
	}
	
	@Override
	protected boolean canExecuteOk() {
		return StringUtils.isEmpty(tabDefinition.getTextWidget().getText()) == false
				&& StringUtils.isEmpty(txtName.getText()) == false && super.canExecuteOk();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final ViewModel viewModel = getTargetModel();
		getShell().setText(Messages.Dialog_Title);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(7, false));
		
		// ---- A-1. ビュー名
		Label label = new Label(composite, SWT.NULL);
		label.setText(Messages.Label_View_Name);
		
		txtName = new Text(composite, SWT.BORDER);
		txtName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtName.setText(JiemamyPropertyUtil.careNull(viewModel.getName()));
		txtName.addFocusListener(new TextSelectionAdapter(txtName));
		txtName.addKeyListener(editListener);
		
		// ---- A-2. 論理名
		label = new Label(composite, SWT.NULL);
		label.setText(Messages.Label_View_LogicalName);
		
		txtLogicalName = new Text(composite, SWT.BORDER);
		txtLogicalName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtLogicalName.setText(JiemamyPropertyUtil.careNull(viewModel.getLogicalName()));
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
					NodeModel nodeAdapter = viewModel.getAdapter(NodeModel.class);
					jiemamyFacade.setColor(diagramIndex, nodeAdapter, ConvertUtil.convert(rgb));
				}
			}
		});
		
		Button btnDefaultColor = new Button(composite, SWT.PUSH);
		btnDefaultColor.setText("default color"); // RESOURCE
		btnDefaultColor.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				NodeModel nodeAdapter = viewModel.getAdapter(NodeModel.class);
				jiemamyFacade.setColor(diagramIndex, nodeAdapter, null);
			}
		});
		
		final Button btnDisable = new Button(composite, SWT.CHECK);
		btnDisable.setText("無効(&G)"); // RESOURCE
		btnDisable.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent evt) {
				if (viewModel.hasAdapter(Disablable.class) == false) {
					JiemamyFactory factory = viewModel.getJiemamy().getFactory();
					viewModel.registerAdapter(factory.newAdapter(Disablable.class));
				}
				viewModel.getAdapter(Disablable.class).setDisabled(btnDisable.getSelection());
			}
			
		});
		if (viewModel.hasAdapter(Disablable.class)
				&& Boolean.TRUE.equals(viewModel.getAdapter(Disablable.class).isDisabled())) {
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
		
		createTabs(viewModel, tabFolder);
		
		return composite;
	}
	
	@Override
	protected Point getDefaultSize() {
		return DEFAULT_SIZE;
	}
	
	@Override
	protected boolean performOk() {
		ViewModel viewModel = getTargetModel();
		
		String name = JiemamyPropertyUtil.careNull(txtName.getText(), false);
		jiemamyFacade.changeModelProperty(viewModel, EntityProperty.name, name);
		
		String logicalName = JiemamyPropertyUtil.careNull(txtLogicalName.getText(), true);
		jiemamyFacade.changeModelProperty(viewModel, EntityProperty.logicalName, logicalName);
		
		String definition = JiemamyPropertyUtil.careNull(tabDefinition.getTextWidget().getText(), false);
		jiemamyFacade.changeModelProperty(viewModel, ViewProperty.definition, definition);
		
		String beginScript = JiemamyPropertyUtil.careNull(tabBeginScript.getTextWidget().getText(), true);
		jiemamyFacade.changeModelProperty(viewModel, EntityProperty.beginScript, beginScript);
		
		String endScript = JiemamyPropertyUtil.careNull(tabEndScript.getTextWidget().getText(), true);
		jiemamyFacade.changeModelProperty(viewModel, EntityProperty.endScript, endScript);
		
		String description = JiemamyPropertyUtil.careNull(tabDescription.getTextWidget().getText(), true);
		jiemamyFacade.changeModelProperty(viewModel, EntityProperty.description, description);
		
		return true;
	}
	
	private void createTabs(final ViewModel viewModel, TabFolder tabFolder) {
		// ---- B-1. Definition
		String definition = JiemamyPropertyUtil.careNull(viewModel.getDefinition());
		tabDefinition = new TextEditTab(tabFolder, Messages.Tab_View_Definition, definition);
		tabDefinition.addKeyListener(editListener);
		addTab(tabDefinition);
		
		// ---- B-2. BeginScript
		String beginScript = JiemamyPropertyUtil.careNull(viewModel.getBeginScript());
		tabBeginScript = new TextEditTab(tabFolder, Messages.Tab_View_BeginScript, beginScript);
		tabBeginScript.addKeyListener(editListener);
		addTab(tabBeginScript);
		
		// ---- B-3. EndScript
		String endScript = JiemamyPropertyUtil.careNull(viewModel.getEndScript());
		tabEndScript = new TextEditTab(tabFolder, Messages.Tab_View_EndScript, endScript);
		tabEndScript.addKeyListener(editListener);
		addTab(tabEndScript);
		
		// ---- B-4. Description
		String description = JiemamyPropertyUtil.careNull(viewModel.getDescription());
		tabDescription = new TextEditTab(tabFolder, Messages.Tab_View_Description, description);
		tabDefinition.addKeyListener(editListener);
		addTab(tabDescription);
		
		createAdditionalTabs(tabFolder);
	}
}

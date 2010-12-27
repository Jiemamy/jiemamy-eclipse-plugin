/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.editor.dialog.root;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dialect.Dialect;
import org.jiemamy.eclipse.JiemamyCorePlugin;
import org.jiemamy.eclipse.ui.JiemamyEditDialog;
import org.jiemamy.eclipse.ui.helper.TextSelectionAdapter;
import org.jiemamy.eclipse.ui.tab.AbstractTab;
import org.jiemamy.eclipse.ui.tab.TextEditTab;

/**
 * {@link JiemamyContext}設定ダイアログクラス。
 * 
 * @author daisuke
 */
public class RootEditDialog extends JiemamyEditDialog<JiemamyContext> {
	
	private static final Point DEFAULT_SIZE = new Point((int) (400 * 1.618), 400);
	
	private static Logger logger = LoggerFactory.getLogger(RootEditDialog.class);
	
	/** インストールされているSQL方言のリスト */
	private final List<Dialect> dialects;
	
	/** スキーマ名入力欄 */
	private Text txtSchema;
	
	/** Dialect選択 */
	private Combo cmbDialect;
	
	/** 開始スクリプト入力欄 */
	private TextEditTab tabBeginScript;
	
	/** 終了スクリプト入力欄 */
	private TextEditTab tabEndScript;
	
	/** 説明文入力欄 */
	private TextEditTab tabDescription;
	
	private final JiemamyFacade jiemamyFacade;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param shell 親シェルオブジェクト
	 * @param context 編集対象{@link JiemamyContext}
	 * @param jiemamyFacade 操作に用いるファサード
	 * @throws IllegalArgumentException 引数rootModel, jiemamyFacadeに{@code null}を与えた場合
	 */
	public RootEditDialog(Shell shell, JiemamyContext context, JiemamyFacade jiemamyFacade) {
		super(shell, context, context, JiemamyContext.class);
		
		Validate.notNull(context);
		Validate.notNull(jiemamyFacade);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.jiemamyFacade = jiemamyFacade;
		dialects = JiemamyCorePlugin.getDialectResolver().getAllInstance();
	}
	
	@Override
	protected boolean canExecuteOk() {
		return super.canExecuteOk() && StringUtils.isEmpty(cmbDialect.getText()) == false;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		JiemamyContext rootModel = getTargetModel();
		getShell().setText(Messages.Dialog_Title);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(4, false));
		
		Label label;
		
		// ---- A-1. スキーマ名
		label = new Label(composite, SWT.NULL);
		label.setText(Messages.Label_SchemaName);
		
		txtSchema = new Text(composite, SWT.BORDER);
		txtSchema.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSchema.setText(JiemamyPropertyUtil.careNull(rootModel.getSchemaName()));
		txtSchema.addFocusListener(new TextSelectionAdapter(txtSchema));
		
		// ---- A-2. RDBMS
		label = new Label(composite, SWT.NULL);
		label.setText(Messages.Label_RDBMS);
		
		cmbDialect = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		cmbDialect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (Dialect dialect : dialects) {
			cmbDialect.add(dialect.getName());
			logger.debug("installed Dialect: " + dialect.toString());
			
		}
		try {
			cmbDialect.setText(rootModel.findDialect().getName());
		} catch (ClassNotFoundException e) {
			cmbDialect.setText(dialects.get(0).getName());
		}
		cmbDialect.setVisibleItemCount(20);
		
		// ---- B. タブ
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 4;
		tabFolder.setLayoutData(gd);
		
		// ---- B-1. Domains
		AbstractTab tabDomains = new RootEditDialogDomainTab(tabFolder, SWT.NONE, rootModel, jiemamyFacade);
		addTab(tabDomains);
		
		// ---- B-2. DataSets
		AbstractTab tabDataSets = new RootEditDialogDataSetTab(tabFolder, SWT.NONE, rootModel, jiemamyFacade);
		addTab(tabDataSets);
		
		// ---- B-3. BeginScript
		
		String beginScript = JiemamyPropertyUtil.careNull(rootModel.getBeginScript());
		tabBeginScript = new TextEditTab(tabFolder, Messages.Tab_BeginScript, beginScript);
		addTab(tabBeginScript);
		
		// ---- B-4. EndScript
		String endScript = JiemamyPropertyUtil.careNull(rootModel.getEndScript());
		tabEndScript = new TextEditTab(tabFolder, Messages.Tab_EndScript, endScript);
		addTab(tabEndScript);
		
		// ---- B-5. Description
		String description = JiemamyPropertyUtil.careNull(rootModel.getDescription());
		tabDescription = new TextEditTab(tabFolder, Messages.Tab_Description, description);
		addTab(tabDescription);
		
		createAdditionalTabs(tabFolder);
		
		return composite;
	}
	
	@Override
	protected Point getDefaultSize() {
		return DEFAULT_SIZE;
	}
	
	@Override
	protected boolean performOk() {
		JiemamyContext rootModel = getTargetModel();
		
		int selectionIndex = cmbDialect.getSelectionIndex();
		String dialectClassName = dialects.get(selectionIndex).toString();
		jiemamyFacade.changeModelProperty(rootModel, RootProperty.dialectClassName, dialectClassName);
		
		String schemaName = JiemamyPropertyUtil.careNull(txtSchema.getText(), true);
		jiemamyFacade.changeModelProperty(rootModel, RootProperty.schemaName, schemaName);
		
		String beginScript = JiemamyPropertyUtil.careNull(tabBeginScript.getTextWidget().getText(), true);
		jiemamyFacade.changeModelProperty(rootModel, RootProperty.beginScript, beginScript);
		
		String endScript = JiemamyPropertyUtil.careNull(tabEndScript.getTextWidget().getText(), true);
		jiemamyFacade.changeModelProperty(rootModel, RootProperty.endScript, endScript);
		
		String description = JiemamyPropertyUtil.careNull(tabDescription.getTextWidget().getText(), true);
		jiemamyFacade.changeModelProperty(rootModel, RootProperty.description, description);
		
		return true;
	}
}

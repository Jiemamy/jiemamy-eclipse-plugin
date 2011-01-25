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
package org.jiemamy.eclipse.core.ui.editor.dialog.context;

import java.util.List;
import java.util.UUID;

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

import org.jiemamy.DefaultContextMetadata;
import org.jiemamy.JiemamyContext;
import org.jiemamy.SqlFacet;
import org.jiemamy.dialect.Dialect;
import org.jiemamy.eclipse.JiemamyCorePlugin;
import org.jiemamy.eclipse.core.ui.editor.dialog.AbstractTab;
import org.jiemamy.eclipse.core.ui.editor.dialog.JiemamyEditDialog0;
import org.jiemamy.eclipse.core.ui.editor.dialog.TextEditTab;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.model.script.DefaultAroundScriptModel;
import org.jiemamy.model.script.Position;

/**
 * {@link JiemamyContext}設定ダイアログクラス。
 * 
 * @author daisuke
 */
public class JiemamyContextEditDialog extends JiemamyEditDialog0<JiemamyContext> {
	
	private static final Point DEFAULT_SIZE = new Point((int) (400 * 1.618), 400);
	
	private static Logger logger = LoggerFactory.getLogger(JiemamyContextEditDialog.class);
	
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
	
	private DefaultContextMetadata metadata;
	
	private DefaultAroundScriptModel universalAroundScript;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param shell 親シェルオブジェクト
	 * @param context 編集対象{@link JiemamyContext}
	 * @throws IllegalArgumentException 引数rootModel, jiemamyFacadeに{@code null}を与えた場合
	 */
	public JiemamyContextEditDialog(Shell shell, JiemamyContext context) {
		super(shell, context, context, JiemamyContext.class);
		
		Validate.notNull(context);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		dialects = JiemamyCorePlugin.getDialectResolver().getAllInstance();
	}
	
	public DefaultContextMetadata getMetadata() {
		return metadata;
	}
	
	public DefaultAroundScriptModel getUniversalAroundScript() {
		return universalAroundScript;
	}
	
	@Override
	protected boolean canExecuteOk() {
		return super.canExecuteOk() && StringUtils.isEmpty(cmbDialect.getText()) == false;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		JiemamyContext context = getTargetCoreModel();
		getShell().setText(Messages.Dialog_Title);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(4, false));
		
		Label label;
		
		// ---- A-1. スキーマ名
		label = new Label(composite, SWT.NULL);
		label.setText(Messages.Label_SchemaName);
		
		txtSchema = new Text(composite, SWT.BORDER);
		txtSchema.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSchema.setText(StringUtils.defaultString(context.getMetadata().getSchemaName()));
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
			cmbDialect.setText(context.findDialect().getName());
		} catch (ClassNotFoundException e) {
			cmbDialect.setText(dialects.get(0).getName());
		}
		cmbDialect.setVisibleItemCount(20);
		
		// ---- B. タブ
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 4;
		tabFolder.setLayoutData(gd);
		
		// ---- B-1. DataSets
		AbstractTab tabDataSets = new JiemamyContextEditDialogDataSetTab(tabFolder, SWT.NONE, context);
		addTab(tabDataSets);
		
//		// ---- B-2. Domains
//		AbstractTab tabDomains = new RootEditDialogDomainTab(tabFolder, SWT.NONE, context);
//		addTab(tabDomains);
		
		String beginScript = "";
		String endScript = "";
		SqlFacet facet = getContext().getFacet(SqlFacet.class);
		DefaultAroundScriptModel aroundScript = (DefaultAroundScriptModel) facet.getUniversalAroundScript();
		if (aroundScript != null) {
			beginScript = StringUtils.defaultString(aroundScript.getScript(Position.BEGIN));
			endScript = StringUtils.defaultString(aroundScript.getScript(Position.END));
		}
		
		// ---- B-3. BeginScript
		tabBeginScript = new TextEditTab(tabFolder, Messages.Tab_BeginScript, beginScript);
		addTab(tabBeginScript);
		
		// ---- B-4. EndScript
		tabEndScript = new TextEditTab(tabFolder, Messages.Tab_EndScript, endScript);
		addTab(tabEndScript);
		
		// ---- B-5. Description
		String description = StringUtils.defaultString(context.getMetadata().getDescription());
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
		metadata = new DefaultContextMetadata();
		
		int selectionIndex = cmbDialect.getSelectionIndex();
		String dialectClassName = dialects.get(selectionIndex).getClass().getName();
		metadata.setDialectClassName(dialectClassName);
		
		String schemaName = StringUtils.defaultString(txtSchema.getText());
		metadata.setSchemaName(schemaName);
		
		String description = StringUtils.defaultString(tabDescription.getTextWidget().getText());
		metadata.setDescription(description);
//		
//		context.setMetadata(meta);
//		
		String beginScript = StringUtils.defaultString(tabBeginScript.getTextWidget().getText());
		String endScript = StringUtils.defaultString(tabEndScript.getTextWidget().getText());
		
		if (StringUtils.isEmpty(beginScript) == false || StringUtils.isEmpty(endScript) == false) {
			SqlFacet facet = getContext().getFacet(SqlFacet.class);
			universalAroundScript = (DefaultAroundScriptModel) facet.getUniversalAroundScript();
			if (universalAroundScript == null) {
				universalAroundScript = new DefaultAroundScriptModel(UUID.randomUUID());
			}
			universalAroundScript.setScript(Position.BEGIN, beginScript);
			universalAroundScript.setScript(Position.END, endScript);
//			facet.setUniversalAroundScript(aroundScript);
		} else {
//			facet.setUniversalAroundScript(null);
		}
		
		return true;
	}
}

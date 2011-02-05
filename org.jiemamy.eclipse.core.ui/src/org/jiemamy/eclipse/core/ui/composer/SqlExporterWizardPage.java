/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.composer;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * SQLエクスポートに関する設定を行うウィザードページ。
 * 
 * @author daisuke
 */
class SqlExporterWizardPage extends WizardPage {
	
	private Button chkDropStatements;
	
	private Button chkCreateSchema;
	
	private Combo cmbDataSets;
	
	private final List<String> dataSetNames;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param dataSetNames データセット名のリスト
	 */
	SqlExporterWizardPage(List<String> dataSetNames) {
		super("SQL出力設定", "SQL出力設定", (ImageDescriptor) null); // RESOURCE
		this.dataSetNames = dataSetNames;
	}
	
	public void createControl(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		chkDropStatements = new Button(composite, SWT.CHECK);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		chkDropStatements.setLayoutData(gd);
		chkDropStatements.setText("DROP文を出力する"); // RESOURCE
		
		chkCreateSchema = new Button(composite, SWT.CHECK);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		chkCreateSchema.setLayoutData(gd);
		chkCreateSchema.setText("CREATE SCHEMA文を出力する"); // RESOURCE
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("出力するデータセット"); // RESOURCE
		
		cmbDataSets = new Combo(composite, SWT.READ_ONLY);
		cmbDataSets.add(StringUtils.EMPTY);
		for (String dataSetName : dataSetNames) {
			cmbDataSets.add(dataSetName);
		}
		
		setControl(composite);
	}
	
	/**
	 * 出力するデータセットのインデックスを取得する。
	 * 
	 * @return 出力するデータセットのインデックス. 出力しない場合は負数
	 */
	public int getDataSetIndex() {
		return cmbDataSets.getSelectionIndex() - 1;
	}
	
	/**
	 * CREATE SCHEMA文を出力するかどうかを取得する。
	 * 
	 * @return 出力する場合は{@code true}、そうでない場合は{@code false}
	 */
	public boolean getEmitCreateSchema() {
		return chkCreateSchema.getSelection();
	}
	
	/**
	 * DROP文を出力するかどうかを取得する。
	 * 
	 * @return 出力する場合は{@code true}、そうでない場合は{@code false}
	 */
	public boolean getEmitDropStatements() {
		return chkDropStatements.getSelection();
	}
	
}

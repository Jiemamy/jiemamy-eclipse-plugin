/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/02/26
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
package org.jiemamy.eclipse.core.ui.utils;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * ファイルを選択するウィザードページ。
 * 
 * @author daisuke
 */
public class FileSelectWizardPage extends WizardPage {
	
	private Text txtFolder;
	
	private Button chkOverwrite;
	
	private final String[] filterNames;
	
	private final String[] filterExtensions;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param pageName ウィザードページ名
	 * @param title タイトル
	 * @param titleImage タイトル画像. may be null.
	 * @param filterNames 選択ダイアログにおける拡張子フィルタの名前の配列
	 * @param filterExtensions 選択ダイアログにおける拡張子フィルタの配列
	 */
	public FileSelectWizardPage(String pageName, String title, ImageDescriptor titleImage, String[] filterNames,
			String[] filterExtensions) {
		super(pageName, title, titleImage);
		this.filterNames = filterNames.clone();
		this.filterExtensions = filterExtensions.clone();
	}
	
	public void createControl(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("出力先:");
		
		txtFolder = new Text(composite, SWT.BORDER);
		txtFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFolder.setText("");
		txtFolder.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				if (StringUtils.isEmpty(txtFolder.getText())) {
					setPageComplete(false);
				} else {
					setPageComplete(true);
				}
			}
			
		});
		setPageComplete(false);
		
		Button btnBrowse = new Button(composite, SWT.PUSH);
		btnBrowse.setText("参照(&B)"); // RESOURCE
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
				dialog.setFilterNames(filterNames);
				dialog.setFilterExtensions(filterExtensions);
				dialog.setText("保存先"); // RESOURCE
				String fileName = dialog.open();
				txtFolder.setText(fileName);
			}
		});
		
		chkOverwrite = new Button(composite, SWT.CHECK);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		chkOverwrite.setLayoutData(gd);
		chkOverwrite.setText("存在したら上書きする"); // RESOURCE
		chkOverwrite.setSelection(true);
		
		setControl(composite);
	}
	
	/**
	 * 上書きを行うかどうかを取得する。
	 * 
	 * @return 上書きを行う場合は{@code true}、そうでない場合は{@code false}
	 */
	public boolean getOverwrite() {
		return chkOverwrite.getSelection();
	}
	
	/**
	 * 出力ファイルのパスを取得する。
	 * 
	 * @return 出力ファイルのパス
	 */
	public String getPath() {
		return txtFolder.getText();
	}
	
	/**
	 * 出力ファイルのパスを設定する。
	 * 
	 * @param path 出力ファイルのパス
	 */
	public void setPath(String path) {
		txtFolder.setText(path);
	}
}

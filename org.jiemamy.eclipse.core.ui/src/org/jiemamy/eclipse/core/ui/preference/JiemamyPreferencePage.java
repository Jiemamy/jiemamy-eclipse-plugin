/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2008/07/28
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
package org.jiemamy.eclipse.core.ui.preference;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;

/**
 * 設定ページクラス。
 * 
 * @author daisuke
 */
public class JiemamyPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	/** {@link PreferenceKey#CREATE_COLUMNS_WITH_FK}用チェックボックス */
	private Button chkCreateColumnWithFk;
	
	/** {@link PreferenceKey#CONNECTION_ROUTER}用コンボ */
	private Combo cmbConnectionRouter;
	
	/** プリファレンス */
	private JiemamyPreference pref = JiemamyUIPlugin.getPreference();
	
	
	public void init(IWorkbench workbench) {
		// nothing to do
	}
	
	@Override
	public boolean performOk() {
		storeValues();
		JiemamyUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, true));
		
		Group group = new Group(composite, SWT.NONE);
		group.setText(Messages.Group_Connection);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		chkCreateColumnWithFk = new Button(group, SWT.CHECK);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		chkCreateColumnWithFk.setLayoutData(gd);
		chkCreateColumnWithFk.setText(Messages.Connection_CreateColumnWithFk);
		
		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.Connection_Router);
		
		cmbConnectionRouter = new Combo(group, SWT.READ_ONLY);
		cmbConnectionRouter.setItems(ConnectionRouters.getLabels());
		
		setCurrentValueToControls();
		
		return composite;
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		pref.loadSimpleValues();
		setCurrentValueToControls();
	}
	
	/**
	 * 現在のPreferenceの値をコントロールに反映する。
	 */
	private void setCurrentValueToControls() {
		chkCreateColumnWithFk.setSelection(pref.isCreateColumnWithFk());
		cmbConnectionRouter.setText(pref.getConnectionRouter().getLabel());
	}
	
	/**
	 * 現在のコントロールの値をPreferenceに反映する。 
	 */
	private void storeValues() {
		// store new value
		IPreferenceStore ps = JiemamyUIPlugin.getDefault().getPreferenceStore();
		ps.setValue(PreferenceKey.CREATE_COLUMNS_WITH_FK.toString(), chkCreateColumnWithFk.getSelection());
		ps.setValue(PreferenceKey.CONNECTION_ROUTER.toString(), ConnectionRouters.get(cmbConnectionRouter.getText())
			.name());
	}
}
